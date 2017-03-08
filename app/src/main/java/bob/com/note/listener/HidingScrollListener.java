package bob.com.note.listener;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * bob.com.note.listener
 * Created by BOB on 2017/3/1.
 * 描述：用于实现主界面上下滑动recycle View的时候Toolbar以及FAB的隐藏效果
 * 博客园：http://www.cnblogs.com/ghylzwsb/
 * 个人网站：www.wensibo.top
 */

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
