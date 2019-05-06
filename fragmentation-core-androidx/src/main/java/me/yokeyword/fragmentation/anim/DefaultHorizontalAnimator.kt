package me.yokeyword.fragmentation.anim

import android.os.Parcel
import android.os.Parcelable

import me.yokeyword.fragmentation.R

/**
 * Created by YoKeyword on 16/2/5.
 */
class DefaultHorizontalAnimator : FragmentAnimator, Parcelable {

    constructor() {
        mEnter = R.anim.h_fragment_enter
        mExit = R.anim.h_fragment_exit
        mPopEnter = R.anim.h_fragment_pop_enter
        mPopExit = R.anim.h_fragment_pop_exit
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
        val CREATOR: Parcelable.Creator<DefaultHorizontalAnimator> = object : Parcelable.Creator<DefaultHorizontalAnimator> {
            override fun createFromParcel(source: Parcel): DefaultHorizontalAnimator {
                return DefaultHorizontalAnimator(source)
            }

            override fun newArray(size: Int): Array<DefaultHorizontalAnimator?> {
                return arrayOfNulls(size)
            }
        }
    }
}
