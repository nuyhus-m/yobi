package com.example.myapplication.ui.care.report.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemReportDateBinding
import com.example.myapplication.ui.care.report.data.ReportDate

class CareReportAdapter(
    private val onClick: (ReportDate) -> Unit

) : RecyclerView.Adapter<CareReportAdapter.ReportViewHolder>() {
    private val items = mutableListOf<ReportDate>()

    fun submitList(data: List<ReportDate>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    class ReportViewHolder(val binding: ItemReportDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding =
            ItemReportDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ReportViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding
        binding.tvDateRange.text = item.rangeText

        val context = binding.root.context
        val color = if (position == 0) {
            R.color.purple
        } else {
            R.color.purple_sub
        }
        binding.viewIndicator.setBackgroundColor(
            ContextCompat.getColor(context, color)
        )


        holder.itemView.setOnClickListener {
            onClick(item)
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }
}