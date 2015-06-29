package me.imid.swipebacklayout.lib;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SwipeBackActivity extends AppCompatActivity {

    private SwipeBackDelegate mDelegate = new SwipeBackDelegate(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDelegate.onCreate();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDelegate.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mDelegate != null)
            return mDelegate.findViewById(id);
        return v;
    }

    public SwipeBackLayout getSwipeBackLayout() {
        return mDelegate.getSwipeBackLayout();
    }

    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }

    public void scrollToFinishActivity() {
        SwipeBackActivityCompat.convertToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }

}
