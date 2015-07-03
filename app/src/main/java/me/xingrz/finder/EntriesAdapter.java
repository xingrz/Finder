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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public abstract class EntriesAdapter<E> extends RecyclerView.Adapter<EntriesAdapter.ViewHolder> {

    public static final int TYPE_FOLDER_HEADER = 1;
    public static final int TYPE_FOLDER_ITEM = 2;
    public static final int TYPE_FILE_HEADER = 3;
    public static final int TYPE_FILE_ITEM = 4;

    private final LayoutInflater inflater;

    private final ArrayList<EntryHolder> items = new ArrayList<>();

    public EntriesAdapter(Context context, List<E> entries) {
        this.inflater = LayoutInflater.from(context);

        if (entries != null) {
            boolean hasFolders = false;
            boolean hasFiles = false;

            for (E entry : entries) {
                if (isFolder(entry)) {
                    hasFolders = true;
                    items.add(new EntryHolder(TYPE_FOLDER_ITEM, entry));
                } else {
                    hasFiles = true;
                    items.add(new EntryHolder(TYPE_FILE_ITEM, entry));
                }
            }

            if (hasFolders) {
                items.add(new EntryHolder(TYPE_FOLDER_HEADER, null));
            }

            if (hasFiles) {
                items.add(new EntryHolder(TYPE_FILE_HEADER, null));
            }

            Collections.sort(items, new Comparator<EntryHolder>() {
                @Override
                public int compare(EntryHolder lhs, EntryHolder rhs) {
                    switch (lhs.type) {
                        case TYPE_FOLDER_HEADER:
                            switch (rhs.type) {
                                case TYPE_FOLDER_HEADER:
                                    return 0;
                                default:
                                    return -1;
                            }
                        case TYPE_FOLDER_ITEM:
                            switch (rhs.type) {
                                case TYPE_FOLDER_HEADER:
                                    return 1;
                                case TYPE_FOLDER_ITEM:
                                    return EntriesAdapter.this.compare(lhs.entry, rhs.entry);
                                default:
                                    return -1;
                            }
                        case TYPE_FILE_HEADER:
                            switch (rhs.type) {
                                case TYPE_FOLDER_HEADER:
                                case TYPE_FOLDER_ITEM:
                                    return 1;
                                case TYPE_FILE_HEADER:
                                    return 0;
                                default:
                                    return -1;
                            }
                        case TYPE_FILE_ITEM:
                            switch (rhs.type) {
                                case TYPE_FILE_ITEM:
                                    return EntriesAdapter.this.compare(lhs.entry, rhs.entry);
                                default:
                                    return 1;
                            }
                        default: // impossible
                            return 0;
                    }
                }
            });

            notifyDataSetChanged();
        }
    }

    protected abstract boolean isFolder(E entry);

    protected abstract int getIcon(E entry);

    protected abstract String getName(E entry);

    protected abstract String getDescription(E entry);

    protected abstract int compare(E lhs, E rhs);

    @Override
    public EntriesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(EntriesAdapter.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_HEADER:
            case TYPE_FILE_HEADER:
                onBindHeaderViewHolder((EntriesAdapter.HeaderViewHolder) holder);
                break;
            case TYPE_FOLDER_ITEM:
            case TYPE_FILE_ITEM:
                onBindEntryViewHolder((EntriesAdapter.EntryViewHolder) holder, position);
                break;
        }
    }

    private void onBindHeaderViewHolder(EntriesAdapter.HeaderViewHolder holder) {
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_HEADER:
                holder.name.setText("文件夹");
                break;
            case TYPE_FILE_HEADER:
                holder.name.setText("文件");
                break;
        }
    }

    private void onBindEntryViewHolder(EntriesAdapter.EntryViewHolder holder, int position) {
        EntryHolder item = items.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_ITEM:
                holder.icon.setBackgroundResource(R.drawable.bg_folder);
                holder.icon.setImageResource(item.icon);
                holder.name.setText(item.name);
                holder.description.setText(item.description);
                break;
            case TYPE_FILE_ITEM:
                holder.icon.setBackgroundResource(R.drawable.bg_file);
                holder.icon.setImageResource(item.icon);
                holder.name.setText(item.name);
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
        EntryHolder item = items.get(holder.getAdapterPosition());
        switch (item.type) {
            case TYPE_FOLDER_ITEM:
                openFolder(item.entry);
                break;
            case TYPE_FILE_ITEM:
                openFile(item.entry);
                break;
        }
    }

    protected abstract void openFolder(E folder);

    protected abstract void openFile(E file);

    class EntryHolder {

        public int type;

        public E entry;

        public int icon;
        public String name;
        public String description;

        public EntryHolder(int type, E entry) {
            this.type = type;

            this.entry = entry;

            if (entry != null) {
                this.icon = getIcon(entry);
                this.name = getName(entry);
                this.description = getDescription(entry);
            }
        }

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }

    }

    class HeaderViewHolder extends EntriesAdapter.ViewHolder {

        @Bind(R.id.name)
        public TextView name;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

    class EntryViewHolder extends EntriesAdapter.ViewHolder {

        @Bind(R.id.icon)
        public ImageView icon;

        @Bind(R.id.name)
        public TextView name;

        @Bind(R.id.description)
        public TextView description;

        public EntryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(EntryViewHolder.this);
                }
            });
        }

    }

}
