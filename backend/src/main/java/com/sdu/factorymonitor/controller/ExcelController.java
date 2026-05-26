package com.sdu.factorymonitor.controller;

import com.sdu.factorymonitor.dto.ExcelImportResponse;
import com.sdu.factorymonitor.service.ExcelImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelImportService excelImportService;

    @PostMapping(path = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ExcelImportResponse importExcel(@RequestParam("file") MultipartFile file) {
        return excelImportService.importExcel(file);
    }
}
