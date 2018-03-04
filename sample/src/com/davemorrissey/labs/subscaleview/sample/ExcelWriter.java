package com.davemorrissey.labs.subscaleview.sample; /**
 * Created by TC34677 on 13/04/2017.
 */
import android.util.Log;

import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.Hyperlink;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.IndexedColors;
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

    private final String TAG = getClass().getName();
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
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String WriteData(){
        try{
            Row row;
            Cell cell;
            row = sheet.getRow(14);
            cell = row.getCell(2*ed.getSeriesNum()-1);
            cell.setCellValue(ed.getRange());

            row = sheet.getRow(19);
            cell = row.getCell(2*ed.getSeriesNum()-1);
            cell.setCellValue(ed.getFireType());

            row = sheet.getRow(24);
            cell = row.getCell(2*ed.getSeriesNum()-1);
            cell.setCellValue(ed.getCamType());


            DecimalFormat df = new DecimalFormat("#.#");
            //clean older data if exists
            for (int i=START_LINE; i<START_LINE+NUM_OF_LINES_TO_FILL; i++){
                row = sheet.getRow(i);
                cell = row.getCell(2*ed.getSeriesNum()-1);
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
                row = sheet.getRow(i);
                cell = row.getCell(2*ed.getSeriesNum()-1);
                cell.setCellValue(x);
                cell = row.getCell(2*ed.getSeriesNum());
                cell.setCellValue(y);
            }
            String outFileName = ed.getFileName();

//            addImage();
            addImageHyperLink();
//          evaluateCellsFormulas();

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

    private void addImageHyperLink() {
        try{
            HSSFHyperlink file_link=new HSSFHyperlink(HSSFHyperlink.LINK_FILE);
            String address = ed.getImagePath();
            file_link.setAddress(address);

            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            style.setFillPattern(CellStyle.SOLID_FOREGROUND);
            Font font = workbook.createFont();
            font.setColor(HSSFColor.WHITE.index);
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);


            String sheetName = String.valueOf(ed.getSeriesNum());
            HSSFSheet sheet = workbook.getSheet(sheetName);
            Row row = sheet.getRow(38);
            Cell cell = row.createCell(13);
            cell.setCellValue("Original Hits Image");
            cell.setHyperlink(file_link);
            cell.setCellStyle(style);


        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void evaluateCellsFormulas() {
        try{
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            int numOfSheets = workbook.getNumberOfSheets();
            for (int i=0; i<numOfSheets; i++) {
                HSSFSheet sheet = workbook.getSheetAt(i);
                for (Row r : sheet) {
                    for (Cell c : r) {
                        if (c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                            evaluator.evaluateFormulaCell(c);
                            Log.d(TAG, "Evaluated: sheet: "+c.getSheet().getSheetName()+" row: "+c.getRow().getRowNum()+" col: "+c.getColumnIndex());

                        }
                    }
                }
            }
        } catch (Exception e){
            Log.d(TAG, e.getStackTrace().toString());
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
            int measure = 2250;
            sheet.setDefaultRowHeight((short) 1000);

            for (int i = 0; i<50; i++){
                sheet.setColumnWidth(i,measure);
            }

            // Create the drawing patriarch.  This is the top level container for all shapes.
            Drawing drawing = sheet.createDrawingPatriarch();

            //add a picture shape
            ClientAnchor anchor = helper.createClientAnchor();
            //set top-left corner of the picture,
            //subsequent call of Picture#resize() will operate relative to it
            anchor.setCol1(0);
            anchor.setCol2(8);
            anchor.setRow1(0);
            anchor.setRow1(11);
            Picture pict = drawing.createPicture(anchor, pictureIdx);

            //auto-size picture relative to its top-left corner
//            pict.resize();
        } catch (Exception e) {
            Log.e("ExcelWriter:",e.getStackTrace().toString());
        }
    }






}
