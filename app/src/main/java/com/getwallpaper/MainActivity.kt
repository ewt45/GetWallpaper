package com.getwallpaper

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import com.getwallpaper.ui.theme.GetWallpaperTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GWViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GetWallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.hasPermission.value = Build.VERSION.SDK_INT < Build.VERSION_CODES.R
                || Environment.isExternalStorageManager()

        if (!viewModel.hasPermission.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts("package", packageName, null)
            startActivity(intent)
        } else {
            viewModel.readWallPapers(this)
        }

    }
}

@Composable
fun MainScreen(
    viewModel: GWViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val perm by viewModel.hasPermission
    val wp by viewModel.wallpaper

    val saveFileLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/jpeg")) { uri ->
            uri?.let {
                wp.let { btm ->
                    val result = context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        btm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    Toast.makeText(
                        context,
                        (if (result == true) "success!" else "failure!"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    Column(modifier = modifier) {
        if (!perm) {
            Text("Please grant permission first")
        } else if (wp != viewModel.emptyBitmap) {
            Text("display wallpaper. click to save")
            Image(
                painter = BitmapPainter(wp.asImageBitmap()),
                contentDescription = "wallpaper",
                Modifier.clickable {
                    saveFileLauncher.launch("lockscreen_wallpaper.jpg")
                }
            )
        }
    }
}



