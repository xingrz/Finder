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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ZipFinderActivity extends EntriesActivity implements Runnable {

    public static final String EXTRA_PREFIX = "prefix";

    private static final String TAG = "ZipFinderActivity";

    private static final int REQUEST_OPEN_FILE = 1;

    private static final long PROGRESS_INTERVAL = 40;

    private File current;
    private ZipFile zipFile;

    private ProgressMonitor progressMonitor;

    private Intent pendingIntent;

    private FileHeader extracting;

    private AlertDialog passwordPrompt;

    private Handler handler;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreateInternal(Bundle savedInstanceState) {
        super.onCreateInternal(savedInstanceState);

        current = new File(getIntent().getData().getPath());

        handler = new Handler(Looper.getMainLooper());

        try {
            zipFile = new ZipFile(current);
            zipFile.setRunInThread(true);
        } catch (ZipException e) {
            Log.d(TAG, "failed to open zip file " + current.getAbsolutePath(), e);
        }

        if (getIntent().hasExtra(EXTRA_PREFIX)) {
            toolbar.setTitle(FilenameUtils.getName(getIntent().getStringExtra(EXTRA_PREFIX)));
            toolbar.setSubtitle(current.getName());
        }

        passwordPrompt = new AlertDialog.Builder(this)
                .setView(R.layout.dialog_password)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        confirmFilePassword();
                    }
                })
                .create();

        progressMonitor = zipFile.getProgressMonitor();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
                if (file.isDirectory || file.header == null) {
                    Log.e(TAG, "not a valid file to extract");
                    return;
                }

                extracting = file.header;
                openFileInZip();
            }
        };
    }

    private void openFileInZip() {
        if (getExternalCacheDir() == null) {
            Log.e(TAG, "no external cache dir to extract");
            return;
        }

        File target = new File(getExternalCacheDir(), extracting.getFileName());
        Intent intent = intentToView(Uri.fromFile(target), mimeOfFile(target));

        if (intent.resolveActivityInfo(getPackageManager(), 0) == null) {
            Log.e(TAG, "no activity to handle file " + extracting.getFileName());
            Toast.makeText(this, "没有打开该文件的应用", Toast.LENGTH_SHORT).show();
            return;
        }

        if (extracting.isEncrypted() && (extracting.getPassword() == null || extracting.getPassword().length == 0)) {
            passwordPrompt.show();
            return;
        }

        try {
            zipFile.extractFile(extracting, getExternalCacheDir().getAbsolutePath());
        } catch (ZipException ignored) {
        }

        pendingIntent = intent;

        progressDialog.setProgress(0);
        progressDialog.show();

        handler.post(this);
    }

    private void confirmFilePassword() {
        Editable password = ((EditText) passwordPrompt.findViewById(R.id.password)).getText();

        char[] chars = new char[password.length()];
        password.getChars(0, password.length(), chars, 0);

        extracting.setPassword(chars);
        openFileInZip();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_FILE:
                deleteTempFile();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void deleteTempFile() {
        if (getExternalCacheDir() == null) {
            Log.d(TAG, "nothing to clean");
        }

        try {
            // FIXME: sub dir in the future
            FileUtils.cleanDirectory(getExternalCacheDir());
            Log.d(TAG, "cleaned cache dir");
        } catch (IOException e) {
            Log.e(TAG, "failed to clean cache dir", e);
        }
    }

    @Override
    public void run() {
        if (progressMonitor.getState() == ProgressMonitor.STATE_BUSY) {
            progressDialog.setProgress(progressMonitor.getPercentDone());
            handler.postDelayed(this, PROGRESS_INTERVAL);
        } else if (progressMonitor.getResult() == ProgressMonitor.RESULT_SUCCESS) {
            progressDialog.dismiss();
            startActivityForResult(pendingIntent, REQUEST_OPEN_FILE);
            overridePendingTransitionForBuiltInViewer(pendingIntent);
            extracting = null;
        } else if (progressMonitor.getResult() == ProgressMonitor.RESULT_ERROR) {
            progressDialog.dismiss();
            Log.e(TAG, "failed to extract file " + extracting.getFileName(), progressMonitor.getException());
            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            extracting.setPassword(null);
            extracting = null;
        }
    }

}
