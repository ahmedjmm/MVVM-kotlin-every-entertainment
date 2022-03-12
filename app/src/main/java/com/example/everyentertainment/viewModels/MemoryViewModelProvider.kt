package com.example.everyentertainment.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.everyentertainment.models.File

@Suppress("UNCHECKED_CAST")
class MemoryViewModelProvider: ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val mutableList = mutableListOf<File?>()
        return MemoryViewModel(mutableList) as T
    }
}