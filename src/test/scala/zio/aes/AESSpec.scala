package zio.aes

import zio.aes.AES.{ClearText, Password, UTF_8}
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zio.{Scope, ZIO}

//noinspection SimplifyAssertInspection
object AESSpec extends ZIOSpecDefault {

  private val anyPassword: Gen[Any, Password] =
    for {
      size     <- Gen.int(40, 200) // if the max value here is too high, tests will take forever.
      password <- Gen.listOfN(size)(Gen.alphaChar)
    } yield Password.unsafe(password.mkString)

  private val encryptSpec =
    suite("::encrypt")(
      test("encrypted text is not just base64'ed text") {
        check(anyPassword, Gen.string) { (password, secret) =>
          val service: AES      = new AESLive(password)
          val (encrypted, _, _) = service.encrypt(ClearText(secret))

          assert(new String(AES.base64Decode(encrypted), UTF_8))(not(equalTo(secret)))
        }
      },
      test("salt should be 16 bytes long") {
        check(anyPassword, Gen.string) { (password, secret) =>
          val service: AES = new AESLive(password)
          val (_, salt, _) = service.encrypt(ClearText(secret))

          assert(AES.base64Decode(salt).length)(equalTo(16))
        }
      },
      test("iv should be 12 bytes long") {
        check(anyPassword, Gen.string) { (password, secret) =>
          val service: AES = new AESLive(password)
          val (_, _, iv)   = service.encrypt(ClearText(secret))

          assert(AES.base64Decode(iv).length)(equalTo(12))
        }
      },
    )

  private val decryptSpec =
    suite("::decrypt")(
      test("decrypted encrypted text should be equal to initial text") {
        check(anyPassword, Gen.string) { (password, secret) =>
          val service: AES          = new AESLive(password)
          val (encrypted, salt, iv) = service.encrypt(ClearText(secret))
          val decrypted             = service.decrypt(encrypted, salt, iv)

          assert(decrypted)(equalTo(secret))
        }
      }
    )

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("AES")(
      encryptSpec,
      decryptSpec,
    ) @@ samples(1000) @@ shrinks(0) @@ parallel
}
