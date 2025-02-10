package com.example.fleetapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fleetapp.R
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
    val partnerName by formViewModel.partnerName.collectAsState()
    val vehicleNumber by formViewModel.vehicleNumber.collectAsState()
    val driverName by formViewModel.driverName.collectAsState()
    val amount by formViewModel.amount.collectAsState()
    val incentives by formViewModel.incentive.collectAsState()
    val errorMessage by formViewModel.errorMessage.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
            .imePadding(),
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
                .padding(horizontal = 25.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        if (partnerName.isBlank() || vehicleNumber.isBlank()
                            || driverName.isBlank() || amount.isBlank()
                        ) {
                            formViewModel.errorMessage.value =
                                context.getString(R.string.fill_all_field)
                            return@Button
                        }
                        formViewModel.addFormData(context = context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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
                        .fillMaxWidth()
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

                Button(
                    onClick = {
                        formViewModel.clearFields(true, context = context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
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
            }
        }
    }
}
