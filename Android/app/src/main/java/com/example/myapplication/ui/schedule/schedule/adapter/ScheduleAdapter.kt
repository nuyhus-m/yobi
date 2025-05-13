package com.example.myapplication.ui.schedule.schedule.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.dto.model.ScheduleItemModel
import com.example.myapplication.databinding.ItemScheduleBinding
import com.example.myapplication.ui.schedule.schedule.viewmodel.ScheduleViewModel

class ScheduleAdapter(
    private var scheduleList: List<ScheduleItemModel>,
    private val viewModel: ScheduleViewModel,
    private val onEditClick: (Int) -> Unit,
    private val onLogCreateClick: (Int, String, Long) -> Unit,
    private val onLogViewClick: (Int) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduleItemModel) {
            binding.tvTime.text = item.timeRange
            binding.tvNameWithType.text = "${item.clientName}님 방문요양"

            val reportText = if (item.hasLogContent) "일지 보기" else "일지 생성"
            binding.btnCreateReport.text = reportText


            // 클라이언트 ID에 따른 색상 도넛 적용
            val colorHex = viewModel.clientColorMap[item.clientId] ?: "#000000"
            val colorInt = colorHex.toColorInt()
            binding.viewColorDot.setBackgroundResource(R.drawable.ic_schedule_dot)
            binding.viewColorDot.background.mutate().setTint(colorInt)

            binding.btnCreateReport.setOnClickListener {
                if (item.hasLogContent) {
                    onLogViewClick(item.scheduleId)
                } else {
                    onLogCreateClick(item.scheduleId, item.clientName, item.visitedDate)
                }
            }

            binding.btnEditSchedule.setOnClickListener {
                onEditClick(item.scheduleId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding =
            ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int = scheduleList.size

    fun submitList(newList: List<ScheduleItemModel>) {
        scheduleList = newList
        notifyDataSetChanged()
    }

}