package heitezy.reestr;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import static com.itextpdf.text.Font.NORMAL;
import static com.itextpdf.text.Rectangle.BOTTOM;
import static com.itextpdf.text.Rectangle.TOP;


class Convertor {

    private static final String[] seller = {"default", "Вента", "БАДМ", "Оптіма", "Юніфарма", "Дельта", "Фіто-Лек"};

    static void convert(String inputpath, String outputpath) throws IOException {
        File inputfiles = new File(inputpath);
        try {
            ArrayList<File> listoffiles = new ArrayList<>(Arrays.asList(Objects.requireNonNull(inputfiles.listFiles((file, filterstring) -> {
                if (filterstring.lastIndexOf('.') > 0) {
                    int lastIndex = filterstring.lastIndexOf('.');
                    String extension = filterstring.substring(lastIndex);
                    if (extension.equals(".xls")) {
                        return true;
                    } else return extension.equals(".csv");
                }
                return false;
            }))));
        HSSFWorkbook[] outputfiles = new HSSFWorkbook[listoffiles.size()];
        batchProcess(listoffiles, outputfiles, outputpath);
        } catch (NullPointerException e) {
            System.out.println("Невірний шлях до теки.");
        }
    }

    private static void batchProcess(ArrayList<File> filesToProcess, HSSFWorkbook[] wbToProcess, String outputpath) throws IOException {

        String[] wbname = new String[wbToProcess.length];

        for (int i = 0; i < wbToProcess.length; i++) {
            String XlsOrCSV = filesToProcess.get(i).toString();
            int lastIndex = XlsOrCSV.lastIndexOf('.');
            String extension = XlsOrCSV.substring(lastIndex);

            if (extension.equals(".csv")) {
                wbToProcess[i] = convertCsvToXls(filesToProcess.get(i).toString());
            } else {
                wbToProcess[i] = readWorkbook(filesToProcess.get(i).toString());
            }

            String filename = filesToProcess.get(i).toString();
            wbname [i] = outputpath + filename.substring(filename.lastIndexOf(File.separator));
        }
        magic(wbToProcess, wbname);
    }

