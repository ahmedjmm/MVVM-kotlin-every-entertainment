package com.example.everyentertainment.views.adapters

import android.graphics.Color
import android.graphics.Color.*
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.everyentertainment.R
import com.example.everyentertainment.databinding.MemoryListViewItemBinding
import com.example.everyentertainment.models.File
import com.example.everyentertainment.viewModels.MemoryViewModel


class MemoryRecyclerViewAdapter(private val interaction: Interaction? = null, private val memoryViewModel: MemoryViewModel):
    RecyclerView.Adapter<MemoryRecyclerViewAdapter.ViewHolder>() {
    private var selectionItemsTracker: SelectionTracker<Long>? = null
    lateinit var listViewBinding: MemoryListViewItemBinding
    lateinit var viewHolder: ViewHolder

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<File>() {
        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.file.name == newItem.file.name
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    init {
//        set ID for each list item
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryRecyclerViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        listViewBinding = MemoryListViewItemBinding.inflate(inflater, parent, false)
        viewHolder = ViewHolder(listViewBinding, interaction)
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(differ.currentList[position], position)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

//    get ID of the selected item, this function should be overridden when using setHasStableIds(true)
    override fun getItemId(position: Int): Long = position.toLong()

    fun submitList(list: MutableList<File?>) {
        differ.submitList(list)
    }

//    set memory fragment tracker to the adapter
    fun setTracker(selectionTracker: SelectionTracker<Long>) {
        this.selectionItemsTracker = selectionTracker
    }

    inner class ViewHolder
    constructor(
        _itemViewBinding: MemoryListViewItemBinding,
        private val interaction: Interaction?
    ) : RecyclerView.ViewHolder(_itemViewBinding.root), View.OnClickListener {
        private val itemBinding = _itemViewBinding
        fun bind(item: File, position: Int) = with(itemView.rootView) {
            itemView.setOnClickListener {
                interaction?.onItemClicked(adapterPosition, item)
            }

            val nameTextView = itemBinding.nameTextView
            nameTextView.text = item.file.name

            val dateTextView = itemBinding.dateTextView
            dateTextView.text = memoryViewModel.getFolderDateModified(item.file.lastModified())

            val sizeTextView = itemBinding.sizeTextView
            sizeTextView.text = item.size

            val innerFilesTextView = itemBinding.numberOfFilesTextView
            innerFilesTextView.text = memoryViewModel.getSubFoldersQuantity(context, item.file)

            val fileImageView = itemBinding.imageView
            if(item.file.isDirectory)
                fileImageView.setImageResource(R.drawable.ic_folders)
            else
                fileImageView.setImageResource(R.drawable.ic_file)

            val checkBox = itemBinding.checkBox
            val dropDownMenu = itemBinding.dropDownMenu
            if(selectionItemsTracker?.selection?.size() == 0){
                dropDownMenu.visibility = View.VISIBLE
                checkBox.visibility = View.GONE
            }else{
                dropDownMenu.visibility = View.GONE
                checkBox.visibility = View.VISIBLE
            }

            if(selectionItemsTracker!!.isSelected(position.toLong())) {
                itemView.background = ColorDrawable(GRAY)
                checkBox.isChecked = true
            }
            else {
                itemView.background = ColorDrawable(WHITE)
                checkBox.isChecked = false
            }
            itemBinding.root.isActivated = true
            return@with itemView
        }

        //get details object of selected item in recyclerView
        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object: ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int = adapterPosition
                override fun getSelectionKey(): Long = itemId
            }

        override fun onClick(p0: View?) {
            interaction?.onItemClicked(adapterPosition, differ.currentList[adapterPosition])
        }
    }

    interface Interaction {
        fun onItemClicked(position: Int, file: File)
    }
}
