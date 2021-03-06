package ca.wlu.johnny.akanksha.maptap;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ca.wlu.johnny.akanksha.maptap.database.UserBaseHelper;
import ca.wlu.johnny.akanksha.maptap.database.UserCursorWrapper;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.UserTable;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.FavPlacesTable;

/**
 * Created by johnny on 2017-11-21.
 */

public class DbUtils {

    private static final String ARG_SESSION_EXISTS = "ca.wlu.johnny.akanksha.maptap.sessionExists";

    private static DbUtils sDbUtils;
    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static DbUtils get(Context context) {
        if (sDbUtils == null) {
        sDbUtils = new DbUtils(context);
    }
        return sDbUtils;
    }

    private DbUtils(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new UserBaseHelper(mContext).getWritableDatabase();
    }

    public void addUser(User user) {
        ContentValues userValues = getUserValues(user);
        mDatabase.insert(UserTable.NAME, null, userValues);
    }

    public void addPlace(SelectedPlace selectedPlcae) {
        ContentValues placeValues = getplaceValues(selectedPlcae);
        mDatabase.insert(FavPlacesTable.NAME, null, placeValues);
    }

    public User getUser(String email) {
        UserCursorWrapper cursor = queryUsers(UserTable.Cols.EMAIL + " = ?", new String[]{email});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getUser();
        } finally {
            cursor.close();
        }
    }

    public SelectedPlace getPlace(String id, String userEmail) {
        UserCursorWrapper cursor = queryFavPlace(FavPlacesTable.Cols.ID +
                        " = ? AND " + FavPlacesTable.Cols.USEREMAIL + " = ? " ,
                new String[]{id, userEmail});
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            return cursor.getPlace();
        } finally {
            cursor.close();
        }
    }

    public void updateEmail(String newEmail, User user){

        mDatabase.execSQL("UPDATE "+ UserTable.NAME + " SET " + UserTable.Cols.EMAIL + "='"+newEmail +
                "' WHERE email='"+user.getEmail()+"'");

        user.setEmail(newEmail);

        // update session preference
        SharedPreferences.Editor editor = MainActivity.sharedpreferences.edit();
        editor.putString(ARG_SESSION_EXISTS, newEmail);
        editor.commit();
    }

    public void updatePw(String newPw, User user){

        mDatabase.execSQL("UPDATE "+ UserTable.NAME + " SET " + UserTable.Cols.PASSWORD + "='"+newPw +
                "' WHERE password='"+user.getPassword()+"'");

        user.setPassword(newPw);
    }

    public int deleteUser(String email) {
        int status = -1;

        try {
            status = mDatabase.delete(UserTable.NAME, UserTable.Cols.EMAIL + " = ?", new String[] {email});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public int deletePlace(String id, String userEmail) {
        int status = -1;

        try {
            status = mDatabase.delete(FavPlacesTable.NAME, FavPlacesTable.Cols.ID +
                            " = ? AND " + FavPlacesTable.Cols.USEREMAIL + " = ? ",
                    new String[] {id, userEmail});

        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public List<User> getUsers() {
        List<User> cards = new ArrayList<>();
        UserCursorWrapper cursor = queryUsers(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                cards.add(cursor.getUser());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return cards;
    }

    public List<SelectedPlace> getPlaces(String userEmail) {
        List<SelectedPlace> places = new ArrayList<>();
        UserCursorWrapper cursor = queryFavPlace(FavPlacesTable.Cols.USEREMAIL +
                        " = ?" , new String[]{userEmail});
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                places.add(cursor.getPlace());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return places;
    }

    private UserCursorWrapper queryUsers(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(UserTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new UserCursorWrapper(cursor);
    }

    private UserCursorWrapper queryFavPlace(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(FavPlacesTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new UserCursorWrapper(cursor);
    }

    public File getPhotoFile(User user) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, user.getPhotoFilename());
    }

    private static ContentValues getUserValues(User user) {
        ContentValues values = new ContentValues();
        values.put(UserTable.Cols.UUID, user.getId().toString());
        values.put(UserTable.Cols.NAME, user.getName());
        values.put(UserTable.Cols.EMAIL, user.getEmail());
        values.put(UserTable.Cols.PASSWORD, user.getPassword());
        return values;
    }

    private static ContentValues getplaceValues(SelectedPlace place) {
        ContentValues values = new ContentValues();
        values.put(FavPlacesTable.Cols.ID, place.getId());
        values.put(FavPlacesTable.Cols.NAME, place.getName());
        values.put(FavPlacesTable.Cols.ADDRESS, place.getAddress());
        values.put(FavPlacesTable.Cols.PHONENUM, place.getPhoneNumber());
        values.put(FavPlacesTable.Cols.URL, place.getUrl());
        values.put(FavPlacesTable.Cols.LATLNG, place.getLatLng());
        values.put(FavPlacesTable.Cols.TYPE, place.getType());
        values.put(FavPlacesTable.Cols.PRICE, Integer.toString(place.getPrice()));
        values.put(FavPlacesTable.Cols.RATING, String.valueOf(place.getRating()));
        values.put(FavPlacesTable.Cols.USEREMAIL, String.valueOf(place.getUserEmail()));

        return values;
    }

}//DbUtils