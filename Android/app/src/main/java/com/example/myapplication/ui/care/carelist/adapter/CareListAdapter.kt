package com.example.myapplication.ui.care.carelist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.myapplication.data.dto.response.care.ClientDetailResponse
import com.example.myapplication.databinding.ItemCareUserBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CareListAdapter(
    private val onClick: (ClientDetailResponse) -> Unit
) : RecyclerView.Adapter<CareListAdapter.CareViewHolder>() {
    private val items = mutableListOf<ClientDetailResponse>()

    fun submitList(data: List<ClientDetailResponse>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    /** ViewHolder 에 Job 을 달아두어 재활용 시 취소 */
    inner class CareViewHolder(val binding: ItemCareUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var shimmerJob: Job? = null

        fun bind(item: ClientDetailResponse) = with(binding) {
            // Cancel any existing job first
            shimmerJob?.cancel()

            // Initialize shimmer effect
            sflIcon.startShimmer()
            sflUsername.startShimmer()
            sflGender.startShimmer()
            sflBirth.startShimmer()

            // Make shimmer visible
            sflIcon.visibility = View.VISIBLE
            sflUsername.visibility = View.VISIBLE
            sflGender.visibility = View.VISIBLE
            sflBirth.visibility = View.VISIBLE

            // Hide actual content initially
            ivIcon.visibility = View.INVISIBLE
            tvUsername.visibility = View.INVISIBLE
            tvGender.visibility = View.INVISIBLE
            tvBirth.visibility = View.INVISIBLE

            // ───────────── 데이터 세팅 ─────────────
            tvUsername.text = item.name
            tvGender.text = when (item.gender) {
                0 -> "남"; 1 -> "여"; else -> "기타"
            }
            tvBirth.text = item.birth

            // Load image with Glide but keep it invisible initially
            Glide.with(itemView.context)
                .load(item.image)
                .transform(
                    CenterCrop(),
                    RoundedCorners(dpToPx(12, itemView.context))
                )
                .into(ivIcon)

            // ───────────── 0.5초 후 실뷰 표시 ─────────────
            // Get the lifecycleOwner safely
            val lifecycleOwner = itemView.findViewTreeLifecycleOwner()
            if (lifecycleOwner != null) {
                // Launch a coroutine that will delay showing real content
                shimmerJob = lifecycleOwner.lifecycleScope.launch {
                    delay(500) // Wait for 500ms (0.5 seconds)
                    showRealContent()
                }
            } else {
                // If no lifecycle owner, still delay the content reveal
                // This is a fallback that should rarely be needed
                shimmerJob?.cancel() // Just in case
                shimmerJob = null

                // Even without a lifecycle owner, we should still delay showing the content
                // We can use a postDelayed on the view instead
                itemView.postDelayed({
                    showRealContent()
                }, 500)
            }

            // Set click listener
            itemView.setOnClickListener { onClick(item) }
        }

        // Function to show real content and hide shimmer
        private fun showRealContent() = with(binding) {
            // Stop shimmer effects
            sflIcon.stopShimmer()
            sflUsername.stopShimmer()
            sflGender.stopShimmer()
            sflBirth.stopShimmer()

            // Hide shimmer containers
            sflIcon.visibility = View.GONE
            sflUsername.visibility = View.GONE
            sflGender.visibility = View.GONE
            sflBirth.visibility = View.GONE

            // Show real content
            ivIcon.visibility = View.VISIBLE
            tvUsername.visibility = View.VISIBLE
            tvGender.visibility = View.VISIBLE
            tvBirth.visibility = View.VISIBLE
        }

        /** 재활용 시 : Job 취소 & Shimmer 도 정지 */
        fun recycle() = with(binding) {
            shimmerJob?.cancel()
            shimmerJob = null
            sflIcon.stopShimmer()
            sflUsername.stopShimmer()
            sflGender.stopShimmer()
            sflBirth.stopShimmer()
        }
    }

    // ─────────────────────────────────────────────
    // Adapter overrides
    // ─────────────────────────────────────────────
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CareViewHolder {
        val binding = ItemCareUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CareViewHolder, position: Int) {
        if (position < items.size) {
            holder.bind(items[position])
        }
    }

    override fun onViewRecycled(holder: CareViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    override fun getItemCount(): Int = items.size

    private fun dpToPx(dp: Int, context: android.content.Context): Int =
        (dp * context.resources.displayMetrics.density).toInt()
}