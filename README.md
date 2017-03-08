# Note(小便签)

**欢迎访问我的[个人博客](http://www.wensibo.top)**

一直想要写一个便签应用，因为我一直在用的是锤子便签和一加便签，觉得体验还是可以的，但是始终觉得自己也是可以做的，这段时间因为有些事情耽误了，项目前几天做好了，一直没有时间上传到[github](https://github.com/Wensibob/Note) (各位大爷路过给个star呗😉) ，今天趁着有时间顺便写了这篇文章，介绍一下写这个便签时遇到的一些问题，当作是跟大家一起分享吧！


## 功能
* 实现最基本的增加、删除、修改便签
* 便签能够保存到本地
* 主界面采用Material Design设计风格，相对美观(勿喷)
* Recycle View上下滑动可以自动隐藏Toolbar,以及Floating Action Button
* Recycle View的Item可以实现如QQ的侧滑效果，可以通过点击删除、置顶进行编辑
* 可以设置便签为星✨，那么将会在便签界面左边增加一个红色的标志，以提醒用户此便签为重要便签
* 变迁主界面标有时间，并且按照编辑时间从新到旧进行排列

## 效果
![效果图](/screenshot/1.gif)

**Talk is cheap,show me your code**

## 如何在RecycleView中使用本地Sqlite数据库数据
RecycleView是google在推出Material Design时着重介绍的一个组件，它对传统的ListView已经可以说是完全代替了，功能强大是他的一个最大优点，但是有一点局限的就是我们自定义的RecycleView必须继承重写 RecyclerView.Adapter 和 RecyclerView.ViewHolder，虽然在里面我们可以随意重写方法，但是可以发现如果我们使用数据库作为数据源，RecyclerView.Adapter是无法支持读取Cursor的，但是开源的力量又再次显现了，直接给上[github地址](https://github.com/flzyup/ExRecyclerViewLibrary)，但是我们这里只需要复用其中的两个文件就行了，容我娓娓道来。

**1、添加下面的RecyclerViewCursorAdapter 和 CursorFilter到工程中**
由于代码太长影响排版，我就直接附上下载链接
[RecyclerViewCursorAdapter](http://www.wensibo.top/file/img/2017-3-8Note/RecyclerViewCursorAdapter.java)
[CursorFilter](http://www.wensibo.top/file/img/2017-3-8Note/CursorFilter.java)

**2、新建自定义的Adapter并且继承RecyclerViewCursorAdapter**    
* NoteAdapter     

```java
public class NoteAdapter extends RecyclerViewCursorAdapter<NoteAdapter.MyNoteViewHolder> {

    private Context mContext;
    private RecyclerViewOnItemClickListener mOnItemClickListener;
    private onSwipeListener mOnSwipeListener;


    public NoteAdapter(Context context,Cursor cursor,int flags) {
        super(context,cursor,flags);
        this.mContext = context;
    }

    @Override
    public MyNoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_row, parent, false);
        MyNoteViewHolder holder = new MyNoteViewHolder(root);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyNoteViewHolder holder, Cursor cursor) {
        int position = cursor.getPosition();
        holder.tv.setText(cursor.getString(cursor.getColumnIndex(NoteDbAdapter.COL_CONTENT)));
        holder.tv_dateTime.setText(cursor.getString(cursor.getColumnIndex(NoteDbAdapter.COL_DATETIME)));
        holder.mRowtab.setBackgroundColor(cursor.getInt(cursor.getColumnIndex(NoteDbAdapter.COL_IMPORTANT)) == 1?
                mContext.getResources().getColor(R.color.colorAccent):mContext.getResources().getColor(android.R.color.white)
        );
        holder.root.setTag(position);

        holder.tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClickListener(view, holder.getAdapterPosition());
                }
            }
        });


    }

    @Override
    protected void onContentChanged() {

    }

    /** 设置点击事件 */
    public void setRecyclerViewOnItemClickListener(RecyclerViewOnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public RecyclerViewOnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /** 点击事件接口 */
    public interface RecyclerViewOnItemClickListener {
        void onItemClickListener(View view, int position);
    }


    /**
     * 内部类Holder
     */
    class MyNoteViewHolder extends RecyclerView.ViewHolder {
        private TextView tv;
        private TextView tv_dateTime;
        private View mRowtab;
        private Button btnTop;
        private Button btnDelete;
        private View root;

        public MyNoteViewHolder(View root) {
            super(root);
            this.root = root;
            tv = (TextView) root.findViewById(R.id.row_text);
            tv_dateTime = (TextView) root.findViewById(R.id.tv_note_time);
            mRowtab = root.findViewById(R.id.row_tab);
            btnTop = (Button) root.findViewById(R.id.btnTop);
            btnDelete = (Button) root.findViewById(R.id.btnDelete);
        }
    }

}
```
**3、在Activity中这样用**
```java
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

        //设置适配器
        mRecyclerView.setAdapter(mNoteAdapter);
```

## 如何在RecycleView上下滑动时隐藏Toolbar和FAB按钮
思路很简单，只需要记录RecycleView向下滑动(手指向上滑动)时移动的距离，超过一定范围时就会调用Toolbar以及Floating Action Button的animate().translationY方法，令其在Y轴方向上移动，当RecycleView向上滑动(手指向下滑动)时又会反过来回到初始状态，并且当滑动到RecycleView底部时会强制Toolbar和FAB回到初始状态，上代码。     
* HidingScrollListener

```java
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {

    private static final int HIDE_THRESHOLD = 20;
    private int scrolledDistance = 0;
    private boolean controlsVisible = true;
    private int mItemSize=0;

    public HidingScrollListener(int itemSize) {
        this.mItemSize = itemSize - 1;
    }

    /**
     *
     * @param recyclerView
     * @param dx 横向的滚动距离
     * @param dy 纵向的滚动距离
     *           记录的是两个滚动事件之间的偏移量，而不是总的滚动距离。
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        int firstVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        int lastVisibleItem = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

        if (firstVisibleItem == 0||lastVisibleItem==mItemSize) {
            if (!controlsVisible) {
                onShow();
                controlsVisible = true;
            }
        }else{
            //如果总的滚动距离超多了一定值
            // （这个值取决于你自己的设定，越大，需要滑动的距离越长才能显示或者隐藏），
            // 我们就根据其方向显示或者隐藏Toolbar（dy>0意味着下滚，dy<0意味着上滚）。
            if (scrolledDistance > HIDE_THRESHOLD && controlsVisible) {
                onHide();
                controlsVisible = false;
                scrolledDistance = 0;
            } else if (scrolledDistance < -HIDE_THRESHOLD && !controlsVisible) {
                onShow();
                scrolledDistance = 0;
                controlsVisible = true;
            }
        }
        //计算出滚动的总距离（deltas相加），
        // 但是只在Toolbar隐藏且上滚或者Toolbar未隐藏且下滚的时候
        if ((controlsVisible && dy > 0) || (!controlsVisible && dy < 0)) {
            scrolledDistance += dy;
        }
    }

    public abstract void onHide();
    public abstract void onShow();
}
```
在Avtivity中使用回掉方法
```java
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
```
**特别注意布局文件**
如果你发现你运行的效果像下面的截图一样的话，那你肯定是因为布局文件上少写了这两句
```
android:clipToPadding="false"
android:paddingTop="?attr/actionBarSize"
```
![bug截图](/screenshot/2.png)    
完整的布局代码如下：   
* main_activity.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
             xmlns:app="http://schemas.android.com/apk/res-auto"
              android:layout_width="match_parent"
              android:layout_height="match_parent">

    <android.support.v7.widget.RecyclerView
        android:id="@+id/recycle_notes"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:clipToPadding="false"
        android:paddingTop="?attr/actionBarSize"
        />

    <android.support.v7.widget.Toolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="?attr/colorPrimary"
        android:clipToPadding="false"
        app:titleTextColor="@android:color/white"
        />

    <android.support.design.widget.FloatingActionButton
        android:id="@+id/button_add_note"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="bottom|right"
        android:layout_marginBottom="16dp"
        android:layout_marginRight="16dp"
        android:src="@drawable/ic_action_new"
        android:elevation="15dp"
        app:fabSize="normal"
        app:pressedTranslationZ="8dp"
        app:rippleColor="#ff87eb"
        />

</FrameLayout>
```
## 最后来讲讲如何实现仿QQ的侧滑出现删除、指定操作
首先得谢谢[张旭童](http://blog.csdn.net/zxt0601/article/details/53157090) ,他的一个库帮我解决了这个问题，[点击这里](https://github.com/mcxtzhang/SwipeDelMenuLayout)可以访问他的项目。    
**1、在布局文件中使用`com.mcxtzhang.swipemenulib.SwipeMenuLayout`布局，在ItemView后添加button表示删除置顶按钮。**    
**2、在Adapter中设置打开侧滑菜单，并且可以设置菜单在左还是在右**      
```java
((SwipeMenuLayout) holder.root.findViewById(R.id.swipeMenuLayout)).setIos(false).setLeftSwipe(false).setSwipeEnable(true);
```
**3、在Activity中设置动作事件**
```java
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
```

**大功告成，如果想要看详细代码，或者有什么建议可以到[Github](https://github.com/Wensibob/Note)上给我发Issue或者直接在站内给我留言哦，记得star哦**

