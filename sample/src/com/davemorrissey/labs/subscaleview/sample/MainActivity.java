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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.sample.R.id;
import com.davemorrissey.labs.subscaleview.sample.signHits.SignHitsActivity;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements OnClickListener {

    private static final int REQUEST_CODE = 99;
    private boolean PicTaken = false;
    private Button scanButton;
    private ImageButton cameraButton;
    private ImageButton mediaButton;
    private ImageView scannedImageView;
    private Bitmap mBitmap;
    private Uri mUri;
    private String mSeriesNumber;
    private FileDialog mFileDialog;
    private String mFilePath = "";
    private String mFileDirStr = "";
    private String mNewFileDir = "";
    private String mFileName = "";
    private String PROJ_NAME = "";
    private String FIRE_FILE_TYPE = ".xls"; //".xlsx";
    private String SeriesNum;
    private ArrayList<String> DIRECTORIES = new ArrayList<String>();
    private boolean bIsNewProject = false;
    private ArrayList<String> userData;
    private String m_Text = "";



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
        findViewById(id.btnCamera).setOnClickListener(this);
        findViewById(id.btnLibrary).setOnClickListener(this);
        findViewById(id.self).setOnClickListener(this);
        findViewById(id.btnExcel).setOnClickListener(this);
        init();
        Toast toast =Toast.makeText(this, "select your target from camera or photo library and click on \"Mark Hits\"",
                Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
//        toast.show();



    }

    private void showAboutDialog(){
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("About");
//
//// Set up the input
//        final ImageView iv = new ImageView(this);
//        iv.setImageResource(R.drawable.icon);
//
//        final TextView tv= new TextView(this);
//        tv.setText("Version: "+ getString(R.string.Version));
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//        builder.setView(iv);
//        builder.setView(tv);
//
//// Set up the buttons
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
////                m_Text = input.getText().toString();
//            }
//        });
//
//        builder.show();

        AlertDialog.Builder alertadd = new AlertDialog.Builder(this);
        LayoutInflater factory = LayoutInflater.from(this);
        final View view = factory.inflate(R.layout.about, null);
        alertadd.setView(view);
        alertadd.setNeutralButton("Here!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dlg, int sumthin) {

            }
        });

        alertadd.show();
    }

    private void init() {
        try {
            cameraButton = (ImageButton) findViewById(id.btnCamera);
            cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
            mediaButton = (ImageButton) findViewById(id.btnLibrary);
            mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
            scannedImageView = (ImageView) findViewById(R.id.scannedImage);

//            createDirectories();

            File mPath = new File(Environment.getExternalStorageDirectory() + "/Elbit Mark Target");
            if (!mPath.exists() || !mPath.isDirectory()) {
                mPath.mkdir();
            }


            //creating template directory
            String outputPath = Environment.getExternalStorageDirectory() + "/Elbit Mark Target/Infrastructure";
            File mInfrastructurePath = new File(outputPath);
            if (!mInfrastructurePath.exists() || !mInfrastructurePath.isDirectory()) {
                mInfrastructurePath.mkdir();
                bIsNewProject = true;
            }

            if (bIsNewProject){
                copyResources(R.raw.template, "template", outputPath);
                XmlRW xml = new XmlRW(outputPath+"/template.xml");
                xml.saveToXML();
            }

//            userData = new ArrayList<>();
//            userData =

            mFileDialog = new FileDialog(this, mPath, FIRE_FILE_TYPE);
            mFileDialog.addFileListener(new FileDialog.FileSelectedListener() {
                public void fileSelected(File file) {
                    Log.d(getClass().getName(), "selected file " + file.toString());
                    mFilePath = file.toString();
                    mFileDirStr = file.getParent();
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

            AutoCompleteTextView etProjName = (AutoCompleteTextView) findViewById(id.etProjName);
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

            EditText etSerNum = (EditText) findViewById(id.etSerNum);
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

            ImageButton ibAddExcelFile = (ImageButton) findViewById(id.btnAddExcelFile);
            ibAddExcelFile.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    AutoCompleteTextView etProjName = (AutoCompleteTextView) findViewById(id.etProjName);
                    String projName = etProjName.getText().toString();
                    createNewProject(projName);

                    mFileDialog.showDialog();
                }
            });


            listOfDirectories(mPath.getAbsolutePath());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_dropdown_item_1line, DIRECTORIES);
            final AutoCompleteTextView textView = (AutoCompleteTextView)
                    findViewById(R.id.etProjName);
            textView.setAdapter(adapter);

            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.showDropDown();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void createNewProject(String projName) {
        setTitleProjName(projName);
        if (!DIRECTORIES.contains(projName)){
            //create new directory
            createDirectories(projName);
            //add template inside
            copyResources(R.raw.template, projName, mNewFileDir);
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
                // create new directory if there isn't one compatiable with current project
                AutoCompleteTextView etProjName = (AutoCompleteTextView)findViewById(id.etProjName);
                createDirectories(etProjName.getText().toString());
                EditText et = (EditText)findViewById(id.etSerNum);
                mSeriesNumber = et.getText().toString();
                Intent intent = new Intent(this, SignHitsActivity.class);  //SignHitsActivity
                intent.putExtra("UriSrc",        mUri);
                intent.putExtra("projName",      PROJ_NAME);
                intent.putExtra("seriesNum",     mSeriesNumber);
                intent.putExtra("filePath",      mFilePath);
                intent.putExtra("fileDirStr",    mFileDirStr);
                intent.putExtra("newFileDirStr", mNewFileDir);
                intent.putExtra("fileName",      mFileName);

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
                // TODO: change it to default file and delete test
//                Toast.makeText(MainActivity.this, "first pick a file", Toast.LENGTH_SHORT).show();
                mUri = Uri.parse("content://media/external/images/media/32093");
                PROJ_NAME = "Emint";
                mSeriesNumber = "9";
                mFilePath = "/storage/emulated/0/Elbit Mark Target/EMINT/EMINT.xlsx";
                mFileDirStr = "/storage/emulated/0/Elbit Mark Target/EMINT";
                mNewFileDir = "";
                mFileName = "EMINT.xlsx";
                PicTaken = true;
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
        PROJ_NAME= projName;
        getActionBar().setTitle("Project: "+PROJ_NAME);
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

    public void listOfDirectories(String directoryName) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] dicList = directory.listFiles();
        for (File file : dicList) {
            if (file.isDirectory()) {
                DIRECTORIES.add(file.getName());
            }
        }
    }

    public void copyResources(int resId, String filename, String outputPath){
        Log.i("Test", "Setup::copyResources");

        InputStream in = getResources().openRawResource(resId);
        String suffix = ".xls";
        filename += suffix;
        File f = new File(filename);

        if(!f.exists()){
            try {
                OutputStream out = new FileOutputStream(new File(outputPath, filename));
                byte[] buffer = new byte[1024];
                int len;
                while((len = in.read(buffer, 0, buffer.length)) != -1){
                    out.write(buffer, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                Log.i("Test", "Setup::copyResources - "+e.getMessage());
            } catch (IOException e) {
                Log.i("Test", "Setup::copyResources - "+e.getMessage());
            }
        }
    }






}// class ending