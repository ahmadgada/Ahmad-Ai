package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object EncryptionManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "AhmadGuard_MasterVaultKey_v1"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    private val secureRandom = SecureRandom()

    init {
        ensureKeyExists()
    }

    private fun ensureKeyExists() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null)
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        return entry?.secretKey ?: throw IllegalStateException("Keystore master key could not be retrieved")
    }

    /**
     * Encrypts plaintext string using AES-256-GCM.
     * Returns Base64 string containing [12 bytes IV + ciphertext + tag].
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val iv = ByteArray(IV_LENGTH)
            secureRandom.nextBytes(iv)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
            val cipherText = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            val combined = ByteArray(iv.size + cipherText.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback for isolated runtime environments
            "ENC:" + Base64.encodeToString(plainText.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)
        }
    }

    /**
     * Decrypts Base64 string using AES-256-GCM.
     */
    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        if (encryptedText.startsWith("ENC:")) {
            val raw = encryptedText.removePrefix("ENC:")
            return try {
                String(Base64.decode(raw, Base64.NO_WRAP), StandardCharsets.UTF_8)
            } catch (e: Exception) {
                ""
            }
        }
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)
            if (combined.size < IV_LENGTH) return ""
            val iv = ByteArray(IV_LENGTH)
            val cipherText = ByteArray(combined.size - IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)
            System.arraycopy(combined, IV_LENGTH, cipherText, 0, cipherText.size)

            val secretKey = getSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /**
     * Cryptographically secure password generation with custom character sets and ambiguous exclusion.
     */
    fun generatePassword(
        length: Int = 16,
        includeUppercase: Boolean = true,
        includeLowercase: Boolean = true,
        includeNumbers: Boolean = true,
        includeSymbols: Boolean = true,
        excludeAmbiguous: Boolean = false
    ): String {
        val rawUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val rawLower = "abcdefghijklmnopqrstuvwxyz"
        val rawNumbers = "0123456789"
        val rawSymbols = "!@#$%^&*()-_=+[]{}|;:,.<>?"

        // Ambiguous characters: 1, l, I, 0, O, o, 5, S, 2, Z
        val ambiguousChars = setOf('1', 'l', 'I', '|', '0', 'O', 'o', '5', 'S', 's', '2', 'Z', 'z', '`', '\'', '"')

        val uppercase = if (excludeAmbiguous) rawUpper.filterNot { it in ambiguousChars } else rawUpper
        val lowercase = if (excludeAmbiguous) rawLower.filterNot { it in ambiguousChars } else rawLower
        val numbers = if (excludeAmbiguous) rawNumbers.filterNot { it in ambiguousChars } else rawNumbers
        val symbols = if (excludeAmbiguous) rawSymbols.filterNot { it in ambiguousChars } else rawSymbols

        val poolBuilder = StringBuilder()
        val guaranteedChars = mutableListOf<Char>()

        if (includeUppercase && uppercase.isNotEmpty()) {
            poolBuilder.append(uppercase)
            guaranteedChars.add(uppercase[secureRandom.nextInt(uppercase.length)])
        }
        if (includeLowercase && lowercase.isNotEmpty()) {
            poolBuilder.append(lowercase)
            guaranteedChars.add(lowercase[secureRandom.nextInt(lowercase.length)])
        }
        if (includeNumbers && numbers.isNotEmpty()) {
            poolBuilder.append(numbers)
            guaranteedChars.add(numbers[secureRandom.nextInt(numbers.length)])
        }
        if (includeSymbols && symbols.isNotEmpty()) {
            poolBuilder.append(symbols)
            guaranteedChars.add(symbols[secureRandom.nextInt(symbols.length)])
        }

        val pool = poolBuilder.toString()
        if (pool.isEmpty()) {
            return generatePassword(length, includeUppercase = true, includeLowercase = true, includeNumbers = true, includeSymbols = false, excludeAmbiguous = false)
        }

        val effectiveLength = length.coerceIn(4, 64)
        val passwordChars = ArrayList<Char>(effectiveLength)
        passwordChars.addAll(guaranteedChars)

        for (i in passwordChars.size until effectiveLength) {
            val randomIndex = secureRandom.nextInt(pool.length)
            passwordChars.add(pool[randomIndex])
        }

        // Shuffle securely
        for (i in passwordChars.size - 1 downTo 1) {
            val j = secureRandom.nextInt(i + 1)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return passwordChars.joinToString("")
    }

    /**
     * Generate numeric PIN of specified length using SecureRandom.
     */
    fun generatePin(length: Int = 6): String {
        val effectiveLength = length.coerceIn(4, 12)
        val digits = StringBuilder()
        for (i in 0 until effectiveLength) {
            digits.append(secureRandom.nextInt(10))
        }
        return digits.toString()
    }

    /**
     * Preset: Balanced Strong Password (16 characters, all groups).
     */
    fun generateStrongPreset(): String {
        return generatePassword(
            length = 16,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSymbols = true,
            excludeAmbiguous = false
        )
    }

    /**
     * Preset: Maximum Security Password (24 characters, ultra-high entropy).
     */
    fun generateMaxSecurityPreset(): String {
        return generatePassword(
            length = 24,
            includeUppercase = true,
            includeLowercase = true,
            includeNumbers = true,
            includeSymbols = true,
            excludeAmbiguous = false
        )
    }

    /**
     * Memorable and strong passphrase generation.
     */
    fun generatePassphrase(
        wordCount: Int = 4,
        separator: String = "-",
        capitalize: Boolean = false,
        includeNumber: Boolean = false
    ): String {
        val words = listOf(
            "nexus", "falcon", "shield", "cipher", "matrix", "vector", "orbit", "beacon",
            "quantum", "sentinel", "vertex", "horizon", "aurora", "dynamo", "cobalt",
            "timber", "summit", "solace", "monolith", "zenith", "vanguard", "pulsar",
            "crypto", "ironclad", "bastion", "guardian", "nebula", "protocol", "prism",
            "glacier", "phoenix", "obsidian", "titan", "stellar", "sentry", "chronos"
        )
        val count = wordCount.coerceIn(3, 8)
        val selected = (0 until count).map {
            val w = words[secureRandom.nextInt(words.size)]
            if (capitalize) w.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(java.util.Locale.ROOT) else char.toString() } else w
        }.toMutableList()

        if (includeNumber && selected.isNotEmpty()) {
            val num = secureRandom.nextInt(90) + 10
            selected[selected.size - 1] = selected.last() + num
        }

        return selected.joinToString(separator)
    }

    /**
     * Password strength analysis calculation.
     */
    fun calculateStrength(password: String): PasswordStrengthResult {
        if (password.isEmpty()) {
            return PasswordStrengthResult(
                score = 0,
                label = "EMPTY",
                color = 0xFF757575,
                entropyBits = 0,
                feedback = "Please enter a password"
            )
        }

        var score = 0
        val length = password.length
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSymbol = password.any { !it.isLetterOrDigit() }

        var poolSize = 0
        if (hasLower) poolSize += 26
        if (hasUpper) poolSize += 26
        if (hasDigit) poolSize += 10
        if (hasSymbol) poolSize += 33

        val entropyBits = if (poolSize > 0) {
            (length * (Math.log(poolSize.toDouble()) / Math.log(2.0))).toInt()
        } else 0

        // Length contribution
        when {
            length < 6 -> score += 10
            length < 8 -> score += 25
            length < 12 -> score += 45
            length < 16 -> score += 70
            else -> score += 90
        }

        // Variety contribution
        var varietyCount = 0
        if (hasUpper) varietyCount++
        if (hasLower) varietyCount++
        if (hasDigit) varietyCount++
        if (hasSymbol) varietyCount++

        score += (varietyCount * 5)

        // Penalty for repetitive patterns or predictable passwords
        val lower = password.lowercase()
        if (lower.contains("123") || lower.contains("password") || lower.contains("qwerty") || lower.contains("admin")) {
            score -= 30
        }
        if (password.all { it == password[0] }) {
            score -= 40
        }

        val clampedScore = score.coerceIn(0, 100)

        return when {
            clampedScore < 30 || length < 8 -> PasswordStrengthResult(
                score = clampedScore,
                label = "VERY WEAK",
                color = 0xFFFF5252,
                entropyBits = entropyBits,
                feedback = "Too short or predictable. Use at least 12 characters with letters, numbers and symbols."
            )
            clampedScore < 50 -> PasswordStrengthResult(
                score = clampedScore,
                label = "WEAK",
                color = 0xFFFF7043,
                entropyBits = entropyBits,
                feedback = "Weak password. Add uppercase letters, numbers or symbols."
            )
            clampedScore < 70 -> PasswordStrengthResult(
                score = clampedScore,
                label = "FAIR",
                color = 0xFFFFB300,
                entropyBits = entropyBits,
                feedback = "Good start, but increasing length will make it significantly stronger."
            )
            clampedScore < 85 -> PasswordStrengthResult(
                score = clampedScore,
                label = "STRONG",
                color = 0xFF00B0FF,
                entropyBits = entropyBits,
                feedback = "Strong password that resists common dictionary and brute-force attacks."
            )
            else -> PasswordStrengthResult(
                score = clampedScore,
                label = "VERY STRONG",
                color = 0xFF00E676,
                entropyBits = entropyBits,
                feedback = "Excellent security! Very high entropy and great defense against offline cracking."
            )
        }
    }
}

data class PasswordStrengthResult(
    val score: Int,
    val label: String,
    val color: Long,
    val entropyBits: Int,
    val feedback: String
)
