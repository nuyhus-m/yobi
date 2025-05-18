package com.example.myapplication.ui.care.seven.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener

class DailyMetricAdapter(
    private val onRequestMoreData: (() -> Unit)? = null
) : RecyclerView.Adapter<DailyMetricAdapter.MetricViewHolder>() {
    private var items = mutableListOf<DailyMetric>()

    // chart랑 viewPager 충돌 방지 변수
    private var chartIsDragging = false

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

        // 중복 데이터 제거 (동일한 날짜는 한 번만 포함)
        val deduplicatedData = removeDuplicateEntries(item)

        // 최신 데이터가 먼저 오도록 순서 유지 (reverse 제거)
        val values = deduplicatedData.first
        val dates = deduplicatedData.second

        // chart 초기화 post -> 안하면 에러 뜸 nullException
        binding.lineChart.post {
            binding.lineChart.clear()
            val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
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

                // 차트 스크롤 설정
                setTouchEnabled(true)
                isDragEnabled = true
                // X축 확대/축소 O
                isScaleXEnabled = true
                // Y축 확대/축소 X
                isScaleYEnabled = false
                // X축 드래그만 활성화
                setPinchZoom(false)

                // 차트 제스처 리스너 설정
                onChartGestureListener = object : OnChartGestureListener {
                    override fun onChartGestureStart(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartTouchListener.ChartGesture?
                    ) {
                        if (me?.action == MotionEvent.ACTION_DOWN) {
                            parent?.requestDisallowInterceptTouchEvent(true)
                            chartIsDragging = true
                        }
                    }

                    override fun onChartGestureEnd(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartTouchListener.ChartGesture?
                    ) {
                        parent?.requestDisallowInterceptTouchEvent(false)
                        chartIsDragging = false
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
                        parent?.requestDisallowInterceptTouchEvent(false)
                    }

                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {}
                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                        // 오른쪽 끝에 도달했을 때 추가 데이터 요청 (최신에서 과거로 스크롤)
                        val atLeft = lowestVisibleX <= 0f && dX > 0
                        if (atLeft) onRequestMoreData?.invoke()
                    }
                }

                description.isEnabled = false
                legend.isEnabled = false
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false

                // X축 설정
                xAxis.apply {
                    this.position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    valueFormatter = IndexAxisValueFormatter(dates)
                    textSize = 9f
                    labelRotationAngle = 45f
                    setAvoidFirstLastClipping(true)

                    // 표시할 라벨 개수 결정
                    val dataSize = dates.size
                    val visibleEntryCount = when {
                        dataSize <= 3 -> dataSize  // 3개 이하면 전부 표시
                        dataSize <= 7 -> dataSize  // 7개 이하면 전부 표시
                        else -> 7  // 그 이상이면 7개만 표시
                    }
                    setLabelCount(visibleEntryCount, false)

                    // 첫/마지막 라벨 여백 최소화
                    spaceMin = 0.01f
                    spaceMax = 0.01f
                }

                // Y축 비활성화
                axisLeft.isEnabled = false
                axisRight.isEnabled = false

                // 표시할 데이터 포인트 수 결정
                val dataSize = dates.size
                val visibleDataPoints = when {
                    dataSize <= 3 -> dataSize.toFloat()  // 3개 이하면 전부 표시
                    dataSize <= 7 -> dataSize.toFloat()  // 7개 이하면 전부 표시
                    else -> 7f  // 그 이상이면 7개만 표시
                }

                // 최소/최대 표시 범위 설정
                setVisibleXRangeMinimum(min(3f, dates.size.toFloat()))
                setVisibleXRangeMaximum(min(7f, dates.size.toFloat()))

                // 최신 데이터 보여주기 (왼쪽에 최신 데이터가 있음)
                moveViewToX(dates.size.toFloat())

                // 스크롤 제한 설정 (차트 끝에서 스크롤 멈춤)
                extraBottomOffset = 15f // X축 라벨을 위한 추가 여백

                // 애니메이션 (0.5초)
                animateY(500)

                // 차트 업데이트
                invalidate()
            }

            // 터치 이벤트 처리
            binding.lineChart.setOnTouchListener { v, event ->
                val parent = v.parent
                when (event.action) {
                    // 누르고 있을때
                    MotionEvent.ACTION_DOWN -> {
                        parent?.requestDisallowInterceptTouchEvent(true)
                        chartIsDragging = true
                    }

                    // 움직일때
                    MotionEvent.ACTION_MOVE -> {
                        if (event.historySize > 0) {
                            val deltaX = event.x - event.getHistoricalX(0)

                            // 현재 차트의 위치 확인 -> 데이터의 처음과 끝에 도달 확인
                            val chart = binding.lineChart
                            val lowestVisibleX = chart.lowestVisibleX
                            val highestVisibleX = chart.highestVisibleX
                            val totalCount = dates.size - 1

                            // 맨 왼쪽 또는 맨 오른쪽에 도달한 경우, 부모 스크롤(recyclerView) 허용
                            val atLeftEdge = lowestVisibleX <= 0f && deltaX > 0
                            val atRightEdge = highestVisibleX >= totalCount && deltaX < 0

                            if (atLeftEdge || atRightEdge) {
                                parent?.requestDisallowInterceptTouchEvent(false)
                            } else if (Math.abs(deltaX) > 5) {
                                parent?.requestDisallowInterceptTouchEvent(true)
                            }
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        parent?.requestDisallowInterceptTouchEvent(false)
                        chartIsDragging = false
                    }
                }

                return@setOnTouchListener false
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // 중복 날짜 데이터 제거 함수
    private fun removeDuplicateEntries(item: DailyMetric): Pair<List<Float>, List<String>> {
        val dateValuePairs = item.dates.zip(item.values)

        // 최신 날짜가 앞으로 오도록 정렬
        val sortedPairs = dateValuePairs.sortedBy { it.first }

        // 결과 저장할 리스트
        val uniqueValues = mutableListOf<Float>()
        val uniqueDates = mutableListOf<String>()

        // 이미 처리한 날짜 추적
        val processedDates = mutableSetOf<String>()

        // 각 날짜를 한 번씩만 추가 (이미 최신순으로 정렬됨)
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
                uniqueDates.add(uniqueDates[0]) // 동일한 날짜 복제
                uniqueValues.add(uniqueValues[0]) // 동일한 값 복제
            }
        }

        return Pair(uniqueValues, uniqueDates)
    }

    // min 구현
    private fun min(a: Float, b: Float): Float = if (a < b) a else b
    private fun min(a: Int, b: Int): Int = if (a < b) a else b
}