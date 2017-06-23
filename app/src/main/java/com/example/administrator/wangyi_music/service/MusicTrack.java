package com.example.administrator.wangyi_music.service;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Administrator on 2017/6/12.
 */

public class MusicTrack implements Parcelable {
    public long mId;
    public int mSourcePosition;
    public String mTitle;
    public String mAlbum;
    public String mArtist;
    public MusicTrack(long id,int sourcePosition) {
        mId=id;
        mSourcePosition=sourcePosition;
    }

    public MusicTrack(Parcel in) {
        mId=in.readLong();
        mSourcePosition=in.readInt();
    }

    public static final Creator<MusicTrack> CREATOR = new Creator<MusicTrack>() {
        @Override
        public MusicTrack createFromParcel(Parcel in) {
            return new MusicTrack(in);
        }

        @Override
        public MusicTrack[] newArray(int size) {
            return new MusicTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeInt(mSourcePosition);
    }
}