    private static HSSFWorkbook readWorkbook(String filename) {
        try {
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(filename));
            return new HSSFWorkbook(fs.getRoot(), true);
        } catch (Exception e) {
            return null;
        }
    }

    private static void magic(HSSFWorkbook[] wbToProcess, String[] wbname) {
        for (int i = 0; i < wbToProcess.length; i++) {
            HSSFWorkbook wbToProcessSingle = wbToProcess[i];

            if ((wbname[i].substring(wbname[i].lastIndexOf(File.separator)).contains("Delta"))) {
                deltaMagic(wbToProcess[i], wbname[i]);
            } else {
                try {
                    String dateOfDocument = null;
                    int reestr_type = 0;

                    HSSFWorkbook templatewb = new HSSFWorkbook();
                    templatewb.createSheet("TempSheet");
                    HSSFSheet sheetSingle = wbToProcessSingle.getSheetAt(0);
                    HSSFSheet templateSheet = templatewb.getSheetAt(0);

                    if (sheetSingle.getRow(2).getCell(1,
                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().contains("Юніфарма")) {

                        String datecell = sheetSingle.getRow(2).getCell(2).toString();
                        int predate = datecell.lastIndexOf(' ');
                        dateOfDocument = datecell.substring(predate + 1);
                        reestr_type = 4;

                        cellIterate(sheetSingle, templateSheet, 2, 0);
                        wbname[i] = mkdirs(seller[reestr_type], wbname[i], dateOfDocument);
                    } else if (sheetSingle.getRow(4).getCell(1,
                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().contains("ВЕНТА. ЛТД")) {

                        String datecell = sheetSingle.getRow(4).getCell(2).toString();
                        int predate = datecell.lastIndexOf(' ');
                        dateOfDocument = datecell.substring(predate + 1);
                        reestr_type = 1;

                        cellIterate(sheetSingle, templateSheet, 4, 2);
                        wbname[i] = mkdirs(seller[reestr_type], wbname[i], dateOfDocument);
                    } /*else if (sheetSingle.getRow(5).getCell(1,
                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().contains("ФІТО-ЛЕК")) {

                        String datecell = sheetSingle.getRow(5).getCell(2).toString();
                        int predate = datecell.lastIndexOf(' ');
                        dateOfDocument = datecell.substring(predate + 1);
                        reestr_type = 6;

                        cellIterate(sheetSingle, templateSheet, 5, 3);
                        wbname[i] = mkdirs(seller[reestr_type], wbname[i], dateOfDocument);
                    } */else if (sheetSingle.getRow(7).getCell(1,
                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().contains("БаДМ")) {

                        String datecell = sheetSingle.getRow(7).getCell(2).toString();
                        int predate = datecell.lastIndexOf(' ');
                        dateOfDocument = datecell.substring(predate + 1);
                        reestr_type = 2;

                        cellIterate(sheetSingle, templateSheet, 7, 0);
                        wbname[i] = mkdirs(seller[reestr_type], wbname[i], dateOfDocument);
                    } else if (sheetSingle.getRow(8).getCell(1,
                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK).toString().contains("Оптiма-Фарм")) {

                        String datecell = sheetSingle.getRow(9).getCell(2).toString();
                        int predate = datecell.lastIndexOf(' ');
                        dateOfDocument = datecell.substring(predate + 1);
                        reestr_type = 3;

                        cellIterate(sheetSingle, templateSheet, 8, 3);
                        wbname[i] = mkdirs(seller[reestr_type], wbname[i], dateOfDocument);
                    }
                    convertToPdf(templatewb, wbname[i], dateOfDocument, reestr_type);
                } catch (Exception e) {
                    //todo move to new folder and show result dialog with path
                    System.out.println("Не вдалось сконвертувати: " + wbname[i]);
                }
            }
        }
    }

    private static void deltaMagic(HSSFWorkbook wbToProcess, String wbname) {
        try {
            String dateOfDocument;
            int reestr_type = 5;

            HSSFWorkbook templatewb = new HSSFWorkbook();
            templatewb.createSheet("TempSheet");
            HSSFSheet sheetSingle = wbToProcess.getSheetAt(0);
            HSSFSheet templateSheet = templatewb.getSheetAt(0);

            String findFirstRow = "\"ДЕЛЬТА МЕДІКЕЛ\" ліцензія";
            int firstRow = 0;

            for (Row row : sheetSingle) {
                if (row.getCell(2) != null) {
                    if (row.getCell(2).getRichStringCellValue().getString().contains(findFirstRow)) {
                        firstRow = row.getRowNum();
                        break;
                    }
                }
            }

            int rowCount = 0;
            for (Row row : sheetSingle) {
                if (row.getCell(2) != null) {
                    if (row.getCell(2).getRichStringCellValue().getString().contains(findFirstRow)) {
                        rowCount++;
                    }
                }
            }

            String datecell = sheetSingle.getRow(firstRow).getCell(8).toString();
            int predate = datecell.lastIndexOf(' ');
            dateOfDocument = datecell.substring(predate + 1);

            if (firstRow == 11) {
                for (int i = firstRow; i < (firstRow + rowCount); i++) {

                    HSSFRow rowSingle = sheetSingle.getRow(i);
                    HSSFRow rowTemplate = templateSheet.createRow(i - 2);

                    Iterator<Cell> cellIterator = rowSingle.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cellIn = cellIterator.next();
                        if (cellIn.getCellType() == CellType.BLANK) {
                            continue;
                        }
                        Cell cellOut = rowTemplate.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

                        switch (cellIn.getCellType()) {
                            case BOOLEAN:
                                cellOut.setCellValue(cellIn.getBooleanCellValue());
                                break;

                            case ERROR:
                                cellOut.setCellValue(cellIn.getErrorCellValue());
                                break;

                            case FORMULA:
                                cellOut.setCellFormula(cellIn.getCellFormula());
                                break;

                            case NUMERIC:
                                cellOut.setCellValue(cellIn.getNumericCellValue());
                                break;

                            case STRING:
                                cellOut.setCellValue(cellIn.getStringCellValue());
                                break;
                        }
                    }
                    Cell cellOut = rowTemplate.createCell(22, CellType.STRING);
                    cellOut.setCellValue("Відповідає");
                }
            } else {
                for (int i = firstRow; i < (firstRow + rowCount / 2); i++) {

                    HSSFRow rowSingle = sheetSingle.getRow(i);
                    HSSFRow rowTemplate = templateSheet.createRow(i - 18 + rowCount / 2);

                    Iterator<Cell> cellIterator = rowSingle.cellIterator();

                    while (cellIterator.hasNext()) {
                        Cell cellIn = cellIterator.next();
                        if (cellIn.getCellType() == CellType.BLANK) {
                            continue;
                        }
                        Cell cellOut = rowTemplate.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

                        switch (cellIn.getCellType()) {
                            case BOOLEAN:
                                cellOut.setCellValue(cellIn.getBooleanCellValue());
                                break;

                            case ERROR:
                                cellOut.setCellValue(cellIn.getErrorCellValue());
                                break;

                            case FORMULA:
                                cellOut.setCellFormula(cellIn.getCellFormula());
                                break;

                            case NUMERIC:
                                cellOut.setCellValue(cellIn.getNumericCellValue());
                                break;

                            case STRING:
                                if (cellIn.getStringCellValue().equals("Позитивний")) {
                                    cellOut.setCellValue("Відповідає");
                                } else {
                                    cellOut.setCellValue(cellIn.getStringCellValue());
                                }
                                break;
                        }
                    }
                }
            }
            wbname = mkdirs(seller[reestr_type], wbname, dateOfDocument);
            convertToPdf(templatewb, wbname, dateOfDocument, reestr_type);
        } catch (Exception e) {
            System.out.println("Невідомий формат");
        }
    }

    private static HSSFWorkbook convertCsvToXls(String csvFile) throws IOException {
        HSSFWorkbook tempwb = new HSSFWorkbook();
        HSSFSheet tempst = tempwb.createSheet("TempCsvSheet");

        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();
        CSVReader reader = new CSVReaderBuilder(new StringReader
                (new String(Files.readAllBytes(Paths.get(csvFile)), "Cp1251")))
                        .withCSVParser(parser)
                        .build();

        String[] nextLine;
        int rowNum = 0;
        int lineNum = 0;

        String[] csv_type = reader.readNext();

        if (csv_type[0].contains("Додаток")) {
            while ((nextLine = reader.readNext()) != null) {

                HSSFRow currentRow = tempst.createRow(rowNum++);
                lineNum++;

                if (rowNum<8) {
                    currentRow.createCell(0).setCellType(CellType.STRING);
                    currentRow.getCell(0).setCellValue(nextLine[0]);
                } else {
                    currentRow.createCell(0).setCellType(CellType.NUMERIC);
                    currentRow.getCell(0).setCellValue(lineNum-7);
                    for (int j = 1; j < 9; j++) {
                        currentRow.createCell(j).setCellType(CellType.STRING);
                        currentRow.getCell(j).setCellValue(nextLine[j-1]);
                    }
                    currentRow.createCell(9).setCellType(CellType.STRING);
                    currentRow.getCell(9).setCellValue("Відповідає");
                }
            }
        } else if (csv_type[0].contains("Реєстр")) {
            while ((nextLine = reader.readNext()) != null) {

                HSSFRow currentRow = tempst.createRow(rowNum++);

                for (int j = 0; j < 9; j++) {
                    currentRow.createCell(j).setCellType(CellType.STRING);
                    currentRow.getCell(j).setCellValue(nextLine[j]);
                }
                currentRow.createCell(9).setCellType(CellType.STRING);
                currentRow.getCell(9).setCellValue("Відповідає");

            }
        }
        return tempwb;
    }

    private static void convertToPdf(HSSFWorkbook wb, String wbname, String dateOfDocument, int reestr_type) throws DocumentException, IOException {
        HSSFSheet wsheet = Objects.requireNonNull(wb).getSheetAt(0);
        Iterator<Row> rowIterator = wsheet.iterator();
        Document xls_2_pdf = new Document(PageSize.A4.rotate());

        int lastIndex = wbname.lastIndexOf('.');
        String extension = wbname.substring(lastIndex);
        if (extension.equals(".csv")) {
            PdfWriter.getInstance(xls_2_pdf, new FileOutputStream(wbname.replace(".csv", ".pdf")));
        } else {
            PdfWriter.getInstance(xls_2_pdf, new FileOutputStream(wbname.replace(".xls", ".pdf")));
        }
        xls_2_pdf.open();

        float[] columnWidths;
        switch (reestr_type) {
            case (3):
                columnWidths = new float[]{(float) 1.5, 5, 4, 7, 5, 3, 4,(float) 3.5,(float) 3.5,(float) 4.5, 0};
                break;
            case (2):
                columnWidths = new float[]{(float) 1.5, 5, (float) 4.3, 7, 5, 3, 4,(float) 3.5,(float) 3.5,(float) 4.5};
                break;
            case (1):
                columnWidths = new float[]{(float) 1.5, (float) 5.2, 4, 7, 5, 3, 4,(float) 3.5,(float) 3.5,(float) 4.5};
                break;
            case (5):
                columnWidths = new float[]{(float) 1.5, (float) 5.8, (float) 4.4, 7, 5, 3, 4,(float) 3.5,(float) 3.5,(float) 4.5};
                break;
            default:
                columnWidths = new float[]{(float) 1.5, 5, 4, 7, 5, 3, 4,(float) 3.5,(float) 3.5,(float) 4.5};
        }

        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        BaseFont arial = BaseFont.createFont("resources/Arial.ttf", BaseFont.IDENTITY_H, true);
        Font font = new Font(arial, 10, NORMAL, GrayColor.GRAYBLACK);

        PdfPCell header_cell = new PdfPCell(new Phrase("Реєстр\nлікарських засобів, " +
                "які надійшли до суб'єкта господарювання\n" + MainWindow.organizationText +"\n ", font));
        if (reestr_type == 3) {
            header_cell.setColspan(11);
        } else {
            header_cell.setColspan(10);
        }
        header_cell.setVerticalAlignment(Element.ALIGN_CENTER);
        header_cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(header_cell);

        String[] columns = {"№ з/п", "Назва постачальника та номер ліцензії", "Номер та дата накладної",
                "Назва лікарського засобу та його лікарська форма, дата реєстрації та номер реєстраційного посвідчення",
                "Назва виробника", "Номер серії", "Номер і дата сертифіката якості виробника", "Кількість одержаних упаковок",
                "Термін придатності лікарського засобу", "Результат контролю уповноваженою особою"};
        if (reestr_type == 3) {
            for (int i = 0; i < 10; i++) {
                PdfPCell column_cell = new PdfPCell(new Phrase(columns[i], font));
                column_cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                if (i == 9) {
                    column_cell.setColspan(2);
                }
                table.addCell(column_cell);
            }
        } else {
            for (int i = 0; i < 10; i++) {
                PdfPCell column_cell = new PdfPCell(new Phrase(columns[i], font));
                column_cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(column_cell);
            }
        }

        PdfPCell table_cell;

        int rowCount = 0;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                if (rowCount%2==0 && reestr_type == 3) {
                    //todo Optimize code here
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            table_cell = new PdfPCell(new Phrase(String.valueOf(cell.getRichStringCellValue()), font));
                            table_cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                            table_cell.disableBorderSide(BOTTOM);
                            table.addCell(table_cell);
                            break;
                        case NUMERIC:
                            table_cell = new PdfPCell(new Phrase(String.valueOf((int) cell.getNumericCellValue()), font));
                            table_cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                            table_cell.disableBorderSide(BOTTOM);
                            table.addCell(table_cell);
                            break;
                        case ERROR:
                        case BLANK:
                            table_cell = new PdfPCell(new Phrase(" ", font));
                            table_cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
                            table_cell.disableBorderSide(BOTTOM);
                            table.addCell(table_cell);
                            break;
                    }
                } else if (reestr_type == 3){
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            table_cell = new PdfPCell(new Phrase(String.valueOf(cell.getRichStringCellValue()), font));
                            table_cell.setVerticalAlignment(Element.ALIGN_TOP);
                            table_cell.disableBorderSide(TOP);
                            table.addCell(table_cell);
                            break;
                        case NUMERIC:
                            table_cell = new PdfPCell(new Phrase(String.valueOf((int) cell.getNumericCellValue()), font));
                            table_cell.setVerticalAlignment(Element.ALIGN_TOP);
                            table_cell.disableBorderSide(TOP);
                            table.addCell(table_cell);
                            break;
                        case ERROR:
                        case BLANK:
                            table_cell = new PdfPCell(new Phrase(" ", font));
                            table_cell.setVerticalAlignment(Element.ALIGN_TOP);
                            table_cell.disableBorderSide(TOP);
                            table.addCell(table_cell);
                            break;
                    }
                } else {
                    Cell cell = cellIterator.next();
                    switch (cell.getCellType()) {
                        case STRING:
                            table_cell = new PdfPCell(new Phrase(String.valueOf(cell.getRichStringCellValue()), font));
                            table_cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            table.addCell(table_cell);
                            break;
                        case NUMERIC:
                            table_cell = new PdfPCell(new Phrase(String.valueOf((int) cell.getNumericCellValue()), font));
                            table_cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            table.addCell(table_cell);
                            break;
                        case ERROR:
                        case BLANK:
                            table_cell = new PdfPCell(new Phrase(" ", font));
                            table_cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                            table.addCell(table_cell);
                            break;
                    }
                }
            }
            rowCount++;
        }

        PdfPCell footer_cell = new PdfPCell(new Phrase("\nРезультат вхідного контролю якості лікарських засобів " +
                "здійснив — уповноважена особа " + MainWindow.personText + "\n" + dateOfDocument, font));
        if (reestr_type == 3) {
            footer_cell.setColspan(11);
        } else {
            footer_cell.setColspan(10);
        }
        footer_cell.setBorder(Rectangle.NO_BORDER);
        table.addCell(footer_cell);

        File scan = new File("resources/sign.jpg");
        if (!scan.exists()) {
            System.out.println("Не вибрано скан штампа.");
        } else {
            Image image = Image.getInstance("resources/sign.jpg");
            PdfPCell image_cell = new PdfPCell(image);
            if (reestr_type == 3) {
                image_cell.setColspan(11);
            } else {
                image_cell.setColspan(10);
            }
            image_cell.setPaddingLeft((float) 50);
            image_cell.setFixedHeight((float) 100);
            image_cell.setBorder(Rectangle.NO_BORDER);
            table.addCell(image_cell);
            table.keepRowsTogether(table.getLastCompletedRowIndex()-1);
        }
        xls_2_pdf.add(table);
        xls_2_pdf.close();
    }

    private static String mkdirs(String seller, String wbname, String dateOfDocument) {
        String month = dateOfDocument.substring(dateOfDocument.indexOf("."), dateOfDocument.lastIndexOf("."));
        switch (month) {
            case (".01"):
                month = "Січень";
                break;
            case (".02"):
                month = "Лютий";
                break;
            case (".03"):
                month = "Березень";
                break;
            case (".04"):
                month = "Квітень";
                break;
            case (".05"):
                month = "Травень";
                break;
            case (".06"):
                month = "Червень";
                break;
            case (".07"):
                month = "Липень";
                break;
            case (".08"):
                month = "Серпень";
                break;
            case (".09"):
                month = "Вересень";
                break;
            case (".10"):
                month = "Жовтень";
                break;
            case (".11"):
                month = "Листопад";
                break;
            case (".12"):
                month = "Грудень";
                break;
            default:
        }
        wbname = wbname.replace(wbname.substring(wbname.lastIndexOf(File.separator)),
                File.separator + seller + File.separator + dateOfDocument.substring(6, 10)+ File.separator
                        + month + File.separator + dateOfDocument + wbname.substring(wbname.lastIndexOf(File.separator)));
        String directory = wbname.substring(0, wbname.lastIndexOf(File.separator));
        File d = new File (directory);
        //noinspection ResultOfMethodCallIgnored
        d.mkdirs();
        return wbname;
    }

    private static void cellIterate (HSSFSheet sourcesheet, HSSFSheet templatesheet, int first_row_offset, int last_row_offset) {
        for (int j = first_row_offset; j < sourcesheet.getPhysicalNumberOfRows() - last_row_offset; j++) {

            HSSFRow rowSingle = sourcesheet.getRow(j);
            HSSFRow rowTemplate = templatesheet.createRow(j - first_row_offset);

            Iterator<Cell> cellIterator = rowSingle.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cellIn = cellIterator.next();
                Cell cellOut = rowTemplate.createCell(cellIn.getColumnIndex(), cellIn.getCellType());

                switch (cellIn.getCellType()) {
                    case BLANK:
                        break;

                    case BOOLEAN:
                        cellOut.setCellValue(cellIn.getBooleanCellValue());
                        break;

                    case ERROR:
                        cellOut.setCellValue(cellIn.getErrorCellValue());
                        break;

                    case FORMULA:
                        cellOut.setCellFormula(cellIn.getCellFormula());
                        break;

                    case NUMERIC:
                        cellOut.setCellValue(cellIn.getNumericCellValue());
                        break;

                    case STRING:
                        cellOut.setCellValue(cellIn.getStringCellValue());
                        break;
                }
            }
        }
    }
}
