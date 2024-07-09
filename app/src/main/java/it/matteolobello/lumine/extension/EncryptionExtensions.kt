package it.matteolobello.lumine.extension

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun String.encrypt(key: String) = encryptDecrypt(key, true)

fun String.decrypt(key: String) = encryptDecrypt(key, false)

private fun String.encryptDecrypt(key: String, mustEncrypt: Boolean): String {
    val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
    val inputByte = this.toByteArray(Charsets.UTF_8)
    return if (mustEncrypt) {
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"))
        String(Base64.encode(cipher.doFinal(inputByte), Base64.DEFAULT))
    } else {
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key.toByteArray(), "AES"))
        String(cipher.doFinal(Base64.decode(inputByte, Base64.DEFAULT)))
    }
}