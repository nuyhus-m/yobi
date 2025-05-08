package com.example.myapplication.ui.visitlog.visitloglist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemVisitLogBinding
import com.example.myapplication.ui.visitlog.visitloglist.viewmodel.VisitLog

// 아래 Log 리스트 위한 adapter
class VisitLogAdapter(
    private val onClick: (VisitLog) -> Unit
) : RecyclerView.Adapter<VisitLogAdapter.VisitLogViewHolder>() {

    private var items: List<VisitLog> = emptyList()

    fun submitList(logs: List<VisitLog>) {
        items = logs
        notifyDataSetChanged()
    }

    class VisitLogViewHolder(val binding: ItemVisitLogBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VisitLogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemVisitLogBinding.inflate(inflater, parent, false)
        return VisitLogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitLogViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvUsername.text = item.name
        holder.binding.tvDate.text = item.date
        holder.itemView.setOnClickListener{
            onClick(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}