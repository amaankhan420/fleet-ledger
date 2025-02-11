package com.example.fleetapp.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fleetapp.R
import com.example.fleetapp.components.TopBar
import com.example.fleetapp.viewmodels.PDFViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun PDFScreen(
    onBackStack: () -> Unit,
    pdfViewModel: PDFViewModel,
    context: Context
) {

    LaunchedEffect(Unit) {
        pdfViewModel.loadPdfs(context)
    }

    val pdfList = pdfViewModel.pdfList

    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())


    val groupedPdfs = pdfList.groupBy {
        dateFormatter.format(Date(it.lastModified()))
    }.toSortedMap(reverseOrder())

    Scaffold(
        topBar = {
            TopBar(
                title = context.getString(R.string.saved_reports),
                showBackButton = true,
                onBackStack = onBackStack
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding(),
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
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                groupedPdfs.forEach { (date, files) ->
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    itemsIndexed(files) { _, pdfFile ->
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
            val details = pdfFile.name.split("--")
            Text(
                text = details[0],
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "From ${details[1].replace("-", "/")} to ${details[2].replace("-", "/")}",
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
