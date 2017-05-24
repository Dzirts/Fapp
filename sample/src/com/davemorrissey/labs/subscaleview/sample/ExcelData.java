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
    private String FilePath;
    private String newFileDir;
    private InputStream InputStream;
    private ArrayList<PointF> HitList;
    private int SeriesNum;
    private String ProjectName;


    public  ExcelData(String iProjectName, int iSeriesNum ,File iDirectory, String iFileName, String iFilePath, String iNewFileDir,
                                                InputStream iStream, ArrayList<PointF> iHitList)
    {
        ProjectName = iProjectName;
        SeriesNum = iSeriesNum;
        Directory = iDirectory;
        FilePath = iFilePath;
        InputStream = iStream;
        HitList = iHitList;
        newFileDir = iNewFileDir;
        FileName = iFileName;


    }


    public File getDirectory() {
        return Directory;
    }

    public String getFileName() {
        return FileName;
    }

    public String getFilePath() {
        return FilePath;
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

    public String getNewFileDir() {
        return newFileDir;
    }




}
