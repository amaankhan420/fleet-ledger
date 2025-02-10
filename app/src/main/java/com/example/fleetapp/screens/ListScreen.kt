package com.example.fleetapp.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fleetapp.R
import com.example.fleetapp.components.FormDataList
import com.example.fleetapp.components.TopBar
import com.example.fleetapp.routes.Routes
import com.example.fleetapp.routes.Routes.Commission
import com.example.fleetapp.viewmodels.FormViewModel


@Composable
fun ListScreen(
    formViewModel: FormViewModel,
    onNavigate: (Routes) -> Unit,
    onBackStack: () -> Unit,
    context: Context
) {
    val formDataMap by formViewModel.formDataFlow.collectAsState()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val partnerNames = remember(formDataMap) {
        listOf(context.getString(R.string.all)) + formDataMap.keys.distinct()
    }

    var selectedPartner by rememberSaveable { mutableStateOf(context.getString(R.string.all)) }

    LaunchedEffect(formDataMap) {
        if (selectedPartner != context.getString(R.string.all) && selectedPartner !in formDataMap.keys) {
            selectedPartner = context.getString(R.string.all)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = context.getString(R.string.data_list),
                showBackButton = true,
                onBackStack = onBackStack,
                enterCommission = true,
                onCommissionClick = { onNavigate(Commission) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    try {
                        showDeleteDialog = true
                    } catch (e: Exception) {
                        Log.e("ListScreen", context.getString(R.string.fab_error), e)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.error
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = context.getString(R.string.download),
                )
            }
        },
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var expanded by remember { mutableStateOf(false) }

            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)) {
                OutlinedTextField(
                    value = selectedPartner,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Partner") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Dropdown",
                            modifier = Modifier.clickable { expanded = true }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    partnerNames.forEach { partner ->
                        DropdownMenuItem(
                            text = { Text(partner) },
                            onClick = {
                                selectedPartner = partner
                                expanded = false
                            }
                        )
                    }
                }
            }

            val filteredData = remember(selectedPartner, formDataMap) {
                if (selectedPartner == context.getString(R.string.all)) formDataMap else formDataMap.filterKeys { it == selectedPartner }
            }

            if (filteredData.isEmpty()) {
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
                    formDataMap = filteredData,
                    formViewModel = formViewModel
                )
            }
        }
    }

    if (showDeleteDialog && formDataMap.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = { Text(context.getString(R.string.confirm_deletion)) },
            text = { Text(context.getString(R.string.are_you_sure)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        formViewModel.deleteAllFormData(context = context)
                        showDeleteDialog = false
                    }
                ) {
                    Text(context.getString(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(context.getString(R.string.no))
                }
            }
        )
    }
}
