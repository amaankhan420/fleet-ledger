package com.example.fleetapp.components

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fleetapp.R
import com.example.fleetapp.dataclasses.FormData
import com.example.fleetapp.viewmodels.FormViewModel


@Composable
fun FormDataList(
    formDataMap: Map<String, List<FormData>>,
    formViewModel: FormViewModel,
) {
    val context = LocalContext.current
    var entry = 1
    LazyColumn {
        formDataMap.forEach { (partnerName, formDataList) ->
            items(
                items = formDataList,
                key = { entry++ }
            ) { formData ->
                FormDataItem(
                    formData = formData,
                    partnerName = partnerName,
                    formViewModel = formViewModel,
                    context = context
                )
            }
        }
    }
}

@Composable
private fun FormDataItem(
    formData: FormData,
    partnerName: String,
    formViewModel: FormViewModel,
    context: Context
) {
    var editingVehicleNumber by rememberSaveable { mutableStateOf(formData.vehicleNumber) }
    var editingDriverName by rememberSaveable { mutableStateOf(formData.driverName) }
    var editingAmount by rememberSaveable { mutableStateOf(formData.amount.toString()) }
    var editingIncentive by rememberSaveable { mutableStateOf(formData.incentive.toString()) }
    val errorMessage by formViewModel.editingError.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }

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
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = partnerName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                )
            }

            Text(
                text = "${context.getString(R.string.vehicle_number)}: ${formData.vehicleNumber}",
                modifier = Modifier.padding(top = 5.dp, start = 8.dp),
                style = TextStyle(fontSize = 16.sp)
            )
            Text(
                text = "${context.getString(R.string.driver_name)}: ${formData.driverName}",
                modifier = Modifier.padding(top = 5.dp, start = 8.dp),
                style = TextStyle(fontSize = 16.sp)
            )
            Text(
                text = "${context.getString(R.string.amount)}: ₹${formData.amount}",
                modifier = Modifier.padding(top = 5.dp, start = 8.dp),
                style = TextStyle(fontSize = 16.sp)
            )
            if (formData.incentive > 0f) {
                Text(
                    text = "${context.getString(R.string.incentive)}: ₹${formData.incentive}",
                    modifier = Modifier.padding(top = 5.dp, start = 8.dp),
                    style = TextStyle(fontSize = 16.sp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .width(150.dp)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = context.getString(R.string.edit),
                        fontSize = 16.sp
                    )
                }


                Button(
                    onClick = { formViewModel.removeFormData(partnerName, formData, context) },
                    modifier = Modifier
                        .width(150.dp)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = context.getString(R.string.delete),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(partnerName) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                ) {
                    if (errorMessage.isNotBlank()) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.padding(bottom = 8.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    FormInputs(
                        editing = true,
                        vehicleNumber = editingVehicleNumber,
                        onVehicleNumberChange = {
                            editingVehicleNumber = it
                            formViewModel.editingError.value = ""
                        },
                        driverName = editingDriverName,
                        onDriverNameChange = {
                            editingDriverName = it
                            formViewModel.editingError.value = ""
                        },
                        amount = editingAmount,
                        onAmountChange = {
                            editingAmount = it
                            formViewModel.editingError.value = ""
                        },
                        incentive = editingIncentive,
                        onIncentiveChanges = {
                            editingIncentive = it
                            formViewModel.editingError.value = ""
                        }
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
                            if (editingAmount.isBlank() || editingDriverName.isBlank() || editingVehicleNumber.isBlank()) {
                                formViewModel.editingError.value = context.getString(R.string.fill_all_field)
                                return@Button
                            }

                            if (editingIncentive.isBlank()) {
                                editingIncentive = "0.0"
                            }
                            val newFormData = FormData(
                                vehicleNumber = editingVehicleNumber,
                                driverName = editingDriverName,
                                amount = editingAmount.toFloat(),
                                incentive = editingIncentive.toFloat(),
                            )

                            if (newFormData == formData) {
                                showEditDialog = false
                            }
                            val success =
                                formViewModel.editFormData(partnerName, formData, newFormData, context = context)
                            if (success) {
                                showEditDialog = false
                            }
                        } catch (e: NumberFormatException) {
                            formViewModel.editingError.value = context.getString(R.string.please_enter_valid_numbers)
                        }
                    }
                ) {
                    Text(context.getString(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    onClick = { showEditDialog = false }
                ) {
                    Text(context.getString(R.string.cancel))
                }
            }
        )
    }
}
