package me.yokeyword.fragmentation.helper.internal;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @Hide Result 记录
 * Created by YoKeyword on 16/6/2.
 */
public final class ResultRecord implements Parcelable {
    public int mRequestCode;
    public int mResultCode = 0;
    public Bundle mResultBundle;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mRequestCode);
        dest.writeInt(this.mResultCode);
        dest.writeBundle(this.mResultBundle);
    }

    public ResultRecord() {
    }

    private ResultRecord(Parcel in) {
        this.mRequestCode = in.readInt();
        this.mResultCode = in.readInt();
        this.mResultBundle = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<ResultRecord> CREATOR = new Creator<ResultRecord>() {
        @Override
        public ResultRecord createFromParcel(Parcel source) {
            return new ResultRecord(source);
        }

        @Override
        public ResultRecord[] newArray(int size) {
            return new ResultRecord[size];
        }
    };
}
