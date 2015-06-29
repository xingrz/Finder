package me.xingrz.finder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public abstract class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    public static final int TYPE_FOLDER_HEADER = 1;
    public static final int TYPE_FOLDER_ITEM = 2;
    public static final int TYPE_FILE_HEADER = 3;
    public static final int TYPE_FILE_ITEM = 4;

    private final SimpleDateFormat dateFormat
            = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());

    private final LayoutInflater inflater;

    private final ArrayList<File> folders = new ArrayList<>();
    private final ArrayList<File> files = new ArrayList<>();

    public FilesAdapter(Context context, File[] entries) {
        this.inflater = LayoutInflater.from(context);

        if (entries != null) {
            for (File entry : entries) {
                if (entry.isDirectory()) {
                    folders.add(entry);
                } else if (entry.isFile()) {
                    files.add(entry);
                }
            }

            Collections.sort(folders);
            Collections.sort(files);
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
                onBindEntryViewHolder((EntryViewHolder) holder, position - 1);
                break;
            case TYPE_FILE_ITEM:
                if (folders.isEmpty()) {
                    onBindEntryViewHolder((EntryViewHolder) holder, position - 1);
                } else {
                    onBindEntryViewHolder((EntryViewHolder) holder, position - 2 - folders.size());
                }
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
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_ITEM:
                File folder = folders.get(position);
                holder.icon.setBackgroundResource(R.drawable.bg_folder);
                holder.name.setText(folder.getName());
                holder.description.setText(dateFormat.format(folder.lastModified()));
                break;
            case TYPE_FILE_ITEM:
                File file = files.get(position);
                holder.icon.setBackgroundResource(R.drawable.bg_file);
                holder.name.setText(file.getName());
                holder.description.setText(dateFormat.format(file.lastModified()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;

        if (!folders.isEmpty()) {
            count += folders.size() + 1;
        }

        if (!files.isEmpty()) {
            count += files.size() + 1;
        }

        return count;
    }

    @Override
    public int getItemViewType(int position) {
        if (folders.isEmpty() && !files.isEmpty()) {
            if (position == 0) {
                return TYPE_FILE_HEADER;
            } else {
                return TYPE_FILE_ITEM;
            }
        }

        if (!folders.isEmpty() && files.isEmpty()) {
            if (position == 0) {
                return TYPE_FOLDER_HEADER;
            } else {
                return TYPE_FOLDER_ITEM;
            }
        }

        if (position == 0) {
            return TYPE_FOLDER_HEADER;
        } else if (position < folders.size() + 1) {
            return TYPE_FOLDER_ITEM;
        } else if (position == folders.size() + 1) {
            return TYPE_FILE_HEADER;
        } else {
            return TYPE_FILE_ITEM;
        }
    }

    private void onItemClick(EntryViewHolder holder) {
        int position = holder.getAdapterPosition();
        switch (holder.getItemViewType()) {
            case TYPE_FOLDER_ITEM:
                openFolder(folders.get(position - 1));
                break;
            case TYPE_FILE_ITEM:
                if (!folders.isEmpty()) {
                    position -= (folders.size() + 1);
                }

                openFile(files.get(position - 1));
                break;
        }
    }

    protected abstract void openFolder(File folder);

    protected abstract void openFile(File file);

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
