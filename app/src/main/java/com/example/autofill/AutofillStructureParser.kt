package com.example.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.text.InputType
import android.view.View
import android.view.autofill.AutofillId
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.O)
data class AutofillFieldNode(
    val autofillId: AutofillId,
    val hints: List<String> = emptyList(),
    val idEntry: String? = null,
    val hintText: String? = null,
    val className: String? = null,
    val isPassword: Boolean = false,
    val isUsername: Boolean = false,
    val value: String? = null
)

@RequiresApi(Build.VERSION_CODES.O)
data class AutofillParsedStructure(
    val packageName: String = "",
    val activityName: String? = null,
    val webDomain: String? = null,
    val usernameField: AutofillFieldNode? = null,
    val passwordField: AutofillFieldNode? = null,
    val allFields: List<AutofillFieldNode> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
object AutofillStructureParser {

    fun parse(structure: AssistStructure): AutofillParsedStructure {
        val packageName = structure.activityComponent?.packageName ?: ""
        val activityName = structure.activityComponent?.className
        var detectedWebDomain: String? = null

        val allNodes = mutableListOf<AutofillFieldNode>()

        val windowCount = structure.windowNodeCount
        for (i in 0 until windowCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val rootNode = windowNode.rootViewNode
            traverseNode(rootNode, allNodes) { domain ->
                if (detectedWebDomain == null && !domain.isNullOrBlank()) {
                    detectedWebDomain = domain
                }
            }
        }

        // Identify password field
        val passwordField = allNodes.firstOrNull { it.isPassword }
            ?: allNodes.firstOrNull { node ->
                val id = node.idEntry?.lowercase() ?: ""
                val hint = node.hintText?.lowercase() ?: ""
                id.contains("password") || id.contains("pass") || id.contains("pwd") ||
                        hint.contains("password") || hint.contains("pass")
            }

        // Identify username / email field
        val usernameField = allNodes.firstOrNull { it.isUsername }
            ?: allNodes.firstOrNull { node ->
                if (node.autofillId == passwordField?.autofillId) return@firstOrNull false
                val id = node.idEntry?.lowercase() ?: ""
                val hint = node.hintText?.lowercase() ?: ""
                id.contains("username") || id.contains("email") || id.contains("user") ||
                        id.contains("login") || id.contains("account") || id.contains("identifier") ||
                        hint.contains("username") || hint.contains("email") || hint.contains("phone") ||
                        hint.contains("user")
            }
            ?: allNodes.firstOrNull { node ->
                // First non-password editable field before password field
                node.autofillId != passwordField?.autofillId && !node.isPassword
            }

        return AutofillParsedStructure(
            packageName = packageName,
            activityName = activityName,
            webDomain = detectedWebDomain,
            usernameField = usernameField,
            passwordField = passwordField,
            allFields = allNodes
        )
    }

    private fun traverseNode(
        node: AssistStructure.ViewNode?,
        collected: MutableList<AutofillFieldNode>,
        onDomainFound: (String?) -> Unit
    ) {
        if (node == null) return

        if (!node.webDomain.isNullOrBlank()) {
            onDomainFound(node.webDomain)
        }

        val autofillId = node.autofillId
        if (autofillId != null && node.visibility == View.VISIBLE) {
            val hints = node.autofillHints?.toList() ?: emptyList()
            val idEntry = node.idEntry
            val hintText = node.hint?.toString()
            val className = node.className
            val inputType = node.inputType

            val isPasswordType = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_TEXT &&
                    (inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                            inputType and InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ||
                            inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ||
                    (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER &&
                    (inputType and InputType.TYPE_NUMBER_VARIATION_PASSWORD == InputType.TYPE_NUMBER_VARIATION_PASSWORD)

            val hasPasswordHint = hints.any { it.equals(View.AUTOFILL_HINT_PASSWORD, ignoreCase = true) }
            val isPassword = isPasswordType || hasPasswordHint

            val isEmailType = (inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) ||
                    (inputType and InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS)

            val hasUsernameHint = hints.any {
                it.equals(View.AUTOFILL_HINT_USERNAME, ignoreCase = true) ||
                        it.equals(View.AUTOFILL_HINT_EMAIL_ADDRESS, ignoreCase = true) ||
                        it.equals(View.AUTOFILL_HINT_PHONE, ignoreCase = true) ||
                        it.equals(View.AUTOFILL_HINT_NAME, ignoreCase = true)
            }

            val isUsername = isEmailType || hasUsernameHint

            val currentValue = node.autofillValue?.textValue?.toString()
                ?: node.text?.toString()

            // Collect if it's an interactive editable text field
            if (isPassword || isUsername || !hintText.isNullOrBlank() || !idEntry.isNullOrBlank() || inputType != InputType.TYPE_NULL) {
                collected.add(
                    AutofillFieldNode(
                        autofillId = autofillId,
                        hints = hints,
                        idEntry = idEntry,
                        hintText = hintText,
                        className = className,
                        isPassword = isPassword,
                        isUsername = isUsername,
                        value = currentValue
                    )
                )
            }
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), collected, onDomainFound)
        }
    }
}
