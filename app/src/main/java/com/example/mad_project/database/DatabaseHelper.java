package com.example.mad_project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.mad_project.models.Post;
import com.example.mad_project.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "campusconnect.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_POSTS = "posts";

    // User Table Columns
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password"; // Hashed in real app
    private static final String KEY_BIO = "bio";
    private static final String KEY_TIMESTAMP = "timestamp";

    // Post Table Columns
    private static final String KEY_POST_ID = "post_id";
    private static final String KEY_POST_USER_ID = "user_id";
    private static final String KEY_POST_USERNAME = "username";
    private static final String KEY_POST_EMAIL = "email";
    private static final String KEY_POST_USER_BIO = "user_bio";
    private static final String KEY_TITLE = "title";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_IMAGE_URL = "image_url";
    private static final String KEY_LIKES_COUNT = "likes_count";
    private static final String KEY_COMMENTS_COUNT = "comments_count";
    private static final String KEY_POST_TIMESTAMP = "post_timestamp";
    private static final String KEY_FORMATTED_DATE = "formatted_date";
    private static final String KEY_LIKED_BY = "liked_by";

    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + KEY_USER_ID + " TEXT PRIMARY KEY,"
                + KEY_USERNAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_BIO + " TEXT,"
                + KEY_TIMESTAMP + " INTEGER" + ")";
        db.execSQL(CREATE_USERS_TABLE);

        // Create Posts Table
        String CREATE_POSTS_TABLE = "CREATE TABLE " + TABLE_POSTS + "("
                + KEY_POST_ID + " TEXT PRIMARY KEY,"
                + KEY_POST_USER_ID + " TEXT,"
                + KEY_POST_USERNAME + " TEXT,"
                + KEY_POST_EMAIL + " TEXT,"
                + KEY_POST_USER_BIO + " TEXT,"
                + KEY_TITLE + " TEXT,"
                + KEY_CONTENT + " TEXT,"
                + KEY_CATEGORY + " TEXT,"
                + KEY_IMAGE_URL + " TEXT,"
                + KEY_LIKES_COUNT + " INTEGER DEFAULT 0,"
                + KEY_COMMENTS_COUNT + " INTEGER DEFAULT 0,"
                + KEY_POST_TIMESTAMP + " INTEGER,"
                + KEY_FORMATTED_DATE + " TEXT,"
                + KEY_LIKED_BY + " TEXT" + ")";
        db.execSQL(CREATE_POSTS_TABLE);

        // Insert demo data
        insertDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
        onCreate(db);
    }

    // ==================== USER OPERATIONS ====================

    public long registerUser(String username, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        String userId = "local_" + System.currentTimeMillis();

        values.put(KEY_USER_ID, userId);
        values.put(KEY_USERNAME, username);
        values.put(KEY_EMAIL, email);
        values.put(KEY_PASSWORD, password); // In real app, hash this!
        values.put(KEY_BIO, "Hey there! I'm using CampusConnect");
        values.put(KEY_TIMESTAMP, System.currentTimeMillis());

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        if (result == -1) {
            Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Registration successful (Local DB)", Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    public boolean loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_USER_ID};
        String selection = KEY_EMAIL + " = ? AND " + KEY_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] columns = {KEY_USER_ID, KEY_USERNAME, KEY_EMAIL, KEY_BIO, KEY_TIMESTAMP};
        String selection = KEY_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs,
                null, null, null);

        if (cursor.moveToFirst()) {
            user = new User();
            user.setUserId(cursor.getString(0));
            user.setUsername(cursor.getString(1));
            user.setEmail(cursor.getString(2));
            user.setBio(cursor.getString(3));
            // Note: Not storing password in User object for security
        }

        cursor.close();
        db.close();
        return user;
    }

    public boolean updateUserProfile(String userId, String username, String bio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_USERNAME, username);
        values.put(KEY_BIO, bio);

        int rowsAffected = db.update(TABLE_USERS, values, KEY_USER_ID + " = ?",
                new String[]{userId});
        db.close();

        return rowsAffected > 0;
    }

    // ==================== POST OPERATIONS ====================

    public long addPost(Post post) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_POST_ID, post.getPostId());
        values.put(KEY_POST_USER_ID, post.getUserId());
        values.put(KEY_POST_USERNAME, post.getUsername());
        values.put(KEY_POST_EMAIL, post.getUserEmail());
        values.put(KEY_POST_USER_BIO, post.getUserBio());
        values.put(KEY_TITLE, post.getTitle());
        values.put(KEY_CONTENT, post.getContent());
        values.put(KEY_CATEGORY, post.getCategory());
        values.put(KEY_IMAGE_URL, post.getImageUrl());
        values.put(KEY_LIKES_COUNT, post.getLikesCount());
        values.put(KEY_COMMENTS_COUNT, post.getCommentsCount());
        values.put(KEY_POST_TIMESTAMP, post.getTimestamp());
        values.put(KEY_FORMATTED_DATE, post.getFormattedDate());
        values.put(KEY_LIKED_BY, ""); // Simplified for SQLite

        long result = db.insert(TABLE_POSTS, null, values);
        db.close();

        return result;
    }

    public List<Post> getAllPosts() {
        List<Post> postList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_POSTS + " ORDER BY " + KEY_POST_TIMESTAMP + " DESC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Post post = new Post();
                post.setPostId(cursor.getString(0));
                post.setUserId(cursor.getString(1));
                post.setUsername(cursor.getString(2));
                post.setUserEmail(cursor.getString(3));
                post.setUserBio(cursor.getString(4));
                post.setTitle(cursor.getString(5));
                post.setContent(cursor.getString(6));
                post.setCategory(cursor.getString(7));
                post.setImageUrl(cursor.getString(8));
                post.setLikesCount(cursor.getInt(9));
                post.setCommentsCount(cursor.getInt(10));
                post.setTimestamp(cursor.getLong(11));
                post.setFormattedDate(cursor.getString(12));

                postList.add(post);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return postList;
    }

    public boolean updatePostLikes(String postId, int likesCount, String likedBy) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_LIKES_COUNT, likesCount);
        values.put(KEY_LIKED_BY, likedBy);

        int rowsAffected = db.update(TABLE_POSTS, values, KEY_POST_ID + " = ?",
                new String[]{postId});
        db.close();

        return rowsAffected > 0;
    }

    public void deleteAllPosts() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_POSTS, null, null);
        db.close();
    }

    // ==================== DEMO DATA ====================

    private void insertDemoData(SQLiteDatabase db) {
        // Insert demo users
        ContentValues user1 = new ContentValues();
        user1.put(KEY_USER_ID, "local_demo1");
        user1.put(KEY_USERNAME, "John Doe");
        user1.put(KEY_EMAIL, "test@test.com");
        user1.put(KEY_PASSWORD, "password123");
        user1.put(KEY_BIO, "Computer Science Student");
        user1.put(KEY_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_USERS, null, user1);

        ContentValues user2 = new ContentValues();
        user2.put(KEY_USER_ID, "local_demo2");
        user2.put(KEY_USERNAME, "Jane Smith");
        user2.put(KEY_EMAIL, "jane@example.com");
        user2.put(KEY_PASSWORD, "password123");
        user2.put(KEY_BIO, "Physics Major");
        user2.put(KEY_TIMESTAMP, System.currentTimeMillis());
        db.insert(TABLE_USERS, null, user2);

        // Insert demo posts
        ContentValues post1 = new ContentValues();
        post1.put(KEY_POST_ID, "local_post1");
        post1.put(KEY_POST_USER_ID, "local_demo1");
        post1.put(KEY_POST_USERNAME, "John Doe");
        post1.put(KEY_POST_EMAIL, "test@test.com");
        post1.put(KEY_POST_USER_BIO, "Computer Science Student");
        post1.put(KEY_TITLE, "Welcome to CampusConnect!");
        post1.put(KEY_CONTENT, "This is our community platform. Share announcements, ask for help, or post events!");
        post1.put(KEY_CATEGORY, "Announcements");
        post1.put(KEY_IMAGE_URL, "");
        post1.put(KEY_LIKES_COUNT, 15);
        post1.put(KEY_COMMENTS_COUNT, 3);
        post1.put(KEY_POST_TIMESTAMP, System.currentTimeMillis() - 3600000);
        post1.put(KEY_FORMATTED_DATE, "1 hour ago");
        post1.put(KEY_LIKED_BY, "");
        db.insert(TABLE_POSTS, null, post1);

        ContentValues post2 = new ContentValues();
        post2.put(KEY_POST_ID, "local_post2");
        post2.put(KEY_POST_USER_ID, "local_demo2");
        post2.put(KEY_POST_USERNAME, "Jane Smith");
        post2.put(KEY_POST_EMAIL, "jane@example.com");
        post2.put(KEY_POST_USER_BIO, "Physics Major");
        post2.put(KEY_TITLE, "Need help with Calculus");
        post2.put(KEY_CONTENT, "Struggling with integrals. Study session anyone?");
        post2.put(KEY_CATEGORY, "Study Help");
        post2.put(KEY_IMAGE_URL, "");
        post2.put(KEY_LIKES_COUNT, 8);
        post2.put(KEY_COMMENTS_COUNT, 5);
        post2.put(KEY_POST_TIMESTAMP, System.currentTimeMillis() - 7200000);
        post2.put(KEY_FORMATTED_DATE, "2 hours ago");
        post2.put(KEY_LIKED_BY, "");
        db.insert(TABLE_POSTS, null, post2);
    }
}