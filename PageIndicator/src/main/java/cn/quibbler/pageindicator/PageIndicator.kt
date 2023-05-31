package cn.quibbler.pageindicator

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Interpolator
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

class PageIndicator : View, TargetScrollListener {

    companion object {
        inline val Int.dp: Int
            get() = (this * Resources.getSystem().displayMetrics.density).toInt()

        inline val Float.dp: Int
            get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    }

    private lateinit var dotSizes: IntArray
    private lateinit var dotAnimators: Array<ValueAnimator>

    private val defaultPaint = Paint().apply { isAntiAlias = true }
    private val selectedPaint = Paint().apply { isAntiAlias = true }

    private val dotSize: Int
    private val dotSizeMap: MutableMap<Byte, Int>
    private val dotBound: Int
    private val dotSpacing: Int
    private val animDuration: Long
    private val animInterpolator: Interpolator
    private var centered: Boolean = true
    private val customInitalPadding: Int

    private var dotManager: DotManager? = null
    private var scrollAmount: Int = 0
    private var scrollAnimator: ValueAnimator? = null
    private var initialPadding: Int = 0

    private lateinit var scrollListener: RecyclerView.OnScrollListener
    private lateinit var pageChangeListener: ViewPager.OnPageChangeListener

    var count:Int =0
        set(value) {
            dotManager = DotManager()
        }

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    init {

    }

    override fun scrollToTarget(target: Int) {

    }

    fun swipePrevious() {

    }

    fun swipeNext() {

    }

}