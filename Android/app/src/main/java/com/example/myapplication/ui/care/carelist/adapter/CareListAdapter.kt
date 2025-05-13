package com.example.myapplication.ui.care.carelist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.data.dto.response.care.ClientResponse
import com.example.myapplication.databinding.ItemCareUserBinding

class CareListAdapter(
    private val onClick: (ClientResponse) -> Unit
) : RecyclerView.Adapter<CareListAdapter.CareViewHolder>() {

    private val items = mutableListOf<ClientResponse>()

    fun submitList(data: List<ClientResponse>) {
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

        Glide.with(holder.itemView.context)
            .load(item.image)
            .transform(
                CenterCrop(),
                RoundedCorners(dpToPx(12, holder.itemView.context)))
            .into(binding.ivIcon)

        binding.tvUsername.text = item.name
        binding.tvGender.text = when (item.gender) {
            0 -> "남"
            1 -> "여"
            else -> "기타"
        }
        binding.tvBirth.text = item.birth


        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun dpToPx(dp: Int, context: android.content.Context): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}