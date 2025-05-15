package com.example.myapplication.ui.care.report.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.dto.response.care.ReportDto
import com.example.myapplication.databinding.ItemReportDateBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CareReportAdapter(
    private val onClick: (ReportDto) -> Unit
) : RecyclerView.Adapter<CareReportAdapter.ReportViewHolder>() {

    private val items = mutableListOf<ReportDto>()

    fun submitList(data: List<ReportDto>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    class ReportViewHolder(val binding: ItemReportDateBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportDateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.tvDateRange.text = convertMillisToRange(item.createdAt)

        val context = binding.root.context
        val drawable = ContextCompat.getDrawable(context, R.drawable.bg_purple_sub_radius_5)
        drawable?.setTint(
            ContextCompat.getColor(
                context,
                if (position == 0) R.color.purple else R.color.purple_sub
            )
        )
        binding.viewIndicator.background = drawable

        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    override fun getItemCount(): Int = items.size

    private fun convertMillisToRange(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy/MM/dd", Locale.KOREA)
        val start = Date(millis - 6 * 24 * 60 * 60 * 1000)
        val end = Date(millis)
        return "${formatter.format(start)} - ${formatter.format(end)}"
    }
}
