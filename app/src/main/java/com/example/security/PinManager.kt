package com.example.security

import android.content.Context
import android.util.Base64
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinManager {
    private const val PREFS_NAME = "ahmad_guard_sec_prefs"
    private const val KEY_PIN_HASH = "master_pin_hash"
    private const val KEY_PIN_SALT = "master_pin_salt"
    private const val KEY_FAILED_ATTEMPTS = "failed_attempts_count"
    private const val KEY_LOCKOUT_UNTIL = "lockout_until_timestamp"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_AUTO_LOCK_TIMEOUT = "auto_lock_timeout"
    private const val KEY_CLIPBOARD_TIMEOUT = "clipboard_timeout"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_AI_LANGUAGE = "ai_language"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_AUTOFILL_ENABLED = "autofill_enabled"
    private const val KEY_AUTOFILL_AUTH_REQUIRED = "autofill_auth_required"

    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    private val secureRandom = SecureRandom()

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isMasterPinSet(context: Context): Boolean {
        val hash = getPrefs(context).getString(KEY_PIN_HASH, null)
        return !hash.isNullOrEmpty()
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ONBOARDING_DONE, false) && isMasterPinSet(context)
    }

    fun isPinCorrect(context: Context, inputPin: String): Boolean {
        val storedHash = getPrefs(context).getString(KEY_PIN_HASH, null) ?: return false
        val storedSaltStr = getPrefs(context).getString(KEY_PIN_SALT, null) ?: return false
        val salt = Base64.decode(storedSaltStr, Base64.NO_WRAP)
        val computedHash = hashPin(inputPin, salt) ?: return false
        return constantTimeEquals(storedHash, computedHash)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
    }

    fun setMasterPin(context: Context, pin: String): Boolean {
        if (pin.length < 4) return false
        val salt = ByteArray(16)
        secureRandom.nextBytes(salt)
        val hash = hashPin(pin, salt) ?: return false

        getPrefs(context).edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
        return true
    }

    fun resetFailedAttempts(context: Context) {
        getPrefs(context).edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .putLong(KEY_LOCKOUT_UNTIL, 0L)
            .apply()
    }

    fun getFailedAttempts(context: Context): Int {
        return getPrefs(context).getInt(KEY_FAILED_ATTEMPTS, 0)
    }

    fun getLockoutDurationSeconds(attempts: Int): Int {
        return when {
            attempts < 5 -> 0
            attempts == 5 -> 60     // 1 minute
            attempts == 6 -> 300    // 5 minutes
            attempts == 7 -> 900    // 15 minutes
            attempts == 8 -> 1800   // 30 minutes
            attempts == 9 -> 3600   // 60 minutes
            attempts == 10 -> 7200  // 120 minutes (2 hours)
            else -> 86400           // 24 hours
        }
    }

    fun verifyPin(context: Context, inputPin: String): PinVerificationResult {
        val prefs = getPrefs(context)
        val lockoutUntil = prefs.getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        val currentAttempts = prefs.getInt(KEY_FAILED_ATTEMPTS, 0)

        if (now < lockoutUntil) {
            val remainingSeconds = ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
            return PinVerificationResult.LockedOut(remainingSeconds, currentAttempts)
        }

        val storedHash = prefs.getString(KEY_PIN_HASH, null)
        val storedSaltStr = prefs.getString(KEY_PIN_SALT, null)

        if (storedHash == null || storedSaltStr == null) {
            return PinVerificationResult.NotSet
        }

        val salt = Base64.decode(storedSaltStr, Base64.NO_WRAP)
        val computedHash = hashPin(inputPin, salt)

        if (computedHash != null && constantTimeEquals(storedHash, computedHash)) {
            // Reset failed counter on successful PIN verification
            prefs.edit()
                .putInt(KEY_FAILED_ATTEMPTS, 0)
                .putLong(KEY_LOCKOUT_UNTIL, 0L)
                .apply()
            return PinVerificationResult.Success
        } else {
            val newAttempts = currentAttempts + 1
            val editor = prefs.edit().putInt(KEY_FAILED_ATTEMPTS, newAttempts)

            val lockoutSecs = getLockoutDurationSeconds(newAttempts)

            if (lockoutSecs > 0) {
                val newLockoutUntil = now + (lockoutSecs * 1000L)
                editor.putLong(KEY_LOCKOUT_UNTIL, newLockoutUntil)
                editor.apply()
                return PinVerificationResult.LockedOut(lockoutSecs, newAttempts)
            }

            editor.apply()
            val remainingBeforeLockout = (5 - newAttempts).coerceAtLeast(1)
            return PinVerificationResult.Failed(newAttempts, remainingBeforeLockout)
        }
    }

    fun getRemainingLockoutSeconds(context: Context): Int {
        val lockoutUntil = getPrefs(context).getLong(KEY_LOCKOUT_UNTIL, 0L)
        val now = System.currentTimeMillis()
        return if (now < lockoutUntil) {
            ((lockoutUntil - now) / 1000).toInt().coerceAtLeast(1)
        } else 0
    }

    private fun hashPin(pin: String, salt: ByteArray): String? {
        return try {
            val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
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

    // Biometric setting
    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    // Auto lock timeout
    fun getAutoLockTimeout(context: Context): Long {
        return getPrefs(context).getLong(KEY_AUTO_LOCK_TIMEOUT, 60_000L) // Default 1 min
    }

    fun setAutoLockTimeout(context: Context, millis: Long) {
        getPrefs(context).edit().putLong(KEY_AUTO_LOCK_TIMEOUT, millis).apply()
    }

    // Clipboard timeout
    fun getClipboardTimeoutSeconds(context: Context): Int {
        return getPrefs(context).getInt(KEY_CLIPBOARD_TIMEOUT, 30) // Default 30s
    }

    fun setClipboardTimeoutSeconds(context: Context, seconds: Int) {
        getPrefs(context).edit().putInt(KEY_CLIPBOARD_TIMEOUT, seconds).apply()
    }

    // Theme mode
    fun getThemeMode(context: Context): String {
        return getPrefs(context).getString(KEY_THEME_MODE, "DARK") ?: "DARK"
    }

    fun setThemeMode(context: Context, mode: String) {
        getPrefs(context).edit().putString(KEY_THEME_MODE, mode).apply()
    }

    // AI Language
    fun getAiLanguage(context: Context): String {
        return getPrefs(context).getString(KEY_AI_LANGUAGE, "AUTO") ?: "AUTO"
    }

    fun setAiLanguage(context: Context, language: String) {
        getPrefs(context).edit().putString(KEY_AI_LANGUAGE, language).apply()
    }

    // Autofill settings
    fun isAutofillEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTOFILL_ENABLED, true)
    }

    fun setAutofillEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTOFILL_ENABLED, enabled).apply()
    }

    fun isAutofillAuthRequired(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_AUTOFILL_AUTH_REQUIRED, true)
    }

    fun setAutofillAuthRequired(context: Context, required: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_AUTOFILL_AUTH_REQUIRED, required).apply()
    }

    fun clearAllData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}

sealed class PinVerificationResult {
    data object Success : PinVerificationResult()
    data object NotSet : PinVerificationResult()
    data class Failed(val attemptCount: Int, val attemptsRemainingBeforeLock: Int) : PinVerificationResult()
    data class LockedOut(val remainingSeconds: Int, val attemptCount: Int = 5) : PinVerificationResult()
}
