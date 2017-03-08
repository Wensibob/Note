package bob.com.note.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import bob.com.note.bean.Note;

/**
 * bob.com.note.database
 * Created by BOB on 2017/3/1.
 * 描述：数据库的操作类，用于对数据的增删改查操作
 * 博客园：http://www.cnblogs.com/ghylzwsb/
 * 个人网站：www.wensibo.top
 */

public class NoteDbAdapter {

    //判断是否首次打开app
    public static final String CONFIG = "config";//存放用户信息
    public static final String IS_FIRST_START="is_first_start";//app是否首次安装

    //数据库表的列名
    public static final String COL_ID = "_id";
    public static final String COL_CONTENT = "content";
    public static final String COL_IMPORTANT = "important";
    public static final String COL_DATETIME = "last_motidied_time";
    //相关的索引
    public static final int INDEX_ID = 0;
    public static final int INDEX_CONTENT = INDEX_ID + 1;
    public static final int INDEX_IMPORTANT = INDEX_ID + 2;
    public static final int INDEX_DATETIME = INDEX_ID + 3;
    //数据库名称，表名称，数据库版本
    private static final String DATABASE_NAME = "dba_note";
    private static final String TABLE_NAME = "tb1_note";
    private static final int DATABASE_VERSION = 1;

    //打印日志
    private static final String TAG = "NoteBdAdapter";
    private DataBaseHelper mDataBaseHelper;
    private SQLiteDatabase mDb;
    private final Context mContext;

    //创建数据库表的语句
    private static final String DATABASE_CREATE =
            "CREATE TABLE if not exists " + TABLE_NAME + " ( " +
                    COL_ID + " INTEGER PRIMARY KEY autoincrement, " +
                    COL_CONTENT + " TEXT," +
                    COL_IMPORTANT + " INTEGER ," +
                    COL_DATETIME + " TEXT "+" );";
    private static final String UPGRADING_DATABASE = "DROP TABLE IF EXISTS " + TABLE_NAME;


    public NoteDbAdapter(Context context) {
        mContext = context;
    }

    /**
     * open database
     * @throws SQLException
     */
    public void open() throws SQLException {
        mDataBaseHelper = new DataBaseHelper(mContext);
        mDb = mDataBaseHelper.getWritableDatabase();
    }

    /**
     * close database
     */
    public void close(){
        if (mDataBaseHelper != null) {
            mDataBaseHelper.close();
        }
    }

    //创建便签
    public long createNote(String name, boolean important,String dateTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CONTENT, name);
        contentValues.put(COL_IMPORTANT, important ? 1 : 0);
        contentValues.put(COL_DATETIME,dateTime);
        return mDb.insert(TABLE_NAME, null, contentValues);
    }

    //创建便签的重载
    public long createNote(Note note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CONTENT, note.getContent());
        contentValues.put(COL_IMPORTANT, note.getInportant());
        contentValues.put(COL_DATETIME,note.getDateTime());
        return mDb.insert(TABLE_NAME, null, contentValues);
    }

    //根据ID取出便签
    public Note fetchNoteById(int id) {
        Cursor cursor=mDb.query(TABLE_NAME,new String[]{COL_ID,COL_CONTENT,COL_IMPORTANT,COL_DATETIME},
                COL_ID+"=?",new String[]{String.valueOf(id)},null,null,null,null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return new Note(
                cursor.getInt(INDEX_ID),
                cursor.getString(INDEX_CONTENT),
                cursor.getInt(INDEX_IMPORTANT),
                cursor.getString(INDEX_DATETIME));
    }

    //取出全部便签
    public Cursor fetchAllNotes() {
        Cursor mCursor = mDb.query(TABLE_NAME, new String[]{COL_ID, COL_CONTENT, COL_IMPORTANT,COL_DATETIME},
                null, null, null, null,"last_motidied_time desc");
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    //根据便签对象更新便签
    public int updateNote(Note note) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_CONTENT, note.getContent());
        contentValues.put(COL_IMPORTANT, note.getInportant());
        contentValues.put(COL_DATETIME,note.getDateTime());
        return mDb.update(TABLE_NAME, contentValues,
                COL_ID + "=?", new String[]{String.valueOf(note.getId())});
    }

    //通过便签ID删除
    public void deleteNoteById(int id) {
        mDb.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    //删除所有便签
    public void deleteAllNotes() {
        mDb.delete(TABLE_NAME, null, null);
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            Log.w(TAG, DATABASE_CREATE);
            database.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
            database.execSQL(UPGRADING_DATABASE);
            onCreate(database);
        }
    }

}
