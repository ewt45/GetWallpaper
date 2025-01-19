package com.getwallpaper

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class GWViewModel:ViewModel() {

    val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
    val wallpaper:MutableState<Bitmap> = mutableStateOf(emptyBitmap)
    val hasPermission = mutableStateOf(false)


    fun readWallPapers(ctx: MainActivity) {
        var wp = WallpaperManager.getInstance(ctx)
            .getWallpaperFile(WallpaperManager.FLAG_LOCK)?.use {
                BitmapFactory.decodeFileDescriptor(it.fileDescriptor)
            }
        if (wp == null)
            wp = emptyBitmap
        wallpaper.value = wp
    }


}