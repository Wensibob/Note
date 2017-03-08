package bob.com.note;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import bob.com.note.bean.Note;
import bob.com.note.util.DateUtil;

/**
 * bob.com.note
 * Created by BOB on 2017/3/3.
 * 描述：便签内容页面，可以对便签进行简单的编辑，保存便签之后可以存储在数据库中
 * 博客园：http://www.cnblogs.com/ghylzwsb/
 * 个人网站：www.wensibo.top
 */

public class NoteContentActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private EditText mEtNoteContent;
    private ScrollView mScrollView;
    private Note mNote;
    private boolean isImportant = true;
    private Intent mIntent;
    private int mNoteID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_content);

        mEtNoteContent = (EditText) findViewById(R.id.et_note_content);

        mScrollView = (ScrollView) findViewById(R.id.scrollview_note_content);
        mIntent = this.getIntent();

        if ((mNote = (Note) mIntent.getSerializableExtra("note")) != null) {
            mNoteID = mNote.getId();
            mEtNoteContent.setText(mNote.getContent());
            mEtNoteContent.setSelection(mEtNoteContent.getText().length());
            isImportant = mNote.getInportant() == 1 ? true : false;
            Log.d("NoteContentActivity", mNote.getContent() + isImportant);
        }
        initToolbar(isImportant);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }


    /**
     * 初始化toolbar
     */
    private void initToolbar(boolean isImportant) {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_note_content);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_star).setChecked(isImportant);
        setItemIcon(menu.findItem(R.id.action_star), isImportant);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_star://星型按钮
                if (item.isChecked()) {
                    item.setChecked(false);
                    setItemIcon(item, false);
                    Snackbar.make(mScrollView,R.string.not_important,Snackbar.LENGTH_SHORT).show();
                } else {
                    item.setChecked(true);
                    setItemIcon(item, true);
                    Snackbar.make(mScrollView,R.string.important,Snackbar.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_save://保存按钮
                if (mNote != null) {//编辑之后的便签，需要保存
                    String content = mEtNoteContent.getText().toString();
                    Note editNote = new Note(mNote.getId(), content, isImportant ? 1 : 0,DateUtil.formatDateTime());
                    int result=NoteActivity.mNoteDbAdapter.updateNote(editNote);
                    Log.d("NoteContentActivity", "在数据库更新数据结果为：" + result);

                    finish();
                    break;
                } else {//新建便签
                    if (TextUtils.isEmpty(mEtNoteContent.getText().toString())) {
                        Toast.makeText(NoteContentActivity.this, R.string.not_empty, Toast.LENGTH_SHORT).show();
                        Snackbar.make(mScrollView,R.string.not_empty,Snackbar.LENGTH_SHORT).show();
                        break;
                    } else {
                        String content = mEtNoteContent.getText().toString();
                        long result=NoteActivity.mNoteDbAdapter.createNote(content,isImportant, DateUtil.formatDateTime());
                        Log.d("NoteContentActivity", "向数据库中插入数据结果为：" + result);
                        finish();
                        break;
                    }
                }


        }
        return  super.onOptionsItemSelected(item);
    }

    //设置星型的图片
    private void setItemIcon(MenuItem item, boolean isImportant) {
        item.setIcon(isImportant ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
        this.isImportant = isImportant;
    }


}
