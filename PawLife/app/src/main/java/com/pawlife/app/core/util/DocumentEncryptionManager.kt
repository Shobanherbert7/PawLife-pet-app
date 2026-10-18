package com.pawlife.app.core.util

import android.content.Context
import android.net.Uri
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentEncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val vaultDir = File(context.filesDir, "vault").apply {
        if (!exists()) mkdirs()
    }

    /**
     * Encrypts a file from a Uri and saves it into the internal vault directory.
     * Returns the relative path to the encrypted file.
     */
    fun encryptAndSave(sourceUri: Uri, originalFileName: String): String {
        val safeFileName = "${UUID.randomUUID()}_${originalFileName}.enc"
        val destFile = File(vaultDir, safeFileName)

        val encryptedFile = EncryptedFile.Builder(
            context,
            destFile,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            encryptedFile.openFileOutput().use { output ->
                input.copyTo(output)
            }
        }
        return "vault/$safeFileName"
    }

    /**
     * Opens an InputStream to dynamically decrypt the file on the fly for viewing.
     */
    fun openDecryptedStream(relativePath: String): InputStream {
        val file = File(context.filesDir, relativePath)
        val encryptedFile = EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        return encryptedFile.openFileInput()
    }
    
    fun deleteEncryptedFile(relativePath: String): Boolean {
        val file = File(context.filesDir, relativePath)
        return if (file.exists()) file.delete() else false
    }
}
