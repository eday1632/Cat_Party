package www.appawareinc.org.catparty;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/* this class represents the customized recycler view we use in both the main party and VIP room.
 * We customized it by overriding the "fling" method that controls what happens to the frame when
 * the user "flings" it with her finger. The customized fling method only allows one new frame to
  * come into view when the old one is flung out. The new view will also be centered on the screen*/
public class SnappyRecyclerView extends RecyclerView {

    public static LinearLayoutManager layoutManager;
    public static int lastVisibleViewPosition;
    public static int firstVisibleViewPosition;
    public static ViewHolderAdapter.SimpleViewHolder firstView;
    public static ViewHolderAdapter.SimpleViewHolder lastView;

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

        try {
            layoutManager = (LinearLayoutManager) getLayoutManager();
            lastVisibleViewPosition = layoutManager.findLastVisibleItemPosition();
            firstVisibleViewPosition = layoutManager.findFirstVisibleItemPosition();
            firstView = (ViewHolderAdapter.SimpleViewHolder) findViewHolderForPosition(firstVisibleViewPosition);
            lastView = (ViewHolderAdapter.SimpleViewHolder) findViewHolderForPosition(lastVisibleViewPosition);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if(lastVisibleViewPosition > -1 && firstView != null && lastView != null) {
            int scrollDistanceLeft = lastView.getLeft() -
                    Math.round((TwoRooms.screenWidthDp - lastView.width) / 2 * TwoRooms.densityMultiple);
            int scrollDistanceRight = Math.round(((TwoRooms.screenWidthDp - firstView.width) / 2 + firstView.width)
                    * TwoRooms.densityMultiple) - firstView.getRight();

            if (velocityX > 0) smoothScrollBy(scrollDistanceLeft, 0);
            else smoothScrollBy(-scrollDistanceRight, 0);
        }
        Log.d("xkcd RecyclerView", "Flung!");
        return true;
    }
}
