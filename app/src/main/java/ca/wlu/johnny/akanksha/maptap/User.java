package ca.wlu.johnny.akanksha.maptap;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
* Created by johnny on 2017-11-21.
*/

public class User implements Parcelable {

    private UUID mId;
    private String mName;
    private String mEmail;
    private String mPassword;
    private double mLat;
    private double mLng;

    @Override
    public int describeContents() {

        return 0;
    }

    public User(String myName, String myEmail, String myPassword) {
        this(UUID.randomUUID(), myName, myEmail, myPassword);
    }

    public User(UUID id, String theName, String theEmail, String thePassword){
        mId = id;
        mName = theName;
        mEmail = theEmail;
        mPassword = thePassword;
    }

    public User(Parcel in){
        this.mId = (UUID) in.readSerializable();
        this.mName = in.readString();
        this.mEmail = in.readString();
        this.mPassword = in.readString();
        this.mLat = in.readDouble();
        this.mLng = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mId);
        dest.writeString(mName);
        dest.writeString(mEmail);
        dest.writeString(mPassword);
        dest.writeDouble(mLat);
        dest.writeDouble(mLng);
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public String toString() {
        return mId + ", " + mName  + ", " + mEmail;
    }

    // getter methods for returning image, question, and answer
    public UUID getId() {

        return mId;
    }

    public String getName() {

        return mName;
    }

    public String getEmail() {

        return mEmail;
    }

    public String getPassword() {

        return mPassword;
    }

    public double getLat() {
        return mLat;
    }

    public double getLng() {
        return mLng;
    }

    public void setLat(double myLat) {
        this.mLat = myLat;
    }

    public void setLng(double myLng) {
        this.mLng = myLng;
    }

    public void setEmail(String newEmail) {
        this.mEmail = newEmail;
    }

    public void setPassword(String newPassword){
        this.mPassword = newPassword;
    }
}// User