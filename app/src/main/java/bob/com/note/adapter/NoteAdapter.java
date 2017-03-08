package bob.com.note.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import bob.com.note.R;
import bob.com.note.util.RecyclerViewCursorAdapter;

/**
 * bob.com.note.adapter
 * Created by BOB on 2017/3/1.
 * 描述：Recycle View的适配器，由于用到了Cursor，所以这里只能是继承RecyclerViewCursorAdapter，
 * 同时这里还适配了recycle View的item的侧滑事件，监听删除、置顶操作
 *
 * 博客园：http://www.cnblogs.com/ghylzwsb/
 * 个人网站：www.wensibo.top
 */

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
        ((SwipeMenuLayout) holder.root.findViewById(R.id.swipeMenuLayout)).setIos(false).setLeftSwipe(false).setSwipeEnable(true);

        holder.btnTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnSwipeListener != null) {
                    mOnSwipeListener.onTop(holder.getAdapterPosition());
                }
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnSwipeListener != null) {
                    mOnSwipeListener.onDel(holder.getAdapterPosition());
                }
            }
        });

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


    /** 获取侧滑时间监听器*/
    public onSwipeListener getOnSwipeListener() {
        return mOnSwipeListener;
    }

    /** 设置侧滑时间监听器*/
    public void setOnSwipeListener(onSwipeListener mOnSwipeListener) {
        this.mOnSwipeListener = mOnSwipeListener;
    }

    /** 点击事件接口 */
    public interface RecyclerViewOnItemClickListener {
        void onItemClickListener(View view, int position);
    }

    /** 侧滑事件接口 */
    public interface onSwipeListener {
        void onDel(int pos);
        void onTop(int pos);
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
