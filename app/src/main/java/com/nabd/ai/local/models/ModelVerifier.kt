package com.nabd.ai.local.models

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object ModelVerifier {

    fun verifyChecksum(file: File, expectedSha256: String): Boolean {
        if (!file.exists()) return false

        try {
            val digest = MessageDigest.getInstance("SHA-256")
            val inputStream = FileInputStream(file)
            val buffer = ByteArray(8192)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }

            val hashBytes = digest.digest()
            val hexString = StringBuilder()
            for (b in hashBytes) {
                val hex = Integer.toHexString(0xff and b.toInt())
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }

            return hexString.toString().equals(expectedSha256, ignoreCase = true)
        } catch (e: Exception) {
            return false
        }
    }
}
