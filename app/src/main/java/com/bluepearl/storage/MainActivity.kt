package com.bluepearl.storage

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bluepearl.storage.ui.theme.StorageTheme
import com.bluepearl.storage.util.Downloader

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StorageTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {

    val context  = LocalContext.current
    var showLoader by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    val pdfUrl = "https://research.nhm.org/pdfs/10840/10840.pdf"

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){


        if(showLoader){
            CircularProgressIndicator(
                modifier = Modifier.width(64.dp),
                color = MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }


        Button(
            onClick = {
                showLoader = true
                val downloader = Downloader()
                val downloadId = downloader.downloadPdfPublic(
                    context,
                    pdfUrl,
                    onSuccess = { uri: Uri ->
                        showLoader = false
                        showAlert = true
                        fileUri = uri
                        message = "Your download has successfully completed."
                    },
                    onFailure = {
                        showLoader = false
                        showAlert = true
                        message = "Download failed."
                    }
                )
                Log.d("downloadId", downloadId.toString())
            },
            modifier = Modifier.padding(32.dp)
        ) {
            Text("Download Pdf")
        }

        if (showAlert){
            ShowAlert(
                onDismissRequest = {
                    showAlert = false
                },
                onConfirmation = {
                    showAlert = false

                     //Create an intent to view the downloaded PDF
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(fileUri, "application/pdf")
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION

                    // Check if there is an app that can handle the intent
                    try {
                        context.startActivity(intent)
                    }catch (e: Exception){
                        Log.d("Downloader", "Failed to open PDF: ${e.message}")
                        Toast.makeText(context, "Error opening PDF file.", Toast.LENGTH_SHORT).show()
                    }
                },
                "Report download",
                "Your download has successfully completed.",
                Icons.Default.Done
            )
        }

    }
}

@Composable
fun ShowAlert(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Download Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Okay")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onConfirmation()
                }
            ) {
                Text("Open")
            }
        }
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    StorageTheme {
        Greeting("Android")
    }
}