package com.base.common.base.recyclerview

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SpaceItemDecoration(private val spacePx: Float, val beforeFirst: Boolean = false, val afterLast: Boolean = false) : RecyclerView.ItemDecoration() {

    @SuppressLint("SwitchIntDef")
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val spacePx = this.spacePx.toInt()
        val adapterPos = parent.getChildAdapterPosition(view)
        val adapterSize = parent.adapter?.itemCount ?: 0

        when (val layoutManager = parent.layoutManager) {
            is GridLayoutManager -> {
                val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams

                val isAxisEndSpan = (layoutParams.spanIndex + 1) % layoutManager.spanCount == 0
                val isCrossAxisEndSpan = adapterPos > (adapterSize - layoutManager.spanCount)
                val dividedSpace = spacePx / 2

                when (layoutManager.orientation) {
                    GridLayoutManager.VERTICAL -> {
                        outRect.left = if (layoutParams.spanIndex == 0) 0 else dividedSpace
                        outRect.right = if (isAxisEndSpan) 0 else dividedSpace
                        outRect.top = if (adapterPos < layoutManager.spanCount && !beforeFirst) 0 else dividedSpace
                        outRect.bottom = if (isCrossAxisEndSpan && !afterLast) 0 else dividedSpace
                    }
                    GridLayoutManager.HORIZONTAL -> {
                        outRect.top = if (layoutParams.spanIndex == 0) 0 else dividedSpace
                        outRect.bottom = if (isAxisEndSpan) 0 else dividedSpace
                        outRect.left = if (adapterPos < layoutManager.spanCount && !beforeFirst) 0 else dividedSpace
                        outRect.right = if (isCrossAxisEndSpan && !afterLast) 0 else dividedSpace
                    }
                }
            }
            is LinearLayoutManager -> {
                val isFirst = adapterPos == 0
                val isLast = adapterPos == adapterSize - 1
                val isVertical = layoutManager.orientation == LinearLayoutManager.VERTICAL

                outRect.top = if (isFirst && beforeFirst && isVertical) spacePx else outRect.top
                outRect.right = if ((!isLast || afterLast) && !isVertical) spacePx else outRect.right
                outRect.bottom = if ((!isLast || afterLast) && isVertical) spacePx else outRect.bottom
                outRect.left = if (isFirst && beforeFirst && !isVertical) spacePx else outRect.left
            }
        }
    }
}