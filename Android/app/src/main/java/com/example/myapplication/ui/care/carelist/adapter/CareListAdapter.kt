package com.example.myapplication.ui.care.carelist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCareUserBinding
import com.example.myapplication.ui.care.carelist.data.CareUser

class CareListAdapter(
    private val onClick: (CareUser) -> Unit
) : RecyclerView.Adapter<CareListAdapter.CareViewHolder>() {

    private val items = mutableListOf<CareUser>()

    fun submitList(data: List<CareUser>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    class CareViewHolder(val binding: ItemCareUserBinding) :
        RecyclerView.ViewHolder(binding.root)

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
        val item = items[position]
        val binding = holder.binding
        binding.ivIcon.setImageResource(item.image)
        binding.tvUsername.text = item.name
        binding.tvGender.text = item.gender
        binding.tvBirth.text = item.birth


        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}