package com.example.myapplication.ui.care.seven.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
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
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DailyMetricAdapter(
    private val onRequestMoreData: (() -> Unit)? = null
) : ListAdapter<DailyMetric, DailyMetricAdapter.MetricViewHolder>(DiffCallback()) {

    // chart랑 viewPager 충돌 방지 변수
    private var chartIsDragging = false

    class MetricViewHolder(val binding: ItemDailyMetricBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // 각 ViewHolder마다 독립적인 코루틴 스코프 생성
        val scope = CoroutineScope(Dispatchers.Main)

        fun bind(item: DailyMetric, onRequestMoreData: (() -> Unit)?) {
            binding.tvTitle.text = item.title

            // 백그라운드에서 데이터 처리
            scope.launch(Dispatchers.Default) {
                // 중복 데이터 제거
                val deduplicatedData = removeDuplicateEntries(item)
                val values = deduplicatedData.first
                val dates = deduplicatedData.second

                // UI 작업은 메인 스레드로 전환
                withContext(Dispatchers.Main) {
                    setupChart(values, dates, item.title, onRequestMoreData)
                }
            }
        }

        // 차트 설정 함수
        private fun setupChart(values: List<Float>, dates: List<String>, title: String, onRequestMoreData: (() -> Unit)?) {
            val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            val context = binding.root.context
            val drawable = ContextCompat.getDrawable(context, R.drawable.chart_fade)

            val dataSet = LineDataSet(entries, title).apply {
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
                // 이전에 설정된 데이터가 있으면 재사용 판단
                if (data != null && data.dataSetCount > 0) {
                    val currentDataSet = data.getDataSetByIndex(0) as LineDataSet
                    currentDataSet.values = entries
                    data.notifyDataChanged()
                    notifyDataSetChanged()
                } else {
                    data = LineData(dataSet)
                }

                // 차트 스크롤 설정
                setTouchEnabled(true)
                isDragEnabled = true
                isScaleXEnabled = true
                isScaleYEnabled = false
                setPinchZoom(false)

                // 차트 제스처 리스너 설정 - 단순화
                onChartGestureListener = createChartGestureListener(onRequestMoreData)

                description.isEnabled = false
                legend.isEnabled = false
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false

                // X축 설정
                setupXAxis(dates)

                // Y축 비활성화
                axisLeft.isEnabled = false
                axisRight.isEnabled = false

                // 표시할 데이터 포인트 수 결정
                val visibleDataPoints = when {
                    dates.size <= 3 -> dates.size.toFloat()
                    dates.size <= 7 -> dates.size.toFloat()
                    else -> 7f
                }

                // 최소/최대 표시 범위 설정
                setVisibleXRangeMinimum(min(3f, dates.size.toFloat()))
                setVisibleXRangeMaximum(min(7f, dates.size.toFloat()))

                // 최신 데이터 보여주기
                moveViewToX(dates.size.toFloat())

                extraBottomOffset = 15f

                // 애니메이션 제거 또는 짧게 설정
                animateY(250)

                // 차트 업데이트
                invalidate()
            }

            // 터치 이벤트 단순화
            setupTouchListener()
        }

        private fun setupXAxis(dates: List<String>) {
            binding.lineChart.xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                setDrawAxisLine(false)
                valueFormatter = IndexAxisValueFormatter(dates)
                textSize = 9f
                labelRotationAngle = 45f
                setAvoidFirstLastClipping(true)

                val dataSize = dates.size
                val visibleEntryCount = when {
                    dataSize <= 7 -> dataSize
                    else -> 7
                }
                setLabelCount(visibleEntryCount, false)

                spaceMin = 0.01f
                spaceMax = 0.01f
            }
        }

        private fun createChartGestureListener(onRequestMoreData: (() -> Unit)?): OnChartGestureListener {
            return object : OnChartGestureListener {
                override fun onChartGestureStart(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                    if (me?.action == MotionEvent.ACTION_DOWN) {
                        binding.lineChart.parent?.requestDisallowInterceptTouchEvent(true)
                    }
                }

                override fun onChartGestureEnd(
                    me: MotionEvent?,
                    lastPerformedGesture: ChartTouchListener.ChartGesture?
                ) {
                    binding.lineChart.parent?.requestDisallowInterceptTouchEvent(false)
                }

                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(
                    me1: MotionEvent?,
                    me2: MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ) {
                    binding.lineChart.parent?.requestDisallowInterceptTouchEvent(false)
                }

                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    val atLeft = binding.lineChart.lowestVisibleX <= 0f && dX > 0
                    if (atLeft) onRequestMoreData?.invoke()
                }
            }
        }

        private fun setupTouchListener() {
            binding.lineChart.setOnTouchListener { v, event ->
                val parent = v.parent
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        parent?.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (event.historySize > 0) {
                            val deltaX = event.x - event.getHistoricalX(0)
                            val chart = binding.lineChart
                            val atLeftEdge = chart.lowestVisibleX <= 0f && deltaX > 0
                            val atRightEdge = chart.highestVisibleX >= (chart.data?.entryCount?.minus(1) ?: 0) && deltaX < 0

                            parent?.requestDisallowInterceptTouchEvent(!(atLeftEdge || atRightEdge))
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        parent?.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }
        }

        // ViewHolder가 재활용될 때 호출
        fun recycle() {
            // 차트 데이터 정리 - NPE 방지를 위한 안전한 정리
            binding.lineChart.setOnTouchListener(null)
            binding.lineChart.onChartGestureListener = null

            // 차트 데이터만 제거 (clear() 대신 데이터만 null로 설정)
            if (binding.lineChart.data != null) {
                binding.lineChart.data = null
                binding.lineChart.invalidate()
            }
        }

        companion object {
            // 중복 날짜 데이터 제거 함수
            private fun removeDuplicateEntries(item: DailyMetric): Pair<List<Float>, List<String>> {
                val dateValuePairs = item.dates.zip(item.values)
                val sortedPairs = dateValuePairs.sortedBy { it.first }

                val uniqueValues = mutableListOf<Float>()
                val uniqueDates = mutableListOf<String>()
                val processedDates = mutableSetOf<String>()

                sortedPairs.forEach { (date, value) ->
                    if (!processedDates.contains(date)) {
                        uniqueValues.add(value)
                        uniqueDates.add(date)
                        processedDates.add(date)
                    }
                }

                // 데이터가 너무 적으면 최소 표시 개수 보장
                if (uniqueDates.size == 1) {
                    // 데이터가 한 개만 있으면 최소 3개의 데이터 포인트 보장
                    while (uniqueDates.size < 3) {
                        uniqueDates.add(uniqueDates[0])
                        uniqueValues.add(uniqueValues[0])
                    }
                }

                return Pair(uniqueValues, uniqueDates)
            }

            // min 함수
            private fun min(a: Float, b: Float): Float = if (a < b) a else b
            private fun min(a: Int, b: Int): Int = if (a < b) a else b
        }
    }

    // ViewHolder 생성
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemDailyMetricBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetricViewHolder(binding)
    }

    // ViewHolder 바인딩
    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onRequestMoreData)
    }

    // ViewHolder 재활용 시 호출
    override fun onViewRecycled(holder: MetricViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    // 어댑터 객체 소멸 시 호출되어야 할 메서드
    fun release() {
        // 현재 활성화된 모든 코루틴 취소
        for (i in 0 until currentList.size) {
            val viewHolder = getViewHolderForAdapterPosition(i) as? MetricViewHolder
            viewHolder?.scope?.cancel()
        }
    }

    private fun getViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder? {
        val recyclerView = getCurrentRecyclerView() ?: return null
        return recyclerView.findViewHolderForAdapterPosition(position)
    }

    private fun getCurrentRecyclerView(): RecyclerView? {
        // 현재 어댑터가 붙은 RecyclerView 찾기
        // 이 코드는 어댑터가 RecyclerView에 붙은 상태일 때만 동작함
        return try {
            val field = RecyclerView.Adapter::class.java.getDeclaredField("mRecyclerView")
            field.isAccessible = true
            field.get(this) as? RecyclerView
        } catch (e: Exception) {
            null
        }
    }

    // DiffUtil을 사용한 효율적인 아이템 업데이트
    class DiffCallback : DiffUtil.ItemCallback<DailyMetric>() {
        override fun areItemsTheSame(oldItem: DailyMetric, newItem: DailyMetric): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: DailyMetric, newItem: DailyMetric): Boolean {
            return oldItem == newItem
        }
    }

    // 아이템 리스트 업데이트 메서드
    fun updateList(list: List<DailyMetric>) {
        submitList(list)
    }
}