package com.example.myapplication.ui.schedule.schedule.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemScheduleBinding
import com.example.myapplication.ui.schedule.schedule.viewmodel.ScheduleItem

class ScheduleAdapter (
    private var scheduleList: List<ScheduleItem>
): RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>(){

    // 클라이언트 ID별 고정 색상
    private val clientColorMap = mapOf(
        1 to "#00B383",
        2 to "#735BF2",
        3 to "#0095FF",
        4 to "#EA9A86",
        5 to "#FF5E5E",
        6 to "#FF6AD5",
        7 to "#946FCF",
        8 to "#39FF14",
        9 to "#D2691E",
        10 to "#FFF200"
    )

    inner class ScheduleViewHolder(private val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScheduleItem) {
            // 시간 표시
            binding.tvTime.text = "${item.startAt.substring(0,5)}-${item.endAt.substring(0,5)}"

            // 이름 + 유형 표시
            binding.tvNameWithType.text = "${item.clientName}님 방문요양"

            // 클라이언트 ID에 따른 색상 도넛 적용
            val colorHex = clientColorMap[item.clientId] ?: "#000000"
            val colorInt = android.graphics.Color.parseColor(colorHex)
            binding.viewColorDot.setBackgroundResource(R.drawable.ic_schedule_dot)
            binding.viewColorDot.background.mutate().setTint(colorInt)

            // 버튼은 필요 시 리스너 연결 가능
            binding.btnCreateReport.setOnClickListener {
                // 예: 보고서 작성 화면 이동
            }
            binding.btnEditSchedule.setOnClickListener {
                // 예: 일정 수정 화면 이동
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bind(scheduleList[position])
    }

    override fun getItemCount(): Int = scheduleList.size

    fun submitList(newList: List<ScheduleItem>) {
        scheduleList = newList
        notifyDataSetChanged()
    }

}