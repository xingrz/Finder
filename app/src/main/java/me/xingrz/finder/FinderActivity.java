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
import android.support.v7.widget.RecyclerView;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class FinderActivity extends BaseActivity {

    private File current;

    @Override
    protected void onCreateInternal(Bundle savedInstanceState) {
        super.onCreateInternal(savedInstanceState);
        current = determineCurrentFile();
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
            protected void openFolder(File folder) {
                startFinder(Uri.fromFile(folder), FinderActivity.class);
            }

            @Override
            protected void openFile(File file) {
                String extension = FilenameUtils.getExtension(file.getName());
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                safelyStartViewActivity(Uri.fromFile(file), mime);
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
