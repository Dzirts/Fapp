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

package com.davemorrissey.labs.subscaleview.sample.Data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.sample.ExcelData;
import com.davemorrissey.labs.subscaleview.sample.ExcelWriter;
import com.davemorrissey.labs.subscaleview.sample.MainActivity;
import com.davemorrissey.labs.subscaleview.sample.R.id;
import com.davemorrissey.labs.subscaleview.sample.R.layout;
import com.davemorrissey.labs.subscaleview.sample.myToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataActivity extends Activity implements OnClickListener {
    private ArrayList<PointF> scaledMapPins;

    private static final String BUNDLE_POSITION = "position";
    private String projectName;
    private String serieNumber;
    private String filePath;
    private String fileDir;
    private ExcelData ed;
    private String newFilePlace;
    private String fileName;
    private String mImagePath;
    private myToast mToast;
    private boolean mToastsAreOn;
    private String mImageName;
    private String range;
    private String fireType;
    private String  camType;
    private String targetPos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isDeviceIsPhone()){
            setContentView(layout.data_activity_phone);
        } else {
            setContentView(layout.data_activity);
        }
        setAppTitle();
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getIntentParams();
        setAppSubtitle();
        arrangeExcelData();
        ExcelWriter ew = new ExcelWriter(ed);
        newFilePlace = ew.WriteData();
        handleButtons();
        getAppParams();
        mToast = new myToast(this, true, mToastsAreOn);
        createDataTable();
    }

    private void handleButtons() {
        ImageButton openExcelBtn = (ImageButton)findViewById(id.btnExcel);
        openExcelBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!newFilePlace.equals("")){
                    File file = new File(newFilePlace);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file),"application/vnd.ms-excel");
                    startActivity(intent);
                }
            }
        });
        ImageButton openFolder = (ImageButton)findViewById(id.btnFolder);
        openFolder.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                openFolder();
            }
        });
        findViewById(id.next).setOnClickListener(this);

        ImageButton showImageBtn = (ImageButton)findViewById(id.btnImage);
        showImageBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView iv = (ImageView) findViewById(id.hitsImage);
                File imgFile = new  File(mImagePath);
                if(imgFile.exists())
                {
                    iv.setImageURI(Uri.fromFile(imgFile));
                    iv.setVisibility(View.VISIBLE);
                }
            }
        });
        findViewById(id.next).setOnClickListener(this);
    }

    private void createDataTable() {
        TableLayout tl = (TableLayout) findViewById(id.main_table);
        DecimalFormat df = new DecimalFormat("#.#");
        // i=0 is the center so we are not caulating it
        for (int i=1; i<scaledMapPins.size(); i++){
            TableRow tr = new TableRow(this);
            if(i%2==0) tr.setBackgroundColor(Color.parseColor("#bdbdbd"));
            tr.setBackgroundColor(Color.parseColor((i%2==0)?"#606060":"#404040"));
            tr.setLayoutParams(new TableRow.LayoutParams(
                    TableRow.LayoutParams.FILL_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            final String x = df.format(scaledMapPins.get(i).x);
            final String y = df.format(scaledMapPins.get(i).y);

            final int finalI = i;
            ArrayList<String> rowValues = new ArrayList<String>(){{add(String.valueOf(finalI));add(x); add(y);}};

            for (String val: rowValues){
                TextView tv = new TextView(this);
                tv.setText(val);
                tv.setPadding(2, 0, 5, 0);
                tv.setTextColor(Color.WHITE);
                tv.setTextSize(27);
                tv.setGravity(Gravity.CENTER);
                tr.addView(tv);
            }

            tl.addView(tr, new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT));
        }

    }

    private void getAppParams() {
//        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences sharedPref = this.getSharedPreferences("param_toasts_are_on",Context.MODE_PRIVATE);
        String toastsAreOn = sharedPref.getString("param_toasts_are_on2", "false");
        mToastsAreOn = (toastsAreOn.equals("true"))? true : false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == id.next) {
            mToast.setTextAndShow("Moving to next session");
//            Toast.makeText(DataActivity.this, "Moving to next session", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    private void setAppTitle(){
        getActionBar().setTitle("Data Entry Window");
    }

    private void setAppSubtitle(){
        getActionBar().setSubtitle("Project:  "+ projectName +",  Series:  #"+ serieNumber);
    }

    @Override
    public void onBackPressed()
    {
        scaledMapPins.clear();
        super.onBackPressed();
    }

    private void arrangeExcelData(){

//        InputStream stream = getResources().openRawResource(R.raw.temp_book);
        InputStream stream = null;
        try {
            stream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            Button b = (Button)findViewById(id.openExcel);
            b.setText("failed  writing to excel");
            e.printStackTrace();
        }

        File f = new File(filePath);
        File oDirectory = f.getParentFile();

        ed = new ExcelData(projectName, Integer.parseInt(serieNumber),
                oDirectory,fileName, filePath, fileDir,
                stream, scaledMapPins, mImagePath,range,fireType,camType);
    }

    public void openFolder() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(filePath.substring(0, filePath.lastIndexOf("/")));
        intent.setDataAndType(uri, "*/*");//tried with application/pdf and file/*
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        PackageManager pm = this.getPackageManager();
        List<ResolveInfo> apps =
                pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (apps.size() > 0)
            startActivity(intent);//Intent.createChooser(intent, "Open folder"


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


    public void getIntentParams() {
        Intent intent = getIntent();

        projectName = intent.getStringExtra("projectName");
        serieNumber = intent.getStringExtra("seriesNumber");
        filePath    = intent.getStringExtra("filePath");
        fileDir     = intent.getStringExtra("fileDir");
        fileName    = intent.getStringExtra("fileName");
        mImagePath  = intent.getStringExtra("imagePath");
        mImageName  = intent.getStringExtra("imageName");
        range        = intent.getStringExtra("range");
        fireType     = intent.getStringExtra("fireType");
        camType      = intent.getStringExtra("cameraType");
        targetPos    = intent.getStringExtra("targetPos");
        scaledMapPins = new ArrayList<PointF>();
        scaledMapPins = intent.getParcelableArrayListExtra("ScaledPoints");
    }
}
