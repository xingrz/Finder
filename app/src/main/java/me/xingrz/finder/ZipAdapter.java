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
import android.text.TextUtils;

import com.google.common.collect.Lists;

import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.HashSet;
import java.util.List;

public abstract class ZipAdapter extends EntriesAdapter<ZipAdapter.AbstractFile> {

    public ZipAdapter(Context context, List<FileHeader> headers, String prefix) {
        super(context, abstractFiles(headers, prefix));
    }

    private static List<AbstractFile> abstractFiles(List<FileHeader> headers, String prefix) {
        HashSet<AbstractFile> files = new HashSet<>();

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

            boolean isDirectory = name.contains(File.separator);

            if (isDirectory) {
                int index = name.indexOf(File.separator);
                name = name.substring(0, index);
                path = path.substring(0, clipping + index);
            }

            files.add(new AbstractFile(name, path, isDirectory));
        }

        return Lists.newArrayList(files);
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
        return "";
    }

    @Override
    protected int compare(AbstractFile lhs, AbstractFile rhs) {
        return lhs.name.compareTo(rhs.name);
    }

    public static class AbstractFile {

        public final String name;

        public final String path;

        public boolean isDirectory;

        AbstractFile(String name, String path, boolean isDirectory) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof AbstractFile) && path.equals(((AbstractFile) o).path);
        }

        @Override
        public int hashCode() {
            return path.hashCode() + (isDirectory ? 1 : 0);
        }

        @Override
        public String toString() {
            return String.format("AbstractFile[name:%s path:%s, isDirectory:%s]",
                    name, path, isDirectory);
        }

    }

}
