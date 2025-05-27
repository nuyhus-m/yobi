package com.example.myapplication.ui.visitlog.visitloglist.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.dto.response.visitlog.DailyHumanDTO
import com.example.myapplication.databinding.ItemVisitLogBinding

// 아래 Log 리스트 위한 adapter
class VisitLogAdapter(
    private val onClick: (DailyHumanDTO) -> Unit
) : RecyclerView.Adapter<VisitLogAdapter.VisitLogViewHolder>() {

    private var items: List<DailyHumanDTO> = emptyList()

    fun submitList(logs: List<DailyHumanDTO>) {
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
        holder.binding.tvUsername.text = item.clientName

        val formattedDate = formatTimestampToDate(item.visitedDate)
        holder.binding.tvDate.text = formattedDate
        holder.itemView.setOnClickListener {
            onClick(item)
        }
    }

    // 타임스탬프를 "YYYY/MM/DD" 형식으로 변환
    fun formatTimestampToDate(timestamp: Long): String {
        val date = java.util.Date(timestamp)
        val format = java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
        return format.format(date)
    }


    override fun getItemCount(): Int {
        return items.size
    }
}