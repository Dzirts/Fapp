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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
    private String mImageName;
    public  ExcelReader er;
    private Uri mScannedImage;

    private float measureWidth;
    private float measureHeight;

    AlertDialog addPrevHitsDialog;
    final ArrayList seletedItems=new ArrayList();
    AlertDialog.Builder builder;
    private myToast mToast;

    private boolean measureBtnSelected = false;

    private boolean nextClicked = false;

    private int position;
    private int pinsCounter=0;
    private int tempPinsCounter=0;
    private PinView pinView;
    private float rotationDegree = 0;

    private boolean mToastsAreOn;

    private boolean centerSelected = false;
    private boolean centerAttached = false;
    private boolean doneRotateAndCenter  = false;
    private PointF pfCenterPt;

    private List<Note> notes;
    private List<PinView> pins;
    private ArrayList<PointF> CenterPins;
    private ArrayList<PointF> scaledMapPins;
    private ArrayList<Pair<PointF, String>> hitList = new ArrayList<Pair<PointF, String>>();
    private ArrayList<Pair<PointF, String>> tempHitList = new ArrayList<Pair<PointF, String>>();
    private ArrayList<ArrayList<PointF>> prevHitList = new ArrayList<ArrayList<PointF>>();
    private ArrayList<Integer> indexList = new ArrayList<Integer>();

    private boolean b = true;

    private ImageView ivHitImg;
    private ImageView ivDeleteImg;
    private ImageView ivColorsImg;
    private ImageView ivShowAllHitsImg;
    private ImageView ivCenterDone;
    private ImageView ivNext;
    private ImageView ivMeasure;

    private SeekBar sizeSeekBar;
    private float mesTrvSizeMeter;
    private float mesTrvSizePixel;
    private float mesElvSizeMeter;
    private float mesElvSizePixel;

    private enum ButtonChecked {HIT, DELETE} ;
    private enum markMode {MARK_CENTER, MARK_HITS, MARK_MES};
    private enum mesureMode {MES_WIDTH, MES_HEIGHT};
    private enum ButtonState {ON, OFF};

    private int EXPLENATION_DURATION = 2000;


    private ButtonState colorBtnState = ButtonState.OFF;
    private ButtonState showAllBtnState = ButtonState.OFF;
    private ButtonChecked buttonChecked = ButtonChecked.HIT;
    private markMode MarkMode = markMode.MARK_CENTER;
    private mesureMode MesMode = mesureMode.MES_WIDTH;

    private double targetElvSize;
    private double targetTrvSize;

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
        getAppParams();
        mToast = new myToast(this, true, mToastsAreOn);
        setAppTitle("Center Selection Window");
        setAppSubtitle();


    }

    private void getAppParams() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String toastsAreOn = sharedPref.getString("param_toasts_are_on2", "false");
        mToastsAreOn = (toastsAreOn.equals("true"))? true : false;
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
        ivMeasure = (ImageView) findViewById(id.MeasureButton);


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
        }  else if (view.getId() == id.MeasureButton){
            handleMeasureButton();
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
        ivMeasure.setVisibility(View.VISIBLE);

        ivShowAllHitsImg.setVisibility(View.VISIBLE);

        mToast.setTextAndShow("mark the hits over the target, you can find the average and clear all marks, when finish click done button");
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
        updateTargetSize();
        takeScreenshot();
        scaleHitsToCenter();
        Intent intent = new Intent(this, DataActivity.class);
        intent.putParcelableArrayListExtra("ScaledPoints", scaledMapPins);
        intent.putExtra("projectName" ,projectName);
        intent.putExtra("seriesNumber" ,seriesNumber);
        intent.putExtra("filePath" ,filePath);
        intent.putExtra("fileName" ,fileName);
        intent.putExtra("imagePath" ,mImageName);
        intent.putExtra("fileDir", fileDir);
        startActivity(intent);
    }

    private boolean updateTargetSize() {
        final EditText  elvText = (EditText)findViewById(id.elvTxt);
        final EditText  trvText = (EditText)findViewById(id.trvTxt);
        if (elvText.getText().toString().matches("") || trvText.getText().toString().matches("")){
            mToast.setTextAndShow( "please enter target height and width size first");
            return false;
        }
        targetElvSize = Double.parseDouble(elvText.getText().toString());
        targetTrvSize = Double.parseDouble(trvText.getText().toString());
        return true;
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
                                        MarkMode==markMode.MARK_CENTER  || MarkMode==markMode.MARK_MES )) {
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
                    } else if(MarkMode==markMode.MARK_HITS) {
                        updateNotes(++pinsCounter);
                        //change big center to small center
                        Pair<PointF, String> center = hitList.get(0);
                        Pair<PointF, String> newCenter = new Pair<PointF, String>(center.first,"CenterSmall");
                        hitList.remove(0);
                        hitList.add(0,newCenter);

                        Pair<PointF, String> p = new Pair<PointF, String>(sCoord,"newHit");
                        hitList.add(p);
                        pinView.setPins(hitList);
                    } else if(MarkMode==markMode.MARK_MES){
                        updateNotes(++pinsCounter);
                        Pair<PointF, String> p = new Pair<PointF, String>(sCoord,"mesurePt");
                        hitList.add(p);
                        pinView.setPins(hitList);
                    }
                    pinView.post(new Runnable(){
                        public void run(){
                            pinView.getRootView().postInvalidate();
                        }
                    });
                    centerAttached =true;
                    if(MarkMode==markMode.MARK_MES ){
                        if (pinsCounter == 2){
                            getMeasurementSizes(MesMode);
                            MesMode = mesureMode.MES_HEIGHT;
                        }
                        if (pinsCounter == 4){
                            getMeasurementSizes(MesMode);
                            MesMode = mesureMode.MES_WIDTH;
                        }
                    }
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
        mScannedImage= intent.getParcelableExtra("UriSrc");
        projectName  = intent.getStringExtra("projName");
        seriesNumber = intent.getStringExtra("seriesNum");
        filePath = intent.getStringExtra("filePath");
        fileDir = intent.getStringExtra("fileDirStr");
        fileName = intent.getStringExtra("fileName");

        setAppSubtitle();

        imageView.setImage(ImageSource.uri(mScannedImage));
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

    void scaleHitsToCenter(){
        scaledMapPins.clear();
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
        int imageWidth = imageView.getSWidth();
        int imageHight = imageView.getSHeight();
        float newX = 0, newY = 0 ;

//        imageView.
        for(int i=0; i<hitList.size(); i++){
            if (hitList.get(i).second.equals("oldHit")) { continue;}
            if (!measureBtnSelected){
                newX= (float)((hitList.get(i).first.x- pfCenterPt.x)*targetTrvSize/imageWidth)*100;
                newY= (float)((pfCenterPt.y-hitList.get(i).first.y)*targetElvSize/imageHight)*100;
            } else {
                newX= (float)((hitList.get(i).first.x- pfCenterPt.x)*mesTrvSizeMeter/mesTrvSizePixel)*100;
                newY= (float)((pfCenterPt.y-hitList.get(i).first.y)*mesTrvSizeMeter/mesElvSizePixel)*100;
            }


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
            mImagePath = fileDir + "/" + projectName +"_Ser_"+seriesNumber + ".jpg";
            mImageName = "./" + projectName +"_Ser_"+seriesNumber + ".jpg";
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

    private void handleMeasureButton() {
        measureBtnSelected = !measureBtnSelected;
        ivMeasure.setImageResource(measureBtnSelected? R.drawable.measure_pink:  R.drawable.measure_transparent);
        runToMesurementMode(measureBtnSelected);
        showMesExplnation(measureBtnSelected);
    }

    private void runToMesurementMode(boolean measureBtnSelected) {
        if (measureBtnSelected){
            // clean list only when moving from hits to mesure mode
            if (MarkMode == markMode.MARK_HITS){
                tempPinsCounter = pinsCounter;
                pinsCounter=0;
                updateNotes(pinsCounter);
                for (Pair p : hitList){
                    tempHitList.add(p);
                }
                hitList.clear();
                pinView.setPins(hitList);
                pinView.post(new Runnable(){
                    public void run(){
                        pinView.getRootView().postInvalidate();
                    }
                });
                MarkMode = markMode.MARK_MES;
            }
        } 
    }



    private void goBackToHitsMode() {
        hitList.clear();
        for (Pair p : tempHitList){
            hitList.add(p);
        }
        tempHitList.clear();
        pinsCounter = tempPinsCounter;
        updateNotes(pinsCounter);
        pinView.setPins(hitList);
        pinView.post(new Runnable(){
            public void run(){
                pinView.getRootView().postInvalidate();
            }
        });
        MarkMode = markMode.MARK_HITS;
    }

    private void resetAndScaleImage(){
        final SubsamplingScaleImageView imageView = (SubsamplingScaleImageView)findViewById(id.imageView);
        imageView.resetScaleAndCenter();
    }

    private void closeKeyboard(){
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );
    }

    private void getMeasurementSizes(final mesureMode MesureMode){
        final mesureMode mesMode = MesureMode;
        LayoutInflater factory = LayoutInflater.from(this);
        final View measureSizesView = factory.inflate(layout.measure_sizes_alert_layout, null);
        final View measureView = factory.inflate(layout.measure_alert_layout, null);
        final EditText mesET = (EditText)measureSizesView.findViewById(R.id.mesSizeET);
        TextView mesTV = (TextView)measureSizesView.findViewById(R.id.mesSizeTV);
        String title, body;
        AlertDialog.Builder al = new AlertDialog.Builder(this);
        if (MesureMode == mesureMode.MES_WIDTH){
            title = "Enter width of selected object";
            body = "Width [M]";
        } else {
            title = "Enter height of selected object";
            body = "Height [M]";
        }
        mesTV.setText(body);
        al.setTitle(title);
        al.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        float sizeInPixel = getInternalMeasure(mesMode);
                        calculateNewMeasureSize(mesMode, Float.parseFloat(mesET.getText().toString()), sizeInPixel);
                        TextView expTV = (TextView) findViewById(R.id.explenationsTV);
                        TextView expTVdailog = (TextView) measureView.findViewById(R.id.expTV);
                        if (mesMode == mesureMode.MES_WIDTH){
                            expTV.setText("Mark 2 point to define object height.");
                            expTVdailog.setText("Mark 2 point to define object height.");
                            closeKeyboard();
                            showMesExplnation(measureBtnSelected);
                            resetAndScaleImage();
                        } else {
                            expTV.setText("Mark 2 point to define object width.");
                            expTVdailog.setText("Mark 2 point to define object width.");
                            expTV.setVisibility(View.INVISIBLE);
                            closeKeyboard();
                            resetAndScaleImage();
                            goBackToHitsMode();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel
                        goBackToHitsMode();
                    }
                })
        .setView(measureSizesView).show();


    }

    private float getInternalMeasure(mesureMode mesType) {
        PointF tmpPF1, tmpPF2;
        switch (mesType){
            case MES_WIDTH:
                tmpPF1 = hitList.get(0).first;
                tmpPF2 = hitList.get(1).first;
                return Math.abs(tmpPF1.x - tmpPF2.x) ;
            case MES_HEIGHT:
                tmpPF1 = hitList.get(2).first;
                tmpPF2 = hitList.get(3).first;
                return Math.abs(tmpPF1.y - tmpPF2.y) ;
        }
        throw new IllegalArgumentException("Incompatible Arguments");
    }

    private void calculateNewMeasureSize(mesureMode mesMode,float sizeInMeter, float sizeInPixel){
        Log.d("new measure points", mesMode +": size in pixel:"+ sizeInPixel + ", size in meter: "+ sizeInMeter);
        if (mesMode == mesureMode.MES_WIDTH){
            mesTrvSizeMeter = sizeInMeter;
            mesTrvSizePixel = sizeInPixel;
        } else {
            mesElvSizeMeter = sizeInMeter;
            mesElvSizePixel = sizeInPixel;
        }
    }

    private void showMesExplnation(boolean measureBtnSelected){
        if (!measureBtnSelected){
            Toast.makeText(this, "mesure mode canceled", Toast.LENGTH_SHORT).show();
            return;
            // TODO: add snackbar
        }
        try {
            TextView explenationsTV = (TextView) findViewById(id.explenationsTV);
            explenationsTV.setVisibility((explenationsTV.getVisibility()==View.VISIBLE) ? View.INVISIBLE : View.VISIBLE);
            final RelativeLayout rl = (RelativeLayout) findViewById(id.rl);
            LayoutInflater factory = LayoutInflater.from(this);
            final View measureExplainView = factory.inflate(layout.measure_alert_layout, null);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ImageView leftPt = (ImageView)measureExplainView.findViewById(id.mesPtLeft);
            ImageView RightPt = (ImageView)measureExplainView.findViewById(id.mesPtRight);
            ImageView topPt = (ImageView)measureExplainView.findViewById(id.mesPtTop);
            ImageView bottomPt = (ImageView)measureExplainView.findViewById(id.mesPtBottom);
            if (MesMode == mesureMode.MES_WIDTH){
                leftPt.setVisibility(View.VISIBLE);
                RightPt.setVisibility(View.VISIBLE);
                topPt.setVisibility(View.INVISIBLE);
                bottomPt.setVisibility(View.INVISIBLE);
                leftPt.animate().rotation(359).setDuration(EXPLENATION_DURATION).start();
                RightPt.animate().rotation(359).setDuration(EXPLENATION_DURATION).start();
            } else{
                topPt.setVisibility(View.VISIBLE);
                bottomPt.setVisibility(View.VISIBLE);
                leftPt.setVisibility(View.INVISIBLE);
                RightPt.setVisibility(View.INVISIBLE);
                topPt.animate().rotation(359).setDuration(EXPLENATION_DURATION).start();
                bottomPt.animate().rotation(359).setDuration(EXPLENATION_DURATION).start();
            }
            animateMeasurePoints(MesMode);


            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    rl.removeViewAt(rl.getChildCount()-1);
                }
            }, EXPLENATION_DURATION);
            rl.addView(measureExplainView, params);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void animateMeasurePoints(mesureMode mesMode) {
        LayoutInflater factory = LayoutInflater.from(this);
        final View measureExplainView = factory.inflate(layout.measure_alert_layout, null);

    }


    public void buildMeasurmentDialog() {    //MeasureStages measureStage
        try{
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);

            LayoutInflater factory = LayoutInflater.from(this);
            final View view = factory.inflate(layout.measure_alert_layout, null);
            final ImageView plusImg =(ImageView)view.findViewById(id.rotate);

            AnimationDrawable frameAnimation = null;
            if (plusImg != null) {
                plusImg.setVisibility(View.VISIBLE);
                frameAnimation = (AnimationDrawable)plusImg.getBackground();
                frameAnimation.start();

            }
            builder.setView(view);
            final AlertDialog alert = builder.create();
            alert.show();

        } catch (Exception e){
            e.printStackTrace();
        }


    }











}

