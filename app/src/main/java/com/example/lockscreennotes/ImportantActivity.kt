package com.example.lockscreennotes

import android.content.Intent
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
import androidx.compose.ui.Modifier
import com.example.lockscreennotes.ui.theme.LockScreenNotesTheme
import android.os.Build

class ImportantActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // https://stackoverflow.com/a/69314329
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        enableEdgeToEdge()

        val activity = this
        setContent {
            LockScreenNotesTheme {
                Scaffold { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        Button(onClick = {
                            Toast.makeText(activity, "abobus", Toast.LENGTH_SHORT);
                        }) {
                            Text("bobibop")
                        }
                    }
                }
            }
        }
    }
}