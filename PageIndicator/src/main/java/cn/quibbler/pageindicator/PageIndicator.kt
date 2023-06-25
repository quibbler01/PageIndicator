package cn.quibbler.pageindicator

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlin.math.max
import kotlin.math.min

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
            dotManager = DotManager(value, dotSize, dotSpacing, dotBound, dotSizeMap, this)

            dotSizes = IntArray(value)
            dotManager?.let { it.dots.forEachIndexed { index, byte -> dotSizes[index] = it.dotSizeFor(byte) } }
            dotAnimators = Array(value) { ValueAnimator() }

            initialPadding = when {
                !centered -> 0
                customInitalPadding != -1 -> customInitalPadding
                else -> when (value) {
                    in 0..4 -> (dotBound + (4 - value) * (dotSize + dotSpacing) + dotSpacing) / 2
                    else -> 2 * (dotSize + dotSpacing)
                }
            }

            field = value
            invalidate()
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
            R.styleable.PageIndicator_piDefaultColor, ContextCompat.getColor(getContext(), R.color.pi_default_color)
        )
        selectedPaint.color = a.getColor(
            R.styleable.PageIndicator_piSelectedColor, ContextCompat.getColor(getContext(), R.color.pi_selected_color)
        )
        animInterpolator =
            AnimationUtils.loadInterpolator(context, a.getResourceId(R.styleable.PageIndicator_piCentered, R.anim.pi_default_interpolator))
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(4 * (dotSize + dotSpacing) + dotBound + initialPadding, dotSize)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var paddingStart = initialPadding
        val (start, end) = getDrawingRange()

        paddingStart += (dotSize + dotSpacing) * start
        (start until end).forEach {
            canvas?.drawCircle(
                paddingStart + dotSize / 2f - scrollAmount, dotSize / 2f, dotSizes[it] / 2f, when (dotManager?.dots?.get(it)) {
                    BYTE_6 -> selectedPaint
                    else -> defaultPaint
                }
            )
            paddingStart += dotSize + dotSpacing
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState() ?: return null
        val savedState = SavedState(superState)
        savedState.count = this.count
        savedState.selectedIndex = this.dotManager?.selectedIndex ?: 0
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }

        super.onRestoreInstanceState(state.superState)

        this.count = state.count
        for (i in 0 until state.selectedIndex) {
            swipeNext()
        }
    }

    override fun scrollToTarget(target: Int) {
        scrollAnimator?.cancel()
        scrollAnimator = ValueAnimator.ofInt(scrollAmount, target).apply {
            duration = animDuration
            interpolator = DEFAULT_INTERPOLATOR
            addUpdateListener { animation ->
                scrollAmount = animation.animatedValue as Int
                invalidate()
            }
            start()
        }
    }

    infix fun attachTo(recyclerView: RecyclerView) {
        if (::scrollListener.isInitialized) {
            recyclerView.removeOnScrollListener(scrollListener)
        }
        count = recyclerView.adapter?.itemCount ?: 0
        scrollListener = ScrollListener(this)
        recyclerView.addOnScrollListener(scrollListener)
        scrollToTarget(0)
    }

    infix fun attachTo(viewPager: ViewPager) {
        if (::pageChangeListener.isInitialized) {
            viewPager.removeOnPageChangeListener(pageChangeListener)
        }
        count = (viewPager.adapter as PagerAdapter).count
        pageChangeListener = PageChangeListener(this)
        viewPager.addOnPageChangeListener(pageChangeListener)
        scrollToTarget(0)
    }

    fun swipePrevious() {
        dotManager?.goToPrevious()
        animateDots()
    }

    fun swipeNext() {
        dotManager?.goToNext()
        animateDots()
    }

    private fun animateDots() {
        dotManager?.let {
            val (start, end) = getDrawingRange()
            (start until end).forEach { index ->
                dotAnimators[index].cancel()
                dotAnimators[index] = ValueAnimator.ofInt(dotSizes[index], it.dotSizeFor(it.dots[index])).apply {
                    duration = animDuration
                    interpolator = DEFAULT_INTERPOLATOR
                    addUpdateListener { animation ->
                        dotSizes[index] = animation.animatedValue as Int
                        invalidate()
                    }
                    start()
                }
            }
        }
    }

    private fun getDrawingRange(): Pair<Int, Int> {
        val start = max(0, (dotManager?.selectedIndex ?: 0) - MOST_VISIBLE_COUNT)
        val end = min(dotManager?.dots?.size ?: 0, (dotManager?.selectedIndex ?: 0) + MOST_VISIBLE_COUNT)
        return Pair(start, end)
    }

    class SavedState : BaseSavedState {

        var count: Int = 0

        var selectedIndex = 0

        constructor(superState: Parcelable) : super(superState)

        constructor(source: Parcel?) : this(source, null) {
            count = source?.readInt() ?: 0
            selectedIndex = source?.readInt() ?: 0
        }

        constructor(source: Parcel?, loader: ClassLoader?) : super(source, loader) {
            this.count = source?.readInt() ?: 0
            this.selectedIndex = source?.readInt() ?: 0
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(this.count)
            out.writeInt(this.selectedIndex)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(parcel: Parcel): SavedState {
                    return SavedState(parcel)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }

    }

}