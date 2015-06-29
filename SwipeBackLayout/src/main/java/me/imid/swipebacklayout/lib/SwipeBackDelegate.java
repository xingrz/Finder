package me.imid.swipebacklayout.lib;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * @author Yrom
 */
public class SwipeBackDelegate {
    private Activity mActivity;

    private SwipeBackLayout mSwipeBackLayout;

    public SwipeBackDelegate(Activity activity) {
        mActivity = activity;
    }

    public void onCreate() {
        Window window = mActivity.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.getDecorView().setBackground(null);

        mSwipeBackLayout = new SwipeBackLayout(mActivity);
        mSwipeBackLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mSwipeBackLayout.addSwipeListener(new SwipeBackLayout.SwipeListener() {
            @Override
            public void onScrollStateChange(int state, float scrollPercent) {
            }

            @Override
            public void onEdgeTouch(int edgeFlag) {
                SwipeBackActivityCompat.convertToTranslucent(mActivity);
            }

            @Override
            public void onScrollOverThreshold() {
            }
        });
    }

    public void onPostCreate() {
        mSwipeBackLayout.attachToActivity(mActivity);
    }

    public View findViewById(int id) {
        if (mSwipeBackLayout != null) {
            return mSwipeBackLayout.findViewById(id);
        }
        return null;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mSwipeBackLayout;
    }
}
