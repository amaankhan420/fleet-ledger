package com.example.fleetapp.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fleetapp.R
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    showBackButton: Boolean = true,
    onBackStack: () -> Unit = {},
    showPDF: Boolean = false,
    onPDFClick: () -> Unit = {}
) {
    val isBackPressed = remember { mutableStateOf(false) }
    val context = LocalContext.current

    CenterAlignedTopAppBar(
        modifier = Modifier.wrapContentHeight(align = Alignment.CenterVertically),
        title = {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = {
                        if (!isBackPressed.value) {
                            isBackPressed.value = true
                            onBackStack()
                        }
                    }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = context.getString(R.string.back_arrow),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },

        actions = {
            if (showPDF) {
                IconButton(
                    onClick = {
                        onPDFClick()
                    }) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = context.getString(R.string.pdf_screen_button),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },

        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
    )

    LaunchedEffect(isBackPressed.value) {
        if (isBackPressed.value) {
            delay(1000L)
            isBackPressed.value = false
        }
    }
}
