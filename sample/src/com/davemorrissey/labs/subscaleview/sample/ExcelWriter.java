package com.davemorrissey.labs.subscaleview.sample; /**
 * Created by TC34677 on 13/04/2017.
 */
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.ss.formula.functions.Hyperlink;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;


public class ExcelWriter {

    private ExcelData ed;
    private  int START_LINE = 51;
    private  int NUM_OF_LINES_TO_FILL = 30;
    private  HSSFWorkbook workbook = null;
    private  HSSFSheet sheet = null;

    public ExcelWriter(ExcelData iED){
        ed = iED;
        try {
            workbook = new HSSFWorkbook(ed.getInputStream());
            sheet = workbook.getSheetAt(0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String WriteData(){
        try{




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
                NumberFormat nf = NumberFormat.getInstance();
                Number parsedDouble = nf.parse(df.format(ed.getHitList().get(i-START_LINE+1).x));
                double x =  parsedDouble.doubleValue();
                parsedDouble = nf.parse(df.format(ed.getHitList().get(i-START_LINE+1).y));
                double y =  parsedDouble.doubleValue();
                Row row = sheet.getRow(i);
                Cell cell = row.getCell(2*ed.getSeriesNum()-1);
                cell.setCellValue(x);
                cell = row.getCell(2*ed.getSeriesNum());
                cell.setCellValue(y);
            }
            String outFileName = ed.getFileName();

            addImage();

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


    private void addImage(){
        try {
            //add picture data to this workbook.
            InputStream is = new FileInputStream(ed.getImagePath());
            byte[] bytes = IOUtils.toByteArray(is);
            int pictureIdx = workbook.addPicture(bytes, workbook.PICTURE_TYPE_JPEG);
            is.close();

            CreationHelper helper = workbook.getCreationHelper();
            //create sheet
            Sheet sheet = workbook.createSheet(ed.getSeriesNum()+" image");

            //set cell in same width and height
            int measure = 225;
            sheet.setDefaultRowHeight((short) 100);

            for (int i = 0; i<50; i++){
                sheet.setColumnWidth(i,measure);
            }

            // Create the drawing patriarch.  This is the top level container for all shapes.
            Drawing drawing = sheet.createDrawingPatriarch();

            //add a picture shape
            ClientAnchor anchor = helper.createClientAnchor();
            //set top-left corner of the picture,
            //subsequent call of Picture#resize() will operate relative to it
            anchor.setCol1(40);
            anchor.setRow1(50);
            Picture pict = drawing.createPicture(anchor, pictureIdx);

            //auto-size picture relative to its top-left corner
//            pict.resize();
        } catch (Exception e) {
            Log.e("ExcelWriter:",e.getStackTrace().toString());
        }
    }






}
