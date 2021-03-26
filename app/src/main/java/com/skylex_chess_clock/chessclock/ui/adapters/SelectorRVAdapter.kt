package com.skylex_chess_clock.chessclock.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skylex_chess_clock.chessclock.R
import com.skylex_chess_clock.chessclock.databinding.SelectorRvAdapterListItemBinding

class SelectorRVAdapter<T>(private val eventHandler: EventHandler<T>? = null): RecyclerView.Adapter<SelectorRVAdapter<T>.ViewHolder>() {

    private val listItems: MutableList<T> = mutableListOf()
    private var selectedItem: T? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.selector_rv_adapter_list_item,
                parent,
                false
        )
        return ViewHolder(itemView, eventHandler)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listItems[position], selectedItem)
    }

    fun setItems(items: List<T>) {
        listItems.clear()
        listItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setSelectedItem(item: T?) {
        selectedItem = item
    }


    inner class ViewHolder(itemView: View, private val eventHandler: EventHandler<T>?) : RecyclerView.ViewHolder(itemView) {
        private val binding: SelectorRvAdapterListItemBinding = SelectorRvAdapterListItemBinding.bind(itemView)
        private var item: T? = null

        init {
            setupViewListeners()
        }

        private fun setupViewListeners() {
            binding.rootView.setOnClickListener {
                if (this.item != null) {
                    eventHandler?.onItemClicked(item!!)
                }
            }
        }

        fun bind(item: T, selectedItem: T?) {
            this.item = item
            binding.apply {
                text.text = item.toString()
                if (item!!.equals(selectedItem)) {
                    tick.visibility = View.VISIBLE
                } else {
                    tick.visibility = View.GONE
                }

                if (adapterPosition == itemCount - 1) {
                    divider.visibility = View.INVISIBLE
                } else {
                    divider.visibility = View.VISIBLE
                }
            }
        }
    }

    interface EventHandler<T> {
        fun onItemClicked(item: T)
    }
}