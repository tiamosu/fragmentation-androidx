package me.yokeyword.sample.demo_wechat.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by YoKeyword on 16/6/30.
 */
class Chat() : Parcelable {
    private var mName: String? = null
    private var mMessage: String? = null
    private var mTime: Long = 0
    private var mAvatar: Int = 0

    fun getName(): String? {
        return mName
    }

    fun setName(name: String) {
        mName = name
    }

    fun getMessage(): String? {
        return mMessage
    }

    fun setMessage(message: String) {
        mMessage = message
    }

    fun getTime(): Long {
        return mTime
    }

    fun setTime(time: Long) {
        mTime = time
    }

    fun getAvatar(): Int {
        return mAvatar
    }

    fun setAvatar(avatar: Int) {
        mAvatar = avatar
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.mName)
        dest.writeString(this.mMessage)
        dest.writeLong(this.mTime)
        dest.writeInt(this.mAvatar)
    }

    private constructor(`in`: Parcel) : this() {
        this.mName = `in`.readString()
        this.mMessage = `in`.readString()
        this.mTime = `in`.readLong()
        this.mAvatar = `in`.readInt()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Chat> = object : Parcelable.Creator<Chat> {
            override fun createFromParcel(source: Parcel): Chat {
                return Chat(source)
            }

            override fun newArray(size: Int): Array<Chat?> {
                return arrayOfNulls(size)
            }
        }
    }
}
