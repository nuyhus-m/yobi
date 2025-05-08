package com.example.myapplication.ui.visitlog.visitloglist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemFilterBinding
import com.example.myapplication.ui.visitlog.visitloglist.viewmodel.FilterItem

// 사람 고르기용 adpater
class FilterAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private var items: List<FilterItem> = emptyList()

    fun submitList(list: List<FilterItem>) {
        items = list
        notifyDataSetChanged()
    }

    inner class FilterViewHolder(val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilterItem) = with(binding) {
            btnFilter.text = item.name
            btnFilter.isSelected = item.isSelected


            val layoutParams = root.layoutParams as ViewGroup.MarginLayoutParams

            // 첫 번째만 marginStart
            val context = root.context
            layoutParams.marginStart = if (position == 0) {
                context.resources.getDimensionPixelSize(R.dimen.space_24dp)
            } else {
                0
            }
            root.layoutParams = layoutParams

            // 색 입히기
            val colorResId = if (item.isSelected) {
                R.color.white
            } else {
                R.color.purple
            }
            btnFilter.setTextColor(ContextCompat.getColor(btnFilter.context, colorResId))
            btnFilter.setOnClickListener { onClick(item.name) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}