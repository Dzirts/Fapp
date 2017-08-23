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

package com.davemorrissey.labs.subscaleview.sample.signHits;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.sample.ExcelReader;
import com.davemorrissey.labs.subscaleview.sample.R;
import com.davemorrissey.labs.subscaleview.sample.R.id;
import com.davemorrissey.labs.subscaleview.sample.R.layout;
import com.davemorrissey.labs.subscaleview.sample.Data.DataActivity;
import com.davemorrissey.labs.subscaleview.sample.extension.views.PinView;
import com.davemorrissey.labs.subscaleview.sample.myToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static java.lang.Math.sqrt;

public class SignHitsActivity extends Activity implements OnClickListener {

    private static final String BUNDLE_POSITION = "position";
    private static final int LIMIT_OF_HITS = 20;
    private String projectName;
    private String seriesNumber;
    private String filePath;
    private String fileDir;
    private String fileName;
    private String mImagePath;
    public  ExcelReader er;

    AlertDialog addPrevHitsDialog;
    final ArrayList seletedItems=new ArrayList();
    AlertDialog.Builder builder;
    private myToast mToast;



    private boolean nextClicked = false;

    private int position;
    private int pinsCounter=0;
    private PinView pinView;
    private float rotationDegree = 0;


    private boolean centerSelected = false;
    private boolean centerAttached = false;
    private boolean doneRotateAndCenter  = false;
    private PointF pfCenterPt;

    private List<Note> notes;
    private List<PinView> pins;
    private ArrayList<PointF> CenterPins;
    private ArrayList<PointF> scaledMapPins;
    private ArrayList<Pair<PointF, String>> hitList = new ArrayList<Pair<PointF, String>>();
    private ArrayList<ArrayList<PointF>> prevHitList = new ArrayList<ArrayList<PointF>>();
    private ArrayList<Integer> indexList = new ArrayList<Integer>();

    private ImageView ivHitImg;
    private ImageView ivDeleteImg;
    private ImageView ivColorsImg;
    private ImageView ivShowAllHitsImg;
    private ImageView ivCenterDone;
    private ImageView ivNext;

    private SeekBar sizeSeekBar;

    private enum ButtonChecked {HIT, DELETE} ;
    private enum markMode {MARK_CENTER, MARK_HITS};
    private enum ButtonState {ON, OFF};


    private ButtonState colorBtnState = ButtonState.OFF;
    private ButtonState showAllBtnState = ButtonState.OFF;
    private ButtonChecked buttonChecked = ButtonChecked.HIT;
    private markMode MarkMode = markMode.MARK_CENTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.sign_hits_activity);
