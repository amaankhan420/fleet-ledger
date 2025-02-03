package com.example.fleetapp.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.fleetapp.R
import com.example.fleetapp.dataclasses.FormData
import com.example.fleetapp.utilities.PdfFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale


class FormViewModel : ViewModel() {
    val startDate = MutableStateFlow("")
    val endDate = MutableStateFlow("")
    val partnerName = MutableStateFlow("")
    val vehicleNumber = MutableStateFlow("")
    val driverName = MutableStateFlow("")
    val amount = MutableStateFlow("")
    val incentive = MutableStateFlow("")
    val commission = MutableStateFlow("")

    val errorMessage = MutableStateFlow("")
    val editingError = MutableStateFlow("")
    private val dataCount = MutableStateFlow(0)

    private val _formDataMap = MutableStateFlow<Map<String, List<FormData>>>(emptyMap())
    val formDataFlow = _formDataMap.asStateFlow()

    private val vehicleSet: MutableSet<String> = mutableSetOf()

    fun addFormData(context: Context) {
        try {
            var key = partnerName.value.trim().lowercase(Locale.ROOT)
            key =
                key.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            val amountValue = if (amount.value.trim() == "") 0f else amount.value.trim().toFloat()
            val incentiveValue =
                if (incentive.value.trim() == "") 0f else incentive.value.trim().toFloat()
            val commissionValue =
                if (commission.value.trim() == "") 0f else commission.value.trim().toFloat()

            if (_formDataMap.value.containsKey(key) && commissionValue > 0f) {
                errorMessage.value = context.getString(R.string.commission_already_added)
                return
            }
            if (vehicleSet.contains(vehicleNumber.value) && incentiveValue > 0f) {
                errorMessage.value = context.getString(R.string.incentive_already_added)
                return
            }
            if (!_formDataMap.value.containsKey(key) && commissionValue == 0f) {
                errorMessage.value =
                    context.getString(R.string.commission_is_mandatory_for_first_entry)
                return
            }


            val formData = FormData(
                vehicleNumber = vehicleNumber.value,
                driverName = driverName.value.trim(),
                amount = amountValue,
                incentive = incentiveValue,
                commission = commissionValue
            )

            _formDataMap.update { currentMap ->
                val updatedList = currentMap[key]?.toMutableList() ?: mutableListOf()
                updatedList.add(formData)
                currentMap + (key to updatedList)
            }

            if (incentiveValue > 0f) {
                vehicleSet.add(vehicleNumber.value)
            }

            dataCount.value++
            clearFields(context = context)
        } catch (e: NumberFormatException) {
            errorMessage.value = context.getString(R.string.please_enter_valid_numbers)
        } catch (e: Exception) {
            errorMessage.value = context.getString(R.string.unexpected_error)
        }
    }

    fun removeFormData(partnerName: String, formData: FormData, context: Context) {
        try {
            val updatedMap = _formDataMap.value.toMutableMap()

            val updatedList = updatedMap[partnerName]?.toMutableList()
                ?: throw IllegalStateException("${context.getString(R.string.no_data_found)}: $partnerName")

            updatedList.remove(formData)

            if (formData.commission > 0f && updatedList.isNotEmpty()) {
                updatedList[0] = updatedList[0].copy(commission = formData.commission)
            }

            if (updatedList.isEmpty()) {
                updatedMap.remove(partnerName)
            } else {
                updatedMap[partnerName] = updatedList
            }

            _formDataMap.value = updatedMap

            if (formData.incentive > 0f) {
                vehicleSet.remove(formData.vehicleNumber)
            }

            dataCount.value = updatedMap.values.sumOf { it.size }
        } catch (e: IllegalStateException) {
            Toast.makeText(context, context.getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.failed_to_remove_input), Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteAllFormData(context: Context) {
        try {
            _formDataMap.value = emptyMap()
            dataCount.value = 0
            vehicleSet.clear()
        } catch (e: Exception) {
            errorMessage.value = context.getString(R.string.failed_to_remove_data)
        }
    }

    fun editFormData(partnerName: String, oldFormData: FormData, newFormData: FormData, context: Context): Boolean {
        return try {
            val partnerEntries = _formDataMap.value[partnerName]?.toMutableList()
                ?: throw IllegalStateException("${R.string.no_data_found}: $partnerName")

            if (partnerEntries.size == 1 && newFormData.commission == 0f) {
                editingError.value = context.getString(R.string.commission_is_mandatory_for_first_entry)
                return false
            }

            if (newFormData.vehicleNumber != oldFormData.vehicleNumber) {
                if (vehicleSet.contains(newFormData.vehicleNumber) && newFormData.incentive != 0f) {
                    editingError.value = context.getString(R.string.incentive_already_added)
                    return false
                } else {
                    if (newFormData.incentive != 0f) {
                        vehicleSet.add(newFormData.vehicleNumber)
                    }
                }
            } else if (newFormData.incentive != 0f && oldFormData.incentive == 0f) {
                if (vehicleSet.contains(newFormData.vehicleNumber)) {
                    editingError.value = context.getString(R.string.incentive_already_added)
                    return false
                }
                vehicleSet.add(newFormData.vehicleNumber)
            }

            if (oldFormData.incentive != 0f) {
                vehicleSet.remove(oldFormData.vehicleNumber)
            }

            val index = partnerEntries.indexOf(oldFormData)
            if (index == -1) {
                throw IndexOutOfBoundsException(context.getString(R.string.entry_not_found))
            }

            partnerEntries[index] = newFormData

            _formDataMap.update { currentMap ->
                currentMap + (partnerName to partnerEntries.toList())
            }

            editingError.value = ""
            true
        } catch (e: IndexOutOfBoundsException) {
            editingError.value = "${context.getString(R.string.failed_to_edit_data)}: ${context.getString(R.string.entry_not_found)}"
            false
        } catch (e: IllegalStateException) {
            editingError.value = "${context.getString(R.string.data_inconsistency_detected)}: ${e.message}"
            false
        } catch (e: Exception) {
            editingError.value = "${context.getString(R.string.unexpected_error)}: ${e.message}"
            false
        }
    }

    fun clearFields(removePartner: Boolean = false, context: Context) {
        try {
            if (removePartner) {
                partnerName.value = ""
            }
            vehicleNumber.value = ""
            driverName.value = ""
            amount.value = ""
            errorMessage.value = ""
            incentive.value = ""
            commission.value = ""
        } catch (e: Exception) {
            errorMessage.value = "${context.getString(R.string.failed_to_clear_data)}: ${e.message}"
        }
    }

    fun createPdfOfData(
        context: Context,
        formDataMap: Map<String, List<FormData>>,
        nameOfCompany: String
    ) {
        try {
            val result = PdfFunctions.createPdf(
                context,
                formDataMap,
                nameOfCompany,
                startDate.value,
                endDate.value
            )
            if (result) {
                clearFields(true, context)
                deleteAllFormData(context)
            }
        } catch (e: NullPointerException) {
            Toast.makeText(context, context.getString(R.string.no_data_to_display), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.failed_to_generate_pdf), Toast.LENGTH_SHORT).show()
        }
    }
}
