package ca.wlu.johnny.akanksha.maptap.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.util.UUID;

import ca.wlu.johnny.akanksha.maptap.SelectedPlace;
import ca.wlu.johnny.akanksha.maptap.User;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.UserTable;
import ca.wlu.johnny.akanksha.maptap.database.UserDbSchema.FavPlacesTable;

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

    public SelectedPlace getPlace(){
        String myId = getString(getColumnIndex(FavPlacesTable.Cols.ID));
        String myName = getString(getColumnIndex(FavPlacesTable.Cols.NAME));
        String myAddress = getString(getColumnIndex(FavPlacesTable.Cols.ADDRESS));
        String myPhoneNum = getString(getColumnIndex(FavPlacesTable.Cols.PHONENUM));
        String myUrl = getString(getColumnIndex(FavPlacesTable.Cols.URL));
        String myLatlng = getString(getColumnIndex(FavPlacesTable.Cols.LATLNG));
        String myType = getString(getColumnIndex(FavPlacesTable.Cols.TYPE));
        String myPrice = getString(getColumnIndex(FavPlacesTable.Cols.PRICE));
        String myRating = getString(getColumnIndex(FavPlacesTable.Cols.RATING));
        String myUserEmail = getString(getColumnIndex(FavPlacesTable.Cols.USEREMAIL));


        SelectedPlace selectedPlace = new SelectedPlace(myId, myName, myAddress, myPhoneNum,
                myUrl, myLatlng, myType, Integer.parseInt(myPrice),
                Float.parseFloat(myRating), myUserEmail);

        return selectedPlace;
    }
}
