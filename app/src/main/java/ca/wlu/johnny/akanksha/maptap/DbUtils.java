package ca.wlu.johnny.akanksha.maptap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.wlu.johnny.akanksha.maptap.database.UserBaseHelper;
import ca.wlu.johnny.akanksha.maptap.database.UserCursorWrapper;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.UserTable;
/**
 * Created by johnny on 2017-11-21.
 */

public class DbUtils {

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

    public int deleteCard(UUID id) {
        int status = -1;

        try {
            status = mDatabase.delete(UserTable.NAME, UserTable.Cols.UUID + " = ?", new String[] {id.toString()});
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

    private UserCursorWrapper queryUsers(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(UserTable.NAME, null, whereClause, whereArgs, null, null, null);
        return new UserCursorWrapper(cursor);
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
