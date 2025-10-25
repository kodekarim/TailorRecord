package com.example.tailorrecords.utils

import android.content.Context
import android.content.SharedPreferences

object ItemTypeManager {
    private const val PREFS_NAME = "tailor_prefs"
    private const val KEY_ITEM_TYPES = "item_types"

    private val defaultTypes = setOf("Shirt", "Pant", "Other")

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getItemTypes(context: Context): List<String> {
        val prefs = getPrefs(context)
        // Get custom types and add them to the default list.
        val customTypes = prefs.getStringSet(KEY_ITEM_TYPES, emptySet()) ?: emptySet()
        // Ensure "Other" is always last.
        return (defaultTypes.minus("Other") + customTypes).sorted() + "Other"
    }

    fun addItemType(context: Context, type: String) {
        if (type.isBlank() || defaultTypes.contains(type)) return

        val prefs = getPrefs(context)
        val currentTypes = prefs.getStringSet(KEY_ITEM_TYPES, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (currentTypes.add(type)) {
            prefs.edit().putStringSet(KEY_ITEM_TYPES, currentTypes).apply()
        }
    }
}
