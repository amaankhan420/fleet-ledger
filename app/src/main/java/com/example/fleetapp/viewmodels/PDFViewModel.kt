package com.example.fleetapp.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetapp.utilities.PdfFunctions
import kotlinx.coroutines.launch
import java.io.File


class PDFViewModel : ViewModel() {

    var pdfList by mutableStateOf<List<File>>(emptyList())
        private set

    fun loadPdfs(context: Context) {
        viewModelScope.launch {
            pdfList = PdfFunctions.loadPdfs(context)
        }
    }

    fun deletePdf(context: Context, file: File) {
        viewModelScope.launch {
            if (PdfFunctions.deletePdf(context, file)) {
                loadPdfs(context)
            }
        }
    }

    fun sharePdf(context: Context, file: File) {
        PdfFunctions.sharePdf(context, file)
    }

    fun openPdf(context: Context, file: File) {
        PdfFunctions.openPdf(context, file)
    }
}
