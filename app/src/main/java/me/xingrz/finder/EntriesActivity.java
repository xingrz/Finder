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
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import me.imid.swipebacklayout.lib.SwipeBackActivity;

public abstract class EntriesActivity extends SwipeBackActivity {

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

    @Override
    protected final void onDestroy() {
        onDestroyInternal();
        super.onDestroy();
    }

    protected void onDestroyInternal() {
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

    public void startFinder(Uri uri, Class<? extends EntriesActivity> activity) {
        startFinder(new Intent(null, uri), activity);
    }

    public void startFinder(Intent intent, Class<? extends EntriesActivity> activity) {
        intent.setClass(this, activity);
        intent.putExtra(EXTRA_ALLOW_BACK, true);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, 0);
    }

    public void safelyStartViewActivity(File file) {
        safelyStartViewActivity(Uri.fromFile(file), mimeOfFile(file));
    }

    public void safelyStartViewActivity(Uri uri, String mime) {
        Intent intent = intentToView(uri, mime);

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.d(TAG, "Failed to start viewer activity for uri " + uri.toString(), e);
            Toast.makeText(this, "没有打开该文件的应用", Toast.LENGTH_SHORT).show();
        }

        overridePendingTransitionForBuiltInViewer(intent);
    }

    protected String mimeOfFile(File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    protected Intent intentToView(Uri uri, String mime) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addCategory(Intent.CATEGORY_DEFAULT);

        for (ResolveInfo resolved : getPackageManager().queryIntentActivities(intent, 0)) {
            if (BuildConfig.APPLICATION_ID.equals(resolved.activityInfo.packageName)) {
                intent.setClassName(this, resolved.activityInfo.name);
                intent.putExtra(EXTRA_ALLOW_BACK, true);
            }
        }

        return intent;
    }

    protected void overridePendingTransitionForBuiltInViewer(Intent intent) {
        if (intent.getComponent() != null &&
                BuildConfig.APPLICATION_ID.equals(intent.getComponent().getPackageName())) {
            overridePendingTransition(R.anim.slide_in, 0);
        }
    }

}
