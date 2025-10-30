package com.example.tailorrecords.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CustomizationManager {
    private const val PREFS_NAME = "customization_prefs"
    private const val KEY_CUSTOMIZATIONS = "customizations"
    private val gson = Gson()

    // Get all customization options for a specific item type
    fun getCustomizations(context: Context, itemType: String): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CUSTOMIZATIONS, null) ?: return emptyList()
        
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        val allCustomizations: Map<String, List<String>> = gson.fromJson(json, type)
        
        return allCustomizations[itemType.lowercase()] ?: emptyList()
    }

    // Add a new customization option for a specific item type
    fun addCustomization(context: Context, itemType: String, customization: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CUSTOMIZATIONS, null)
        
        val type = object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type
        val allCustomizations: MutableMap<String, MutableList<String>> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableMapOf()
        }
        
        val key = itemType.lowercase()
        val list = allCustomizations.getOrPut(key) { mutableListOf() }
        
        if (!list.contains(customization)) {
            list.add(customization)
            allCustomizations[key] = list
            
            val newJson = gson.toJson(allCustomizations)
            prefs.edit().putString(KEY_CUSTOMIZATIONS, newJson).apply()
        }
    }

    // Remove a customization option
    fun removeCustomization(context: Context, itemType: String, customization: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CUSTOMIZATIONS, null) ?: return
        
        val type = object : TypeToken<MutableMap<String, MutableList<String>>>() {}.type
        val allCustomizations: MutableMap<String, MutableList<String>> = gson.fromJson(json, type)
        
        val key = itemType.lowercase()
        allCustomizations[key]?.remove(customization)
        
        val newJson = gson.toJson(allCustomizations)
        prefs.edit().putString(KEY_CUSTOMIZATIONS, newJson).apply()
    }

    // Get all item types that have customizations
    fun getAllItemTypes(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CUSTOMIZATIONS, null) ?: return emptyList()
        
        val type = object : TypeToken<Map<String, List<String>>>() {}.type
        val allCustomizations: Map<String, List<String>> = gson.fromJson(json, type)
        
        return allCustomizations.keys.toList()
    }
}


