package cn.quibbler.pageindicator

class DotManager(
    count: Int,
    private val dotSize: Int,
    private val dotSpacing: Int,
    private val dotBound: Int,
    private val dotSizes: Map<Byte, Int>,
    private val targetScrollListener: TargetScrollListener? = null
) {

    companion object {
        private const val SIZE_THRESHOLD = 5
    }

    var dots: ByteArray = ByteArray(count)
    var selectedIndex = 0

    private var scrollAmount = 0

    init {
        if (count > 0) {
            dots[0] = 6
        }

        if (count <= SIZE_THRESHOLD) {
            (1 until count).forEach { i -> dots[i] = 5 }
        } else {
            (1..3).forEach { i -> dots[i] = 5 }
            dots[4] = 4
            if (count > SIZE_THRESHOLD) {
                dots[5] = 2
            }
            (SIZE_THRESHOLD + 1 until count).forEach { i -> dots[i] = 0 }
        }
    }

    fun dots() = dots.joinToString("")

    fun dotSizeFor(size: Byte) = dotSizes[size] ?: 0

    fun goToNext() {
        if (selectedIndex >= dots.size - 1) {
            return
        }

        ++selectedIndex

        if (dots.size <= SIZE_THRESHOLD) {
            goToNextSmall()
        } else {
            goToNextLarge()
        }
    }

    fun toToPrevious() {
        if (selectedIndex == 0) {
            return
        }

        --selectedIndex

        if (dots.size <= SIZE_THRESHOLD) {
            goToPreviousSmall()
        } else {
            goToPreviousLarge()
        }
    }

}