package cn.quibbler.pageindicator

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager

class PageIndicator constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
    View(context, attrs, defStyleAttr, defStyleRes), TargetScrollListener {

    companion object {
        private const val BYTE_1 = 1.toByte()
        private const val BYTE_2 = 2.toByte()
        private const val BYTE_3 = 3.toByte()
        private const val BYTE_4 = 4.toByte()
        private const val BYTE_5 = 5.toByte()
        private const val BYTE_6 = 6.toByte()

        private const val MOST_VISIBLE_COUNT = 10

        private const val DEFAULT_ANIM_DURATION = 200

        private val DEFAULT_INTERPOLATOR = DecelerateInterpolator()

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
    private val dotSizeMap: Map<Byte, Int>
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

    var count: Int = 0
        set(value) {
            dotManager = DotManager()
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PageIndicator)

        dotSizeMap = mapOf(
            BYTE_6 to a.getDimensionPixelSize(R.styleable.PageIndicator_piSize1, 6.dp),
            BYTE_5 to a.getDimensionPixelSize(R.styleable.PageIndicator_piSize2, 5f.dp),
            BYTE_4 to a.getDimensionPixelSize(R.styleable.PageIndicator_piSize3, 4.5f.dp),
            BYTE_3 to a.getDimensionPixelSize(R.styleable.PageIndicator_piSize4, 3f.dp),
            BYTE_2 to a.getDimensionPixelSize(R.styleable.PageIndicator_piSize5, 2.5f.dp),
            BYTE_1 to a.getDimensionPixelSize(R.styleable.PageIndicator_piSize6, .5f.dp)
        )

        dotSize = dotSizeMap.values.max() ?: 0
        dotSpacing = a.getDimensionPixelSize(R.styleable.PageIndicator_piSize3, 3.dp)

        centered = a.getBoolean(R.styleable.PageIndicator_piCentered, true)

        dotBound = a.getDimensionPixelSize(R.styleable.PageIndicator_piDotBound, 40.dp)

        customInitalPadding = a.getDimensionPixelSize(R.styleable.PageIndicator_piInitialPadding, -1)

        animDuration = a.getInteger(R.styleable.PageIndicator_piAnimDuration, DEFAULT_ANIM_DURATION).toLong()

        defaultPaint.color = a.getColor(
            R.styleable.PageIndicator_piDefaultColor,
            ContextCompat.getColor(getContext(), R.color.pi_default_color)
        )
        selectedPaint.color = a.getColor(
            R.styleable.PageIndicator_piSelectedColor,
            ContextCompat.getColor(getContext(), R.color.pi_selected_color)
        )
        animInterpolator =
            AnimationUtils.loadInterpolator(context, a.getResourceId(R.styleable.PageIndicator_piCentered, R.anim.pi_default_interpolator))
        a?.recycle()
    }

    override fun scrollToTarget(target: Int) {

    }

    fun swipePrevious() {

    }

    fun swipeNext() {

    }

}