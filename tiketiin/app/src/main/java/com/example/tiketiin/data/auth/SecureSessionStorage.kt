package com.example.tiketiin.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.example.tiketiin.data.remote.dto.AuthResponse
import com.google.gson.Gson
import java.io.File
import java.nio.ByteBuffer
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureSessionStorage(context: Context) {

    companion object {

        private const val KEYSTORE = "AndroidKeyStore"

        private const val KEY_ALIAS =
            "tiketiin_session_key"

        private const val TRANSFORMATION =
            "AES/GCM/NoPadding"
    }

    private val gson = Gson()

    private val sessionFile = AtomicFile(
        File(
            context.applicationContext.noBackupFilesDir,
            "tiketiin_session.enc"
        )
    )

    private fun getKeyStore(): KeyStore {

        return KeyStore.getInstance(KEYSTORE).apply {
            load(null)
        }
    }

    private fun getOrCreateKey(): SecretKey {

        val keyStore = getKeyStore()

        val existingKey =
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey

        if (existingKey != null) {
            return existingKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE
        )

        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(keySpec)

        return keyGenerator.generateKey()
    }

    fun saveSession(session: AuthResponse) {

        val cipher =
            Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.ENCRYPT_MODE,
            getOrCreateKey()
        )

        val json = gson.toJson(session)

        val encryptedData =
            cipher.doFinal(json.toByteArray(Charsets.UTF_8))

        val iv = cipher.iv

        val payload = ByteBuffer
            .allocate(4 + iv.size + encryptedData.size)
            .putInt(iv.size)
            .put(iv)
            .put(encryptedData)
            .array()

        val output = sessionFile.startWrite()

        try {

            output.write(payload)

            sessionFile.finishWrite(output)

        } catch (e: Exception) {

            sessionFile.failWrite(output)

            throw e
        }
    }

    fun loadSession(): AuthResponse? {

        if (!sessionFile.baseFile.exists()) {
            return null
        }

        val buffer = ByteBuffer.wrap(
            sessionFile.readFully()
        )

        val ivSize = buffer.int

        require(ivSize == 12)
        require(buffer.remaining() > ivSize)

        val iv = ByteArray(ivSize)

        buffer.get(iv)

        val encryptedData =
            ByteArray(buffer.remaining())

        buffer.get(encryptedData)

        val key = getKeyStore()
            .getKey(KEY_ALIAS, null) as? SecretKey
            ?: error("Kunci sesi tidak tersedia")

        val cipher =
            Cipher.getInstance(TRANSFORMATION)

        cipher.init(
            Cipher.DECRYPT_MODE,
            key,
            GCMParameterSpec(128, iv)
        )

        val decryptedData =
            cipher.doFinal(encryptedData)

        val result = gson.fromJson(
            String(decryptedData, Charsets.UTF_8),
            AuthResponse::class.java
        )

        require(result.accessToken.isNotBlank())
        require(result.refreshToken.isNotBlank())
        require(result.user.id.isNotBlank())

        return result
    }

    fun clearSession() {

        // Hapus kunci agar data lama tidak dapat didekripsi.
        getKeyStore().deleteEntry(KEY_ALIAS)

        sessionFile.delete()
    }
}