package com.example.everyentertainment.views.viewsUtils.memoryListiewUtils

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView
import com.example.everyentertainment.views.adapters.MemoryRecyclerViewAdapter

//recyclerView item details object
class MemoryListItemDetailsLookUp (private val rv: RecyclerView) : ItemDetailsLookup<Long>() {
    override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
        val view = rv.findChildViewUnder(event.x, event.y)
        if(view != null) {
            return (rv.getChildViewHolder(view) as MemoryRecyclerViewAdapter.ViewHolder).getItemDetails()
        }
        return null
    }
}