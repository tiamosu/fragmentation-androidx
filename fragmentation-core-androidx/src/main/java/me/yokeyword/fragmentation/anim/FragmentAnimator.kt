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
    protected var mEnter: Int = 0
    @AnimRes
    protected var mExit: Int = 0
    @AnimRes
    protected var mPopEnter: Int = 0
    @AnimRes
    protected var mPopExit: Int = 0

    constructor()

    constructor(enter: Int, exit: Int) {
        this.mEnter = enter
        this.mExit = exit
    }

    constructor(enter: Int, exit: Int, popEnter: Int, popExit: Int) {
        this.mEnter = enter
        this.mExit = exit
        this.mPopEnter = popEnter
        this.mPopExit = popExit
    }

    fun copy(): FragmentAnimator {
        return FragmentAnimator(getEnter(), getExit(), getPopEnter(), getPopExit())
    }

    fun getEnter(): Int {
        return mEnter
    }

    fun setEnter(enter: Int): FragmentAnimator {
        this.mEnter = enter
        return this
    }

    fun getExit(): Int {
        return mExit
    }

    fun setExit(exit: Int): FragmentAnimator {
        this.mExit = exit
        return this
    }

    fun getPopEnter(): Int {
        return mPopEnter
    }

    fun setPopEnter(popEnter: Int): FragmentAnimator {
        this.mPopEnter = popEnter
        return this
    }

    fun getPopExit(): Int {
        return mPopExit
    }

    fun setPopExit(popExit: Int): FragmentAnimator {
        this.mPopExit = popExit
        return this
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.mEnter)
        dest.writeInt(this.mExit)
        dest.writeInt(this.mPopEnter)
        dest.writeInt(this.mPopExit)
    }

    protected constructor(`in`: Parcel) {
        this.mEnter = `in`.readInt()
        this.mExit = `in`.readInt()
        this.mPopEnter = `in`.readInt()
        this.mPopExit = `in`.readInt()
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
