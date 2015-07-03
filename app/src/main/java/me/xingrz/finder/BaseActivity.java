/*
 * Copyright 2015 XiNGRZ <chenxingyu92@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.xingrz.finder;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.SwipeBackActivity;

public abstract class BaseActivity extends SwipeBackActivity {

    public static final String EXTRA_ALLOW_BACK = "allow_back";

    private static final String TAG = "BaseActivity";

    @Bind(R.id.toolbar)
    protected Toolbar toolbar;

    @Bind(R.id.files)
    protected RecyclerView files;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(provideContentView());
        ButterKnife.bind(this);

        onCreateInternal(savedInstanceState);

        setSupportActionBar(toolbar);
        setTitle(getCurrentDisplayName());

        boolean allowBack = getIntent().getBooleanExtra(EXTRA_ALLOW_BACK, false);

        setSwipeBackEnable(allowBack);

        if (allowBack) {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    supportFinishAfterTransition();
                }
            });
        }

        files.setLayoutManager(new LinearLayoutManager(this));
        files.setAdapter(getAdapter());
    }

    protected void onCreateInternal(Bundle savedInstanceState) {
    }

    @LayoutRes
    protected abstract int provideContentView();

    protected abstract String getCurrentDisplayName();

    protected abstract RecyclerView.Adapter getAdapter();

    @Override
    public void supportFinishAfterTransition() {
        super.supportFinishAfterTransition();
        if (getIntent().getBooleanExtra(EXTRA_ALLOW_BACK, false)) {
            overridePendingTransition(0, R.anim.slide_out);
        }
    }

    public void startFinder(Uri uri, Class<? extends BaseActivity> activity) {
        Intent intent = new Intent(this, activity);
        intent.setData(uri);
        intent.putExtra(EXTRA_ALLOW_BACK, true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, 0);
    }

    public void safelyStartViewActivity(Uri uri, String type) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, type);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "Failed to start viewer activity for uri " + uri.toString(), e);
        }
    }

}
