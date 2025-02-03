package com.example.fleetapp.viewmodels

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fleetapp.utilities.PdfFunctions
import kotlinx.coroutines.launch
import java.io.File
import androidx.core.content.FileProvider
import com.example.fleetapp.R


class PDFViewModel : ViewModel() {

    var pdfList by mutableStateOf<List<File>>(emptyList())
        private set

    fun loadPdfs(context: Context) {
        viewModelScope.launch {
            try {
                val directory = PdfFunctions.getPublicDirectory(context)
                pdfList = directory.listFiles()?.filter { it.extension == "pdf" } ?: emptyList()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deletePdf(context: Context, file: File) {
        viewModelScope.launch {
            try {
                if (file.delete()) {
                    loadPdfs(context)
                } else {
                    Toast.makeText(context, context.getString(R.string.failed_to_delete_pdf), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.failed_to_delete_pdf), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sharePdf(context: Context, file: File) {
        try {
            val authority = context.getString(R.string.authority)
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.failed_to_share_pdf), Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdf(context: Context, file: File) {
        try {
            val authority = context.getString(R.string.authority)
            val uri = FileProvider.getUriForFile(context, authority, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, context.getString(R.string.failed_to_open_pdf), Toast.LENGTH_SHORT).show()
        }
    }
}
