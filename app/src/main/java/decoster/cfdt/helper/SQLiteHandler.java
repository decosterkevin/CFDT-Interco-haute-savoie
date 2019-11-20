package decoster.cfdt.helper;

/**
 * Created by Decoster on 02/02/2016.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "cfdt";

    // Login table name
    private static final String TABLE_USER = "user";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_SURNAME = "user_uid";
    private static final String KEY_SERVER_ACCESS_CODE = "access_code";
    private static final String KEY_SERVER_GDRIVE_URL = "gdrive_url";
    private static final String KEY_SERVER_MANAGER_EMAIL = "manager_email";
    private static final String KEY_SERVER_ENTITY_NAME = "entity_name";
    private SQLiteDatabase db;

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_USER_SURNAME + " TEXT," + KEY_USER_NAME + " TEXT," + KEY_USER_EMAIL + " TEXT," + KEY_SERVER_ACCESS_CODE + " TEXT," + KEY_SERVER_GDRIVE_URL + " TEXT,"
                + KEY_SERVER_MANAGER_EMAIL + " TEXT UNIQUE," + KEY_SERVER_ENTITY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);

        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     */
    public void addUser(String surname, String name, String userEmail, String serverAccessCode, String gdriveUrl, String managerEmail, String entityName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_USER_SURNAME, surname); // Email
        values.put(KEY_USER_NAME, name);
        values.put(KEY_USER_EMAIL, userEmail); // Name
        values.put(KEY_SERVER_ACCESS_CODE, serverAccessCode); // Email
        values.put(KEY_SERVER_GDRIVE_URL, gdriveUrl); // Email
        values.put(KEY_SERVER_MANAGER_EMAIL, managerEmail); // Email
        values.put(KEY_SERVER_ENTITY_NAME, entityName); // Email

        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            Log.d(TAG, "Fetching user from Sqlite: " + user.toString());
            user.put("user_surname", cursor.getString(1));
            user.put("user_name", cursor.getString(2));
            user.put("user_email", cursor.getString(3));
            user.put("access_code", cursor.getString(4));
            user.put("gdrive_url", cursor.getString(5));
            user.put("manager_email", cursor.getString(6));
            user.put("entity_name", cursor.getString(7));
        }
        cursor.close();
        db.close();
        // return user


        return user;
    }

    /**
     * Re crate database Delete all tables and create them again
     */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

}