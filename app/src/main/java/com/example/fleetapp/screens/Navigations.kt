package com.example.fleetapp.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fleetapp.routes.Routes
import com.example.fleetapp.routes.Routes.Form
import com.example.fleetapp.routes.Routes.ListOfEntry
import com.example.fleetapp.routes.Routes.PDF
import com.example.fleetapp.viewmodels.FormViewModel
import com.example.fleetapp.viewmodels.PDFViewModel


@Composable
fun Navigations() {
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()

        val formViewModel: FormViewModel = viewModel()

        val pdfViewModel: PDFViewModel = viewModel()

        val onNavigate: (Routes) -> Unit = { route ->
            navController.navigate(route)
        }

        val onBackStack: () -> Unit = {
            navController.popBackStack()
        }

        NavHost(
            navController = navController,
            startDestination = Form
        ) {
            composable<Form> {
                FormScreen(onNavigate = onNavigate, formViewModel = formViewModel, context = context)
            }

            composable<ListOfEntry> {
                ListScreen(formViewModel = formViewModel, onBackStack = onBackStack, context = context)
            }

            composable<PDF> {
                PDFScreen(
                    onBackStack = onBackStack,
                    pdfViewModel,
                    context = context)
            }
        }
    }
}
