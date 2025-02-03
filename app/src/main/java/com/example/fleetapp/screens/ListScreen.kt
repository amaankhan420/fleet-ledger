package com.example.fleetapp.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fleetapp.components.FormDataList
import com.example.fleetapp.components.TopBar
import com.example.fleetapp.viewmodels.FormViewModel
import android.util.Log
import com.example.fleetapp.R

@Composable
fun ListScreen(formViewModel: FormViewModel, onBackStack: () -> Unit, context: Context) {
    val formDataMap by formViewModel.formDataFlow.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf("") }
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    LaunchedEffect(context) {
        try {
            permissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        } catch (e: Exception) {
            Log.e("ListScreen", context.getString(R.string.permission_error), e)
            permissionGranted = false
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = context.getString(R.string.data_list), showBackButton = true, onBackStack = onBackStack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    try {
                        if (formDataMap.isEmpty()) {
                            return@FloatingActionButton
                        }
                        if (permissionGranted) {
                            showDialog = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    } catch (e: Exception) {
                        Log.e("ListScreen", context.getString(R.string.fab_error), e)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = context.getString(R.string.download),
                    modifier = Modifier.graphicsLayer(rotationZ = 90f)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (formDataMap.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = context.getString(R.string.no_data_to_display),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                FormDataList(
                    formDataMap = formDataMap,
                    formViewModel = formViewModel
                )
            }

        }
    }

    if (showDialog && formDataMap.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(context.getString(R.string.enter_company_name)) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    OutlinedTextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text(context.getString(R.string.company_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        try {
                            if (companyName.isNotBlank()) {
                                formViewModel.createPdfOfData(context, formDataMap, companyName)
                                showDialog = false
                            }
                        } catch (e: Exception) {
                            Log.e("ListScreen", context.getString(R.string.failed_to_generate_pdf), e)
                        }
                    }
                ) {
                    Text(context.getString(R.string.generate_pdf))
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    onClick = { showDialog = false }
                ) {
                    Text(context.getString(R.string.cancel))
                }
            }
        )
    }
}
