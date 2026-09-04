package com.example.security

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper

object ClipboardHelper {
    private var clearRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastCopiedText: String? = null

    fun copyToClipboard(context: Context, label: String, text: String, timeoutSeconds: Int = 30): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            lastCopiedText = text

            // Cancel any pending clear runnable
            clearRunnable?.let { handler.removeCallbacks(it) }

            if (timeoutSeconds > 0) {
                clearRunnable = Runnable {
                    try {
                        val currentClip = clipboard.primaryClip
                        if (currentClip != null && currentClip.itemCount > 0) {
                            val currentText = currentClip.getItemAt(0).text?.toString()
                            if (currentText == lastCopiedText) {
                                clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                handler.postDelayed(clearRunnable!!, timeoutSeconds * 1000L)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
