package com.davemorrissey.labs.subscaleview.sample; /**
 * Created by TC34677 on 13/04/2017.
 */
import android.util.Log;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;


public class ExcelWriter {

    ExcelData ed;
    int START_LINE = 52;
    int NUM_OF_LINES_TO_FILL = 30;

    public ExcelWriter(ExcelData iED){
        ed = iED;
    }

    public String WriteData(){
        try{
            XSSFWorkbook workbook = new XSSFWorkbook(ed.getInputStream());
            XSSFSheet sheet = workbook.getSheetAt(0);


            DecimalFormat df = new DecimalFormat("#.#");
            //clean older data if exists
            for (int i=START_LINE; i<START_LINE+NUM_OF_LINES_TO_FILL; i++){
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(2*ed.getSeriesNum()-1);
                cell.setCellValue("");
                cell = row.getCell(2*ed.getSeriesNum());
                cell.setCellValue("");
            }


            for (int i=START_LINE; i<START_LINE+ed.getHitList().size()-1; i++){
                double x = Double.parseDouble(df.format(ed.getHitList().get(i-START_LINE+1).x));
                double y = Double.parseDouble(df.format(ed.getHitList().get(i-START_LINE+1).y));
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(2*ed.getSeriesNum()-1);
                cell.setCellValue(x);
                cell = row.getCell(2*ed.getSeriesNum());
                cell.setCellValue(y);
            }
            String outFileName = ed.getFileName();
//            File Dir = ed.getDirectory();
//            File dir = new File(Dir + "/"+ ed.getProjectName());
//            if(!dir.exists() || !dir.isDirectory()) {
//                dir.mkdir();
//            }

            File outFile = new File(ed.getNewFileDir(), outFileName);
            OutputStream outputStream = new FileOutputStream(outFile.getAbsolutePath());
            workbook.write(outputStream);
            outputStream.flush();
            outputStream.close();
            return outFile.getAbsolutePath();


        } catch (Exception e){
            Log.d("ExcelWriter", e.toString());
            e.printStackTrace();
            return "";
        }

    }




}
