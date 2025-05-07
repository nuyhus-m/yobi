package com.example.myapplication.ui.care.carelist.adapter

import android.text.Layout
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCareUserBinding
import com.example.myapplication.ui.care.carelist.data.CareUser

class CareListAdapter : RecyclerView.Adapter<CareListAdapter.CareViewHolder>() {

    private val items = mutableListOf<CareUser>()

    fun submitList(data: List<CareUser>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    class CareViewHolder(private val binding: ItemCareUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CareUser) {
            binding.imageView.setImageResource(item.image)
            binding.tvName.text = item.name
            binding.tvGender.text = item.gender
            binding.tvBirth.text = item.birth
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CareViewHolder {
        val binding = ItemCareUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CareViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }
}