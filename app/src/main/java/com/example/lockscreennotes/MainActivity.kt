package com.example.lockscreennotes

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.edit
import com.example.lockscreennotes.ui.theme.LockScreenNotesTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val CHANNEL_ID = "channel_id"

class MainActivity : ComponentActivity() {
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val activity = this

        setContent {
            LockScreenNotesTheme {
                Scaffold { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            startActivityForResult(intent, 30332)
                        }) {
                            Text("Set directory")
                        }
                    }
                }
            }
        }

        createNotificationChannel(this)

        val fullScreenIntent = Intent(activity, ImportantActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(activity, 0,
            fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // https://developer.android.com/develop/ui/views/notifications/build-notification#urgent-message
        val notification = NotificationCompat.Builder(activity, "channel_iel")
            .setSmallIcon(R.drawable.ic_android_black_24dp)
            .setContentTitle("Compose a Note")
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val noti = activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        noti.notify(4329, notification)
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 30332 && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val prefs = this.getSharedPreferences("CurrentNote", MODE_PRIVATE)
                prefs.edit() { putString("saveDir", uri.toString()) }
                Log.d("ABOBUS", uri.toString())
            }
        }
    }
}

private fun createNotificationChannel(ctx: Context) {
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "yell at me Channel"
        val descriptionText = "here peoples yel(low or high)"
        val channel = NotificationChannel("channel_iel", name, NotificationManager.IMPORTANCE_HIGH).apply {
            description = descriptionText
        }
        val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}