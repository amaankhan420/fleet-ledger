package com.example.fleetapp.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fleetapp.R


@Composable
fun FormInputs(
    partnerName: String = "",
    onPartnerNameChange: (String) -> Unit = {},
    editing: Boolean = false,
    vehicleNumber: String,
    onVehicleNumberChange: (String) -> Unit,
    driverName: String,
    onDriverNameChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    incentive: String,
    onIncentiveChanges: (String) -> Unit,
    commission: String,
    onCommissionChange: (String) -> Unit,
    canEdit: Boolean = true
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .fillMaxWidth()
    ) {
        val context = LocalContext.current

        if (!editing) {
            InputTextField(context.getString(R.string.partner_name), partnerName, onPartnerNameChange)
        }
        InputTextField(context.getString(R.string.vehicle_number), vehicleNumber, onVehicleNumberChange)
        InputTextField(context.getString(R.string.driver_name), driverName, onDriverNameChange)
        InputTextField(context.getString(R.string.amount), amount, onAmountChange, true)
        InputTextField(context.getString(R.string.incentive), incentive, onIncentiveChanges, true)
        InputTextField(context.getString(R.string.commission), commission, onCommissionChange, true, canEdit)
    }
}
