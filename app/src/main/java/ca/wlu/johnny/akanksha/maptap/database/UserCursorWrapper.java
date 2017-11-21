package ca.wlu.johnny.akanksha.maptap.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;

import ca.wlu.johnny.akanksha.maptap.User;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.UserTable;

/**
 * Created by johnny on 2017-11-21.
 */

public class UserCursorWrapper extends CursorWrapper {

    public UserCursorWrapper(Cursor cursor){

        super(cursor);
    }

    public User getUser(){
        String myUuidString = getString(getColumnIndex(UserTable.Cols.UUID));
        String myName = getString(getColumnIndex(UserTable.Cols.NAME));
        String myEmail = getString(getColumnIndex(UserTable.Cols.EMAIL));
        String myPassword = getString(getColumnIndex(UserTable.Cols.PASSWORD));

        User user = new User(UUID.fromString(myUuidString), myName, myEmail, myPassword);

        return user;
    }
}
