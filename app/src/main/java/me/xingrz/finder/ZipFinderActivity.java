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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

public class ZipFinderActivity extends EntriesActivity {

    public static final String EXTRA_PREFIX = "prefix";

    private static final int REQUEST_OPEN_FILE = 1;

    private static final String TAG = "ZipFinderActivity";

    private File current;
    private ZipFile zipFile;

    private File tempFile;
    private FileHeader pendingFile;

    private AlertDialog passwordPrompt;

    @Override
    protected void onCreateInternal(Bundle savedInstanceState) {
        super.onCreateInternal(savedInstanceState);

        current = new File(getIntent().getData().getPath());

        try {
            zipFile = new ZipFile(current);
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

                openFileInZip(file.header);
            }
        };
    }

    private void openFileInZip(FileHeader header) {
        if (getExternalCacheDir() == null) {
            Log.e(TAG, "no external cache dir to extract");
            return;
        }

        File target = new File(getExternalCacheDir(), header.getFileName());
        Intent intent = intentToView(Uri.fromFile(target), mimeOfFile(target));

        if (intent.resolveActivityInfo(getPackageManager(), 0) == null) {
            Log.e(TAG, "no activity to handle file " + header.getFileName());
            Toast.makeText(this, "没有打开该文件的应用", Toast.LENGTH_SHORT).show();
            return;
        }

        if (header.isEncrypted() && (header.getPassword() == null || header.getPassword().length == 0)) {
            pendingFile = header;
            passwordPrompt.show();
            return;
        }

        try {
            zipFile.extractFile(header, getExternalCacheDir().getAbsolutePath());
        } catch (ZipException e) {
            Log.e(TAG, "failed to extract file " + header.getFileName(), e);
            Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            header.setPassword(null);
            return;
        }

        tempFile = target;

        startActivityForResult(intent, REQUEST_OPEN_FILE);
        overridePendingTransitionForBuiltInViewer(intent);
    }

    private void confirmFilePassword() {
        if (pendingFile == null) {
            return;
        }

        Editable password = ((EditText) passwordPrompt.findViewById(R.id.password)).getText();

        char[] chars = new char[password.length()];
        password.getChars(0, password.length(), chars, 0);

        pendingFile.setPassword(chars);
        openFileInZip(pendingFile);

        pendingFile = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_OPEN_FILE:
                if (tempFile != null && tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.d(TAG, "deleted temp file " + tempFile.getAbsolutePath());
                    } else {
                        Log.e(TAG, "failed to delete temp file " + tempFile.getAbsolutePath());
                    }
                }

                tempFile = null;

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
