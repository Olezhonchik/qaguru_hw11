package ru.vkusvillTests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.*;


public class FileParsingTest {

    private final ClassLoader cl = FileParsingTest.class.getClassLoader();

    @Test
    void zipParsingTest() throws Exception {
        try (InputStream is = cl.getResourceAsStream("files/random_test_data.zip");
             ZipInputStream zis = new ZipInputStream(is)) {

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                System.out.println("Файл в архиве: " + fileName);

                byte[] fileBytes = zis.readAllBytes();
                try (InputStream fileStream = new ByteArrayInputStream(fileBytes)) {
                    if (fileName.endsWith(".pdf")) {
                        validatePdf(fileStream);
                    } else if (fileName.endsWith(".csv")) {
                        validateCsv(fileStream);
                    } else if (fileName.endsWith(".xlsx")) {
                        validateXlsx(fileStream);
                    } else {
                        System.out.println("Неизвестный тип файла: " + fileName);
                    }
                }
            }
        }
    }

    private void validatePdf(InputStream stream) throws IOException {
        PDF pdf = new PDF(stream);
        System.out.println("Проверка PDF...");

        if (pdf.text.contains("Column_1")) {
            System.out.println("Проверка пройдена успешно");
        } else {
            System.out.println("PDF не содержит ожидаемого текста 'Column_1'");
        }
    }

    private void validateCsv(InputStream stream) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(stream))) {
            List<String[]> rows = reader.readAll();
            System.out.println("Проверка CSV...");
            assertEquals("id", rows.get(0)[0], "Первый заголовок в CSV не 'id'");
            assertEquals(100, rows.size() - 1, "CSV не содержит 100 строк данных");
        }
    }

    private void validateXlsx(InputStream stream) throws Exception {
        XLS xls = new XLS(stream);
        String firstCell = xls.excel.getSheetAt(0)
                .getRow(0).getCell(0).getStringCellValue();
        System.out.println("Проверка XLSX...");
        assertEquals("id", firstCell, "Первый заголовок в XLSX не 'id'");
        int rowCount = xls.excel.getSheetAt(0).getPhysicalNumberOfRows() - 1;
        assertEquals(100, rowCount, "XLSX не содержит 100 строк данных");
    }

}

