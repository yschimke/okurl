package com.baulsupp.oksocial.security

import com.baulsupp.oksocial.security.KeystoreUtils.createKeyManager
import com.baulsupp.oksocial.security.KeystoreUtils.getKeyStore
import org.junit.Test

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.PasswordCallback
import javax.security.auth.callback.UnsupportedCallbackException

class KeystoreUtilsTest {
    @Test
    @Throws(Exception::class)
    fun loadEmptyPassword() {
        val f = writeFile("")

        createKeyManager(getKeyStore(f), passwordCallback(null))
    }

    @Test
    @Throws(Exception::class)
    fun loadNonEmptyPassword() {
        val f = writeFile("a")

        createKeyManager(getKeyStore(f), passwordCallback("a"))
    }

    private fun passwordCallback(mypass: String?): CallbackHandler {
        return CallbackHandler { callbacks ->
            for (c in callbacks) {
                if (c is PasswordCallback && mypass != null) {
                    c.password = mypass.toCharArray()
                } else {
                    throw UnsupportedCallbackException(c)
                }
            }
        }
    }

    companion object {

        @Throws(KeyStoreException::class, CertificateException::class, NoSuchAlgorithmException::class,
                IOException::class, InvalidKeyException::class, NoSuchProviderException::class,
                SignatureException::class)
        fun writeFile(keyPw: String): File {
            val temp = File.createTempFile("tempkey", ".keystore")
            temp.deleteOnExit()

            val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)

            val keyGen = KeyPairGenerator.getInstance("RSA")
            keyGen.initialize(512)
            val keyPair = keyGen.genKeyPair()
            val privateKey = keyPair.private

            keyStore.setKeyEntry("a", privateKey, keyPw.toCharArray(),
                    generateCertificate())

            FileOutputStream(temp).use { fos -> keyStore.store(fos, "123456".toCharArray()) }

            return temp
        }

        private val certificate = """
      -----BEGIN CERTIFICATE-----
      MIICZTCCAdICBQL3AAC2MA0GCSqGSIb3DQEBAgUAMF8xCzAJBgNVBAYTAlVTMSAw
      HgYDVQQKExdSU0EgRGF0YSBTZWN1cml0eSwgSW5jLjEuMCwGA1UECxMlU2VjdXJl
      IFNlcnZlciBDZXJ0aWZpY2F0aW9uIEF1dGhvcml0eTAeFw05NzAyMjAwMDAwMDBa
      Fw05ODAyMjAyMzU5NTlaMIGWMQswCQYDVQQGEwJVUzETMBEGA1UECBMKQ2FsaWZv
      cm5pYTESMBAGA1UEBxMJUGFsbyBBbHRvMR8wHQYDVQQKExZTdW4gTWljcm9zeXN0
      ZW1zLCBJbmMuMSEwHwYDVQQLExhUZXN0IGFuZCBFdmFsdWF0aW9uIE9ubHkxGjAY
      BgNVBAMTEWFyZ29uLmVuZy5zdW4uY29tMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCB
      iQKBgQCofmdY+PiUWN01FOzEewf+GaG+lFf132UpzATmYJkA4AEA/juW7jSi+LJk
      wJKi5GO4RyZoyimAL/5yIWDV6l1KlvxyKslr0REhMBaD/3Z3EsLTTEf5gVrQS6sT
      WMoSZAyzB39kFfsB6oUXNtV8+UKKxSxKbxvhQn267PeCz5VX2QIDAQABMA0GCSqG
      SIb3DQEBAgUAA34AXl3at6luiV/7I9MN5CXYoPJYI8Bcdc1hBagJvTMcmlqL2uOZ
      H9T5hNMEL9Tk6aI7yZPXcw/xI2K6pOR/FrMp0UwJmdxX7ljV6ZtUZf7pY492UqwC
      1777XQ9UEZyrKJvF5ntleeO0ayBqLGVKCWzWZX9YsXCpv47FNLZbupE=
      -----END CERTIFICATE-----
      """.trimIndent()

        @Throws(NoSuchAlgorithmException::class, CertificateException::class,
                NoSuchProviderException::class, InvalidKeyException::class, SignatureException::class)
        fun generateCertificate(): Array<Certificate> {
            val cf = CertificateFactory.getInstance("X.509")
            return arrayOf(cf.generateCertificate(ByteArrayInputStream(certificate.toByteArray())))
        }
    }
}