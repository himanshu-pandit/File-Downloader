package com.bluepearl.storage.util

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi

class Downloader {

    private var downloadId: Long = -1  // Variable to store the ID of the download request
    private var receiver: BroadcastReceiver? = null

    // Method to initiate a PDF download to the public directory
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun downloadPdfPublic(
        context: Context,
        url: String,
        onSuccess: (Uri) -> Unit,
        onFailure: () -> Unit
    ) {

        Log.d("Downloader", "downloadPdf called with URL: $url")

        // Extract the file name from the URL (assumes it's the part after the last '/')
        val fileName = url.substring(url.lastIndexOf('/') + 1)

        // Define a custom folder within the public Downloads directory
        val eLabFolder = "eLabAssist"

        // Access the system's DownloadManager
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        // Create a request to download the file
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)  // Set the title for the notification
            .setDescription("Downloading PDF from URL")  // Description for the notification
            .setMimeType("application/pdf")  // MIME type of the file to be downloaded
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)  // Allow both mobile data and WiFi
            .setAllowedOverMetered(true)  // Allow downloads over metered networks
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)  // Show a notification when the download is complete
            //.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "$eLabFolder/$fileName")  // Specify the destination for the downloaded file
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "$eLabFolder/$fileName")  // Specify the destination for the internal app's downloaded file

        // Enqueue the download request and store the download ID
        downloadId = downloadManager.enqueue(request)

        Log.d("Downloader", "Download enqueued with ID: $downloadId")

        // Register the broadcast receiver
        receiver = downloadReceiver(onSuccess, onFailure)
        // Register a broadcast receiver to listen for download completion
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
    }

    // BroadcastReceiver to handle the download completion event
    private fun downloadReceiver(onSuccess: (Uri) -> Unit, onFailure: () -> Unit): BroadcastReceiver {
        return object : BroadcastReceiver() {

            @SuppressLint("Range")
            override fun onReceive(context: Context?, intent: Intent?) {

                Log.d("Downloader", "onReceive called")

                // Retrieve the ID of the completed download from the intent
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

                Log.d("DownloadReceiver", "onReceive called with download ID: $id")

                // Check if the download ID matches the one initiated by the app
                if (id == downloadId) {
                    Log.d("DownloadReceiver", "Download ID matches, proceeding with query")

                    // Query the DownloadManager for details about the download
                    val downloadManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val query = DownloadManager.Query().setFilterById(downloadId)
                    val cursor = downloadManager.query(query)

                    // If a matching download is found, check its status
                    if (cursor.moveToFirst()) {
                        val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        Log.d("DownloadReceiver", "Download status: $status")

                        // Handle the download status: success or failure
                        when (status) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                Log.d("DownloadReceiver2", "Download completed successfully")

                                // Get the URI of the downloaded file
                                val uri = downloadManager.getUriForDownloadedFile(downloadId)

                                // Call the success callback
                                onSuccess(uri)
                            }
                            DownloadManager.STATUS_FAILED -> {
                                Log.d("DownloadReceiver2", "Download failed")

                                onFailure()
                            }
                            else -> {
                                Log.d("DownloadReceiver2", "Unknown download status: $status")

                                onFailure()
                            }
                        }
                    } else {
                        Log.d("DownloadReceiver", "Cursor is empty, no matching download found")
                    }
                    cursor.close()  // Close the cursor to avoid memory leaks
                } else {
                    Log.d("DownloadReceiver", "Download ID does not match")
                }
            }
        }
    }
}
