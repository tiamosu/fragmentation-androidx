package me.yokeyword.fragmentation.helper.internal

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable

/**
 * @Hide Result 记录
 * Created by YoKeyword on 16/6/2.
 */
class ResultRecord : Parcelable {
    var mRequestCode: Int = 0
    var mResultCode = 0
    var mResultBundle: Bundle? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.mRequestCode)
        dest.writeInt(this.mResultCode)
        dest.writeBundle(this.mResultBundle)
    }

    constructor()

    private constructor(`in`: Parcel) {
        this.mRequestCode = `in`.readInt()
        this.mResultCode = `in`.readInt()
        this.mResultBundle = `in`.readBundle(javaClass.classLoader)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<ResultRecord> = object : Parcelable.Creator<ResultRecord> {
            override fun createFromParcel(source: Parcel): ResultRecord {
                return ResultRecord(source)
            }

            override fun newArray(size: Int): Array<ResultRecord?> {
                return arrayOfNulls(size)
            }
        }
    }
}
