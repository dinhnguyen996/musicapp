package com.techja.musicapp

import android.widget.ImageView
import java.io.Serializable

data class Song(val title: String,var name:String, val imageResId: Int, val rawResId: Int) : Serializable
