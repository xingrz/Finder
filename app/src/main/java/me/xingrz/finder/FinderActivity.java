package me.xingrz.finder;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import me.imid.swipebacklayout.lib.SwipeBackActivity;

public class FinderActivity extends SwipeBackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_finder);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        boolean allowBack = getIntent().getBooleanExtra("back", false);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(allowBack);
        }

        setSwipeBackEnable(allowBack);

        File current = determineCurrentFile();

        setTitle(current.getName());

        RecyclerView filesView = (RecyclerView) findViewById(R.id.files);
        filesView.setLayoutManager(new LinearLayoutManager(this));
        filesView.setAdapter(new FilesAdapter(this, current.listFiles()) {
            @Override
            protected void openFolder(File folder) {
                Intent intent = new Intent(FinderActivity.this, FinderActivity.class);
                intent.setData(Uri.fromFile(folder));
                intent.putExtra("back", true);
                startActivity(intent);
            }

            @Override
            protected void openFile(File file) {
                String extension = FilenameUtils.getExtension(file.getName());
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), mime);

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });
    }

    private File determineCurrentFile() {
        if (getIntent().getData() != null) {
            return new File(getIntent().getData().getPath());
        } else {
            return Environment.getExternalStorageDirectory();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void supportFinishAfterTransition() {
        if (getIntent().getBooleanExtra("back", false)) {
            scrollToFinishActivity();
        } else {
            super.supportFinishAfterTransition();
        }
    }

}
