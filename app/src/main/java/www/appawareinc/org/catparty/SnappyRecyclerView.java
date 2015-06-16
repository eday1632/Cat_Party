package www.appawareinc.org.catparty;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/* this class represents the customized recycler view we use in both the main party and VIP room.
 * We customized it by overriding the "fling" method that controls what happens to the frame when
 * the user "flings" it with her finger. The customized fling method only allows one new frame to
  * come into view when the old one is flung out. The new view will also be centered on the screen*/
public class SnappyRecyclerView extends RecyclerView {

    /*constructor that initializes screenDimensions and gets context*/
    public SnappyRecyclerView(Context context) {
        super(context);
    }

    /*constructor that initializes screenDimensions, gets context, and passes in attributes*/
    public SnappyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*constructor that initializes screenDimensions, gets context, passes in attributes, and defStyle
    * not sure what defStyle is*/
    public SnappyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /*important override: makes it so only one new gif can be swiped into the screen at a time.
    * also makes a new gif take the center position on the screen*/
    @Override
    public boolean fling(int velocityX, int velocityY) {
        super.fling(velocityX, velocityY);

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();

        int lastVisibleView = linearLayoutManager.findLastVisibleItemPosition();
        int firstVisibleView = linearLayoutManager.findFirstVisibleItemPosition();
        View firstView = linearLayoutManager.findViewByPosition(firstVisibleView);
        View lastView = linearLayoutManager.findViewByPosition(lastVisibleView);

        if(lastVisibleView > -1 && firstView != null && lastView != null) {
            int leftMargin = Math.round((TwoRooms.screenWidthDp * TwoRooms.densityMultiple - lastView.getWidth()) / 2);
            int rightMargin = Math.round((TwoRooms.screenWidthDp * TwoRooms.densityMultiple - firstView.getWidth()) / 2 + firstView.getWidth());
            int leftEdge = lastView.getLeft();
            int rightEdge = firstView.getRight();
            int scrollDistanceLeft = leftEdge - leftMargin;
            int scrollDistanceRight = rightMargin - rightEdge;

            if (velocityX > 0) smoothScrollBy(scrollDistanceLeft, 0);
            else smoothScrollBy(-scrollDistanceRight, 0);
        }
        return true;
    }
}
