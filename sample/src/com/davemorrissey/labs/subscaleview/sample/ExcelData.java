package com.davemorrissey.labs.subscaleview.sample;

import android.graphics.PointF;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by TC34677 on 18/05/2017.
 */

public class ExcelData {

    private File Directory;
    private String FileName;
    private InputStream InputStream;
    private ArrayList<PointF> HitList;
    private int SeriesNum;
    private String ProjectName;


    public  ExcelData(String iProjectName, int iSeriesNum ,File iDirectory, String iFileName,
                                                InputStream iStream, ArrayList<PointF> iHitList)
    {
        ProjectName = iProjectName;
        SeriesNum = iSeriesNum;
        Directory = iDirectory;
        FileName = iFileName;
        InputStream = iStream;
        HitList = iHitList;
    }


    public File getDirectory() {
        return Directory;
    }

    public String getFileName() {
        return FileName;
    }

    public InputStream getInputStream() {
        return InputStream;
    }

    public ArrayList<PointF> getHitList() {
        return HitList;
    }

    public int getSeriesNum() {
        return SeriesNum;
    }

    public String getProjectName() {
        return ProjectName;
    }

}
