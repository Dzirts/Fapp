package com.davemorrissey.labs.subscaleview.sample;

/**
 * Created by Elbit on 8/14/2017.
 */

public class InternalData {
    private int id;
    private String XlsFileName;
    private String XlsFilePath;
    private String XlsFileDirectory;
    private String ProjectName;
    private String CurrentSeries;
    public InternalData()
    {
    }
    public InternalData(int id, String projectName, String SeriesNumber , String xlsName,String xlsPath, String xlsDirectory)
    {
        this.id=id;
        this.ProjectName = projectName;
        this.CurrentSeries = SeriesNumber;
        this.XlsFileName = xlsName;
        this.XlsFilePath = xlsPath;
        this.XlsFileDirectory = xlsDirectory;

    }

    // setters

    public void setId(int id) {
        this.id = id;
    }

    public void setXlsFilePath(String path) {
        this.XlsFilePath = path;
    }
    public void setXlsFileName(String name) {
        this.XlsFileName = name;
    }

    public void setXlsFileDirectory(String directory) {
        this.XlsFileDirectory = directory;
    }

    public void setProjectname(String projName) {
        this.ProjectName = projName;
    }

    public void setSeries(String series) {
        this.CurrentSeries = series;
    }


    // getters

    public String getXlsFileName() {
        return XlsFileName;
    }

    public String getXlsFilePath() {
        return XlsFilePath;
    }

    public String getXlsFileDirectory() {
        return XlsFileDirectory;
    }

    public String getProjectName() {
        return ProjectName;
    }

    public String getCurrentSeries() {
        return CurrentSeries;
    }


    public int getId() {
        return id;
    }
}