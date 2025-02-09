package com.example.fleetapp.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fleetapp.dataclasses.FormData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fleet_data")

class FormDataStore(private val context: Context) {
    private val gson = Gson()
    private val FORM_DATA_KEY = stringPreferencesKey("form_data")
    private val PARTNER_META_KEY = stringPreferencesKey("partner_meta")

    val formDataFlow: Flow<Map<String, List<FormData>>> = context.dataStore.data.map { preferences ->
        val json = preferences[FORM_DATA_KEY]
        val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
        json?.let { gson.fromJson(it, type) } ?: emptyMap()
    }

    val partnerMetaDataFlow: Flow<Map<String, Pair<Float, String>>> = context.dataStore.data.map { preferences ->
        val json = preferences[PARTNER_META_KEY]
        val type = object : TypeToken<Map<String, Pair<Float, String>>>() {}.type
        json?.let { gson.fromJson(it, type) } ?: emptyMap()
    }

    suspend fun addFormData(partnerName: String, formData: FormData) {
        context.dataStore.edit { preferences ->
            val json = preferences[FORM_DATA_KEY]
            val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
            val currentData: MutableMap<String, List<FormData>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            val updatedList = currentData[partnerName]?.toMutableList() ?: mutableListOf()
            updatedList.add(formData)
            preferences[FORM_DATA_KEY] = gson.toJson(currentData + (partnerName to updatedList))
        }
    }

    suspend fun removeFormData(partnerName: String, formData: FormData) {
        context.dataStore.edit { preferences ->
            val json = preferences[FORM_DATA_KEY]
            val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
            val currentData: MutableMap<String, List<FormData>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            currentData[partnerName]?.toMutableList()?.apply {
                remove(formData)
                if (isEmpty()) currentData.remove(partnerName)
                removePartnerMetaData(partnerName)
            }
            preferences[FORM_DATA_KEY] = gson.toJson(currentData)
        }
    }

    suspend fun deleteAllFormData() {
        context.dataStore.edit { preferences ->
            preferences.remove(FORM_DATA_KEY)
            preferences.remove(PARTNER_META_KEY)
        }
    }

    suspend fun editFormData(partnerName: String, oldFormData: FormData, newFormData: FormData) {
        context.dataStore.edit { preferences ->
            val json = preferences[FORM_DATA_KEY]
            val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
            val currentData: MutableMap<String, List<FormData>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            currentData[partnerName]?.toMutableList()?.apply {
                val index = indexOf(oldFormData)
                if (index != -1) this[index] = newFormData
            }
            preferences[FORM_DATA_KEY] = gson.toJson(currentData)
        }
    }

    suspend fun addPartnerMetaData(partnerName: String, commission: Float, remarks: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PARTNER_META_KEY]
            val type = object : TypeToken<Map<String, Pair<Float, String>>>() {}.type
            val currentMetaData: MutableMap<String, Pair<Float, String>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            currentMetaData[partnerName] = Pair(commission, remarks)
            preferences[PARTNER_META_KEY] = gson.toJson(currentMetaData)
        }
    }

    private suspend fun removePartnerMetaData(partnerName: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PARTNER_META_KEY]
            val type = object : TypeToken<Map<String, Pair<Float, String>>>() {}.type
            val currentMetaData: MutableMap<String, Pair<Float, String>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            if (currentMetaData.containsKey(partnerName)) {
                currentMetaData.remove(partnerName)
                preferences[PARTNER_META_KEY] = gson.toJson(currentMetaData)
            }
        }
    }
}
