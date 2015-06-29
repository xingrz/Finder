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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public abstract class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    public static final int TYPE_FOLDER_HEADER = 1;
    public static final int TYPE_FOLDER_ITEM = 2;
    public static final int TYPE_FILE_HEADER = 3;
    public static final int TYPE_FILE_ITEM = 4;

    private final LayoutInflater inflater;

    private final ArrayList<Item> items = new ArrayList<>();

    public FilesAdapter(Context context, File[] entries) {
        this.inflater = LayoutInflater.from(context);

        if (entries != null) {
            ArrayList<File> folders = new ArrayList<>();
            ArrayList<File> files = new ArrayList<>();

            for (File entry : entries) {
                if (entry.isDirectory()) {
                    folders.add(entry);
                } else if (entry.isFile()) {
                    files.add(entry);
                }
            }

            Collections.sort(folders);
            Collections.sort(files);

            if (!folders.isEmpty()) {
                items.add(new Item(TYPE_FOLDER_HEADER, null, null));
                for (File folder : folders) {
                    items.add(new Item(TYPE_FOLDER_ITEM, folder, String.format("包含 %d 个文件", folder.list().length)));
                }
            }

            if (!files.isEmpty()) {
                items.add(new Item(TYPE_FILE_HEADER, null, null));
                for (File file : files) {
                    items.add(new Item(TYPE_FILE_ITEM, file, String.format("%.2f MB", (float) file.length() / 1024 / 1024)));
                }
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_FOLDER_HEADER:
            case TYPE_FILE_HEADER:
                return new HeaderViewHolder(inflater.inflate(R.layout.item_header, parent, false));
            case TYPE_FOLDER_ITEM:
            case TYPE_FILE_ITEM:
                return new EntryViewHolder(inflater.inflate(R.layout.item_entry, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_HEADER:
            case TYPE_FILE_HEADER:
                onBindHeaderViewHolder((HeaderViewHolder) holder);
                break;
            case TYPE_FOLDER_ITEM:
            case TYPE_FILE_ITEM:
                onBindEntryViewHolder((EntryViewHolder) holder, position);
                break;
        }
    }

    private void onBindHeaderViewHolder(HeaderViewHolder holder) {
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_HEADER:
                holder.name.setText("文件夹");
                break;
            case TYPE_FILE_HEADER:
                holder.name.setText("文件");
                break;
        }
    }

    private void onBindEntryViewHolder(EntryViewHolder holder, int position) {
        Item item = items.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_ITEM:
                holder.icon.setBackgroundResource(R.drawable.bg_folder);
                holder.icon.setImageResource(R.drawable.ic_folder_white_24dp);
                holder.name.setText(item.file.getName());
                holder.description.setText(item.description);
                break;
            case TYPE_FILE_ITEM:
                holder.icon.setBackgroundResource(R.drawable.bg_file);
                holder.icon.setImageResource(R.drawable.ic_receipt_white_24dp);
                holder.name.setText(item.file.getName());
                holder.description.setText(item.description);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    private void onItemClick(EntryViewHolder holder) {
        Item item = items.get(holder.getAdapterPosition());
        switch (item.type) {
            case TYPE_FOLDER_ITEM:
                openFolder(item.file);
                break;
            case TYPE_FILE_ITEM:
                openFile(item.file);
                break;
        }
    }

    protected abstract void openFolder(File folder);

    protected abstract void openFile(File file);

    class Item {

        public int type;
        public File file;
        public String description;

        public Item(int type, File file, String description) {
            this.type = type;
            this.file = file;
            this.description = description;
        }

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

    }

    class HeaderViewHolder extends ViewHolder {

        public TextView name;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
        }

    }

    class EntryViewHolder extends ViewHolder {

        public ImageView icon;
        public TextView name;
        public TextView description;

        public EntryViewHolder(View itemView) {
            super(itemView);

            icon = (ImageView) itemView.findViewById(R.id.icon);
            name = (TextView) itemView.findViewById(R.id.name);
            description = (TextView) itemView.findViewById(R.id.description);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(EntryViewHolder.this);
                }
            });
        }

    }

}
