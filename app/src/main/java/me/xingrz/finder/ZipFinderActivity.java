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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class ZipFinderActivity extends EntriesActivity {

    public static final String EXTRA_PREFIX = "prefix";

    private static final String TAG = "ZipFinderActivity";

    private File file;
    private ZipFile zipFile;

    @Override
    protected void onCreateInternal(Bundle savedInstanceState) {
        super.onCreateInternal(savedInstanceState);

        file = new File(getIntent().getData().getPath());

        try {
            zipFile = new ZipFile(file);
        } catch (ZipException e) {
            Log.d(TAG, "failed to open zip file " + file.getAbsolutePath(), e);
        }

        if (getIntent().hasExtra(EXTRA_PREFIX)) {
            toolbar.setTitle(FilenameUtils.getName(getIntent().getStringExtra(EXTRA_PREFIX)));
            toolbar.setSubtitle(file.getName());
        }
    }

    @Override
    protected int provideContentView() {
        return R.layout.activity_finder;
    }

    @Override
    protected String getCurrentDisplayName() {
        return file.getName();
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        List<FileHeader> headers;

        try {
            //noinspection unchecked
            headers = zipFile.getFileHeaders();
        } catch (ZipException e) {
            return null;
        }

        return new ZipAdapter(this, headers, getIntent().getStringExtra(EXTRA_PREFIX)) {
            @Override
            protected void openFolder(AbstractFile folder) {
                Intent intent = new Intent();
                intent.setData(getIntent().getData());
                intent.putExtra(EXTRA_PREFIX, folder.path);
                startFinder(intent, ZipFinderActivity.class);
            }

            @Override
            protected void openFile(AbstractFile file) {
            }
        };
    }

}
