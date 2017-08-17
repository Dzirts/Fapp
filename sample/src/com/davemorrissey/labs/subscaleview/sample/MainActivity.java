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
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {


    private final int NUM_OF_SERIES_IN_XLS_FILE = 20;
    private static final int REQUEST_CODE = 99;
    private final String FIRE_FILE_TYPE = ".xls";
    private String TAG = getClass().getName();

    private ImageButton cameraButton;
    private ImageButton mediaButton;
    private ImageButton ibAddExcelFile;

    private ImageView scannedImageView;
    private AutoCompleteTextView etProjName;
    private EditText etSerNum;

    private boolean bIsNewProject = false;
    private boolean PicTaken = false;

    private String mSeriesNumber;
    private String mProjName;
    private String mFilePath;
    private String mFileDir;
    private String mFileName;
    private String SeriesNum;

    private myToast mToast;

    private Bitmap mBitmap;
    private Uri mUri;
    private FileDialog mFileDialog;
    private ArrayList<String> directoriesList = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle("Project: ");
        chooseAppropriateLayout();
        initLayoutWidgets();
        setOnClickListeners();
        setScannerListeners();
        File mainDirectory = new File(Environment.getExternalStorageDirectory()
                                                        + "/Elbit Mark Target");
        createEnviromntFolders(mainDirectory);
        createFileDialog(mainDirectory);
        addAdapters(mainDirectory);
        setOnFocusListeners();
        getAppParams();
        mToast = new myToast(this, true);

        ImageView iv = (ImageView)findViewById(id.scannedImage);
        iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mToast.setTextAndShow("Welcome to Elbit Target Marker!!");

            }
        });

        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.about_alert_layout, null);
        //mToast.setViewAndShow(view);

        // TODO: fix toast
    }

    private void chooseAppropriateLayout() {
        if (isDeviceIsPhone()){
            setContentView(R.layout.main_phone);
        } else {
            setContentView(R.layout.main);
        }
    }

    private void initLayoutWidgets() {
        etProjName = (AutoCompleteTextView) findViewById(id.etProjName);
        etSerNum = (EditText) findViewById(id.etSerNum);
        ibAddExcelFile = (ImageButton) findViewById(id.btnAddExcelFile);
    }

    private void setOnClickListeners() {
        findViewById(id.btnNext).setOnClickListener(this);
        findViewById(id.btnCamera).setOnClickListener(this);
        findViewById(id.btnLibrary).setOnClickListener(this);
        findViewById(id.self).setOnClickListener(this);
        findViewById(id.btnExcel).setOnClickListener(this);

        ibAddExcelFile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                AutoCompleteTextView etProjName = (AutoCompleteTextView) findViewById(id.etProjName);
                String projName = etProjName.getText().toString();
                createNewProject(projName);

                mFileDialog.showDialog();
            }
        });
    }

    private void setScannerListeners() {
        cameraButton = (ImageButton) findViewById(id.btnCamera);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (ImageButton) findViewById(id.btnLibrary);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);
    }

    private void createEnviromntFolders(File mainDirectory) {
        //            File mPath = new File(Environment.getExternalStorageDirectory() + getString(R.string.Project_Name));
        if (!mainDirectory.exists() || !mainDirectory.isDirectory()) {
            mainDirectory.mkdir();
        }
        //creating template directory
        String outputPath = Environment.getExternalStorageDirectory()+"/"+"Elbit Mark Target"+"/"+"Infrastructure";
        File mInfrastructurePath = new File(outputPath);
        if (!mInfrastructurePath.exists() || !mInfrastructurePath.isDirectory()) {
            mInfrastructurePath.mkdir();
            bIsNewProject = true;
        }

        if (bIsNewProject){
            ResorcesCopier rc =new ResorcesCopier(getApplicationContext());
            rc.copyResources(R.raw.template, "template", outputPath, ".xls");
        }
    }

    private void createFileDialog(File mainDirectory) {
        mFileDialog = new FileDialog(this, mainDirectory, FIRE_FILE_TYPE, getApplicationContext());
        mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                Log.d(getClass().getName(), "selected file " + file.toString());
                mFilePath = file.toString();
                mFileDir = file.getParent();
                //get project name without path
                String[] sArr2 = file.toString().split("/");
                mFileName = sArr2[sArr2.length - 1];
                ImageButton imgbtnExcel = (ImageButton) findViewById(id.btnAddExcelFile);
                imgbtnExcel.setImageResource(R.drawable.add_file_done);
            }
        });
        mFileDialog.addDirectoryListener(new FileDialog.DirectorySelectedListener() {
            public void directorySelected(File directory) {
                Log.d(getClass().getName(), "selected dir " + directory.toString());
            }
        });
        mFileDialog.setSelectDirectoryOption(false);
    }

    private void addAdapters(File mainDirectory) {
        listOfDirectories(mainDirectory.getAbsolutePath());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, directoriesList);
        etProjName.setAdapter(adapter);
        etProjName.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                etProjName.showDropDown();
            }
        });
    }


    private void setAppParams(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.param_project_name)  , mProjName);
        editor.putString(getString(R.string.param_project_series), mSeriesNumber);
        editor.putString(getString(R.string.param_xls_name)      , mFileName);
        editor.putString(getString(R.string.param_xls_path)      , mFilePath);
        editor.putString(getString(R.string.param_xls_dir)       , mFileDir);
        editor.commit();
    }

    private void getAppParams(){
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String def =   "default";
        String projName   = sharedPref.getString(getString(R.string.param_project_name)  , def);
        String currSeries = sharedPref.getString(getString(R.string.param_project_series), def);
        String xlsName    = sharedPref.getString(getString(R.string.param_xls_name)      , def);
        String xlsPath    = sharedPref.getString(getString(R.string.param_xls_path)      , def);
        String xlsDir     = sharedPref.getString(getString(R.string.param_xls_dir)       , def);


        if (projName.equals(def) ||  currSeries.equals(def) ||
            xlsName.equals(def)  ||  xlsPath.equals(def)    ||
            xlsDir.equals(def))
        {
            //new Project
            mProjName = mSeriesNumber = mFilePath = mFileName = mFileDir ="";
            Log.d(TAG, "No saved parameters exists");
        } else {
            mProjName = projName;
            etProjName.setText(mProjName);
            setTitleProjName(mProjName);

            int currSer = Integer.parseInt(currSeries);
            if (currSer >= NUM_OF_SERIES_IN_XLS_FILE) {
                // file is full, starting a new project
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(getString(R.string.param_project_series), def);
                editor.commit();
            }
            mSeriesNumber = String.valueOf(currSer+1);
            EditText etSer = (EditText)findViewById(id.etSerNum);
            etSer.setText(mSeriesNumber);
            setSubTitleSer(mSeriesNumber);

            mFilePath     = xlsPath;
            ImageButton imgbtnExcel = (ImageButton) findViewById(id.btnAddExcelFile);
            imgbtnExcel.setImageResource(R.drawable.add_file_done);

            mFileName     = xlsName;
            mFileDir      = xlsDir;
            Log.d(TAG, "prefences are set in app");

        }
    }


    private void showAboutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.about_alert_layout, null);
        builder.setView(view);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    private void setOnFocusListeners() {
        etProjName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    String projName = editText.getText().toString();
                    createNewProject(projName);
                }
            }
        });

        etSerNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    EditText editText = (EditText) v;
                    SeriesNum = editText.getText().toString();
                    setSubTitleSer(SeriesNum);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
    }





    private void createNewProject(String projName) {
        setTitleProjName(projName);
        if (!directoriesList.contains(projName)){
            //create new directory
            createDirectories(projName);
            //add template inside
            ResorcesCopier rc =new ResorcesCopier(getApplicationContext());
            rc.copyResources(R.raw.template, projName, mFileDir, ".xls");
        }
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
                if (etSerNum.getText().toString().equals("")){
                    Toast toast = Toast.makeText(MainActivity.this, "Please choose a series", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                // create new directory if there isn't one compatiable with current project
                createDirectories(etProjName.getText().toString());
                mSeriesNumber = etSerNum.getText().toString();
                mProjName = etProjName.getText().toString();

                Intent intent = new Intent(this, SignHitsActivity.class);  //SignHitsActivity
                intent.putExtra("UriSrc",        mUri);
                intent.putExtra("projName",      mProjName);
                intent.putExtra("seriesNum",     mSeriesNumber);
                intent.putExtra("filePath",      mFilePath);
                intent.putExtra("fileDirStr",    mFileDir);
                intent.putExtra("fileName",      mFileName);
                setAppParams();
                startActivity(intent);
            } else{
                Toast toast =Toast.makeText(this, "first pick a picture", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

        } else if (view.getId() == id.btnExcel){
            if (!mFilePath.matches("default")){
//              TODO: check new com.davemorrissey.labs.subscaleview.sample.ExcelWriter(mFilePath);
                File file = new File(mFilePath);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file),"application/vnd.ms-excel");
                startActivity(intent);
            } else {
                // TODO: change it to default file and delete test
                Toast.makeText(MainActivity.this, "first pick a file", Toast.LENGTH_SHORT).show();
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
        } else if (id==R.id.action_about){
            showAboutDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setTitleProjName(String projName){
        getActionBar().setTitle("Project: "+projName);
    }

    private void setSubTitleSer(String serieNumber){
        getActionBar().setSubtitle("Series:  #"+serieNumber);
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
        mFileDir = dir.getAbsolutePath();
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

    public void listOfDirectories(String directoryName) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] dicList = directory.listFiles();
        for (File file : dicList) {
            if (file.isDirectory()) {
                directoriesList.add(file.getName());
            }
        }
    }

}// class ending