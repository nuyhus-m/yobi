package com.example.myapplication.ui.care.seven.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemDailyMetricBinding
import com.example.myapplication.ui.care.seven.data.DailyMetric
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter

class DailyMetricAdapter : RecyclerView.Adapter<DailyMetricAdapter.MetricViewHolder>() {

    private var items = mutableListOf<DailyMetric>()

    fun submitList(list: List<DailyMetric>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class MetricViewHolder(val binding: ItemDailyMetricBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemDailyMetricBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetricViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        val item = items[position]
        val binding = holder.binding

        binding.tvTitle.text = item.title

        // chart 초기화 post
        binding.lineChart.post {
            val entries = item.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }

            val context = binding.root.context
            val drawable = ContextCompat.getDrawable(context, R.drawable.chart_fade)

            val dataSet = LineDataSet(entries, item.title).apply {
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawFilled(true)
                fillDrawable = drawable
                fillAlpha = 180

                color = ContextCompat.getColor(context, R.color.chart_purple)
                setDrawCircles(false)
                lineWidth = 2f

                setDrawValues(true)
                valueTextColor = Color.parseColor("#888888")
                valueTextSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getPointLabel(entry: Entry?): String {
                        return entry?.y?.let { String.format("%.1f", it) } ?: ""
                    }
                }
            }

            binding.lineChart.apply {
                data = LineData(dataSet)
                setTouchEnabled(false)
                description.isEnabled = false
                legend.isEnabled = false

                xAxis.apply {
                    this.position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    valueFormatter = IndexAxisValueFormatter(item.dates)
                    textSize = 10f
                }

                axisLeft.isEnabled = false
                axisRight.isEnabled = false

                animateY(1000)
                invalidate()
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
