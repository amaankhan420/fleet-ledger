package com.example.fleetapp.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetapp.R
import com.example.fleetapp.dataclasses.FormData
import com.example.fleetapp.dataclasses.PartnerMetaData
import com.example.fleetapp.datastore.FormDataStore
import com.example.fleetapp.utilities.PdfFunctions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale


class FormViewModel(private val formDataStore: FormDataStore) : ViewModel() {
    val startDate = MutableStateFlow("")
    val endDate = MutableStateFlow("")
    val partnerName = MutableStateFlow("")
    val vehicleNumber = MutableStateFlow("")
    val driverName = MutableStateFlow("")
    val amount = MutableStateFlow("")
    val incentive = MutableStateFlow("")

    val errorMessage = MutableStateFlow("")
    val editingError = MutableStateFlow("")
    private val dataCount = MutableStateFlow(0)

    private val _formDataMap = MutableStateFlow<Map<String, List<FormData>>>(emptyMap())
    val formDataFlow = _formDataMap.asStateFlow()

    private val _partnerMetaData = MutableStateFlow<Map<String, PartnerMetaData>>(emptyMap())
    val partnerMetaDataFlow = _partnerMetaData.asStateFlow()

    private val _vehicleSet = MutableStateFlow<Set<String>>(emptySet())

    init {
        collectDataFlows()
    }

    private fun collectDataFlows() {
        viewModelScope.launch {
            formDataStore.formDataFlow.collect { formDataMap ->
                _formDataMap.value = formDataMap
            }
        }

        viewModelScope.launch {
            formDataStore.partnerMetaDataFlow.collect { partnerMetaData ->
                _partnerMetaData.value = partnerMetaData
            }
        }

        viewModelScope.launch {
            formDataStore.vehicleSetFlow.collect { vehicleSet ->
                _vehicleSet.value = vehicleSet
            }
        }
    }

    fun addFormData(context: Context) {
        viewModelScope.launch {
            try {
                val key = partnerName.value.trim().lowercase(Locale.ROOT)
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

                val amountValue = amount.value.trim().toFloat()
                val incentiveValue = incentive.value.trim().toFloat()

                if (_vehicleSet.value.contains(vehicleNumber.value) && incentiveValue > 0f) {
                    errorMessage.value = context.getString(R.string.incentive_already_added)
                    return@launch
                }
                val formData = FormData(
                    vehicleNumber = vehicleNumber.value.trim(),
                    driverName = driverName.value.trim(),
                    amount = amountValue,
                    incentive = incentiveValue,
                )
                formDataStore.addFormData(key, formData)
                dataCount.value = _formDataMap.value.values.sumOf { it.size }
                clearFields(context = context)
            } catch (e: NumberFormatException) {
                errorMessage.value = context.getString(R.string.please_enter_valid_numbers)
            } catch (e: Exception) {
                errorMessage.value = context.getString(R.string.unexpected_error)
            }
        }
    }

    fun removeFormData(partnerName: String, formData: FormData, context: Context) {
        viewModelScope.launch {
            try {
                formDataStore.removeFormData(partnerName, formData)
                dataCount.value = _formDataMap.value.values.sumOf { it.size }
            } catch (e: IllegalStateException) {
                Toast.makeText(context, context.getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_to_remove_input), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteAllFormData(context: Context) {
        viewModelScope.launch {
            try {
                formDataStore.deleteAllFormData()
                _formDataMap.value = emptyMap()
                _partnerMetaData.value = emptyMap()
                _vehicleSet.value = emptySet()
                dataCount.value = 0
                clearFields(true, context)
            } catch (e: Exception) {
                errorMessage.value = context.getString(R.string.failed_to_remove_data)
            }
        }
    }

    suspend fun editFormData(partnerName: String, oldFormData: FormData, newFormData: FormData, context: Context): Boolean {
        return try {
            if (newFormData.vehicleNumber != oldFormData.vehicleNumber &&
                _vehicleSet.value.contains(newFormData.vehicleNumber) && newFormData.incentive != 0f
            ) {
                editingError.value = context.getString(R.string.incentive_already_added)
                return false
            }

            if (newFormData.incentive != 0f && oldFormData.incentive == 0f &&
                _vehicleSet.value.contains(newFormData.vehicleNumber)
            ) {
                editingError.value = context.getString(R.string.incentive_already_added)
                return false
            }

            formDataStore.editFormData(partnerName, oldFormData, newFormData)
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


    fun addCommissionAndRemarks(partnerName: String, commission: String, remarks: String, context: Context) {
        viewModelScope.launch {
            try {
                val commissionValue = commission.trim().toFloat()
                formDataStore.updateCommissionAndRemarks(partnerName, commissionValue, remarks)
                if (remarks != "") {
                    Toast.makeText(context, "${context.getString(R.string.partner_data_updated)} $partnerName", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(context, "${context.getString(R.string.commission_updated)} $partnerName", Toast.LENGTH_SHORT).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, context.getString(R.string.please_enter_valid_numbers), Toast.LENGTH_SHORT).show()
            } catch (e: IndexOutOfBoundsException) {
                Toast.makeText(context, context.getString(R.string.failed_to_edit_data), Toast.LENGTH_SHORT).show()
            } catch (e: IllegalStateException) {
                Toast.makeText(context, context.getString(R.string.failed_to_edit_data), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_to_save_commission), Toast.LENGTH_SHORT).show()
            }
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
        } catch (e: Exception) {
            errorMessage.value = "${context.getString(R.string.failed_to_clear_data)}: ${e.message}"
        }
    }

    fun createPdfOfData(context: Context, nameOfCompany: String) {
        val formDataMap = _formDataMap.value
        val partnerMetadata = _partnerMetaData.value
        try {
            val result = PdfFunctions.createPdf(
                context,
                formDataMap,
                partnerMetadata,
                nameOfCompany,
                startDate.value,
                endDate.value
            )
            if (result) {
                Toast.makeText(context, context.getString(R.string.pdf_generated_successfully), Toast.LENGTH_SHORT).show()
                clearFields(true, context)
            }
        } catch (e: NullPointerException) {
            Toast.makeText(context, context.getString(R.string.no_data_to_display), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.failed_to_generate_pdf), Toast.LENGTH_SHORT).show()
        }
    }

    fun shareIndividualPDF(context: Context, partnerName: String) {
        val formDataList = _formDataMap.value[partnerName]
        val partnerMetadata = _partnerMetaData.value

        if (formDataList.isNullOrEmpty()) {
            Toast.makeText(context, context.getString(R.string.no_data_found), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val result = PdfFunctions.createPdf(
                context = context,
                formDataMap = mapOf(partnerName to formDataList),
                partnerMetadata = partnerMetadata,
                nameOfCompany = partnerName,
                startDate = startDate.value,
                endDate = endDate.value,
                isIndividual = true
            )

            if (result) {
                PdfFunctions.sharePdf(context, PdfFunctions.loadPdfs(context).last())
            }
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.failed_to_generate_pdf), Toast.LENGTH_SHORT).show()
        }
    }
}