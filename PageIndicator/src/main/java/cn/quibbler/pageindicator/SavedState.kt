package cn.quibbler.pageindicator

import android.os.Parcel
import android.os.Parcelable
import android.view.View.BaseSavedState

class SavedState : BaseSavedState {

    var count: Int = 0

    var selectedIndex = 0

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