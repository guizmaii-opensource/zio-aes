package zio.aes

import zio.{ULayer, ZLayer}
import zio.prelude.Assertion.{greaterThanOrEqualTo, hasLength, matches, notEqualTo}
import zio.prelude.Subtype

import java.nio.charset.{Charset, StandardCharsets}
import java.security.{Key, SecureRandom}
import javax.crypto.spec.{GCMParameterSpec, PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}
import scala.language.implicitConversions

object AES {

  def live(password: Password): ULayer[AES] = ZLayer.succeed(new AESLive(password))

  /**
   * This type represents a password.
   *
   * The constraints on that password are fairly simple but can be improved later if needed.
   *
   * For now, a valid password is just a string which is at least 40 characters long.
   *
   * Why 40? It seems long enough.
   */
  object Password extends Subtype[String] {
    private[zio] def unsafe(s: String): Type = Password.wrap(s)

    /**
     * `\\S` means "Match nonwhitespace" (could probably be improved)
     *
     * Comes from
     * - https://www.tutorialspoint.com/scala/scala_regular_expressions.htm
     * - https://stackoverflow.com/questions/3085539/regular-expression-for-anything-but-an-empty-string
     */
    // noinspection TypeAnnotation
    override inline def assertion = matches("^\\S+$".r) && hasLength(greaterThanOrEqualTo(40))
  }
  type Password = Password.Type

  final val UTF_8: Charset = StandardCharsets.UTF_8

  object Base64String extends Subtype[String]
  type Base64String = Base64String.Type

  object CipherText extends Subtype[Base64String]
  type CipherText = CipherText.Type

  object IV extends Subtype[Base64String]
  type IV = IV.Type

  object Salt extends Subtype[Base64String] {
    implicit final class RichSalt(private val salt: Salt) extends AnyVal {
      def toRawSalt: RawSalt = RawSalt(base64Decode(salt))
    }
  }
  type Salt = Salt.Type

  object RawSalt extends Subtype[Array[Byte]] {
    implicit final class RichRawSalt(private val salt: RawSalt) extends AnyVal {
      def toSalt: Salt = Salt(base64Encode(salt))
    }
  }
  type RawSalt = RawSalt.Type

  object ClearText extends Subtype[String]
  type ClearText = ClearText.Type

  def base64Encode(in: Array[Byte]): Base64String = Base64String(new String(java.util.Base64.getEncoder.encode(in), UTF_8))
  def base64Decode(in: Base64String): Array[Byte] = java.util.Base64.getDecoder.decode(in.getBytes(UTF_8))
}
import zio.aes.AES._

/**
 * Resources that helped:
 *   - https://mkyong.com/java/java-aes-encryption-and-decryption/
 *   - https://wiki.sei.cmu.edu/confluence/display/java/MSC61-J.+Do+not+use+insecure+or+weak+cryptographic+algorithms
 *   - https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9
 *   - https://security.stackexchange.com/a/105788/66294
 *   - https://stackoverflow.com/a/13915596
 */
trait AES {
  def encrypt(in: ClearText): (CipherText, Salt, IV)

  def decrypt(data: CipherText, salt: Salt, iv: IV): ClearText
}

final class AESLive(password: Password) extends AES {

  private val cipher                     = "AES/GCM/NoPadding"
  private val Algorithm                  = "AES"
  private val GcmAuthenticationTagLength = 128
  private val SaltLength                 = 16
  private val IvLength                   = 12

  private val random: SecureRandom = new SecureRandom()

  override def encrypt(in: ClearText): (CipherText, Salt, IV) = {
    val rawSalt: RawSalt        = generateRawSalt
    val key: Key                = getAESKeyFromPassword(rawSalt)
    val iv: GCMParameterSpec    = generateIv
    val cipherText: Array[Byte] = doEncrypt(in.getBytes(UTF_8), key, iv)

    (CipherText(base64Encode(cipherText)), rawSalt.toSalt, IV(base64Encode(iv.getIV)))
  }

  override def decrypt(data: CipherText, salt: Salt, iv: IV): ClearText = {
    val key: Key                    = getAESKeyFromPassword(salt.toRawSalt)
    val gcmParams: GCMParameterSpec = new GCMParameterSpec(GcmAuthenticationTagLength, base64Decode(iv))
    val clearText: Array[Byte]      = doDecrypt(base64Decode(data), key, gcmParams)

    ClearText(new String(clearText, UTF_8))
  }

  private def doEncrypt(in: Array[Byte], key: Key, gcmParams: GCMParameterSpec): Array[Byte] = {
    val c = Cipher.getInstance(cipher)
    c.init(Cipher.ENCRYPT_MODE, key, gcmParams)
    c.doFinal(in)
  }

  private def doDecrypt(in: Array[Byte], key: Key, gcmParams: GCMParameterSpec): Array[Byte] = {
    val c = Cipher.getInstance(cipher)
    c.init(Cipher.DECRYPT_MODE, key, gcmParams)
    c.doFinal(in)
  }

  private def generateRawSalt: RawSalt = {
    val salt = new Array[Byte](SaltLength)
    random.nextBytes(salt)
    RawSalt(salt)
  }

  private def generateIv: GCMParameterSpec = {
    val iv = new Array[Byte](IvLength)
    random.nextBytes(iv)
    new GCMParameterSpec(GcmAuthenticationTagLength, iv)
  }

  /**
   * AES key derived from a password
   *
   * Comes from: https://mkyong.com/java/java-aes-encryption-and-decryption/
   */
  private def getAESKeyFromPassword(salt: RawSalt): SecretKey = {
    val factory        = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    val iterationCount = 65536
    val keyLength      = 256
    val spec           = new PBEKeySpec(password.toCharArray, salt, iterationCount, keyLength)
    new SecretKeySpec(factory.generateSecret(spec).getEncoded, Algorithm)
  }

}
