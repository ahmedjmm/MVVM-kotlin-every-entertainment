package com.example.everyentertainment.viewModels

import android.content.Context
import android.os.Environment.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.everyentertainment.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log10
import kotlin.math.pow


class MemoryViewModel( var mutableList: MutableList<com.example.everyentertainment.models.File?> ): ViewModel() {
    private val foldersPath: MutableList<String?>? = null
    private val foldersName: MutableList<String?>? = null
    private var rootDirectory: File? = null
    var mutableLiveData: MutableLiveData<MutableList<com.example.everyentertainment.models.File?>>? = null

    companion object{
        var currentFolder: File? = null
        private var sort: String? = null
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
//            sort = MemoryFragment.sharedPreferences.getString("memory_sort", "a to z")
            if (mutableLiveData == null) mutableLiveData = MutableLiveData()
            initializeMemoryFragment()
            withContext(Dispatchers.Main){
                mutableLiveData!!.postValue(mutableList)
            }
        }
    }

    private fun initializeMemoryFragment(){
        rootDirectory = getExternalStorageDirectory()
        currentFolder = rootDirectory
        val filesList = rootDirectory?.listFiles()
        for(position in filesList!!.indices)
            if(filesList[position].isDirectory)
                mutableList.add(com.example.everyentertainment.models.File(R.drawable.ic_folders,
                    filesList[position], readableFileSize(fileSize(filesList[position]))))
            else
                mutableList.add(com.example.everyentertainment.models.File(R.drawable.ic_file,
                    filesList[position], readableFileSize(fileSize(filesList[position]))))
    }

    var fileSize: (File) -> Long = size@{
        if(!it.exists())
            return@size 0
        if(!it.isDirectory)
            return@size it.length()
        val dirs: MutableList<File> = LinkedList()
        dirs.add(it)
        var result: Long = 0
        while (dirs.isNotEmpty()) {
            val dir = dirs.removeAt(0)
            if (!dir.exists()) continue
            val listFiles = dir.listFiles()
            if (listFiles == null || listFiles.isEmpty()) continue
            for (child in listFiles) {
                result += child.length()
                if (child.isDirectory) dirs.add(child)
            }
        }
        return@size result
    }

    private fun readableFileSize(size: Long): String {
        if (size <= 0) return "0"
        val units = arrayOf("B", "kB", "MB", "GB", "TB")
        val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / 1024.0.pow(digitGroups.toDouble()))
            .toString() + " " + units[digitGroups]
    }

    private fun getFolderPath(pathList: List<String?>, position: Int): String? {
        val path = StringBuilder()
        for (x in 0..position) {
            path.append("/")
            path.append(pathList[x])
        }
        return path.toString()
    }

    fun deleteFileOrDirectory(foldersList: List<com.example.everyentertainment.models.File>) {
        for (x in foldersList.indices.reversed())
            foldersList[x].file.deleteRecursively()
    }

    fun moveFileOrDirectory(sourceFile: File, destinationFile: File) {
        sourceFile.copyRecursively(destinationFile, false, onError = { _, ioException -> throw ioException })
        sourceFile.deleteRecursively()
    }

    fun copyFileOrDirectory(sourceFile: File, destinationFile: File) {
        sourceFile.copyRecursively(destinationFile, false, onError = { _, ioException -> throw ioException })
    }

    fun renameFileOrFolder(file: File, string: String){
        file.renameTo(File(string))
    }

    private fun sort(sort: String, isReverse: Boolean, foldersList: MutableList<com.example.everyentertainment.models.File>) {
        when (sort) {
            "small to large" -> sortFromSmallToLarge(foldersList, isReverse)
            "old to new" -> sortFromOldToNew(foldersList, isReverse)
            "a to z" -> sortFromAToZ(foldersList, isReverse)
        }
    }

    private fun sortFromAToZ(foldersList: List<com.example.everyentertainment.models.File>, isReverse: Boolean) {
        if (!isReverse) {
//            Collections.sort(foldersList) { folders, f2 -> folders.file.name.lowercase()
//                .compareTo(f2.file.name.lowercase())
//            }
            foldersList.sortedWith { f1, f2 -> f1.file.name.lowercase().compareTo(f2.file.name.lowercase()) }
        }
        else {
            foldersList.sortedWith{ f1, f2 -> f1.file.name.lowercase().compareTo(f2.file.name.lowercase()) }
                .reversed()
//            Collections.sort(foldersList) { folders, f2 -> folders.file.name.lowercase()
//                    .compareTo(f2.file.name.lowercase())
//            }
//            Collections.reverse(foldersList)
//            foldersList.reversed()
        }
    }

    private fun sortFromOldToNew(foldersList: MutableList<com.example.everyentertainment.models.File>, isReverse: Boolean) {
        if (!isReverse)
            foldersList.sortWith { folders, f2 -> folders.file.lastModified().compareTo(f2.file.lastModified()) }
        else {
            foldersList.sortWith { folders, f2 ->
                folders.file.lastModified().compareTo(f2.file.lastModified())
            }
            foldersList.reverse()
        }
    }

    private fun sortFromSmallToLarge(foldersList: List<com.example.everyentertainment.models.File>, isReverse: Boolean) {
        if (!isReverse)
            foldersList.sortedWith { f1, f2 -> f1.size.compareTo(f2.size)}
//            Collections.sort(foldersList) { folders, f2 -> java.lang.Long.compare(folders.size, f2.size) }
        else {
            foldersList.sortedWith { f1, f2 -> f1.size.compareTo(f2.size)}.reversed()
//            Collections.sort(foldersList) { folders, f2 ->
//                folders.size.compareTo(f2.size)
//            }
//            Collections.reverse(foldersList)
        }
    }

     fun newFolders(file: File, isShowHidden: Boolean) = viewModelScope.launch  {
         val newList = mutableListOf<com.example.everyentertainment.models.File>()
         try {
             if (file.isDirectory) {
                 val newFolders = file.listFiles()
                 if (newFolders != null)
                     if (isShowHidden)
                         for (newFolder in newFolders) {
                             if (newFolder.isDirectory)
                                 newList.add(com.example.everyentertainment.models.File(R.drawable.ic_folders,
                                     newFolder, readableFileSize(fileSize(newFolder))))
                             else
                                 newList.add(com.example.everyentertainment.models.File(R.drawable.ic_file, newFolder,
                                     readableFileSize(fileSize(newFolder))))
                         }
                     else
                         for (newFolder in newFolders) {
                             if (!newFolder.name.startsWith(".")) if (newFolder.isDirectory) newList.add(
                                 com.example.everyentertainment.models.File(R.drawable.ic_folders, newFolder,
                                     readableFileSize(fileSize(newFolder))))
                             else
                                 newList.add(com.example.everyentertainment.models.File(R.drawable.ic_file, newFolder,
                                     readableFileSize(fileSize(newFolder))))
                         }
                 currentFolder = file
             }
         }
         finally {
             mutableLiveData?.postValue(newList.toMutableList())
//            sort(sort, isReverse, foldersList)
//            MemoryFragment.currentFolder = newFile
         }
    }

    fun backToFolder(
        position: Int, namesList: MutableList<String>, foldersList: MutableList<com.example.everyentertainment.models.File>,
        sort: String?, isReverse: Boolean, showHidden: Boolean) {
        if (position != namesList.size - 1) {
            val folder = File(getFolderPath(namesList, position)!!)
            foldersList.clear()
            val files = folder.listFiles()
            if (files != null)
                if (showHidden)
                    for (file in files)
                        if (file.isDirectory)
                            foldersList.add(com.example.everyentertainment.models.File(R.drawable.ic_folders, file,
                                readableFileSize(fileSize(file))))
                        else
                            foldersList.add(com.example.everyentertainment.models.File(R.drawable.ic_file, file,
                                readableFileSize(fileSize(file))))
                else
                    for (file in files)
                        if (!file.name.startsWith(".")) {
                            if (file.isDirectory)
                                foldersList.add(com.example.everyentertainment.models.File(R.drawable.ic_folders, file,
                                    readableFileSize(fileSize(file))))
                            else
                                foldersList.add(com.example.everyentertainment.models.File(R.drawable.ic_file, file,
                                    readableFileSize(fileSize(file))))
                        }
            currentFolder = folder
            namesList.subList(position + 1, namesList.size).clear()
            sort(sort!!, isReverse, foldersList)
        }
    }

    fun getFolderDateModified(file: Long): String? {
        val date = Date(file)
        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
        return simpleDateFormat.format(date)
    }

    suspend fun getCheckedFoldersNames(foldersList: List<com.example.everyentertainment.models.File?>?): MutableList<String?>? {
        for (folders in foldersList!!)
            if (folders!!.isSelected)
                foldersName!!.add(folders.file.name)
        return foldersName
    }

    suspend fun getCheckedFoldersPaths(foldersList: List<com.example.everyentertainment.models.File?>?): MutableList<String?>? {
        for (folders in foldersList!!)
            if (folders!!.isSelected) {
                foldersPath!!.add(folders.file.absolutePath)
            }
        return foldersPath
    }

    fun getSubFoldersQuantity(context: Context, file: File): String {
        if (file.isDirectory) {
            val subFoldersFiles = file.listFiles()
            if (subFoldersFiles != null) return subFoldersFiles.size.toString() + " " + context.resources
                .getString(R.string.folders_files_quantity)
        }
        return ""
    }
}