//        getActionBar().setDisplayHomeAsUpEnabled(true);
        setOnClickListeners();
        InitButtuns();
        initArrays();
        initialiseImage();
        InitSeekBar();
        buildHitsDialog();
        notes = Arrays.asList(
                new Note("", ""),
                new Note("", ""),
                new Note("", ""),
                new Note("", "")
        );
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_POSITION)) {
            position = savedInstanceState.getInt(BUNDLE_POSITION);
        }
        updateNotes(0);
        mToast = new myToast(this, true);
        setAppTitle("Center Selection Window");
        setAppSubtitle();


    }

    private void InitSeekBar() {
        sizeSeekBar.setMax(800);
        sizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int size = i+400;
                updateSignSize(size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initArrays() {
        CenterPins= new ArrayList<PointF>();
        scaledMapPins = new ArrayList<PointF>();
        CenterPins.add(new PointF(0,0));
    }

    private void InitButtuns() {
        ivHitImg = (ImageView) findViewById(id.hitImg);
        ivDeleteImg = (ImageView) findViewById(id.delImg);
        ivColorsImg = (ImageView) findViewById(id.colors_btn);
        ivShowAllHitsImg = (ImageView) findViewById(id.ShowAllHits);
        ivCenterDone= (ImageView) findViewById(id.centerDoneBtn);
        ivNext= (ImageView) findViewById(id.next);
        pinView= (PinView)(findViewById(id.imageView));
        sizeSeekBar = (SeekBar)findViewById(id.sizeSeekBar);


    }

    private void  updateSignSize(int id){
        pinView.setSize(id);
        pinView.setPins(hitList);
        pinView.post(new Runnable() {
            public void run() {
                pinView.getRootView().postInvalidate();
            }
        });
    }

    private void setOnClickListeners() {
        findViewById(id.next).setOnClickListener(this);
        findViewById(id.previous).setOnClickListener(this);
        findViewById(id.ShowAllHits).setOnClickListener(this);
        findViewById(id.delImg).setOnClickListener(this);
        findViewById(id.hitImg).setOnClickListener(this);
        findViewById(id.setCenter).setOnClickListener(this);
        findViewById(id.centerDoneBtn).setOnClickListener(this);
        findViewById(id.colors_btn).setOnClickListener(this);
        findViewById(id.MeasureButton).setOnClickListener(this);

        findViewById(id.new_circ_light_blue).setOnClickListener(this);
        findViewById(id.new_circ_blue).setOnClickListener(this);
        findViewById(id.new_circ_green).setOnClickListener(this);
        findViewById(id.new_circ_orange).setOnClickListener(this);
        findViewById(id.new_circ_yellow).setOnClickListener(this);
        findViewById(id.new_circ_purple).setOnClickListener(this);

        findViewById(id.old_circ_red).setOnClickListener(this);
        findViewById(id.old_circ_bordo).setOnClickListener(this);
        findViewById(id.old_circ_green).setOnClickListener(this);
        findViewById(id.old_circ_orange).setOnClickListener(this);
        findViewById(id.old_circ_yellow).setOnClickListener(this);
        findViewById(id.old_circ_purple).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.target_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }




    void createBuilder( ){
        CharSequence[] items = new String[indexList.size()];
        int k=0;
        for (int i: indexList){
            items[k] = " "+i+" ";
            k++;
        }
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Select the series to upload on target");
        builder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    // indexSelected contains the index of item (of which checkbox checked)
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            // write your code when user checked the checkbox
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            // write your code when user Uchecked the checkbox
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here
                        ArrayList<Integer> tmpIndexList = new ArrayList<Integer>();
                        for (Object i: seletedItems){
                            int k = indexList.get((int)i);
                            tmpIndexList.add(k);
                        }
                        addSelectedColsToView(tmpIndexList);

                    }
                })
                .setNeutralButton("Select All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on OK
                        //  You can write the code  to save the selected item here
                        AlertDialog d = (AlertDialog) dialog;
                        ListView v = d.getListView();
                        int i = 0;
                        while(i < indexList.size()) {
                            v.setItemChecked(i, true);
                            i++;
                        }
                        addSelectedColsToView(indexList);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel

                    }
                });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_POSITION, position);
    }

    private void setAppTitle(String s){
        getActionBar().setTitle(s);
    }

    private void setAppSubtitle(){
        getActionBar().setSubtitle("Project:  "+ projectName +",  Series:  #"+ seriesNumber);
    }



    @Override
    public void onClick(View view) {

        if (view.getId() == id.next) {
            handleNextButtun();
        } else if (view.getId() == id.colors_btn) {
            handleColorsButtun();
        } else if (view.getId() == id.ShowAllHits) {
            handleShowAllHitsButtun();
        } else if (view.getId() == id.setCenter) {
            handleSetCenterButtun();
        } else if (view.getId() == id.centerDoneBtn) {
            handleCenterDoneButtun();
        } else if (view.getId() == id.new_circ_light_blue || view.getId() == id.new_circ_blue ||
                view.getId() == id.new_circ_green || view.getId() == id.new_circ_orange ||
                view.getId() == id.new_circ_yellow || view.getId() == id.new_circ_purple) {
            updateColors(view.getId(), "new");
        } else if (view.getId() == id.old_circ_red || view.getId() == id.old_circ_bordo ||
                view.getId() == id.old_circ_green || view.getId() == id.old_circ_orange ||
                view.getId() == id.old_circ_yellow || view.getId() == id.old_circ_purple) {
            updateColors(view.getId(), "old");
        } else if (view.getId() == id.hitImg){
            handleHitButton();
        } else if (view.getId() == id.delImg){
            handleDeleteButton();
        }

    }

    private void handleDeleteButton() {
        buttonChecked = ButtonChecked.DELETE;
        ivHitImg.setImageResource(R.drawable.hit_transparent);
        ivDeleteImg.setImageResource(R.drawable.bin_red);
    }

    private void handleHitButton() {
        buttonChecked = ButtonChecked.HIT;
        centerSelected = true;
        ivHitImg.setImageResource(R.drawable.hit_green);
        ivDeleteImg.setImageResource(R.drawable.bin_transparent);
    }

    private void handleCenterDoneButtun() {
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);

        if (!centerSelected){
            mToast.setTextAndShow( "first select a center");

//                Toast.makeText(SignHitsActivity.this,"first select a center",Toast.LENGTH_SHORT).show();
            return;
        }
        setAppTitle("Marking Hits Window");
        setAppSubtitle();
        imageView.resetScaleAndCenter();
        MarkMode = markMode.MARK_HITS;
        if (!centerAttached){return;}
        updateNotes(0);
        doneRotateAndCenter = true;
        ivCenterDone.setVisibility(View.INVISIBLE);
        ivNext.setVisibility(View.VISIBLE);
        ivHitImg.setVisibility(View.VISIBLE);
        ivDeleteImg.setVisibility(View.VISIBLE);
        ivColorsImg.setVisibility(View.VISIBLE);
        ivShowAllHitsImg.setVisibility(View.VISIBLE);

        mToast.setTextAndShow("mark the hits over the target, you can find the average and clear all marks, when finish click done button");

