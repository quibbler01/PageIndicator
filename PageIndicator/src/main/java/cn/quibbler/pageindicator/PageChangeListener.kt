package cn.quibbler.pageindicator

import androidx.viewpager.widget.ViewPager

class PageChangeListener(private val indicator: PageIndicator) : ViewPager.OnPageChangeListener {

    var selectedPage: Int = 0

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        if (position != selectedPage) {
            when {
                this.selectedPage < position -> indicator.swipeNext()
                else -> indicator.swipePrevious()
            }
        }
        selectedPage = position
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

}