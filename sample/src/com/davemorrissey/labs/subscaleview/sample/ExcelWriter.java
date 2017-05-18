package com.davemorrissey.labs.subscaleview.sample; /**
 * Created by TC34677 on 13/04/2017.
 */
import android.util.Log;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;



public class ExcelWriter {

    private String filename;

    public ExcelWriter(String filename){
        filename = filename;
    }

    public void WriteData(){
        try{
            //Read the spreadsheet that needs to be updated
            FileInputStream fsIP= new FileInputStream(new File(filename));
            //Access the workbook
            XSSFWorkbook wb = new XSSFWorkbook(fsIP);
            //Access the worksheet, so that we can update / modify it.
            XSSFSheet worksheet = wb.getSheetAt(0);
            // declare a Cell object
            Cell cell = null;
            // Access the second cell in second row to update the value
            cell = worksheet.getRow(57).getCell(1);
            // Get current cell value value and overwrite the value
            cell.setCellValue("OverRide existing value");
            //Close the InputStream
            fsIP.close();
            //Open FileOutputStream to write updates
            FileOutputStream output_file =new FileOutputStream(new File(filename));
            //write changes
            wb.write(output_file);
            //close the stream
            output_file.close();
        } catch (Exception e){
            Log.d("ExcelWriter", e.toString());
        }

    }

}
