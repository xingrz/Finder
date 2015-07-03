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

import android.content.Context;

import com.google.common.collect.Lists;

import java.io.File;

public abstract class FilesAdapter extends EntriesAdapter<File> {

    public FilesAdapter(Context context, File[] entries) {
        super(context, Lists.newArrayList(entries));
    }

    @Override
    protected boolean isFolder(File entry) {
        return entry.isDirectory();
    }

    @Override
    protected int getIcon(File entry) {
        if (entry.isDirectory()) {
            return R.drawable.ic_folder_white_24dp;
        } else {
            return R.drawable.ic_receipt_white_24dp;
        }
    }

    @Override
    protected String getName(File entry) {
        return entry.getName();
    }

    @Override
    protected String getDescription(File entry) {
        if (entry.isDirectory()) {
            return String.format("包含 %d 个文件", entry.list().length);
        } else {
            return String.format("%.2f MB", (float) entry.length() / 1024 / 1024);
        }
    }

    @Override
    protected int compare(File lhs, File rhs) {
        return lhs.compareTo(rhs);
    }

}
