package com.example.lockscreennotes

import android.annotation.SuppressLint
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
import androidx.core.content.edit
import com.example.lockscreennotes.ui.theme.LockScreenNotesTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val CHANNEL_ID = "channel_id"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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

        createNotificationChannel()
        showNotification()
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 30332 && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val prefs = this.getSharedPreferences("CurrentNote", Context.MODE_PRIVATE)
                prefs.edit() { putString("saveDir", uri.toString()) }
                Log.d("ABOBUS", uri.toString())
            }
        }
    }


    @SuppressLint("ResourceType")
    private fun showNotification() {
        setupNotification(this, 3, R.layout.notification4)
        // setWhen() DOES NOT WORK
        Thread.sleep(20)

        setupNotification(this, 2, R.layout.notification3)
        Thread.sleep(20)

        setupNotification(this, 1, R.layout.notification2)
        Thread.sleep(20)

        setupNotification(this, 0, R.layout.notification)
        Thread.sleep(20)

        run {
            val prefs = this.getSharedPreferences("CurrentNote", Context.MODE_PRIVATE)
            var text = prefs.getString("text", "")!!
            if(text.isEmpty()) {
                text = "<type your text below>"
            }

            val notification = NotificationCompat.Builder(this, "channel_id_2")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle("")
                .setContentText(text)
                .setGroup("com.example.lockscreennotes.text")
                .setAutoCancel(true)
                .build()

            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(123123, notification)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Channel for default notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Text Channel"
            val descriptionText = "Channel to separate text from keyboard"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("channel_id_2", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

fun setupNotification(ctx: Context, offset: Int, layout: Int) {
    val i1 = Intent(ctx, MyBroadcastReceiver::class.java).apply { this.putExtra("button", (offset * 3 + 0)) }
    val i2 = Intent(ctx, MyBroadcastReceiver::class.java).apply { this.putExtra("button", (offset * 3 + 1)) }
    val i3 = Intent(ctx, MyBroadcastReceiver::class.java).apply { this.putExtra("button", (offset * 3 + 2)) }

    val remoteView = RemoteViews(ctx.packageName, layout)
    remoteView.setOnClickPendingIntent(R.id.button1, PendingIntent.getBroadcast(ctx, offset * 3 + 0, i1, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))
    remoteView.setOnClickPendingIntent(R.id.button2, PendingIntent.getBroadcast(ctx, offset * 3 + 1, i2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))
    remoteView.setOnClickPendingIntent(R.id.button3, PendingIntent.getBroadcast(ctx, offset * 3 + 2, i3, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))

    val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCustomContentView(remoteView)
        .setGroup("com.example.lockscreennotes.buttons_" + offset)
        .build()

    val notificationManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(offset, notification)
}

class MyBroadcastReceiver : BroadcastReceiver() {
    private val values = arrayOf(
        arrayOf(",", ".", "?", "!"),
        arrayOf("a", "b", "c"),
        arrayOf("d", "e", "f"),
        arrayOf("g", "h", "i"),
        arrayOf("j", "k", "l"),
        arrayOf("m", "n", "o"),
        arrayOf("p", "q", "r", "s"),
        arrayOf("t", "u", "v"),
        arrayOf("w", "x", "y", "z"),
    )
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("CurrentNote", Context.MODE_PRIVATE)

        val saveDir = prefs.getString("saveDir", "")!!

        var text = prefs.getString("text", "")!!
        var lastPress = prefs.getLong("lastPress", 0)
        var lastButton = prefs.getInt("lastButton", -2)
        var lastAdvance = prefs.getInt("lastAdvance", 0)

        val currentPress = System.currentTimeMillis()
        val duration = currentPress - lastPress

        var button = intent.getIntExtra("button", -1)
        if(button >= 0 && button < 9) {
            val chars = values[button]
            if(button == lastButton && duration < 1000) {
                lastAdvance = (lastAdvance + 1) % chars.size
                text = text.substring(0, text.length - 1) + chars[lastAdvance]
            }
            else {
                lastAdvance = 0
                text += chars[lastAdvance]
            }
        }
        else if(button == 9) {
            saveFile(text, context, saveDir, currentPress)
            lastAdvance = 0
            text = ""
        }
        else if(button == 10) {
            lastAdvance = 0
            text += " "
        }
        else if(button == 11) {
            lastAdvance = 0
            text = text.substring(0, text.length - 1)
        }

        lastButton = button
        lastPress = currentPress

        val notification = NotificationCompat.Builder(context, "channel_id_2")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentText(text)
            .setGroup("com.example.lockscreennotes.text")
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(123123, notification)


        prefs.edit() {
            putString("text", text)
            putLong("lastPress", lastPress)
            putInt("lastButton", lastButton)
            putInt("lastAdvance", lastAdvance)
        }
    }
}

@SuppressLint("NewApi")
fun saveFile(text: String, ctx: Context, saveDir: String, time: Long) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
        val filename = formatter.format(Instant.ofEpochMilli(time)) + ".txt"

        // Why do I need to convert string to url to string to url using the string and url?
        // Job security.
        // https://stackoverflow.com/questions/58148196/saf-invalid-uri-error-from-documentscontract-createdocument-method-fileoutput
        val uri = Uri.parse(saveDir)
        val docId = DocumentsContract.getTreeDocumentId(uri)
        val dirUri = DocumentsContract.buildDocumentUriUsingTree(uri, docId)

        val docUri = DocumentsContract.createDocument(
            ctx.contentResolver,
            dirUri,
            "text/plain",
            filename
        )
        docUri!!.let {
            ctx.contentResolver.openOutputStream(it)?.use { outputStream ->
                outputStream.write(text.toByteArray())
                outputStream.close()
            }
        }
    }
    catch (err: Throwable) {
        err.printStackTrace()
    }
}