package com.sdu.factorymonitor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
    }

    @Test
    void shouldImportExcelAndGenerateAlarm() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sensor-data.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                buildWorkbook()
        );

        mockMvc.perform(multipart("/api/excel/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedRows").value(2))
                .andExpect(jsonPath("$.newAlarmCount").value(2));

        mockMvc.perform(get("/api/sensors/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sensorCode").value("S1"));

        mockMvc.perform(get("/api/alarms").param("status", "ACTIVE").param("sensorCode", "S1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldImportCsvAndGenerateAlarm() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sensor-data.csv",
                "text/csv",
                buildCsv().getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/excel/import").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.importedRows").value(2))
                .andExpect(jsonPath("$.newAlarmCount").value(2));

        mockMvc.perform(get("/api/alarms").param("status", "ACTIVE").param("sensorCode", "S3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/alarms").param("status", "ACTIVE").param("sensorCode", "S4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldUpdateAlarmRule() throws Exception {
        mockMvc.perform(put("/api/alarm-rule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tempMin": 17.5,
                                  "tempMax": 31.5,
                                  "humidityMin": 38.0,
                                  "humidityMax": 72.0,
                                  "enabled": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tempMin").value(17.5))
                .andExpect(jsonPath("$.tempMax").value(31.5))
                .andExpect(jsonPath("$.humidityMin").value(38.0))
                .andExpect(jsonPath("$.humidityMax").value(72.0))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    private byte[] buildWorkbook() throws Exception {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("data");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("sensor_code");
            header.createCell(1).setCellValue("temperature");
            header.createCell(2).setCellValue("humidity");
            header.createCell(3).setCellValue("collect_time");

            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("S1");
            row1.createCell(1).setCellValue(35.5);
            row1.createCell(2).setCellValue(82.4);
            row1.createCell(3).setCellValue(LocalDateTime.of(2026, 5, 21, 10, 0).toString());

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("S2");
            row2.createCell(1).setCellValue(26.1);
            row2.createCell(2).setCellValue(55.0);
            row2.createCell(3).setCellValue(LocalDateTime.of(2026, 5, 21, 10, 5).toString());

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String buildCsv() {
        return """
                sensor_code,temperature,humidity,collect_time
                S3,17.2,45.5,2026-05-21T10:10:00
                S4,26.8,74.1,2026-05-21T10:15:00
                """;
    }
}
