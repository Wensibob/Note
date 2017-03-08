package bob.com.note;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import bob.com.note.adapter.NoteAdapter;
import bob.com.note.bean.Note;
import bob.com.note.adapter.NoteDbAdapter;
import bob.com.note.listener.HidingScrollListener;
import bob.com.note.util.DateUtil;
import bob.com.note.util.SharedPreferencesUtil;

/**
 * bob.com.note
 * Created by BOB on 2017/3/1.
 * 描述：MainActivity ，当用户首次安装app的时候会自动创建一些示例便签，用户点击每条便签可以来到 NoteContentActivity
 * 博客园：http://www.cnblogs.com/ghylzwsb/
 * 个人网站：www.wensibo.top
 */

public class NoteActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private FloatingActionButton mFloatingActionButton;
    public static NoteDbAdapter mNoteDbAdapter;
    public static NoteAdapter mNoteAdapter;
    private Cursor mCursor;
    private static final String TAG = "NoteActivity";
    private boolean isFirstStart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initToolbar();
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.button_add_note);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(NoteActivity.this, NoteContentActivity.class));
            }
        });
        mNoteDbAdapter = new NoteDbAdapter(this);
        mNoteDbAdapter.open();

        SharedPreferencesUtil shared=new SharedPreferencesUtil(NoteActivity.this,NoteDbAdapter.CONFIG);
        isFirstStart=shared.getBoolean(NoteDbAdapter.IS_FIRST_START);
        if(!isFirstStart){
            //如果是首次安装app，则需要创建数据库
            mNoteDbAdapter.deleteAllNotes();//首先先清除数据库
            insertSomeReminders();//加入一些示例便签
            shared.putBoolean(NoteDbAdapter.IS_FIRST_START,true);//最后还需要将boolean设置为true
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecycleView();
        Log.d(TAG, "onResume()");
    }

    /**
     * 初始化toolbar
     */
    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(R.string.app_name);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
    }

    /**
     * 初始化recycleview
     */
    private void initRecycleView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycle_notes);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCursor = mNoteDbAdapter.fetchAllNotes();
        mNoteAdapter = new NoteAdapter(this, mCursor, 0);
        Log.d(TAG, "mCursor的大小为：" + mCursor.getCount());

        //设置点击事件
        mNoteAdapter.setRecyclerViewOnItemClickListener(new NoteAdapter.RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                if (mCursor == null || mCursor.isClosed()) {
                    if (mCursor == null) {
                        Log.d("NoteActivity", "newCursor is null");
                        Toast.makeText(NoteActivity.this, "newCursor is null", Toast.LENGTH_SHORT).show();
                    } else if (mCursor.isClosed()){
                        Log.d("NoteActivity", "newCursor is closed");
                        Toast.makeText(NoteActivity.this, "newCursor is null", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mCursor.moveToPosition(position);
                    String content = mCursor.getString(mCursor.getColumnIndex(NoteDbAdapter.COL_CONTENT));
                    int importtant = mCursor.getInt(mCursor.getColumnIndex(NoteDbAdapter.COL_IMPORTANT));
                    int id = mCursor.getInt(mCursor.getColumnIndex(NoteDbAdapter.COL_ID));
                    Log.d("NoteActivity", content + importtant);
                    Note clickNote = new Note(id, content, importtant);
                    Intent intent = new Intent();
                    intent.setClass(NoteActivity.this, NoteContentActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("note", clickNote);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

            }
        });

        //设置侧滑事件
        mNoteAdapter.setOnSwipeListener(new NoteAdapter.onSwipeListener() {
            @Override
            public void onDel(int pos) {
                Toast.makeText(NoteActivity.this, "点击了第" + (pos+1) + "条item的删除按钮", Toast.LENGTH_SHORT).show();
                mCursor.moveToPosition(pos);
                int id = mCursor.getInt(mCursor.getColumnIndex(NoteDbAdapter.COL_ID));
                mNoteDbAdapter.deleteNoteById(id);
                mCursor = mNoteDbAdapter.fetchAllNotes();
                mNoteAdapter.changeCursor(mCursor);
            }

            @Override
            public void onTop(int pos) {
                Toast.makeText(NoteActivity.this, "点击了第" + (pos+1) + "条item的Top按钮", Toast.LENGTH_SHORT).show();
                mCursor.moveToPosition(pos);
                int id = mCursor.getInt(mCursor.getColumnIndex(NoteDbAdapter.COL_ID));
                Note editNote = mNoteDbAdapter.fetchNoteById(id);
                editNote.setDateTime(DateUtil.formatDateTime());
                mNoteDbAdapter.updateNote(editNote);
                mCursor = mNoteDbAdapter.fetchAllNotes();
                mNoteAdapter.changeCursor(mCursor);
            }
        });


        //设置适配器
        mRecyclerView.setAdapter(mNoteAdapter);
        //为recycleview设置滚动监听器
        mRecyclerView.setOnScrollListener(new HidingScrollListener(mCursor.getCount()) {
            @Override
            public void onHide() {
                hideView();
            }

            @Override
            public void onShow() {
                showView();
            }
        });

    // 可以用在：当点击外部空白处时，关闭正在展开的侧滑菜单。我个人觉得意义不大，
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                    if (null != viewCache) {
                        viewCache.smoothClose();
                    }
                }
                return false;
            }
        });
    }


    /**
     * 初始化数据库，向数据库插入一些数据
     */
    private void insertSomeReminders() {
        mNoteDbAdapter.createNote("Buy Learn Android Studio", true,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Send Dad birthday gift", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Dinner at the Gage on Friday", true,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("String squash racket", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Shovel and salt walkways", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Prepare Advanced Android syllabus", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Buy new office chair", true,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Call Auto-body shop for quote", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Renew membership to club", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Buy new Galaxy Android phone", true,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Sell old Android phone - auction", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Buy new paddles for kayaks", true,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Call accountant about tax returns", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Buy 300,000 shares of Google", false,DateUtil.formatDateTime());
        mNoteDbAdapter.createNote("Call the Dalai Lama back", true,DateUtil.formatDateTime());
    }

    private void hideView() {
        mToolbar.animate().translationY(
                -mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));
        FrameLayout.LayoutParams ip = (FrameLayout.LayoutParams) mFloatingActionButton.getLayoutParams();
        int fabButtonMargin = ip.bottomMargin;
        mFloatingActionButton.animate().translationY(
                mFloatingActionButton.getHeight() + fabButtonMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    private void showView() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
        mFloatingActionButton.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }
}
