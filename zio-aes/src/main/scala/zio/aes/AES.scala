package zio.aes

import zio.Config.Secret
import zio.prelude.Subtype
import zio.{ULayer, ZLayer}

import java.nio.charset.{Charset, StandardCharsets}
import java.security.{Key, SecureRandom}
import javax.crypto.spec.{GCMParameterSpec, PBEKeySpec, SecretKeySpec}
import javax.crypto.{Cipher, SecretKey, SecretKeyFactory}

object AES {

  def live(password: Secret): ULayer[AES] =
    ZLayer.succeed {
      // We call `.value.toArray` and pass its result to the live class instead of passing the `Secret` directly
      // to avoid having to instantiate a Chunk and a Array[Char] every time we need to use the password
      new AESLive(password.value.toArray)
    }

  final val UTF_8: Charset = StandardCharsets.UTF_8

  type Base64String = Base64String.Type
  object Base64String extends Subtype[String]

  type CipherText = CipherText.Type
  object CipherText extends Subtype[String] // should be Subtype[Base64String] but https://github.com/zio/zio-prelude/issues/1183

  type IV = IV.Type
  object IV extends Subtype[String] // should be Subtype[Base64String] but https://github.com/zio/zio-prelude/issues/1183

  type Salt = Salt.Type
  object Salt extends Subtype[String] { // should be Subtype[Base64String] but https://github.com/zio/zio-prelude/issues/1183
    implicit final class RichSalt(private val salt: Salt) extends AnyVal {
      def toRawSalt: RawSalt = RawSalt(base64Decode(salt))
    }
  }

  type RawSalt = RawSalt.Type
  object RawSalt extends Subtype[Array[Byte]] {
    implicit final class RichRawSalt(private val salt: RawSalt) extends AnyVal {
      def toSalt: Salt = Salt(base64Encode(salt))
    }
  }

  def base64Encode(in: Array[Byte]): Base64String = Base64String(new String(java.util.Base64.getEncoder.encode(in), UTF_8))
  def base64Decode(in: Base64String): Array[Byte] = java.util.Base64.getDecoder.decode(in.getBytes(UTF_8))

  // TODO: Delete these implicit conversions once https://github.com/zio/zio-prelude/issues/1183 is fixed
  @inline implicit private[aes] def saltToBase64String(salt: Salt): Base64String                   = Base64String(salt)
  @inline implicit private[aes] def ivToBase64String(iv: IV): Base64String                         = Base64String(iv)
  @inline implicit private[aes] def cipherTextToBase64String(cipherText: CipherText): Base64String = Base64String(cipherText)
}
import zio.aes.AES.*

/**
 * Resources that helped:
 *   - https://mkyong.com/java/java-aes-encryption-and-decryption/
 *   - https://wiki.sei.cmu.edu/confluence/display/java/MSC61-J.+Do+not+use+insecure+or+weak+cryptographic+algorithms
 *   - https://proandroiddev.com/security-best-practices-symmetric-encryption-with-aes-in-java-7616beaaade9
 *   - https://security.stackexchange.com/a/105788/66294
 *   - https://stackoverflow.com/a/13915596
 */
trait AES {
  def encrypt(in: String): (CipherText, Salt, IV)
  def decrypt(data: CipherText, salt: Salt, iv: IV): String
}

final class AESLive(password: Array[Char]) extends AES {

  private val cipher                     = "AES/GCM/NoPadding"
  private val Algorithm                  = "AES"
  private val GcmAuthenticationTagLength = 128
  private val SaltLength                 = 16
  private val IvLength                   = 12

  private val random: SecureRandom = new SecureRandom()

  override def encrypt(in: String): (CipherText, Salt, IV) = {
    val rawSalt: RawSalt        = generateRawSalt
    val key: Key                = getAESKeyFromPassword(rawSalt)
    val iv: GCMParameterSpec    = generateIv
    val cipherText: Array[Byte] = doEncrypt(in.getBytes(UTF_8), key, iv)

    (CipherText(base64Encode(cipherText)), rawSalt.toSalt, IV(base64Encode(iv.getIV)))
  }

  override def decrypt(data: CipherText, salt: Salt, iv: IV): String = {
    val key: Key                    = getAESKeyFromPassword(salt.toRawSalt)
    val gcmParams: GCMParameterSpec = new GCMParameterSpec(GcmAuthenticationTagLength, base64Decode(iv))
    val clearText: Array[Byte]      = doDecrypt(base64Decode(data), key, gcmParams)

    new String(clearText, UTF_8)
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
    val spec           = new PBEKeySpec(password, salt, iterationCount, keyLength)
    new SecretKeySpec(factory.generateSecret(spec).getEncoded, Algorithm)
  }

}
