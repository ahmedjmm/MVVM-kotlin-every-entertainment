package com.example.everyentertainment.views.fragments.storageFragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.appcompat.view.ActionMode.Callback
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StableIdKeyProvider
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.everyentertainment.R
import com.example.everyentertainment.databinding.FragmentMemoryBinding
import com.example.everyentertainment.models.File
import com.example.everyentertainment.viewModels.MemoryViewModel
import com.example.everyentertainment.views.activities.MainActivity
import com.example.everyentertainment.views.adapters.MemoryRecyclerViewAdapter
import com.example.everyentertainment.views.viewsUtils.memoryListiewUtils.MemoryListItemDetailsLookUp
import com.google.android.material.progressindicator.CircularProgressIndicator

class MemoryFragment: Fragment(R.layout.fragment_memory), MemoryRecyclerViewAdapter.Interaction {
    companion object{
        lateinit var currentFolder: java.io.File
        lateinit var memoryViewModel: MemoryViewModel
        lateinit var sharedPreferences: SharedPreferences
    }

    //    track recyclerView selection
    private var selectionTracker: SelectionTracker<Long>? = null

    private lateinit var listView: RecyclerView
    private lateinit var toolbar: Toolbar
    private lateinit var pathsRecyclerView: RecyclerView
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var refreshSwipe: SwipeRefreshLayout
    private lateinit var memoryItemsRecyclerViewAdapter: MemoryRecyclerViewAdapter
    private lateinit var memoryFragmentBinding: FragmentMemoryBinding

    private var actionMode: android.view.ActionMode? = null
    private val actionModeCallback = object : Callback, android.view.ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false // Return false if nothing is done
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {

        }

        override fun onCreateActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {
            // Called when the action mode is created; startActionMode() was called
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = p0!!.menuInflater
            inflater.inflate(R.menu.memory_fragment_context_menu, p1)
            p1?.removeItem(R.id.rename)
            p1?.removeItem(R.id.create_folder)
            p1?.removeItem(R.id.share)
            p1?.removeItem(R.id.info)
            p1?.removeItem(R.id.hide)
//          change the visibility of checkBox and dropDownMenu depending on the state of the actionMode
            for(index in memoryItemsRecyclerViewAdapter.differ.currentList.indices){
                var viewHolder = listView.layoutManager?.findViewByPosition(index)
                viewHolder?.findViewById<CheckBox>(R.id.checkBox)?.visibility = View.VISIBLE
                viewHolder = listView.layoutManager?.findViewByPosition(index)
                viewHolder?.findViewById<ImageButton>(R.id.drop_down_menu)?.visibility = View.GONE
            }

            return true
        }

        override fun onPrepareActionMode(p0: android.view.ActionMode?, p1: Menu?): Boolean {

            return false
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(p0: android.view.ActionMode?, p1: MenuItem?): Boolean {
            return when (p1?.itemId) {
                R.id.move -> {
                    Toast.makeText(context,"go to hell", Toast.LENGTH_LONG).show()
                    selectionTracker?.clearSelection()
                    p0?.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

//      Called when the user exits the action mode
        override fun onDestroyActionMode(p0: android.view.ActionMode?) {
//          change the visibility of checkBox and dropDownMenu depending on the state of the actionMode
            for(index in memoryItemsRecyclerViewAdapter.differ.currentList.indices){
                var viewHolder = listView.layoutManager?.findViewByPosition(index)
                viewHolder?.findViewById<CheckBox>(R.id.checkBox)?.visibility = View.GONE
                viewHolder = listView.layoutManager?.findViewByPosition(index)
                viewHolder?.findViewById<ImageButton>(R.id.drop_down_menu)?.visibility = View.VISIBLE
            }
            selectionTracker?.clearSelection()
            actionMode = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        memoryViewModel = (activity as MainActivity).memoryViewModel
        memoryItemsRecyclerViewAdapter = MemoryRecyclerViewAdapter(this@MemoryFragment, memoryViewModel)
//        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
//        rootDirectory = Environment.getExternalStorageDirectory()
//        currentFolder = rootDirectory
//        val rootFoldersList = rootDirectory?.listFiles()?.toMutableList()
//        for (item in rootFoldersList!!)
//            if(item.isDirectory)
//                filesList?.add(File(R.drawable.ic_folders, item, item.totalSpace))
//            else
//                filesList?.add(File(R.drawable.ic_file, item, item.totalSpace))
//        memoryViewModel = MemoryViewModel(filesList!!)
//        MemoryViewModel.lazyMemoryViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        pathsRecyclerView = view.findViewById(R.id.)
//        searchView = memoryFragmentBinding.searchView
        refreshSwipe = view.findViewById(R.id.swipe_refresh)
        val spinner: CircularProgressIndicator = view.findViewById(R.id.spinner)
        listView = view.findViewById(R.id.list_view)
        toolbar = view.findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.app_name)

        refreshSwipe.setOnRefreshListener {
            refreshSwipe.isRefreshing = true
            memoryViewModel.run { newFolders(currentFolder, true) }
            refreshSwipe.isRefreshing = false
        }

        listView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = memoryItemsRecyclerViewAdapter
            listView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        memoryViewModel.mutableLiveData?.observe(viewLifecycleOwner) {
            memoryItemsRecyclerViewAdapter.submitList(it)
            currentFolder = MemoryViewModel.currentFolder!!
            spinner.visibility = View.GONE
        }

        selectionTracker = SelectionTracker.Builder(
            "selection",
            listView,
            StableIdKeyProvider(listView), //parameter of type recyclerView
            MemoryListItemDetailsLookUp(listView),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        selectionTracker?.addObserver(
            object: SelectionTracker.SelectionObserver<Long>(){
                override fun onSelectionChanged() {
                    startActionMode()
                    val selectionCount = selectionTracker?.selection?.size()
                    if(selectionCount == 0) {
                        actionMode?.finish()
                    }else
                        actionMode?.title = "$selectionCount"
                }
            }
        )
        memoryItemsRecyclerViewAdapter.setTracker(selectionTracker as SelectionTracker<Long>)
    }

    override fun onItemClicked(position: Int, file: File) {
        val clickedFile = memoryItemsRecyclerViewAdapter.differ.currentList[position].file
        memoryViewModel.newFolders(clickedFile, true)
    }

    //    start actionMode for recyclerView multi-selection
    private fun startActionMode() {
        if (actionMode == null) {
            actionMode = toolbar.startActionMode(actionModeCallback)
        }
    }
}