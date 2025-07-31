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
        //Log.d("ABOBA", R.id::class.java.getDeclaredField("button_q").get(null)!!.toString() + "\n" + R.id.button_q)

        setupNotification(this, 0, listOf("space", "send", "clear"), R.layout.notification4)
        // setWhen() DOES NOT WORK
        Thread.sleep(20)

        setupNotification(this, 20, listOf("digit", *("zxcvbnm".map { it.toString() }.toTypedArray()), "bs"), R.layout.notification3)
        Thread.sleep(20)

        setupNotification(this, 40, "asdfghjkl".map { it.toString() }, R.layout.notification2)
        Thread.sleep(20)

        val prefs = getSharedPreferences("CurrentNote", Context.MODE_PRIVATE)
        val curMode = prefs.getString("mode", "text")!!
        if(curMode == "text") {
            setupNotification(this, 80, "qwertyuiop".map { it.toString() }, R.layout.notification)
        } else {
            setupNotification(this, 80, "1234567890".map { it.toString() }, R.layout.notification_digit)
        }
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

fun setupNotification(ctx: Context, offset: Int, buttons: List<String>, layout: Int) {
    val remoteView = RemoteViews(ctx.packageName, layout)
    for(i in 0 until buttons.size) {
        val name = buttons[i]

        val intent = Intent(ctx, MyBroadcastReceiver::class.java).apply { this.putExtra("button", name) }

        remoteView.setOnClickPendingIntent(
            R.id::class.java.getDeclaredField("button_" + name).get(null) as Int,
            PendingIntent.getBroadcast(ctx, offset + i, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )
    }

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
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("CurrentNote", Context.MODE_PRIVATE)
        val saveDir = prefs.getString("saveDir", "")!!
        var text = prefs.getString("text", "")!!
        var curMode = prefs.getString("mode", "text")!!

        var button = intent.getStringExtra("button")!!
        if(button.length == 1 && button[0] >= 'a' && button[0] <= 'z') {
            text += button
        }
        else if(button.length == 1 && button[0] >= '0' && button[0] <= '9') {
            text += button
        }
        else if(button == "digit") {
            curMode = if(curMode == "text") "digit" else "text"
        }
        else if(button == "bs") {
            text = text.substring(0, text.length - 1)
        }
        else if(button == "space") {
            text += " "
        }
        else if(button == "send") {
            saveFile(text, context, saveDir)
            text = ""
            curMode = "text"
        }
        else if(button == "clear") {
            text = ""
        }

        if(curMode != prefs.getString("mode", "text")) {
            if(curMode == "text") {
                setupNotification(context, 80, "qwertyuiop".map { it.toString() }, R.layout.notification)
            } else {
                setupNotification(context, 80, "1234567890".map { it.toString() }, R.layout.notification_digit)
            }
            Thread.sleep(20)
        }

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
            putString("mode", curMode)
        }
    }
}

@SuppressLint("NewApi")
fun saveFile(text: String, ctx: Context, saveDir: String) {
    try {
        val formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault())
        val filename = formatter.format(Instant.ofEpochMilli(System.currentTimeMillis())) + ".txt"

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