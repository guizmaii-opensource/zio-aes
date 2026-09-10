# zio-aes

[![CI](https://github.com/guizmaii-opensource/zio-aes/actions/workflows/ci.yaml/badge.svg)](https://github.com/guizmaii-opensource/zio-aes/actions/workflows/ci.yaml)
[![Maven Central](https://img.shields.io/maven-central/v/com.guizmaii/zio-aes_3.svg)](https://central.sonatype.com/artifact/com.guizmaii/zio-aes_3)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

Password-based AES-GCM encryption as a ZIO service.

`zio-aes` is a ZIO packaging of
[this gist](https://gist.github.com/guizmaii/6b5d3666081960639c3df0a24e17e2fd).

## What it does

The `AES` service exposes two operations:

```scala
trait AES {
  def encrypt(in: String): (CipherText, Salt, IV)
  def decrypt(data: CipherText, salt: Salt, iv: IV): String
}
```

Encryption derives a fresh key from your password and a **new random salt** on every call, and uses
a **new random IV**, so encrypting the same text twice gives different ciphertexts. That is why
`encrypt` hands back the salt and IV: you must store them alongside the ciphertext to be able to
decrypt later.

`CipherText`, `Salt` and `IV` are [zio-prelude](https://github.com/zio/zio-prelude) subtypes of
`String`, so they cannot be passed to `decrypt` in the wrong order. All three are Base64-encoded.

### Parameters

| | |
|---|---|
| Cipher | `AES/GCM/NoPadding` |
| GCM authentication tag | 128 bits |
| Salt | 16 random bytes, per `encrypt` call |
| IV | 12 random bytes, per `encrypt` call |
| Key derivation | `PBKDF2WithHmacSHA256`, 65536 iterations |
| Key length | 256 bits |

## Installation

```scala
libraryDependencies += "com.guizmaii" %% "zio-aes" % "0.6.1"
```

Scala 3 only. Requires Java 17+.

## Usage

Provide the layer with your password, then use the service:

```scala
import zio.*
import zio.Config.Secret
import zio.aes.*

val program: ZIO[AES, Nothing, Unit] =
  for {
    aes                     <- ZIO.service[AES]
    (cipherText, salt, iv)   = aes.encrypt("some secret value")
    // persist all three: the salt and IV are needed to decrypt
    decrypted                = aes.decrypt(cipherText, salt, iv)
    _                       <- Console.printLine(decrypted).orDie
  } yield ()

program.provide(AES.live(Secret("your-password")))
```

`encrypt` and `decrypt` are plain synchronous methods, not effects — they do CPU-bound work and do
not fail in the happy path. `decrypt` throws if the ciphertext, salt or IV do not match the
password, so wrap it in `ZIO.attempt` if you are decrypting untrusted input.

The password is a `zio.Config.Secret`, so it will not leak into logs through `toString`.

## Contributing

Issues and pull requests are welcome at
[guizmaii-opensource/zio-aes](https://github.com/guizmaii-opensource/zio-aes).

## License

[Apache 2.0](LICENSE)

Copyright 2023-2026 Jules Ivanic and the zio-aes contributors.
