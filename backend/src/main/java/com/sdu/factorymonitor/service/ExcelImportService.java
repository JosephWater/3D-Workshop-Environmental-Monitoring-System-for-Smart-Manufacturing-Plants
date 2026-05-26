package com.sdu.factorymonitor.service;

import com.sdu.factorymonitor.dto.ExcelImportResponse;
import com.sdu.factorymonitor.entity.AlarmRule;
import com.sdu.factorymonitor.entity.SensorData;
import com.sdu.factorymonitor.repository.SensorDataRepository;
import com.sdu.factorymonitor.repository.SensorInfoRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ExcelImportService {

    private static final List<String> REQUIRED_HEADERS = List.of("sensor_code", "temperature", "humidity", "collect_time");
    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    );

    private final SensorInfoRepository sensorInfoRepository;
    private final SensorDataRepository sensorDataRepository;
    private final AlarmRuleService alarmRuleService;
    private final MonitoringService monitoringService;

    @Transactional
    public ExcelImportResponse importExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传非空的数据文件");
        }

        AlarmRule rule = alarmRuleService.getCurrentRuleEntity();
        ImportStats stats;
        String originalFilename = file.getOriginalFilename();

        try {
            if (isCsvFile(originalFilename)) {
                stats = importCsv(file, rule);
            } else {
                stats = importWorkbook(file, rule);
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("文件读取失败: " + exception.getMessage(), exception);
        }

        return new ExcelImportResponse(stats.importedRows(), stats.newAlarmCount(), originalFilename, LocalDateTime.now());
    }

    private ImportStats importWorkbook(MultipartFile file, AlarmRule rule) throws IOException {
        ImportStats stats = new ImportStats();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("表头不能为空");
            }

            Map<String, Integer> headerIndexMap = parseHeader(headerRow);

            for (int rowIndex = headerRow.getRowNum() + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isBlankRow(row, headerIndexMap)) {
                    continue;
                }

                String sensorCode = readString(row.getCell(headerIndexMap.get("sensor_code"))).toUpperCase(Locale.ROOT);
                SensorData sensorData = SensorData.builder()
                        .sensorCode(verifySensorCode(sensorCode))
                        .temperature(readDecimal(row.getCell(headerIndexMap.get("temperature"))))
                        .humidity(readDecimal(row.getCell(headerIndexMap.get("humidity"))))
                        .collectTime(readDateTime(row.getCell(headerIndexMap.get("collect_time"))))
                        .sourceFile(file.getOriginalFilename())
                        .build();

                persistAndEvaluate(sensorData, rule, stats);
            }
        }

        return stats;
    }

    private ImportStats importCsv(MultipartFile file, AlarmRule rule) throws IOException {
        ImportStats stats = new ImportStats();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new IllegalArgumentException("CSV 表头不能为空");
            }

            Map<String, Integer> headerIndexMap = parseHeader(headerLine);
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }

                String[] columns = splitCsvLine(line);
                if (isBlankRow(columns, headerIndexMap)) {
                    continue;
                }

                try {
                    String sensorCode = readColumn(columns, headerIndexMap, "sensor_code").toUpperCase(Locale.ROOT);
                    SensorData sensorData = SensorData.builder()
                            .sensorCode(verifySensorCode(sensorCode))
                            .temperature(readDecimal(readColumn(columns, headerIndexMap, "temperature")))
                            .humidity(readDecimal(readColumn(columns, headerIndexMap, "humidity")))
                            .collectTime(readDateTime(readColumn(columns, headerIndexMap, "collect_time")))
                            .sourceFile(file.getOriginalFilename())
                            .build();

                    persistAndEvaluate(sensorData, rule, stats);
                } catch (IllegalArgumentException exception) {
                    throw new IllegalArgumentException("CSV 第 " + lineNumber + " 行数据无效: " + exception.getMessage(), exception);
                }
            }
        }

        return stats;
    }

    private void persistAndEvaluate(SensorData sensorData, AlarmRule rule, ImportStats stats) {
        SensorData saved = sensorDataRepository.save(sensorData);
        stats.incrementImportedRows();
        stats.addNewAlarmCount(monitoringService.evaluateAndPersistAlarms(saved, rule));
    }

    private String verifySensorCode(String sensorCode) {
        if (sensorCode.isBlank()) {
            throw new IllegalArgumentException("传感器编号不能为空");
        }
        sensorInfoRepository.findBySensorCode(sensorCode)
                .orElseThrow(() -> new IllegalArgumentException("未知传感器编号: " + sensorCode));
        return sensorCode;
    }

    private Map<String, Integer> parseHeader(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            map.put(readString(cell).toLowerCase(Locale.ROOT).trim(), cell.getColumnIndex());
        }
        validateRequiredHeaders(map);
        return map;
    }

    private Map<String, Integer> parseHeader(String headerLine) {
        Map<String, Integer> map = new HashMap<>();
        String[] headers = splitCsvLine(stripUtf8Bom(headerLine));
        for (int index = 0; index < headers.length; index++) {
            map.put(headers[index].trim().toLowerCase(Locale.ROOT), index);
        }
        validateRequiredHeaders(map);
        return map;
    }

    private void validateRequiredHeaders(Map<String, Integer> map) {
        for (String header : REQUIRED_HEADERS) {
            if (!map.containsKey(header)) {
                throw new IllegalArgumentException("缺少必需列: " + header);
            }
        }
    }

    private boolean isBlankRow(Row row, Map<String, Integer> headerIndexMap) {
        return REQUIRED_HEADERS.stream()
                .map(headerIndexMap::get)
                .map(row::getCell)
                .allMatch(cell -> cell == null || cell.getCellType() == CellType.BLANK || readString(cell).isBlank());
    }

    private boolean isBlankRow(String[] columns, Map<String, Integer> headerIndexMap) {
        return REQUIRED_HEADERS.stream()
                .map(headerIndexMap::get)
                .allMatch(index -> index >= columns.length || columns[index].isBlank());
    }

    private String readColumn(String[] columns, Map<String, Integer> headerIndexMap, String header) {
        Integer index = headerIndexMap.get(header);
        if (index == null || index >= columns.length) {
            throw new IllegalArgumentException("缺少列值: " + header);
        }
        return columns[index].trim();
    }

    private String[] splitCsvLine(String line) {
        return stripUtf8Bom(line).split("\\s*,\\s*", -1);
    }

    private String stripUtf8Bom(String text) {
        return text != null && !text.isEmpty() && text.charAt(0) == '\uFEFF' ? text.substring(1) : text;
    }

    private boolean isCsvFile(String filename) {
        return filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".csv");
    }

    private String readString(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.rint(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield BigDecimal.valueOf(numericValue).stripTrailingZeros().toPlainString();
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            case BLANK, _NONE, ERROR -> "";
        };
    }

    private BigDecimal readDecimal(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("存在空数值单元格");
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).setScale(2, RoundingMode.HALF_UP);
            case STRING -> readDecimal(cell.getStringCellValue());
            default -> throw new IllegalArgumentException("数值列格式不正确");
        };
    }

    private BigDecimal readDecimal(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("存在空数值");
        }
        try {
            return new BigDecimal(text.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("数值列格式不正确: " + text, exception);
        }
    }

    private LocalDateTime readDateTime(Cell cell) {
        if (cell == null) {
            throw new IllegalArgumentException("采集时间不能为空");
        }
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return readDateTime(readString(cell));
    }

    private LocalDateTime readDateTime(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("采集时间不能为空");
        }
        for (DateTimeFormatter formatter : TIME_FORMATTERS) {
            try {
                return LocalDateTime.parse(text.trim(), formatter);
            } catch (Exception ignored) {
            }
        }
        throw new IllegalArgumentException("无法解析时间: " + text);
    }

    private static final class ImportStats {
        private int importedRows;
        private int newAlarmCount;

        private int importedRows() {
            return importedRows;
        }

        private int newAlarmCount() {
            return newAlarmCount;
        }

        private void incrementImportedRows() {
            importedRows++;
        }

        private void addNewAlarmCount(int count) {
            newAlarmCount += count;
        }
    }
}
