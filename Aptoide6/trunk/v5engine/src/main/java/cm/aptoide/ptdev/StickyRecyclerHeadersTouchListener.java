package cm.aptoide.ptdev;

/**
 * Created by rmateus on 09-12-2014.
 */

import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

public class StickyRecyclerHeadersTouchListener implements RecyclerView.OnItemTouchListener {
    private final GestureDetector mTapDetector;
    private final RecyclerView mRecyclerView;
    private final StickyRecyclerHeadersDecoration mDecor;
    private OnHeaderClickListener mOnHeaderClickListener;

    public interface OnHeaderClickListener {
        public void onHeaderClick(View header, int position, long headerId);
    }

    public StickyRecyclerHeadersTouchListener(final RecyclerView recyclerView,
                                              final StickyRecyclerHeadersDecoration decor) {
        mTapDetector = new GestureDetector(recyclerView.getContext(), new SingleTapDetector());
        mRecyclerView = recyclerView;
        mDecor = decor;
    }

    public StickyRecyclerHeadersAdapter getAdapter() {
        if (mRecyclerView.getAdapter() instanceof StickyRecyclerHeadersAdapter) {
            return (StickyRecyclerHeadersAdapter) mRecyclerView.getAdapter();
        } else {
            throw new IllegalStateException("A RecyclerView with " +
                    StickyRecyclerHeadersTouchListener.class.getSimpleName() +
                    " requires a " + StickyRecyclerHeadersAdapter.class.getSimpleName());
        }
    }


    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {

        return mTapDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(RecyclerView view, MotionEvent e) { /* do nothing? */ }

    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            int position = mDecor.findHeaderPositionUnder((int) e.getX(), (int) e.getY());
            if (position != -1) {
                View headerView = mDecor.getHeaderView(mRecyclerView, position);
                long headerId = getAdapter().getHeaderId(position);
                mOnHeaderClickListener.onHeaderClick(headerView, position, headerId);
                return true;
            }
            return false;
        }
    }
}

