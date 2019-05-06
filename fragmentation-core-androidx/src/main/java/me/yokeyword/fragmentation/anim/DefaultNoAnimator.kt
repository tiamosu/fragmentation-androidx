package me.yokeyword.fragmentation.anim

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by YoKeyword on 16/2/15.
 */
class DefaultNoAnimator : FragmentAnimator, Parcelable {

    constructor() {
        mEnter = 0
        mExit = 0
        mPopEnter = 0
        mPopExit = 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
    }

    private constructor(`in`: Parcel) : super(`in`)

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<DefaultNoAnimator> = object : Parcelable.Creator<DefaultNoAnimator> {
            override fun createFromParcel(source: Parcel): DefaultNoAnimator {
                return DefaultNoAnimator(source)
            }

            override fun newArray(size: Int): Array<DefaultNoAnimator?> {
                return arrayOfNulls(size)
            }
        }
    }
}
