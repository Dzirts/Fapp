/*
Copyright 2014 David Morrissey

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.davemorrissey.labs.subscaleview.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.sample.R.id;
import com.davemorrissey.labs.subscaleview.sample.signHits.SignHitsActivity;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener {

    private static final int REQUEST_CODE = 99;
    private Button scanButton;
    private Button cameraButton;
    private Button mediaButton;
    private ImageView scannedImageView;
    private Bitmap mBitmap;
    private Uri mUri;
    private boolean PicTaken = false;
    private String PROJ_NAME = "";
    private String FIRE_FILE_TYPE = ".xlsx";
    private String SeriesNum;



    private FileDialog mFileDialog;
    private String mFilePath = "";
    private String mFileDirStr = "";
    private String mNewFileDir = "";
    private String mFileName = "";





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Project: "+PROJ_NAME);
        if (isDeviceIsPhone()){
            setContentView(R.layout.main_phone);
        } else {
            setContentView(R.layout.main);
        }
        findViewById(id.btnNext).setOnClickListener(this);
        findViewById(id.libraryPic).setOnClickListener(this);
        findViewById(id.CameraPic).setOnClickListener(this);
        findViewById(id.self).setOnClickListener(this);
        findViewById(id.btnExcel).setOnClickListener(this);
        init();
        Toast toast =Toast.makeText(this, "select your target from camera or photo library and click on \"Mark Hits\"",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
//        toast.show();



    }

    private void init() {
        cameraButton = (Button) findViewById(id.CameraPic);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (Button) findViewById(id.libraryPic);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);


        File mPath = new File(Environment.getExternalStorageDirectory() + "/Elbit Mark Target");
        if(!mPath.exists() || !mPath.isDirectory()) {
            mPath.mkdir();
        }
        mFileDialog = new FileDialog(this, mPath, FIRE_FILE_TYPE);
        mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Log.d(getClass().getName(), "selected file " + file.toString());
                mFilePath = file.toString();
                mFileDirStr = file.getParent();
                //get project name without path
                 String[] sArr2 = file.toString().split("/");
                mFileName = sArr2[sArr2.length-1];
                if (!PROJ_NAME.equals("")){
                    createDirectories(PROJ_NAME);
                }
                ImageButton imgbtnExcel = (ImageButton)findViewById(id.btnAddExcelFile);
                imgbtnExcel.setImageResource(R.drawable.add_file_done);
            }
        });
        mFileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
          public void directorySelected(File directory) {
              Log.d(getClass().getName(), "selected dir " + directory.toString());
          }
        });
        mFileDialog.setSelectDirectoryOption(false);
//        mFileDialog.showDialog();

        EditText etProjName = (EditText)findViewById(id.etProjName);
        etProjName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    String projName = editText.getText().toString();
                    setTitleProjName(projName);
                }
            }
        });

        EditText etSerNum = (EditText)findViewById(id.etSerNum);
        etSerNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    SeriesNum = editText.getText().toString();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        ImageButton ibAddExcelFile = (ImageButton)findViewById(id.btnAddExcelFile);
        ibAddExcelFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mFileDialog.showDialog();
            }
        });



    }

    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

        public ScanButtonClickListener() {
        }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                //getContentResolver().delete(uri, null, null);
                scannedImageView.setImageBitmap(bitmap);
                scannedImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                mBitmap= bitmap;
                mUri= uri;
                PicTaken = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == id.btnNext) {
            if (PicTaken){
                EditText etSeries = (EditText) findViewById(id.etSerNum);
                if (etSeries.getText().toString().equals("")){
                    Toast toast = Toast.makeText(MainActivity.this, "Please choose a series", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                EditText et = (EditText)findViewById(id.etSerNum);
                String SeriesNumber = et.getText().toString();
                Intent intent = new Intent(this, SignHitsActivity.class);  //SignHitsActivity
                intent.putExtra("UriSrc", mUri);
                intent.putExtra("projName", PROJ_NAME);
                intent.putExtra("seriesNum", SeriesNumber);
                intent.putExtra("filePath", mFilePath);
                intent.putExtra("fileDirStr", mFileDirStr);
                intent.putExtra("newFileDirStr", mNewFileDir);
                intent.putExtra("fileName", mFileName);

                startActivity(intent);
            } else{
                Toast toast =Toast.makeText(this, "first pick a picture", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

        } else if (view.getId() == id.btnExcel){
            if (!mFilePath.matches("")){
//                //TODO: check new com.davemorrissey.labs.subscaleview.sample.ExcelWriter(mFilePath);
                File file = new File(mFilePath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),"application/vnd.ms-excel");
                startActivity(intent);
            } else {
                // TODO: change it to default file
//                Toast.makeText(MainActivity.this, "first pick a file", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.target_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            TODO: change to options
//            mFileDialog.showDialog();
        } else if (id == R.id.action_newproject) {
            // TODO: add option for new user
            return true;
        } else if(id==R.id.action_logout){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitleProjName(String projName){
        PROJ_NAME= projName;
        getActionBar().setTitle("Project: "+PROJ_NAME);
    }

    private void createDirectories(String projName) {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Elbit Mark Target");
        if(!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        dir = new File(dir + "/"+ projName);
        if(!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }
        mNewFileDir = dir.getAbsolutePath();
    }




    private boolean isDeviceIsPhone(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        if (diagonalInches>=6.5){
            // 6.5inch device or bigger
            return false;
        }else{
            // smaller device
            return true;
        }
    }






    }// class ending
