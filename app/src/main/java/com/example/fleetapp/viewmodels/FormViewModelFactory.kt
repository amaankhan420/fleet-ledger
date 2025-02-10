package com.example.fleetapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fleetapp.datastore.FormDataStore


class FormViewModelFactory(private val formDataStore: FormDataStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FormViewModel(formDataStore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}