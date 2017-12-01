package ca.wlu.johnny.akanksha.maptap.database;

/**
 * Created by johnny on 2017-11-21.
 */

public class UserDbSchema {

    public static final class UserTable{
        public static final String NAME = "users";

        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String NAME = "name";
            public static final String EMAIL = "email";
            public static final String PASSWORD = "password";
        }
    }


    public static final class FavPlacesTable{
        public static final String NAME = "favPlaces";

        public static final class Cols{
            public static final String ID = "id";
            public static final String NAME = "name";
            public static final String ADDRESS = "address";
            public static final String PHONENUM = "phonenum";
            public static final String URL = "url";
            public static final String LATLNG = "latlng";
            public static final String TYPE = "type";
            public static final String PRICE = "price";
            public static final String RATING = "rating";
            public static final String USEREMAIL = "useremail";
        }
    }
}
