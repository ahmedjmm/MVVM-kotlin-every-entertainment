package com.example.everyentertainment.models

import java.io.File

data class File(val imageView: Int, val file: File, var size: String){
    var isSelected: Boolean = true
}