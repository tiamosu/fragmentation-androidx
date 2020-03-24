package me.yokeyword.fragmentation.anim

import android.os.Parcel
import android.os.Parcelable

import me.yokeyword.fragmentation.R

/**
 * Created by YoKeyword on 16/2/5.
 */
class DefaultVerticalAnimator : FragmentAnimator, Parcelable {

    constructor() {
        enterAnim = R.anim.v_fragment_enter
        exitAnim = R.anim.v_fragment_exit
        popEnterAnim = R.anim.v_fragment_pop_enter
        popExitAnim = R.anim.v_fragment_pop_exit
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
        val CREATOR: Parcelable.Creator<DefaultVerticalAnimator> = object : Parcelable.Creator<DefaultVerticalAnimator> {
            override fun createFromParcel(source: Parcel): DefaultVerticalAnimator {
                return DefaultVerticalAnimator(source)
            }

            override fun newArray(size: Int): Array<DefaultVerticalAnimator?> {
                return arrayOfNulls(size)
            }
        }
    }
}
