package com.ecs160.packmanager.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ecs160.packmanager.utils.App;
import com.ecs160.packmanager.utils.Session;
import com.ecs160.packmanager.utils.Transaction;
import com.ecs160.packmanager.utils.User;

import java.util.ArrayList;
import java.util.UUID;


/**
 * SQLite database implementation. Note that this class
 * only contains methods that access Jams' private
 * database. For methods that access Android's
 * MediaStore database, see MediaStoreAccessHelper.
 *
 * @author Saravan Pantham
 */
public class DBAccessHelper extends SQLiteOpenHelper {

    //Database instance. Will last for the lifetime of the application.
    private static DBAccessHelper sInstance;

    //Writable database instance.
    private SQLiteDatabase mDatabase;

    //Database Version.
    private static final int DATABASE_VERSION = 1;

    //Database Name.
    private static final String DATABASE_NAME = "Jams.db";

    // Column types.
    public static final String TYPE_TEXT = "TEXT";
    public static final String TYPE_INT = "INTEGER";
    public static final String TYPE_BLOB = "BLOB";

    // ID column.
    public static final String _ID = "_id";

    // Users table.
    public static final String USERS = "users";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String REAL_NAME = "real_name";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String RELIABILITY_INDEX = "reliability";
    public static final String FRIEND_USERNAMES = "friend_usernames";
    public static final String INVOLVED_TRANSACTION_IDS = "involved_transaction_ids";

    // Transactions table.
    public static final String TRANSACTIONS = "transactions";
    public static final String TRANSACTION_ID = "transaction_id";
    public static final String PACKAGE_NAME = "package_name";
    public static final String STATUS = "status";
    public static final String SENDER = "sender";
    public static final String RECEIVER = "receiver";
    public static final String INTERMEDIARY = "intermediary";
    public static final String LOCATION = "location";
    public static final String TIMESTAMP = "timestamp";
    public static final String INTERMEDIARY_LOCATION = "intermediary_location";
    public static final String INTERMEDIARY_TIMESTAMP = "intermediary_timestamp";

    public DBAccessHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Returns a singleton instance for the database.
     * @param context
     * @return
     */
    public static synchronized DBAccessHelper getInstance(Context context) {
        if (sInstance==null)
            sInstance = new DBAccessHelper(context.getApplicationContext());

        return sInstance;
    }

    /**
     * Returns a writable instance of the database. Provides an additional
     * null check for additional stability.
     */
    private synchronized SQLiteDatabase getDatabase() {
        if (mDatabase==null)
            mDatabase = getWritableDatabase();

        return mDatabase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table.
        String[] usersCols = { USERNAME, PASSWORD, REAL_NAME, PHONE_NUMBER,
                               RELIABILITY_INDEX, FRIEND_USERNAMES, INVOLVED_TRANSACTION_IDS };
        String[] userColTypes = { TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                                  TYPE_TEXT, TYPE_TEXT, TYPE_TEXT };

        // Transactions table.
        String[] transactionCols = { TRANSACTION_ID, PACKAGE_NAME, STATUS, SENDER, RECEIVER, INTERMEDIARY,
                                     LOCATION, TIMESTAMP, INTERMEDIARY_LOCATION, INTERMEDIARY_TIMESTAMP };
        String[] transactionColTypes = { TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT, TYPE_TEXT,
                                         TYPE_TEXT, TYPE_TEXT, TYPE_TEXT };

        // Build the table creation queries.
        String createUsersTable = buildCreateStatement(USERS, usersCols, userColTypes);
        String createTransactionsTable = buildCreateStatement(TRANSACTIONS, transactionCols, transactionColTypes);

        //Execute the CREATE statements.
        db.execSQL(createUsersTable);
        db.execSQL(createTransactionsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void finalize() {
        try {
            getDatabase().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a fully formed CREATE statement using the input
     * parameters.
     */
    private String buildCreateStatement(String tableName, String[] columnNames, String[] columnTypes) {
        String createStatement = "";
        if (columnNames.length==columnTypes.length) {
            createStatement += "CREATE TABLE IF NOT EXISTS " + tableName + "("
                    + _ID + " INTEGER PRIMARY KEY, ";

            for (int i=0; i < columnNames.length; i++) {
                if (i==columnNames.length-1) {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ")";
                } else {
                    createStatement += columnNames[i]
                            + " "
                            + columnTypes[i]
                            + ", ";
                }
            }
        }

        return createStatement;
    }

    /**
     * Adds a new user to the database.
     */
    public void addNewUser(String username, String password, String realName, String phoneNumber) {
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);
        values.put(REAL_NAME, realName);
        values.put(PHONE_NUMBER, phoneNumber);
        values.put(RELIABILITY_INDEX, -1); // An index of -1 indicates that the user has no history.
        values.put(FRIEND_USERNAMES, ""); // The user's friends will be comma separated values of usernames.
        values.put(INVOLVED_TRANSACTION_IDS, ""); // Transactions the user is involved in will show up as ids separated by commas.

        getWritableDatabase().insert(USERS, null, values);

    }

    /**
     * Retrieves the password for the specified user. Returns
     * null if the user does not exist.
     */
    public String getUserPassword(String username) {
        Cursor cursor = getWritableDatabase().query(USERS, null, USERNAME + " = '" + username + "'", null, null, null, null);
        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(PASSWORD));
        } else {
            return null;
        }
    }

    /**
     * Retrieves the user details in the form of a Session object.
     */
    public Session getUserSessionObject(String username) {
        Cursor cursor = getWritableDatabase().query(USERS, null, USERNAME + " = '" + username + "'", null, null, null, null);
        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            user.setUsername(username);
            user.setRealName(cursor.getString(cursor.getColumnIndex(REAL_NAME)));
            user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(PASSWORD)));
            user.setReliabilityIndex(cursor.getInt(cursor.getColumnIndex(RELIABILITY_INDEX)));

