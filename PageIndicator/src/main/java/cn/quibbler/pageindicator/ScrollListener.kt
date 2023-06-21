package cn.quibbler.pageindicator

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.floor

class ScrollListener(private val indicator: PageIndicator) : RecyclerView.OnScrollListener() {

    private var midPos = 0

    private var scrollX = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        scrollX = +dx
        recyclerView.getChildAt(0)?.width?.let {
            val midPos = floor(((scrollX + it / 2f) / it).toDouble()).toInt()
            if (this.midPos != midPos) {
                when {
                    this.midPos < midPos -> indicator.swipeNext()
                    else -> indicator.swipePrevious()
                }
            }
            this.midPos = midPos
        }
    }

}