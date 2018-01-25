package decoster.cfdt.helper;

import android.content.Context;
import android.util.Log;


import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import decoster.cfdt.activity.MainActivity;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class TableBuilder {

    private WorkbookSettings ws;
    private File file;
    private Workbook wb;
    public final static String TAG = TableBuilder.class.getSimpleName();
    private final String DIRNAME = "xls_files";
    private File path;
    private boolean[][][] isComplete;
    private ArrayList<ArrayList<Integer>> remainingRows;

    public TableBuilder(File file, File path) {
        this.ws = new WorkbookSettings();
        this.ws.setEncoding("ISO-8859-1");

        this.remainingRows = new ArrayList<>();

        this.path = path;
        this.file = file;
        Log.d("TAG", file.getAbsolutePath());

    }

    public void createWB() {
        try {

            this.wb = Workbook.getWorkbook(file, ws);
            //wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        }
        if(wb != null) {
            this.isComplete = computeIsFinishedArray();
        }

    }




    private boolean[][][] computeIsFinishedArray() {
        boolean[][][] output = null;
        if (wb != null) {
            Sheet[] sheets = wb.getSheets();
            output = new boolean[sheets.length][][];
            for (int sheetI = 0; sheetI < sheets.length; sheetI++) {
                Sheet sheet = sheets[sheetI];
                remainingRows.add(new ArrayList<Integer>());
                output[sheetI] = new boolean[sheet.getRows()][sheet.getColumns()];

                for (int row = 0; row < sheet.getRows(); row++) {
                    boolean isComplete = true;
                    for (int col = 0; col < sheet.getColumns(); col++) {
                        if (sheet.getCell(col, row).getContents().equals("")) {
                            output[sheetI][row][col] = false;
                            isComplete = false;
                        } else {
                            output[sheetI][row][col] = true;
                        }
                    }
                    if (!isComplete) {
                        remainingRows.get(sheetI).add(new Integer(row));
                    }
                }


            }
        }
        return output;
    }




    public void update_cell(Sheet sh, int col, int row, String new_text) {

        WritableWorkbook wwb = null;

        try {
            wwb = Workbook.createWorkbook(file, wb);
            //wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WritableSheet sheet = wwb.getSheet(sh.getName());
        isComplete[getSheetIndex(sh)][row][col] = true;
        WritableCell cell = sheet.getWritableCell(col, row);
        CellFormat cellF = cell.getCellFormat();

        Label l = new Label(col, row, new_text, cellF);


        try {
            sheet.addCell(l);

            wwb.write();
            wwb.close();
            createWB();
            Log.d("TAG", new_text);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        }

    }



    public String createNewFileFromSheet(Sheet sheet) {
        boolean error = false;
        String newFileName = "tmp_" + getName();
        File newFile = new File(path, newFileName);
        WritableWorkbook copyDocument = null;

        // Avoid warning "Maximum number of format records exceeded. Using default format."
        Map<CellFormat, WritableCellFormat> definedFormats = new HashMap<CellFormat, WritableCellFormat>();
        try {
            Sheet sh = wb.getSheet(sheet.getName());

            copyDocument = Workbook.createWorkbook(newFile);
            WritableSheet targetSheet = copyDocument.createSheet(sheet.getName(), 0);

            for (int i = 0; i < sh.getRows(); i++) {
                for (int j = 0; j < sh.getColumns(); j++) {
                    Cell readCell = sh.getCell(j, i);
                        /*WritableCell newCell = readCell.copyTo(j,i);
                        CellFormat readFormat = readCell.getCellFormat();
                    //exception on the following line
                        if (readFormat != null) {
                            if (!definedFormats.containsKey(readFormat)) {
                                definedFormats.put(readFormat, new WritableCellFormat(readFormat));
                            }
                            newCell.setCellFormat(definedFormats.get(readFormat));
                        }*/
                    targetSheet.addCell(new Label(j, i, readCell.getContents()));
                }
            }
            copyDocument.write();
            copyDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
            error = true;
        } catch (RowsExceededException e) {
            e.printStackTrace();
            error = true;
        } catch (WriteException e) {
            e.printStackTrace();
            error = true;
        }

        if (error) return null;
        else {
            return newFile.getAbsolutePath();
        }


    }
    //boolean methode to check row/sheet validation
    public boolean isRowComplete(Sheet sheet, int row) {
        int sheetIndex =getSheetIndex(sheet);
        boolean[] cols = isComplete[sheetIndex][row];
        boolean isComplet = true;
        int rowI = 0;
        while (isComplet && rowI < cols.length) {
            if (!cols[rowI]) {
                isComplet = false;
            }
            rowI++;
        }
        if (isComplet) {
            remainingRows.get(sheetIndex).remove(Integer.valueOf(row));
        }
        return isComplet;
    }

    public boolean isSheetComplete(Sheet sheet) {
        //Log.d(MainActivity.class.getSimpleName(), remainingRows.get(getSheetIndex(sheet)).toString());
        return remainingRows.get(getSheetIndex(sheet)).isEmpty();
    }
    //Getters
    public String getName() {
        return  FilenameUtils.removeExtension(file.getName()).replaceAll("_", " ").toLowerCase();

    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public Sheet getSheet(String name) {
        return wb.getSheet(name);
    }

    public Workbook getWb() {
        return wb;
    }

    public File getFile() {
        return file;
    }

    public Sheet[] getSheets() {
        return wb.getSheets();
    }
    private int getSheetIndex(Sheet sheet) {
        Integer index = null;
        final Sheet[] sheets = wb.getSheets();
        for (int i = 0; i < sheets.length && index == null; i++) {
            if (sheets[i].getName().equals(sheet.getName())) {
                index = i;
            }
        }
        return (int) index;
    }
    public void closeFile() {
       wb.close();
    }

}