            return new Session(user);
        } else {
            return null;
        }
    }

    /**
     * Queries the DB for all users and returns them in an ArrayList.
     */
    public ArrayList<User> getAllUsers() {
        Cursor cursor = getReadableDatabase().query(USERS, null, null, null, null, null, null);
        ArrayList<User> list = new ArrayList<User>();
        if (cursor!=null && cursor.getCount() > 0) {
            for (int i=0; i < cursor.getCount(); i++) {
                // Don't show the logged in user.
                cursor.moveToPosition(i);
                if (App.getCurrentSession().getLoggedInUser().getUsername().equals(cursor.getString(cursor.getColumnIndex(USERNAME)))) {
                    continue;
                }

                User user = new User();
                user.setRealName(cursor.getString(cursor.getColumnIndex(REAL_NAME)));
                user.setUsername(cursor.getString(cursor.getColumnIndex(USERNAME)));
                user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
                user.setReliabilityIndex(cursor.getInt(cursor.getColumnIndex(RELIABILITY_INDEX)));
                list.add(user);
            }
        }

        return list;
    }

    public void addFriend(String friendUsername) {
        String username = App.getCurrentSession().getLoggedInUser().getUsername();
        Cursor cursor = getWritableDatabase().query(USERS, null, USERNAME + " = '" + username + "'", null, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String friends = cursor.getString(cursor.getColumnIndex(FRIEND_USERNAMES));
            if (friends.length()==0 || friends.charAt(friends.length()-1)==',') {
                friends += friendUsername;
            } else {
                friends += "," + friendUsername;
            }

            ContentValues values = new ContentValues();
            values.put(FRIEND_USERNAMES, friends);
            getWritableDatabase().update(USERS, values, USERNAME + " = '" + username + "'", null);
        }
    }

    public void createTransaction(String packageName, String sender, String receiver, String intermediary,
                              String location, String timestamp, String intermediaryTimestamp,
                              String intermediaryLocation) {
        String transactionId = UUID.randomUUID().toString();
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TRANSACTION_ID, transactionId.substring(0, 3));
        values.put(PACKAGE_NAME, packageName);
        values.put(SENDER, sender);
        values.put(RECEIVER, receiver);
        values.put(INTERMEDIARY, intermediary);
        values.put(LOCATION, location);
        values.put(TIMESTAMP, timestamp);
        values.put(INTERMEDIARY_TIMESTAMP, intermediaryTimestamp);
        values.put(INTERMEDIARY_LOCATION, intermediaryLocation);

        db.insert(TRANSACTIONS, null, values);

    }

    public ArrayList<Transaction> getAllTransactions() {
        ArrayList<Transaction> list = new ArrayList<Transaction>();
        String currentUsername = App.getCurrentSession().getLoggedInUser().getUsername();
        String where = SENDER + "='" + currentUsername + "' OR " + RECEIVER + " = '" + currentUsername + "' OR " + INTERMEDIARY + " = '" + currentUsername + "'";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TRANSACTIONS, null, where, null, null, null, null);

        if (cursor!=null && cursor.getCount() > 0) {
            for (int i=0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                String packageName = cursor.getString(cursor.getColumnIndex(PACKAGE_NAME));
                String sender = cursor.getString(cursor.getColumnIndex(SENDER));
                String receiver = cursor.getString(cursor.getColumnIndex(RECEIVER));

                User userSender = getUserFromUsername(sender);
                User userReceiver = getUserFromUsername(receiver);

                Transaction transaction = new Transaction(packageName, userSender, userReceiver);
                transaction.setTimeStamp(cursor.getString(cursor.getColumnIndex(TIMESTAMP)));
                list.add(transaction);

            }
        }

        return list;
    }

    public String getPhoneNumberForUsername(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String where = USERNAME + "='" + username + "'";
        Cursor cursor = db.query(USERS, null, where, null, null, null, null);

        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            return cursor.getString(cursor.getColumnIndex(PHONE_NUMBER));
        }

        return null;
    }

    public User getUserFromUsername(String username) {
        SQLiteDatabase db = getReadableDatabase();
        String where = USERNAME + "= '" + username + "'";
        Cursor cursor = db.query(USERS, null, where, null, null, null, null);

        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            User user = new User();
            user.setRealName(cursor.getString(cursor.getColumnIndex(REAL_NAME)));
            user.setUsername(cursor.getString(cursor.getColumnIndex(USERNAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndex(PASSWORD)));
            user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(PHONE_NUMBER)));
            user.setReliabilityIndex(cursor.getInt(cursor.getColumnIndex(RELIABILITY_INDEX)));
            return user;
        }

        return null;
    }

    public ArrayList<User> getAllFriends() {
        ArrayList<User> usersList = new ArrayList<User>();
        String username = App.getCurrentSession().getLoggedInUser().getUsername();
        SQLiteDatabase db = getReadableDatabase();
        String where = USERNAME + "= '" + username + "'";
        Cursor cursor = db.query(USERS, null, where, null, null, null, null);

        if (cursor!=null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String friends = cursor.getString(cursor.getColumnIndex(FRIEND_USERNAMES));
            String[] friendsList = friends.split(",");

            for (int i=0; i < friendsList.length; i++) {
                usersList.add(getUserFromUsername(friendsList[i]));
            }
        }

        return usersList;
    }

    public void updatePackageStatus(String packageName, int status) {
        SQLiteDatabase db = getReadableDatabase();
        String where = PACKAGE_NAME + "= '" + packageName + "'";
        ContentValues values = new ContentValues();
        values.put(STATUS, status);

        db.update(TRANSACTIONS, values, where, null);
    }

}

