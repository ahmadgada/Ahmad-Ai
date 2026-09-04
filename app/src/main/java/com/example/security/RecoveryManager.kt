package com.example.security

import android.content.Context
import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class RecoveryConfig(
    val isConfigured: Boolean,
    val question1: String,
    val question2: String,
    val hasRecoveryKey: Boolean
)

object RecoveryManager {
    private const val PREFS_NAME = "ahmad_guard_recovery_prefs"
    private const val KEY_IS_CONFIGURED = "recovery_is_configured"
    private const val KEY_QUESTION_1 = "recovery_q1"
    private const val KEY_ANSWER_1_HASH = "recovery_a1_hash"
    private const val KEY_ANSWER_1_SALT = "recovery_a1_salt"
    private const val KEY_QUESTION_2 = "recovery_q2"
    private const val KEY_ANSWER_2_HASH = "recovery_a2_hash"
    private const val KEY_ANSWER_2_SALT = "recovery_a2_salt"
    private const val KEY_RECOVERY_KEY_HASH = "recovery_key_hash"
    private const val KEY_RECOVERY_KEY_SALT = "recovery_key_salt"

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private val secureRandom = SecureRandom()

    val PRESET_QUESTIONS = listOf(
        "What was the name of your first school?",
        "In what city or town did your parents meet?",
        "What was the model of your very first smartphone?",
        "What was the name of your favorite childhood pet?",
        "What was the name of the street you grew up on?"
    )

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isRecoveryConfigured(context: Context): Boolean {
        val prefs = getPrefs(context)
        return prefs.getBoolean(KEY_IS_CONFIGURED, false) &&
                !prefs.getString(KEY_ANSWER_1_HASH, null).isNullOrEmpty()
    }

    fun getRecoveryConfig(context: Context): RecoveryConfig {
        val prefs = getPrefs(context)
        val configured = isRecoveryConfigured(context)
        return RecoveryConfig(
            isConfigured = configured,
            question1 = prefs.getString(KEY_QUESTION_1, "") ?: "",
            question2 = prefs.getString(KEY_QUESTION_2, "") ?: "",
            hasRecoveryKey = !prefs.getString(KEY_RECOVERY_KEY_HASH, null).isNullOrEmpty()
        )
    }

    fun generateNewRecoveryKey(): String {
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val builder = StringBuilder("AGRD-")
        for (i in 0 until 12) {
            if (i > 0 && i % 4 == 0) {
                builder.append("-")
            }
            builder.append(chars[secureRandom.nextInt(chars.length)])
        }
        return builder.toString()
    }

    /**
     * Saves recovery configuration with PBKDF2-hashed answers and recovery key.
     * Never stores plaintext answers.
     */
    fun saveRecoverySetup(
        context: Context,
        question1: String,
        answer1: String,
        question2: String,
        answer2: String,
        recoveryKey: String
    ): Boolean {
        val cleanA1 = answer1.trim().lowercase()
        val cleanA2 = answer2.trim().lowercase()
        val cleanKey = recoveryKey.trim().replace("-", "").uppercase()

        if (question1.isBlank() || cleanA1.length < 2) return false
        if (question2.isBlank() || cleanA2.length < 2) return false

        val salt1 = ByteArray(16).also { secureRandom.nextBytes(it) }
        val hash1 = hashSecret(cleanA1, salt1) ?: return false

        val salt2 = ByteArray(16).also { secureRandom.nextBytes(it) }
        val hash2 = hashSecret(cleanA2, salt2) ?: return false

        val editor = getPrefs(context).edit()
            .putBoolean(KEY_IS_CONFIGURED, true)
            .putString(KEY_QUESTION_1, question1.trim())
            .putString(KEY_ANSWER_1_HASH, hash1)
            .putString(KEY_ANSWER_1_SALT, Base64.encodeToString(salt1, Base64.NO_WRAP))
            .putString(KEY_QUESTION_2, question2.trim())
            .putString(KEY_ANSWER_2_HASH, hash2)
            .putString(KEY_ANSWER_2_SALT, Base64.encodeToString(salt2, Base64.NO_WRAP))

        if (cleanKey.isNotBlank()) {
            val saltKey = ByteArray(16).also { secureRandom.nextBytes(it) }
            val hashKey = hashSecret(cleanKey, saltKey)
            if (hashKey != null) {
                editor.putString(KEY_RECOVERY_KEY_HASH, hashKey)
                editor.putString(KEY_RECOVERY_KEY_SALT, Base64.encodeToString(saltKey, Base64.NO_WRAP))
            }
        }

        editor.apply()
        return true
    }

    /**
     * Verifies security questions against stored hashes in constant-time.
     */
    fun verifyQuestions(
        context: Context,
        answer1: String,
        answer2: String
    ): Boolean {
        val prefs = getPrefs(context)
        if (!prefs.getBoolean(KEY_IS_CONFIGURED, false)) return false

        val storedHash1 = prefs.getString(KEY_ANSWER_1_HASH, null) ?: return false
        val storedSaltStr1 = prefs.getString(KEY_ANSWER_1_SALT, null) ?: return false
        val salt1 = Base64.decode(storedSaltStr1, Base64.NO_WRAP)
        val computedHash1 = hashSecret(answer1.trim().lowercase(), salt1) ?: return false

        val storedHash2 = prefs.getString(KEY_ANSWER_2_HASH, null) ?: return false
        val storedSaltStr2 = prefs.getString(KEY_ANSWER_2_SALT, null) ?: return false
        val salt2 = Base64.decode(storedSaltStr2, Base64.NO_WRAP)
        val computedHash2 = hashSecret(answer2.trim().lowercase(), salt2) ?: return false

        val match1 = constantTimeEquals(storedHash1, computedHash1)
        val match2 = constantTimeEquals(storedHash2, computedHash2)
        return match1 && match2
    }

    /**
     * Verifies Emergency Recovery Key against stored hash in constant-time.
     */
    fun verifyRecoveryKey(
        context: Context,
        inputKey: String
    ): Boolean {
        val prefs = getPrefs(context)
        val storedHash = prefs.getString(KEY_RECOVERY_KEY_HASH, null) ?: return false
        val storedSaltStr = prefs.getString(KEY_RECOVERY_KEY_SALT, null) ?: return false
        val salt = Base64.decode(storedSaltStr, Base64.NO_WRAP)

        val cleanKey = inputKey.trim().replace("-", "").uppercase()
        val computedHash = hashSecret(cleanKey, salt) ?: return false
        return constantTimeEquals(storedHash, computedHash)
    }

    fun disableRecovery(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    private fun hashSecret(secret: String, salt: ByteArray): String? {
        return try {
            val spec = PBEKeySpec(secret.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: NoSuchAlgorithmException) {
            null
        } catch (e: InvalidKeySpecException) {
            null
        }
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val aBytes = a.toByteArray()
        val bBytes = b.toByteArray()
        if (aBytes.size != bBytes.size) return false
        var result = 0
        for (i in aBytes.indices) {
            result = result or (aBytes[i].toInt() xor bBytes[i].toInt())
        }
        return result == 0
    }
}
