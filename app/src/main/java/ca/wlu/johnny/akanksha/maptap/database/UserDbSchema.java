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
}
