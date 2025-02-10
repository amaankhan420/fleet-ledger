package com.example.fleetapp.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.fleetapp.dataclasses.FormData
import com.example.fleetapp.dataclasses.PartnerMetaData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fleet_data")

class FormDataStore(private val context: Context) {
    private val gson = Gson()
    private val FORM_DATA_KEY = stringPreferencesKey("form_data")
    private val PARTNER_META_KEY = stringPreferencesKey("partner_meta")
    private val VEHICLE_SET_KEY = stringPreferencesKey("vehicle_set")

    val formDataFlow: Flow<Map<String, List<FormData>>> = context.dataStore.data.map { preferences ->
        val json = preferences[FORM_DATA_KEY]
        val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
        json?.let { gson.fromJson(it, type) } ?: emptyMap()
    }

    val partnerMetaDataFlow: Flow<Map<String, PartnerMetaData>> = context.dataStore.data.map { preferences ->
        val json = preferences[PARTNER_META_KEY]
        val type = object : TypeToken<Map<String, PartnerMetaData>>() {}.type
        json?.let { gson.fromJson(it, type) } ?: emptyMap()
    }

    val vehicleSetFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        val json = preferences[VEHICLE_SET_KEY]
        val type = object : TypeToken<Set<String>>() {}.type
        json?.let { gson.fromJson(it, type) } ?: emptySet()
    }

    suspend fun addFormData(partnerName: String, formData: FormData) {
        context.dataStore.edit { preferences ->
            val json = preferences[FORM_DATA_KEY]
            val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
            val currentData: MutableMap<String, List<FormData>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            val updatedList = currentData[partnerName]?.toMutableList() ?: mutableListOf()
            updatedList.add(formData)
            preferences[FORM_DATA_KEY] = gson.toJson(currentData + (partnerName to updatedList))

            val vehicleJson = preferences[VEHICLE_SET_KEY]
            val vehicleSet: MutableSet<String> = vehicleJson?.let { gson.fromJson(it, object : TypeToken<Set<String>>() {}.type) } ?: mutableSetOf()
            vehicleSet.add(formData.vehicleNumber)
            preferences[VEHICLE_SET_KEY] = gson.toJson(vehicleSet)
        }
        addPartnerMetaData(partnerName)
    }

    suspend fun removeFormData(partnerName: String, formData: FormData) {
        context.dataStore.edit { preferences ->
            val json = preferences[FORM_DATA_KEY]
            val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
            val currentData: MutableMap<String, List<FormData>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            currentData[partnerName] = currentData[partnerName]?.filterNot { it.vehicleNumber == formData.vehicleNumber } ?: emptyList()
            if (currentData[partnerName].isNullOrEmpty()) {
                currentData.remove(partnerName)
                val metaJson = preferences[PARTNER_META_KEY]
                val metaType = object : TypeToken<Map<String, PartnerMetaData>>() {}.type
                val currentMetaData: MutableMap<String, PartnerMetaData> = metaJson?.let { gson.fromJson(it, metaType) } ?: mutableMapOf()

                currentMetaData.remove(partnerName)
                preferences[PARTNER_META_KEY] = gson.toJson(currentMetaData)
            }

            preferences[FORM_DATA_KEY] = gson.toJson(currentData)

            val vehicleJson = preferences[VEHICLE_SET_KEY]
            val vehicleSet: MutableSet<String> = vehicleJson?.let { gson.fromJson(it, object : TypeToken<Set<String>>() {}.type) } ?: mutableSetOf()
            vehicleSet.remove(formData.vehicleNumber)
            preferences[VEHICLE_SET_KEY] = gson.toJson(vehicleSet)
        }
    }

    suspend fun deleteAllFormData() {
        context.dataStore.edit { preferences ->
            preferences.remove(FORM_DATA_KEY)
            preferences.remove(PARTNER_META_KEY)
            preferences.remove(VEHICLE_SET_KEY)
        }
    }

    suspend fun editFormData(partnerName: String, oldFormData: FormData, newFormData: FormData) {
        context.dataStore.edit { preferences ->
            val json = preferences[FORM_DATA_KEY]
            val type = object : TypeToken<Map<String, List<FormData>>>() {}.type
            val currentData: MutableMap<String, List<FormData>> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            currentData[partnerName] = currentData[partnerName]?.map {
                if (it.vehicleNumber == oldFormData.vehicleNumber) newFormData else it
            } ?: emptyList()

            preferences[FORM_DATA_KEY] = gson.toJson(currentData)

            if (oldFormData.vehicleNumber != newFormData.vehicleNumber) {
                val vehicleJson = preferences[VEHICLE_SET_KEY]
                val vehicleSet: MutableSet<String> = vehicleJson?.let { gson.fromJson(it, object : TypeToken<Set<String>>() {}.type) } ?: mutableSetOf()
                vehicleSet.remove(oldFormData.vehicleNumber)
                vehicleSet.add(newFormData.vehicleNumber)
                preferences[VEHICLE_SET_KEY] = gson.toJson(vehicleSet)
            }
        }
    }

    private suspend fun addPartnerMetaData(partnerName: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PARTNER_META_KEY]
            val type = object : TypeToken<Map<String, PartnerMetaData>>() {}.type
            val currentData: MutableMap<String, PartnerMetaData> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            currentData[partnerName] = PartnerMetaData()

            preferences[PARTNER_META_KEY] = gson.toJson(currentData)
        }
    }

    suspend fun updateCommissionAndRemarks(partnerName: String, newCommission: Float, newRemarks: String) {
        context.dataStore.edit { preferences ->
            val json = preferences[PARTNER_META_KEY]
            val type = object : TypeToken<Map<String, PartnerMetaData>>() {}.type
            val currentMetaData: MutableMap<String, PartnerMetaData> = json?.let { gson.fromJson(it, type) } ?: mutableMapOf()

            val existingMeta = currentMetaData[partnerName]
            if (existingMeta != null) {
                currentMetaData[partnerName] = existingMeta.copy(commission = newCommission, remarks = newRemarks)
                preferences[PARTNER_META_KEY] = gson.toJson(currentMetaData)
            }
        }
    }
}
