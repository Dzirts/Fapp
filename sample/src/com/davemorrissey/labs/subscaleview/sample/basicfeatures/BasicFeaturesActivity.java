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

package com.davemorrissey.labs.subscaleview.sample.basicfeatures;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.sample.ExcelData;
import com.davemorrissey.labs.subscaleview.sample.ExcelWriter;
import com.davemorrissey.labs.subscaleview.sample.R;
import com.davemorrissey.labs.subscaleview.sample.R.id;
import com.davemorrissey.labs.subscaleview.sample.R.layout;
import com.davemorrissey.labs.subscaleview.sample.imagedisplay.decoders.RapidImageRegionDecoder;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicFeaturesActivity extends Activity implements OnClickListener {
    private ArrayList<PointF> scaledMapPins;

    private static final String BUNDLE_POSITION = "position";
    private String projectName;
    private String serieNumber;
    private String filePath;
    private String newFileDir;
    private ExcelData ed;
    private String newFilePlace;
    private String fileName;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.notes_activity);
        getActionBar().setTitle("Data");
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        projectName = intent.getStringExtra("projectName");
        serieNumber = intent.getStringExtra("seriesNumber");
        filePath = intent.getStringExtra("filePath");
        newFileDir = intent.getStringExtra("newFileDir");
        fileName = intent.getStringExtra("fileName");





        getActionBar().setSubtitle(projectName+":  #"+serieNumber);
        scaledMapPins= new ArrayList<PointF>();
        scaledMapPins= intent.getParcelableArrayListExtra("ScaledPoints");
        String s = new String();
        s = String.format("%1$-20s %2$-15s %3$10s", "No.","TRV [cm]", "ELV [cm]") + "\n\n";
        DecimalFormat df = new DecimalFormat("#.#");
        // i=0 is the center so we are not caulating it
        for (int i=1; i<scaledMapPins.size(); i++){
            String x = df.format(scaledMapPins.get(i).x);
            String y = df.format(scaledMapPins.get(i).y);
            String value = String.format("%1$-20s %2$-10s %3$10s",i, x, y);
            s+= value +"\n";
        }
        TextView tv = (TextView)findViewById(id.scaledHitsText);
        tv.setText(s);


        NumberPicker np = (NumberPicker) findViewById(id.numberPicker);

        np.setMinValue(0);
        np.setMaxValue(40);
        np.setValue(20);
        np.setWrapSelectorWheel(false);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                // TODO Auto-generated method stub
                TextView tv = (TextView)findViewById(id.scaledHitsText);
                NumberPicker np = (NumberPicker) findViewById(id.numberPicker);
                tv.setTextSize(np.getValue());
            }
        });

        arrangeExcelData();
        ExcelWriter ew = new ExcelWriter(ed);
        newFilePlace = ew.WriteData();

        Button openExcelBtn = (Button)findViewById(id.openExcel);
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

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == id.next) {
            Toast.makeText(BasicFeaturesActivity.this, "next clicked", Toast.LENGTH_SHORT).show();
        }
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
                oDirectory,fileName, filePath, newFileDir,
                stream, scaledMapPins);
    }

}
