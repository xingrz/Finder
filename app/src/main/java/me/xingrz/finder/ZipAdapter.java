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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.common.collect.Lists;

import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class ZipAdapter extends EntriesAdapter<ZipAdapter.AbstractFile> {

    public ZipAdapter(Context context, List<FileHeader> headers, String prefix) {
        super(context, abstractFiles(headers, prefix));
    }

    private static List<AbstractFile> abstractFiles(List<FileHeader> headers, String prefix) {
        HashMap<String, AbstractFile> files = new HashMap<>();

        for (FileHeader header : headers) {
            String path = header.getFileName();

            int clipping = 0;

            if (!TextUtils.isEmpty(prefix)) {
                if (path.startsWith(prefix + File.separator)) {
                    clipping = prefix.length() + File.separator.length();
                } else {
                    continue;
                }
            }

            String name = path.substring(clipping);

            if (name.isEmpty()) {
                continue;
            }

            boolean isDirectory = name.contains(File.separator);

            if (isDirectory) {
                int index = name.indexOf(File.separator);
                name = name.substring(0, index);
                path = path.substring(0, clipping + index);
            }

            AbstractFile file = new AbstractFile(name, path, isDirectory, isDirectory ? null : header);

            if (files.containsKey(path)) {
                files.get(path).files++;
            } else {
                files.put(path, file);
            }
        }

        return Lists.newArrayList(files.values());
    }

    @Override
    protected EntriesAdapter.ViewHolder onCreateEntryViewHolder(ViewGroup parent) {
        return new LockableEntryViewHolder(inflater.inflate(R.layout.item_entry_lockable, parent, false));
    }

    @Override
    protected void onBindFileItemViewHolder(EntriesAdapter.EntryViewHolder holder, EntryHolder item) {
        super.onBindFileItemViewHolder(holder, item);
        LockableEntryViewHolder lockableHolder = (LockableEntryViewHolder) holder;
        lockableHolder.icon.setBackgroundResource(R.drawable.bg_file_zip);
        lockableHolder.locked.setVisibility(item.entry.header != null && item.entry.header.isEncrypted()
                ? View.VISIBLE
                : View.GONE);
    }

    @Override
    protected boolean isFolder(AbstractFile entry) {
        return entry.isDirectory;
    }

    @Override
    protected int getIcon(AbstractFile entry) {
        if (entry.isDirectory) {
            return R.drawable.ic_folder_white_24dp;
        } else {
            return R.drawable.ic_receipt_white_24dp;
        }
    }

    @Override
    protected String getName(AbstractFile entry) {
        return entry.name;
    }

    @Override
    protected String getDescription(AbstractFile entry) {
        if (entry.isDirectory) {
            return String.format("包含 %d 个文件", entry.files);
        } else if (entry.header != null) {
            return String.format("%.2f MB", (float) entry.header.getUncompressedSize() / 1024 / 1024);
        } else {
            return null;
        }
    }

    @Override
    protected int compare(AbstractFile lhs, AbstractFile rhs) {
        return lhs.name.compareTo(rhs.name);
    }

    public static class AbstractFile {

        public final String name;

        public final String path;

        public final boolean isDirectory;

        @Nullable
        public final FileHeader header;

        public int files;

        AbstractFile(String name, String path, boolean isDirectory, @Nullable FileHeader header) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
            this.header = header;
        }

        @Override
        public String toString() {
            return String.format("AbstractFile[name:%s path:%s, isDirectory:%s]",
                    name, path, isDirectory);
        }

    }

    class LockableEntryViewHolder extends EntryViewHolder {

        @Bind(R.id.locked)
        ImageView locked;

        public LockableEntryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
