package emy.partners.lawapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import emy.partners.lawapp.data.local.AndroidAppContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installCrashLogger()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidAppContext.init(applicationContext)

        setContent {
            CrashBoundary {
                App()
            }
        }
    }

    private fun installCrashLogger() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(CRASH_TAG, "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

@Composable
private fun CrashBoundary(content: @Composable () -> Unit) {
    content()
}

@Composable
private fun CrashScreen(throwable: Throwable) {
    val scrollState = rememberScrollState()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111827))
                .verticalScroll(scrollState)
                .padding(20.dp)
        ) {
            Text(
                text = "Exception detectee",
                color = Color(0xFFFCA5A5),
                fontSize = 24.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = throwable.javaClass.name,
                color = Color.White,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = throwable.message ?: "Aucun message",
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 14.sp
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = Log.getStackTraceString(throwable),
                color = Color.White.copy(alpha = 0.72f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private const val CRASH_TAG = "LawAppCrash"