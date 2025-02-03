package com.example.fleetapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fleetapp.R
import com.example.fleetapp.components.DateRangePicker
import com.example.fleetapp.components.FormInputs
import com.example.fleetapp.components.TopBar
import com.example.fleetapp.routes.Routes
import com.example.fleetapp.routes.Routes.ListOfEntry
import com.example.fleetapp.routes.Routes.PDF
import com.example.fleetapp.viewmodels.FormViewModel


@Composable
fun FormScreen(
    onNavigate: (Routes) -> Unit,
    formViewModel: FormViewModel,
    context: Context
) {
    val formDataMap by formViewModel.formDataFlow.collectAsState()
    val startDate by formViewModel.startDate.collectAsState()
    val endDate by formViewModel.endDate.collectAsState()
    val partnerName by formViewModel.partnerName.collectAsState()
    val vehicleNumber by formViewModel.vehicleNumber.collectAsState()
    val driverName by formViewModel.driverName.collectAsState()
    val amount by formViewModel.amount.collectAsState()
    val incentives by formViewModel.incentive.collectAsState()
    val commission by formViewModel.commission.collectAsState()
    val errorMessage by formViewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current
    var showDeleteDialog by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(
            title = context.getString(R.string.fleet_tracker),
            showBackButton = false,
            showPDF = true,
            onPDFClick = { onNavigate(PDF) })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DateRangePicker { selectedStartDate, selectedEndDate ->
                formViewModel.startDate.value = selectedStartDate
                formViewModel.endDate.value = selectedEndDate
                formViewModel.errorMessage.value = ""
            }

            FormInputs(
                partnerName = partnerName,
                onPartnerNameChange = { formViewModel.partnerName.value = it },
                vehicleNumber = vehicleNumber,
                onVehicleNumberChange = { formViewModel.vehicleNumber.value = it },
                driverName = driverName,
                onDriverNameChange = { formViewModel.driverName.value = it },
                amount = amount,
                onAmountChange = { formViewModel.amount.value = it },
                incentive = incentives,
                onIncentiveChanges = { formViewModel.incentive.value = it },
                commission = commission,
                onCommissionChange = { formViewModel.commission.value = it }
            )

            if (errorMessage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = errorMessage,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(start = 15.dp, end = 5.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Text(
                text = "${context.getString(R.string.total_entries)}: ${formDataMap.values.sumOf { it.size }}",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (partnerName.isBlank() || startDate.isBlank() ||
                                endDate.isBlank() || vehicleNumber.isBlank()
                                || driverName.isBlank() || amount.isBlank()
                            ) {
                                formViewModel.errorMessage.value =
                                    context.getString(R.string.fill_all_field)
                                return@Button
                            }
                            formViewModel.addFormData(context = context)
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
                        Text(
                            context.getString(R.string.add_data),
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            onNavigate(ListOfEntry)
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
                        Text(
                            context.getString(R.string.view_list),
                            fontSize = 16.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            formViewModel.clearFields(true, context = context)
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
                        Text(
                            context.getString(R.string.clear_fields),
                            fontSize = 16.sp
                        )
                    }

                    Button(
                        onClick = {
                            showDeleteDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            context.getString(R.string.delete_all),
                            fontSize = 16.sp
                        )
                    }
                }
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