//            Toast.makeText(this, "mark the hits over the target, you can find the average and clear all marks, when finish click done button",
//                    Toast.LENGTH_LONG).show();
    }

    private void handleSetCenterButtun() {
        centerSelected = true;
    }

    private void handleShowAllHitsButtun() {
        // TODO: insert into function
        addPrevHitsDialog.show();
        ivShowAllHitsImg.setImageResource(R.drawable.star);
    }

    private void handleColorsButtun() {
        RelativeLayout rl = (RelativeLayout) findViewById(id.colors_layout);
        if (rl.getVisibility() == View.VISIBLE) {
            ivColorsImg.setImageResource(R.drawable.brush_transparent);
            rl.setVisibility(View.INVISIBLE);
        } else {
            ivColorsImg.setImageResource(R.drawable.brush_blue);
            rl.setVisibility(View.VISIBLE);
        }
    }

    private void handleNextButtun() {
        final EditText  elvText = (EditText)findViewById(id.elvTxt);
        final EditText  trvText = (EditText)findViewById(id.trvTxt);
        if (elvText.getText().toString().matches("") || trvText.getText().toString().matches("")){
            mToast.setTextAndShow( "please enter target height and width size first");

//                Toast.makeText(this, "please enter target height and width size first", Toast.LENGTH_SHORT).show();
            return;
        }
        takeScreenshot();
        double targetElvSize = Double.parseDouble(elvText.getText().toString());
        double targetTrvSize = Double.parseDouble(trvText.getText().toString());
        scaleHitsToCenter(targetElvSize,targetTrvSize);
        Intent intent = new Intent(this, DataActivity.class);
        intent.putParcelableArrayListExtra("ScaledPoints", scaledMapPins);
        intent.putExtra("projectName" ,projectName);
        intent.putExtra("seriesNumber" ,seriesNumber);
        intent.putExtra("filePath" ,filePath);
        intent.putExtra("fileName" ,fileName);
        intent.putExtra("imagePath" ,mImagePath);
        intent.putExtra("fileDir", fileDir);
        startActivity(intent);
    }

    public void showPin(float x, float y){
        PinView pinView = (PinView)findViewById(id.imageView);
        Random random = new Random();
        if (pinView.isReady()) {
            float maxScale = pinView.getMaxScale();
            float minScale = pinView.getMinScale();
            float scale = (random.nextFloat() * (maxScale - minScale)) + minScale;
            PointF center = new PointF(x, y);
            pinView.setPin(center);
        }
    }


    private void buildHitsDialog(){
        InputStream stream = null;
        try {
            stream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        er = new ExcelReader(stream);
        indexList = er.getAllFilledInCols();
        //reading all previus hits on activity create
        createBuilder();
        addPrevHitsDialog = builder.create();
    }






    private void initialiseImage() {
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
//                here we on mark mode
                if (imageView.isReady() && ( MarkMode==markMode.MARK_HITS && (buttonChecked == ButtonChecked.HIT) || //markHit.isChecked()
                                        MarkMode==markMode.MARK_CENTER )) {
                    PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    if (MarkMode==markMode.MARK_CENTER){
                        updateNotes(1);
                        Pair<PointF, String> p = new Pair<PointF, String>(sCoord,"CenterBig");
                        //change the center place so remove and fter that add
                        if (hitList.size() > 0) {
                            hitList.remove(0);
                        }
                        hitList.add(0, p);
                        pinView.setPins(hitList);
                        centerSelected = true;
                        pfCenterPt=sCoord;
                    } else {
                        updateNotes(++pinsCounter);
                        //change big center to small center
                        Pair<PointF, String> center = hitList.get(0);
                        Pair<PointF, String> newCenter = new Pair<PointF, String>(center.first,"CenterSmall");
                        hitList.remove(0);
                        hitList.add(0,newCenter);

                        Pair<PointF, String> p = new Pair<PointF, String>(sCoord,"newHit");
                        hitList.add(p);
                        pinView.setPins(hitList);
                    }
                    pinView.post(new Runnable(){
                        public void run(){
                            pinView.getRootView().postInvalidate();
                        }
                    });
                    centerAttached =true;
                } else if(imageView.isReady() && (buttonChecked == ButtonChecked.DELETE)){
                    // in case we want to delete points
                    if (pinsCounter == 0){ return true;}
                    PointF sCoord = imageView.viewToSourceCoord(e.getX(), e.getY());
                    detectHitToRemove(sCoord);
                    pinView.setPins(hitList);

                    pinView.post(new Runnable(){
                        public void run(){
                            pinView.getRootView().postInvalidate();
                        }
                    });
                    updateNotes(--pinsCounter);
                    centerAttached =true;
                }
                else {
                    mToast.setTextAndShow("Single tap: Image not ready");
//                    Toast.makeText(getApplicationContext(), "Single tap: Image not ready", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
//
        });
        Intent intent = getIntent();
        Uri ScannedImage= intent.getParcelableExtra("UriSrc");
        projectName  = intent.getStringExtra("projName");
        seriesNumber = intent.getStringExtra("seriesNum");
        filePath = intent.getStringExtra("filePath");
        fileDir = intent.getStringExtra("fileDirStr");
        fileName = intent.getStringExtra("fileName");

        setAppSubtitle();

        imageView.setImage(ImageSource.uri(ScannedImage));
        imageView.setMinimumDpi(25);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
            }
        });
    }

    private void detectHitToRemove(PointF hit){
        //find the nearest point to delete
        double minDist = sqrt(Math.pow(hit.x-hitList.get(1).first.x,2)+Math.pow(hit.y-hitList.get(1).first.y,2));
        int minIdx = 1;
        //i=0 is the center point so dont check for it
        for (int i=1; i<hitList.size(); i++) {
            double currDist = sqrt(Math.pow(hit.x - hitList.get(i).first.x, 2) + Math.pow(hit.y - hitList.get(i).first.y, 2));
            if (minDist > currDist) {
                minDist = currDist;
                minIdx = i;
            }
        }
        hitList.remove(minIdx);
    }

    private void updateColors(int id, String type) {
        pinView.setColor(type, id);
        pinView.setPins(hitList);
        pinView.post(new Runnable() {
            public void run() {
                pinView.getRootView().postInvalidate();
            }
        });
    }



    private void updateNotes(int nHit) {
        int limitOfHits = LIMIT_OF_HITS;
        getActionBar().setSubtitle(notes.get(position).subtitle);
        ((TextView)findViewById(id.note)).setText("marked: "+nHit);
        findViewById(id.next).setVisibility(position >= notes.size() - 1 ? View.INVISIBLE : View.VISIBLE);
        findViewById(id.previous).setVisibility(position <= 0 ? View.INVISIBLE : View.VISIBLE);

        SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
        if (position == 2) {
            imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_CENTER);
        } else {
            imageView.setPanLimit(SubsamplingScaleImageView.PAN_LIMIT_INSIDE);
        }

    }

    private static final class Note {
        private final String text;
        private final String subtitle;
        private Note(String subtitle, String text) {
            this.subtitle = subtitle;
            this.text = text;
        }
    }

    private void clearAllPins(){
        if (pinsCounter == 0){
            return;
        }
        hitList.clear();
        pinView.setPins(hitList);
        pinView.post(new Runnable(){
            public void run(){
                pinView.getRootView().postInvalidate();
            }
        });
        pinsCounter=0;
        updateNotes(pinsCounter);
    }

    void scaleHitsToCenter(double targetElvSize, double targetTrvSize){
        scaledMapPins.clear();
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
        int imageWidth = imageView.getSWidth();
        int imageHight = imageView.getSHeight();
//        imageView.
        for(int i=0; i<hitList.size(); i++){
            if (hitList.get(i).second.equals("oldHit")) { continue;}
            float newX= (float)((hitList.get(i).first.x- pfCenterPt.x)*targetTrvSize/imageWidth)*100;
            float newY= (float)((pfCenterPt.y-hitList.get(i).first.y)*targetElvSize/imageHight)*100;

            PointF tempPF = new PointF(newX,newY);

            scaledMapPins.add(tempPF);
        }
    }

    private void takeScreenshot() {
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
        imageView.resetScaleAndCenter();
        //take off old hits
        clearOldHitsFromHitList();
        pinView.setPins(hitList);
        pinView.post(new Runnable() {
            public void run() {
                pinView.getRootView().postInvalidate();
            }
        });

        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
//            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";
            mImagePath = fileDir + "/" + projectName +"_Ser_"+seriesNumber+"_"+ now + ".jpg";
            mToast.setTextAndShow("pic saved in" + mImagePath);
//            Toast.makeText(SignHitsActivity.this, "pic saved in" + mImagePath, Toast.LENGTH_LONG).show();
            RelativeLayout v1 =(RelativeLayout)findViewById(id.rl);
            // create bitmap screen capture
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);
            File imageFile = new File(mImagePath);
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    private void addSelectedColsToView(ArrayList<Integer> indexList){
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
        final EditText  elvText = (EditText)findViewById(id.elvTxt);
        final EditText  trvText = (EditText)findViewById(id.trvTxt);
        double targetElvSize = Double.parseDouble(elvText.getText().toString());
        double targetTrvSize = Double.parseDouble(trvText.getText().toString());
        clearOldHitsFromHitList();
        prevHitList =  er.getAllHitsByIndexes(indexList);
        for (ArrayList<PointF> arr: prevHitList){
            for (PointF pf: arr){
                int imageWidth = imageView.getSWidth();
                int imageHight = imageView.getSHeight();
                float newX= (float)((pf.x)/targetTrvSize*imageWidth)/100;
                float newY= (float)((pf.y)/targetElvSize*imageHight)/100;
                PointF scaledPF = new PointF(newX+pfCenterPt.x,pfCenterPt.y-newY);
                Pair<PointF, String> tmpPair = new Pair<PointF, String>(scaledPF,"oldHit");
                hitList.add(tmpPair);
            }
        }
        pinView.setPins(hitList);
        pinView.post(new Runnable(){
            public void run(){
                pinView.getRootView().postInvalidate();
            }
        });
    }

    private void clearOldHitsFromHitList(){
        try {
            ArrayList<Pair<PointF, String>> tmpList = new ArrayList<Pair<PointF, String>>();
            for (Pair<PointF, String> p : hitList){
                if (p.second.equals("oldHit")){
                    tmpList.add(p);
                }
            }
            hitList.removeAll(tmpList);
        } catch (Exception e){
            e.printStackTrace();
        }

    }





}

