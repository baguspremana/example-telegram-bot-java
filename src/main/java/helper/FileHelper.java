package helper;

import com.itextpdf.text.pdf.PdfWriter;
import model.DatabaseConnection;
import model.Mahasiswa;
import model.Outbox;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileHelper {

    MessageBotDAO messageBotDAO = new MessageBotDAO();

    public FileHelper() {

    }

    public String getSQL(String keyword, Long chat_id, Outbox outbox){
        String sql = "SELECT processing.`sql` FROM processing WHERE processing.`format` LIKE '%"+keyword+"%';";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                String query = rs.getString("sql");

//                getDataMahasiswa(query);
                outbox.setMessage(query);

                messageBotDAO.insertInbox(chat_id, keyword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return outbox.getMessage();
    }
    public List<Mahasiswa> getDataMahasiswa(String query){
        List<Mahasiswa> mahasiswaList = new ArrayList<>();
        String sql = query;

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()!= false){
                do {

                    int id = rs.getInt("id");
                    String nim = rs.getString("nim");
                    String nama = rs.getString("nama");
                    String prodi = rs.getString("prodi");
                    String alamat = rs.getString("alamat");

                    Mahasiswa tmp = new Mahasiswa(id, nim, nama, prodi, alamat);
                    mahasiswaList.add(tmp);

                }while (rs.next());
            }

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error");
        }

        return mahasiswaList;
    }

    public void generateFile(List<Mahasiswa> mahasiswas){
        String[] columns = {"No", "NIM", "Nama", "Prodi", "Alamat"};
        List<Mahasiswa> mahasiswaList;
        mahasiswaList = mahasiswas;

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Mahasiswa");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);
        headerFont.setColor(IndexedColors.RED.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        for (int i=0; i < columns.length; i++){
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        int rowNum = 1;

        for (Mahasiswa mahasiswa : mahasiswaList){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(mahasiswa.getId());
            row.createCell(1).setCellValue(mahasiswa.getNim());
            row.createCell(2).setCellValue(mahasiswa.getNama());
            row.createCell(3).setCellValue(mahasiswa.getProdi());
            row.createCell(4).setCellValue(mahasiswa.getAlamat());
        }

        for (int i=0; i < columns.length; i++){
            sheet.autoSizeColumn(i);
        }

        try {
            FileOutputStream fileOut = new FileOutputStream("D:/File/mahasiswa.xlsx");
            workbook.write(fileOut);
            fileOut.close();
            System.out.println("Berhasil");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("GAGAL");
        }
    }

    public void convertToPDF(String fileName){
        try {
            FileInputStream input_document = new FileInputStream(new File(fileName));
            XSSFWorkbook my_xls_workbook = new XSSFWorkbook(input_document);
            XSSFSheet my_worksheet = my_xls_workbook.getSheetAt(0);
            Iterator<Row> rowIterator = my_worksheet.iterator();
            Document iText_xls_2_pdf = new Document();
            PdfWriter.getInstance(iText_xls_2_pdf, new FileOutputStream("D:/File/mahasiswa.pdf"));
            iText_xls_2_pdf.open();
            PdfPTable my_table = new PdfPTable(5);
            PdfPCell table_cell;

             do {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                 do {
                    Cell cell = cellIterator.next();
                    switch(cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            table_cell=new PdfPCell(new Phrase(NumberToTextConverter.toText(cell.getNumericCellValue())));
                            my_table.addCell(table_cell);
                            break;

                        case Cell.CELL_TYPE_STRING:
                            table_cell=new PdfPCell(new Phrase(cell.getStringCellValue()));
                            my_table.addCell(table_cell);
                            break;
                    }
                }while(cellIterator.hasNext());

            }while(rowIterator.hasNext());

            iText_xls_2_pdf.add(my_table);
            iText_xls_2_pdf.close();
            input_document.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void uploadFile(String file){
        System.setProperty("webdriver.chrome.driver", "D:\\File\\chromedriver_win32\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get("https://itcc-udayana.com/coba");
        WebElement browse = driver.findElement(By.id("uploadBox"));
        browse.sendKeys(file);
        try {
            Thread.sleep(2000);
            driver.findElement(By.id("submit")).click();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
