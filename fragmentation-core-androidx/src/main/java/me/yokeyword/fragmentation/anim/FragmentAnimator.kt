package me.yokeyword.fragmentation.anim

import android.os.Parcel
import android.os.Parcelable

import androidx.annotation.AnimRes

/**
 * Fragment动画实体类
 * Created by YoKeyword on 16/2/4.
 */
@Suppress("unused")
open class FragmentAnimator : Parcelable {
    @AnimRes
    protected var enterAnim: Int = 0

    @AnimRes
    protected var exitAnim: Int = 0

    @AnimRes
    protected var popEnterAnim: Int = 0

    @AnimRes
    protected var popExitAnim: Int = 0

    constructor()

    constructor(enter: Int, exit: Int) {
        this.enterAnim = enter
        this.exitAnim = exit
    }

    constructor(enter: Int, exit: Int, popEnter: Int, popExit: Int) {
        this.enterAnim = enter
        this.exitAnim = exit
        this.popEnterAnim = popEnter
        this.popExitAnim = popExit
    }

    fun copy(): FragmentAnimator {
        return FragmentAnimator(getEnter(), getExit(), getPopEnter(), getPopExit())
    }

    fun getEnter(): Int {
        return enterAnim
    }

    fun setEnter(enter: Int): FragmentAnimator {
        this.enterAnim = enter
        return this
    }

    fun getExit(): Int {
        return exitAnim
    }

    fun setExit(exit: Int): FragmentAnimator {
        this.exitAnim = exit
        return this
    }

    fun getPopEnter(): Int {
        return popEnterAnim
    }

    fun setPopEnter(popEnter: Int): FragmentAnimator {
        this.popEnterAnim = popEnter
        return this
    }

    fun getPopExit(): Int {
        return popExitAnim
    }

    fun setPopExit(popExit: Int): FragmentAnimator {
        this.popExitAnim = popExit
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.enterAnim)
        dest.writeInt(this.exitAnim)
        dest.writeInt(this.popEnterAnim)
        dest.writeInt(this.popExitAnim)
    }

    protected constructor(`in`: Parcel) {
        this.enterAnim = `in`.readInt()
        this.exitAnim = `in`.readInt()
        this.popEnterAnim = `in`.readInt()
        this.popExitAnim = `in`.readInt()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<FragmentAnimator> = object : Parcelable.Creator<FragmentAnimator> {
            override fun createFromParcel(source: Parcel): FragmentAnimator {
                return FragmentAnimator(source)
            }

            override fun newArray(size: Int): Array<FragmentAnimator?> {
                return arrayOfNulls(size)
            }
        }
    }
}
