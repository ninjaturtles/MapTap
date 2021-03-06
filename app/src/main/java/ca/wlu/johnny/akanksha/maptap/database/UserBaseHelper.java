package ca.wlu.johnny.akanksha.maptap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ca.wlu.johnny.akanksha.maptap.User;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.UserTable;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.FavPlacesTable;

/**
 * Created by johnny on 2017-11-21.
 */

public class UserBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "userBase.db";
    private static final int VERSION = 1;

    public UserBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db){

        db.execSQL("create table " + UserTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                UserTable.Cols.UUID + ", " +
                UserTable.Cols.NAME + ", " +
                UserTable.Cols.EMAIL + ", " +
                UserTable.Cols.PASSWORD + " )");

        // add testing users
        User user = new User("Johnny Khalil", "johnny@wlu.ca", "jk123456");
        ContentValues userValues = getUserValues(user);
        db.insert(UserTable.NAME, null, userValues);

        user = new User("Akanksha Malik", "akanksha@wlu.ca", "am123456");
        userValues = getUserValues(user);
        db.insert(UserTable.NAME, null, userValues);

        user = new User("Chinh Hoang", "hoang@wlu.ca", "ch123456");
        userValues = getUserValues(user);
        db.insert(UserTable.NAME, null, userValues);


        db.execSQL("create table " + FavPlacesTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                FavPlacesTable.Cols.ID + ", " +
                FavPlacesTable.Cols.NAME + ", " +
                FavPlacesTable.Cols.ADDRESS + ", " +
                FavPlacesTable.Cols.PHONENUM + ", " +
                FavPlacesTable.Cols.URL + ", " +
                FavPlacesTable.Cols.LATLNG + ", " +
                FavPlacesTable.Cols.TYPE + ", " +
                FavPlacesTable.Cols.PRICE + ", " +
                FavPlacesTable.Cols.RATING + ", " +
                FavPlacesTable.Cols.USEREMAIL + ", " +
                " FOREIGN KEY ("+FavPlacesTable.Cols.USEREMAIL+") REFERENCES " +
                UserTable.NAME+"("+UserTable.Cols.EMAIL+")" +
                " )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

    private static ContentValues getUserValues(User user) {
        ContentValues values = new ContentValues();
        values.put(UserTable.Cols.UUID, user.getId().toString());
        values.put(UserTable.Cols.NAME, user.getName());
        values.put(UserTable.Cols.EMAIL, user.getEmail());
        values.put(UserTable.Cols.PASSWORD, user.getPassword());
        return values;
    }
}
