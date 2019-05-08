package me.yokeyword.sample.demo_wechat.entity

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by YoKeyword on 16/2/1.
 */
class Article : Parcelable {
    private var mTitle: String? = null
    private var mContent: String? = null
    private var mImgRes: Int = 0

    constructor (title: String, content: String) {
        this.mTitle = title
        this.mContent = content
    }

    constructor(title: String, imgRes: Int) {
        this.mTitle = title
        this.mImgRes = imgRes
    }

    fun getTitle(): String? {
        return mTitle
    }

    fun setTitle(title: String) {
        this.mTitle = title
    }

    fun getContent(): String? {
        return mContent
    }

    fun setContent(content: String) {
        this.mContent = content
    }

    fun getImgRes(): Int {
        return mImgRes
    }

    fun setImgRes(imgRes: Int) {
        this.mImgRes = imgRes
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.mTitle)
        dest.writeString(this.mContent)
        dest.writeInt(this.mImgRes)
    }

    private constructor(`in`: Parcel) {
        this.mTitle = `in`.readString()
        this.mContent = `in`.readString()
        this.mImgRes = `in`.readInt()
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Article> = object : Parcelable.Creator<Article> {
            override fun createFromParcel(source: Parcel): Article {
                return Article(source)
            }

            override fun newArray(size: Int): Array<Article?> {
                return arrayOfNulls(size)
            }
        }
    }
}
