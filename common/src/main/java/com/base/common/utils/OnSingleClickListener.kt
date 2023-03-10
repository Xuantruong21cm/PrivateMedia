package com.base.common.utils

import android.view.View
import java.util.concurrent.atomic.AtomicBoolean

class OnSingleClickListener(
    private val millisecond: Long = 500,
    private val clickListener: (View?) -> Unit,
) : View.OnClickListener {

    private var isCanClick = AtomicBoolean(true)

    override fun onClick(p0: View?) {
        if (isCanClick.getAndSet(false)) {
            p0?.run {
                postDelayed({ isCanClick.set(true) }, millisecond)
                clickListener(p0)
            }
        }
    }
}
