package com.example.hotdealcrawling.crawling.service;

import com.example.hotdealcrawling.crawling.dto.SellerInfo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public ByteArrayInputStream createExcel(List<SellerInfo> sellerInfoList) {
        // 리스트가 비어 있는지 확인
        if (sellerInfoList == null || sellerInfoList.isEmpty()) {
            System.out.println("SellerInfo list is empty, cannot create Excel.");
            throw new RuntimeException("SellerInfo list is empty, cannot create Excel.");
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Seller Info");

            // 헤더 행 생성
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Seller Name");
            header.createCell(1).setCellValue("Business ID");
            header.createCell(2).setCellValue("Contact Info");
            header.createCell(3).setCellValue("Representative");
            header.createCell(4).setCellValue("Location");
            header.createCell(5).setCellValue("Email");

            // 데이터 기록
            int rowIdx = 1;
            for (SellerInfo seller : sellerInfoList) {
                // 디버깅을 위한 로그 출력
                System.out.println("Adding seller to Excel: " + seller.getSellerName() + ", " + seller.getBusinessId());

                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(seller.getSellerName());
                row.createCell(1).setCellValue(seller.getBusinessId());
                row.createCell(2).setCellValue(seller.getContactInfo());
                row.createCell(3).setCellValue(seller.getRepresentative());
                row.createCell(4).setCellValue(seller.getLocation());
                row.createCell(5).setCellValue(seller.getEmail());
            }

            // 엑셀 파일을 ByteArray로 변환
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Fail to create Excel file: " + e.getMessage());
        }
    }
}
