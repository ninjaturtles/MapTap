package ca.wlu.johnny.akanksha.maptap;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by johnny on 2017-11-22.
 */

public class SelectedPlace implements Parcelable {

    String mName;
    String mAddress;
    String mPhoneNumber;
    String mUrl;
    String mLatLng;
    String mType;
    String mPrice;



    @Override
    public int describeContents() {

        return 0;
    }

    public SelectedPlace(String myName, String myAddress, String myPhoneNumber, String myUrl, String myLatLng, String myType, String myPrice){
        mName = myName;
        mAddress = myAddress;
        mPhoneNumber = myPhoneNumber;
        mUrl = myUrl;
        mLatLng = myLatLng;
        mType = myType;
        mPrice = myPrice;
    }

    public SelectedPlace(Parcel in){
        this.mName = in.readString();
        this.mAddress = in.readString();
        this.mPhoneNumber = in.readString();
        this.mUrl = in.readString();
        this.mLatLng = in.readString();
        this.mType = in.readString();
        this.mPrice = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mName);
        dest.writeString(mAddress);
        dest.writeString(mPhoneNumber);
        dest.writeString(mUrl);
        dest.writeString(mLatLng);
        dest.writeString(mType);
        dest.writeString(mPrice);
    }

    public static final Parcelable.Creator<SelectedPlace> CREATOR = new Parcelable.Creator<SelectedPlace>() {

        @Override
        public SelectedPlace createFromParcel(Parcel source) {
            return new SelectedPlace(source);
        }

        @Override
        public SelectedPlace[] newArray(int size) {
            return new SelectedPlace[size];
        }
    };

    public String getName() {
        return mName;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getLatLng() {
        return mLatLng;
    }

    @Override
    public String toString() {
        return "name: " + mName + ", " + "Number: " + mPhoneNumber;
    }

    public String getType() {
        return mType;
    }

    public String getPrice() {
        return mPrice;
    }
}