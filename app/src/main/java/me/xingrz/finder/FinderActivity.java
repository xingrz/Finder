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

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;

import java.io.File;

public class FinderActivity extends EntriesActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private File current;

    @Override
    protected void onCreateInternal(Bundle savedInstanceState) {
        super.onCreateInternal(savedInstanceState);
        current = determineCurrentFile();
    }

    @Override
    protected void onDestroyInternal() {
        super.onDestroyInternal();
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected int provideContentView() {
        return R.layout.activity_finder;
    }

    @Override
    protected String getCurrentDisplayName() {
        return current.getName();
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        return new FilesAdapter(this, current.listFiles()) {
            @Override
            protected void openFolder(final File folder) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startFinder(Uri.fromFile(folder), FinderActivity.class);
                    }
                }, START_ACTIVITY_DELAY);
            }

            @Override
            protected void openFile(final File file) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        safelyStartViewActivity(file);
                    }
                }, START_ACTIVITY_DELAY);
            }
        };
    }

    private File determineCurrentFile() {
        if (getIntent().getData() != null) {
            return new File(getIntent().getData().getPath());
        } else {
            return Environment.getExternalStorageDirectory();
        }
    }

}
