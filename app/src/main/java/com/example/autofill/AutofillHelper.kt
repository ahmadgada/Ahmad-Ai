package com.example.autofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import com.example.R
import com.example.model.VaultItem
import com.example.model.VaultType

object AutofillHelper {

    fun isAutofillSupported(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(AutofillManager::class.java)
            manager != null && manager.isAutofillSupported
        } else {
            false
        }
    }

    fun isAhmadGuardAutofillEnabled(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(AutofillManager::class.java)
            manager != null && manager.hasEnabledAutofillServices()
        } else {
            false
        }
    }

    fun openAutofillSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to general settings or autofill manager request
                try {
                    val fallbackIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(fallbackIntent)
                } catch (_: Exception) {}
            }
        }
    }

    fun buildDatasetPresentation(
        context: Context,
        title: String,
        subtitle: String
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.autofill_dataset_item)
        views.setTextViewText(R.id.autofill_title, title)
        views.setTextViewText(R.id.autofill_subtitle, subtitle)
        return views
    }

    fun buildAuthPresentation(
        context: Context,
        title: String = "Unlock Cyber Guard Vault",
        subtitle: String = "Authenticate with Master PIN / Biometrics"
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.autofill_auth_item)
        views.setTextViewText(R.id.autofill_auth_title, title)
        views.setTextViewText(R.id.autofill_auth_subtitle, subtitle)
        return views
    }

    /**
     * Match vault credentials against requesting package name and web domain.
     */
    fun findMatchingItems(
        items: List<VaultItem>,
        packageName: String,
        webDomain: String?
    ): List<VaultItem> {
        val loginItems = items.filter { it.type == VaultType.LOGIN }
        if (loginItems.isEmpty()) return emptyList()

        val normalizedTerms = extractSearchKeywords(packageName, webDomain)

        // Score and sort items
        val scoredItems = loginItems.map { item ->
            val score = calculateMatchScore(item, normalizedTerms, webDomain)
            Pair(item, score)
        }

        val positiveMatches = scoredItems
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        return if (positiveMatches.isNotEmpty()) {
            positiveMatches
        } else {
            // Fallback: return favorites and recent items so user can choose
            loginItems.sortedWith(compareByDescending<VaultItem> { it.favorite }.thenByDescending { it.updatedAt })
        }
    }

    private fun extractSearchKeywords(packageName: String, webDomain: String?): List<String> {
        val terms = mutableListOf<String>()

        if (!webDomain.isNullOrBlank()) {
            val cleanDomain = webDomain.lowercase()
                .replace("https://", "")
                .replace("http://", "")
                .replace("www.", "")
                .split("/").firstOrNull() ?: ""
            terms.add(cleanDomain)
            val parts = cleanDomain.split(".")
            for (p in parts) {
                if (p.length > 2 && p != "com" && p != "org" && p != "net" && p != "io" && p != "app") {
                    terms.add(p)
                }
            }
        }

        if (packageName.isNotBlank()) {
            val parts = packageName.lowercase().split(".")
            for (part in parts) {
                if (part.length > 2 &&
                    part != "com" && part != "android" && part != "app" && part != "apps" &&
                    part != "org" && part != "net" && part != "google" && part != "mobile"
                ) {
                    terms.add(part)
                }
            }
            // Known popular package mapping
            when {
                packageName.contains("instagram") -> terms.addAll(listOf("instagram", "insta", "ig"))
                packageName.contains("facebook") || packageName.contains("katana") -> terms.addAll(listOf("facebook", "fb", "meta"))
                packageName.contains("twitter") || packageName.contains("com.x") -> terms.addAll(listOf("twitter", "x"))
                packageName.contains("whatsapp") -> terms.addAll(listOf("whatsapp", "wa"))
                packageName.contains("google") || packageName.contains("gmail") -> terms.addAll(listOf("google", "gmail"))
                packageName.contains("linkedin") -> terms.addAll(listOf("linkedin"))
                packageName.contains("spotify") -> terms.addAll(listOf("spotify"))
                packageName.contains("netflix") -> terms.addAll(listOf("netflix"))
                packageName.contains("amazon") -> terms.addAll(listOf("amazon", "aws"))
                packageName.contains("github") -> terms.addAll(listOf("github"))
                packageName.contains("reddit") -> terms.addAll(listOf("reddit"))
                packageName.contains("discord") -> terms.addAll(listOf("discord"))
                packageName.contains("telegram") -> terms.addAll(listOf("telegram"))
                packageName.contains("tiktok") -> terms.addAll(listOf("tiktok"))
            }
        }

        return terms.distinct()
    }

    private fun calculateMatchScore(
        item: VaultItem,
        keywords: List<String>,
        webDomain: String?
    ): Int {
        var score = 0
        val itemName = item.name.lowercase()
        val itemUrl = item.url.lowercase()
        val itemNotes = item.notes.lowercase()
        val itemTags = item.tags.map { it.lowercase() }

        if (!webDomain.isNullOrBlank() && itemUrl.contains(webDomain, ignoreCase = true)) {
            score += 100
        }

        for (kw in keywords) {
            if (kw.isBlank()) continue
            if (itemName == kw) {
                score += 80
            } else if (itemName.contains(kw)) {
                score += 50
            }
            if (itemUrl.contains(kw)) {
                score += 60
            }
            if (itemTags.contains(kw)) {
                score += 40
            }
            if (itemNotes.contains(kw)) {
                score += 20
            }
        }

        if (item.favorite) score += 5
        return score
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createAuthPendingIntent(
        context: Context,
        itemId: Long,
        usernameId: AutofillId?,
        passwordId: AutofillId?,
        targetPackageName: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, AutofillAuthActivity::class.java).apply {
            putExtra(AutofillAuthActivity.EXTRA_ITEM_ID, itemId)
            if (usernameId != null) {
                putExtra(AutofillAuthActivity.EXTRA_USERNAME_ID, usernameId)
            }
            if (passwordId != null) {
                putExtra(AutofillAuthActivity.EXTRA_PASSWORD_ID, passwordId)
            }
            putExtra(AutofillAuthActivity.EXTRA_TARGET_PACKAGE, targetPackageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            flags
        )
    }
}
