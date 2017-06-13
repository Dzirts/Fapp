package com.davemorrissey.labs.subscaleview.sample; /**
 * Created by TC34677 on 13/04/2017.
 */

import android.graphics.PointF;
import android.util.Log;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


public class ExcelReader {

    InputStream mIS;
    XSSFWorkbook workbook;
    XSSFSheet sheet;


    int START_LINE = 52;
    int NUM_OF_LINES_TO_FILL = 30;
    int NUM_OF_SERIES_IN_FILE = 20;
;
    public ExcelReader(InputStream is){
        mIS = is;
        try{
            workbook = new XSSFWorkbook(mIS);
            sheet = workbook.getSheetAt(0);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private ArrayList<PointF> readCol(int i){
        ArrayList<PointF> colHits = new ArrayList<PointF>();
        try{
            for (int k=START_LINE; k<START_LINE+NUM_OF_LINES_TO_FILL; k++){
                Row row = sheet.getRow(k);
                float cell_x = (float)row.getCell(2*i-1).getNumericCellValue();
                float cell_y = (float)row.getCell(2*i).getNumericCellValue();
                PointF pf = new PointF(cell_x,cell_y);
                colHits.add(pf);
            }
        } catch (Exception e){
            Log.d("ExcelReader", e.toString());
            Log.d("ExcelReader", e.getStackTrace().toString());
        }
        return colHits;
    }

    public List<Integer> getAllFilledInCols(){
        List<Integer> indexList = new ArrayList<>();
        try{
            Row row = sheet.getRow(START_LINE);
            for (int i = 1; i< NUM_OF_SERIES_IN_FILE+1; i++){
                Cell cell = row.getCell(2*i-1);
                double d = cell.getNumericCellValue();
                if (cell != null && d!=0.0){
                    indexList.add(i);
                }
            }
            return indexList;
        } catch (Exception e){
            Log.d("ExcelReader", e.toString());
            Log.d("ExcelReader", e.getStackTrace().toString());
        }
        return indexList;
    }

    public ArrayList<ArrayList<PointF>> getAllHitsByIndexes(List<Integer> indexList){
        ArrayList<ArrayList<PointF>> indexHitsList = new ArrayList<ArrayList<PointF>>();
        for(int i: indexList){
            indexHitsList.add(readCol(i));
        }
        return indexHitsList;
    }

}
