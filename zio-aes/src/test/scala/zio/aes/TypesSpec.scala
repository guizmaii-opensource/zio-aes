package zio.aes

import zio.aes.AES.Password
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*
import zio.{NonEmptyChunk, Scope}

//noinspection SimplifyAssertInspection
object TypesSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("types spec")(
      suite("Password")(
        test("should only accept non-blank string at least 40 characters long") {
          check(Gen.string) { string =>
            if (string.isBlank || string.length < 40) assert(Password.make(string).toEither)(isLeft)
            else assert(Password.make(string).toEither)(isRight)
          }
        }
      )
    ) @@ samples(1000) @@ shrinks(0) @@ parallel
}
