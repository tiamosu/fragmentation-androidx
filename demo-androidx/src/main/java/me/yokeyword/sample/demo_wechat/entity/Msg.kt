package me.yokeyword.sample.demo_wechat.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by YoKeyword on 16/6/30.
 */
class Msg : Parcelable {
    private var mMessage: String? = null

    fun getMessage(): String? {
        return mMessage
    }

    constructor(msg: String) {
        mMessage = msg
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.mMessage)
    }

    private constructor(`in`: Parcel) {
        this.mMessage = `in`.readString()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Msg> = object : Parcelable.Creator<Msg> {
            override fun createFromParcel(source: Parcel): Msg {
                return Msg(source)
            }

            override fun newArray(size: Int): Array<Msg?> {
                return arrayOfNulls(size)
            }
        }
    }
}
