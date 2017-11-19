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

package com.davemorrissey.labs.subscaleview.sample.extension.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Pair;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.davemorrissey.labs.subscaleview.sample.MainActivity;
import com.davemorrissey.labs.subscaleview.sample.R;
import com.davemorrissey.labs.subscaleview.sample.R.drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PinView extends SubsamplingScaleImageView {

    private Context context;
    private PointF sPin;
    private ArrayList<Pair<PointF, String>> mapPins;
    private String tag = getClass().getSimpleName();
    private int nNewColor = drawable.circ_light_blue;
    private int nOldColor = drawable.circ_bordo;
    private HashMap<Integer, Integer> mColorsHash;
    private float fSize = 600f;


    public PinView(Context context) {
        this(context, null);
        this.context = context;
        initialiseHashMap();
    }


    public PinView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
        initialise();
        initialiseHashMap();

    }

    private void initialiseHashMap() {
        mColorsHash = new HashMap<>();
        mColorsHash.put(R.id.new_circ_blue, drawable.circ_blue);
        mColorsHash.put(R.id.new_circ_light_blue, drawable.circ_light_blue);
        mColorsHash.put(R.id.new_circ_green, drawable.circ_green);
        mColorsHash.put(R.id.new_circ_purple, drawable.circ_purple);
        mColorsHash.put(R.id.new_circ_orange, drawable.circ_orange);
        mColorsHash.put(R.id.new_circ_yellow, drawable.circ_yellow);

        mColorsHash.put(R.id.old_circ_red, drawable.circ_red);
        mColorsHash.put(R.id.old_circ_bordo, drawable.circ_bordo);
        mColorsHash.put(R.id.old_circ_green, drawable.circ_green);
        mColorsHash.put(R.id.old_circ_purple, drawable.circ_purple);
        mColorsHash.put(R.id.old_circ_orange, drawable.circ_orange);
        mColorsHash.put(R.id.old_circ_yellow, drawable.circ_yellow);
    }


    public void setColor(String type, int id) {
        switch (type) {
            case "new":
                nNewColor = mColorsHash.get(id);
                break;
            case "old":
                nOldColor = mColorsHash.get(id);
                break;
        }
    }

    public void setPin(PointF sPin) {
        this.sPin = sPin;
//        initialise();
//        invalidate();
    }
    public void setPins(ArrayList<Pair<PointF, String>>  mapPins) {
        this.mapPins = mapPins;
        initialise();
        invalidate();
    }

    public void setSize(int id) {
        fSize = (float)id;
    }

    public PointF getPin() {
        return sPin;
    }

    private void initialise() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady()) {
            return;
        }
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        float density = getResources().getDisplayMetrics().densityDpi;
        if (mapPins == null || mapPins.size()==0){ return;}

        for (int i = 0; i < mapPins.size(); i++) {

            float w =0,h=0;
            PointF mPin = mapPins.get(i).first;
            String pinLabel = mapPins.get(i).second;
            Bitmap bmpPin = null;
            switch (pinLabel){
                case "CenterBig":
                    bmpPin = BitmapFactory.decodeResource(this.getResources(), drawable.red_curse_big5);
                    w = (density / 200f) * bmpPin.getWidth();
                    h = (density / 200f) * bmpPin.getHeight();
                    break;
                case "CenterSmall":
                    bmpPin = BitmapFactory.decodeResource(this.getResources(), drawable.red_curse_big5);
                    w = (density / 300f) * bmpPin.getWidth();
                    h = (density / 300f) * bmpPin.getHeight();
                    break;
                case "newHit":
                    bmpPin = BitmapFactory.decodeResource(this.getResources(), nNewColor);
                    w = (density / fSize) * bmpPin.getWidth();
                    h = (density / fSize) * bmpPin.getHeight();
                    break;
                case "oldHit":
                    bmpPin = BitmapFactory.decodeResource(this.getResources(), nOldColor);
                    w = (density / fSize) * bmpPin.getWidth();
                    h = (density / fSize) * bmpPin.getHeight();
                    break;
                case "mesurePt":
                    bmpPin = BitmapFactory.decodeResource(this.getResources(), drawable.measure_point);
                    w = (density / 400f) * bmpPin.getWidth();
                    h = (density / 400f) * bmpPin.getHeight();
                    break;
            }

            bmpPin = Bitmap.createScaledBitmap(bmpPin, (int) w, (int) h, true);

            PointF vPin = sourceToViewCoord(mPin);
            float vX = vPin.x - (bmpPin.getWidth() / 2);
            float vY = vPin.y - (bmpPin.getHeight() /2);
            canvas.drawBitmap(bmpPin, vX, vY, paint);



        }

    }


}
