package com.example.lockscreennotes

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import com.example.lockscreennotes.ui.theme.LockScreenNotesTheme
import android.os.Build
import android.provider.DocumentsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ImportantActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/a/69314329
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        enableEdgeToEdge()

        window.statusBarColor = 0
        window.navigationBarColor = 0

        setContent {
            LockScreenNotesTheme {
                Scaffold { padding ->
                    Content(padding)
                }
            }
        }
    }
}

@Composable
fun Content(padding: PaddingValues) {
    val context = LocalContext.current

    var note by remember {
        val initialText = (context as? Activity)?.run {
            getSharedPreferences("CurrentNote", MODE_PRIVATE).getString("text", "")
        }

        mutableStateOf(TextFieldValue(initialText ?: ""))
    }

    // https://stackoverflow.com/a/77354483
    Column(modifier = Modifier.background(Color.Black).padding(padding).consumeWindowInsets(padding).imePadding()) {
        TextField(
            note,
            { it: TextFieldValue ->
                note = it
                (context as? Activity)?.run {
                    val prefs = this.getSharedPreferences("CurrentNote", MODE_PRIVATE)
                    prefs.edit() { putString("text", it.text) }
                }
            },
            Modifier.fillMaxWidth().weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.Black,
                focusedContainerColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = Color.Black,
            ),
        )
        Row {
            Button(
                onClick = { (context as? Activity)?.finish() },
                Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            ) {
                Text("Close")
            }
            Spacer(Modifier.width(Dp(10f)))
            Button(
                onClick = {
                    val prefs = context.getSharedPreferences("CurrentNote", MODE_PRIVATE)
                    if(saveFile(note.text, context, prefs.getString("saveDir", "")!!)) {
                        note = TextFieldValue()
                        (context as? Activity)?.run {
                            val prefs = this.getSharedPreferences("CurrentNote", MODE_PRIVATE)
                            prefs.edit() { putString("text", "") }
                        }
                    }
                },
                Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            ) {
                Text("Save")
            }
        }
    }
}

@SuppressLint("NewApi")
fun saveFile(text: String, ctx: Context, saveDir: String): Boolean {
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

        Toast.makeText(ctx, "Saved as ${filename}", Toast.LENGTH_SHORT).show()
        return true
    }
    catch (err: Throwable) {
        err.printStackTrace()
        Toast.makeText(ctx, "Error saving the note", Toast.LENGTH_SHORT).show()
        return false
    }
}