package com.example.fleetapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.fleetapp.datastore.FormDataStore
import com.example.fleetapp.screens.Navigations
import com.example.fleetapp.ui.theme.FleetAppTheme
import com.example.fleetapp.viewmodels.FormViewModel
import com.example.fleetapp.viewmodels.FormViewModelFactory


class MainActivity : ComponentActivity() {
    private lateinit var formViewModel: FormViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val formDataStore = FormDataStore(applicationContext)

        val factory = FormViewModelFactory(formDataStore)
        formViewModel = ViewModelProvider(this, factory)[FormViewModel::class.java]
        enableEdgeToEdge()
        setContent {
            FleetAppTheme {
                Navigations(formViewModel)
            }
        }
    }
}
