package com.example.fleetapp.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.fleetapp.R
import com.example.fleetapp.components.DateRangePicker
import com.example.fleetapp.components.InputTextField
import com.example.fleetapp.components.TopBar
import com.example.fleetapp.viewmodels.FormViewModel


@Composable
fun CommissionAdderScreen(
    formViewModel: FormViewModel,
    onBackStack: () -> Unit,
    context: Context
) {
    val partnerMetadata by formViewModel.partnerMetaDataFlow.collectAsState()
    val formDataMap by formViewModel.formDataFlow.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var companyName by remember { mutableStateOf("") }
    var permissionGranted by remember { mutableStateOf(false) }

    val startDate by formViewModel.startDate.collectAsState()
    val endDate by formViewModel.endDate.collectAsState()


    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    LaunchedEffect(context) {
        permissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    Scaffold(
        topBar = {
            TopBar(title = "${context.getString(R.string.commission)}s", showBackButton = true, onBackStack = onBackStack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    try {
                        if (partnerMetadata.isEmpty()) return@FloatingActionButton
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
                    imageVector = Icons.Default.Download,
                    contentDescription = context.getString(R.string.download),
                )
            }
        },
        modifier = Modifier
            .fillMaxSize()
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
            if (formDataMap.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxSize()
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
                LazyColumn {
                    formDataMap.forEach { (partnerName, formDataList) ->
                        val totalProfit = formDataList.sumOf { it.amount.toDouble() }.toFloat()
                        item {
                            CommissionCard(partnerName, totalProfit, context, formViewModel)
                        }
                    }
                }
            }
        }
    }

    if (showDialog && formDataMap.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Column {
                    Text(
                        text = context.getString(R.string.ensure_commission_values),
                        style = MaterialTheme.typography.bodySmall,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DateRangePicker { selectedStartDate, selectedEndDate ->
                        formViewModel.startDate.value = selectedStartDate
                        formViewModel.endDate.value = selectedEndDate
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = context.getString(R.string.enter_company_name),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text(context.getString(R.string.company_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    onClick = {
                        if (companyName.isNotBlank() && startDate.isNotEmpty() && endDate.isNotEmpty()) {
                            formViewModel.createPdfOfData(context, companyName)
                            showDialog = false
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


@Composable
fun CommissionCard(partnerName: String, partnerProfit: Float, context: Context, formViewModel: FormViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(8.dp)
            .clip(MaterialTheme.shapes.medium),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSecondary,
            contentColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        var editingCommission by rememberSaveable {
            mutableStateOf(formViewModel.partnerMetaDataFlow.value[partnerName]?.commission?.toString() ?: "")
        }
        var editingRemarks by rememberSaveable {
            mutableStateOf(formViewModel.partnerMetaDataFlow.value[partnerName]?.remarks ?: "")
        }

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${context.getString(R.string.partner_name)}: $partnerName",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "${context.getString(R.string.amount)}: ₹$partnerProfit",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                IconButton(
                    onClick = {
                        formViewModel.shareIndividualPDF(context, partnerName)
                    }) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = context.getString(R.string.share)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            InputTextField(
                text = context.getString(R.string.remarks),
                fieldName = editingRemarks,
                onChange = {
                    editingRemarks = it
                },
                isNumber = false,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(2f)
                ) {
                    InputTextField(
                        text = context.getString(R.string.commission),
                        fieldName = editingCommission,
                        onChange = {
                            editingCommission = it
                        },
                        isNumber = true,
                    )
                }

                Button(
                    onClick = {
                        formViewModel.addCommissionAndRemarks(partnerName, editingCommission, editingRemarks, context)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(text = context.getString(R.string.add))
                }
            }
        }
    }
}
