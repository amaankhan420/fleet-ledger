package com.example.fleetapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fleetapp.R
import com.example.fleetapp.components.TopBar
import com.example.fleetapp.viewmodels.PDFViewModel
import java.io.File


@Composable
fun PDFScreen(onBackStack: () -> Unit, pdfViewModel: PDFViewModel, context: Context) {

    LaunchedEffect(Unit) {
        pdfViewModel.loadPdfs(context)
    }

    val pdfList = pdfViewModel.pdfList

    Scaffold(
        topBar = {
            TopBar(
                title = context.getString(R.string.saved_reports),
                showBackButton = true,
                onBackStack = onBackStack
            )
        }
    ) { paddingValues ->
        if (pdfList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = context.getString(R.string.no_pdf_available),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                items(pdfList) { pdfFile ->
                    PDFListItem(
                        pdfFile = pdfFile,
                        onDelete = { pdfViewModel.deletePdf(context, pdfFile) },
                        onShare = { pdfViewModel.sharePdf(context, pdfFile) },
                        onClick = { pdfViewModel.openPdf(context, pdfFile) },
                        context = context
                    )
                }
            }
        }
    }
}

@Composable
fun PDFListItem(
    pdfFile: File,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSecondary,
            contentColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = pdfFile.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(5.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = onShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = context.getString(R.string.share)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = context.getString(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
