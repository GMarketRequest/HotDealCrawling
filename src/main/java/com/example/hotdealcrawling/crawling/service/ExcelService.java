package com.example.hotdealcrawling.crawling.service;

import com.example.hotdealcrawling.annotation.ExcelColumn;
import com.example.hotdealcrawling.crawling.dto.SellerInfo;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.RecordComponent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExcelService {

  public ByteArrayInputStream createExcel(List<SellerInfo> sellerInfoList) {
    // 리스트가 비어 있는지 확인
    if (sellerInfoList == null || sellerInfoList.isEmpty()) {
      throw new IllegalArgumentException("SellerInfo list is empty, cannot create Excel.");
    }

    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Seller Info");

      // 열 크기 설정 (256 * 원하는 크기)
      sheet.setColumnWidth(1, 5000); // 상호명 열 크기 설정
      sheet.setColumnWidth(2, 4000); // 사업자 등록번호 열 크기 설정
      sheet.setColumnWidth(3, 4000); // 연락처 열 크기 설정
      sheet.setColumnWidth(5, 10000); // 사업장 소재지 열 크기 설정
      sheet.setColumnWidth(6, 6000);
      sheet.setColumnWidth(7, 4000);
      sheet.setColumnWidth(8, 4000);

      // 헤더 스타일 설정
      CellStyle headerStyle = workbook.createCellStyle();
      headerStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
      headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
      headerStyle.setAlignment(HorizontalAlignment.CENTER);
      Font headerFont = workbook.createFont();
      headerFont.setBold(true);
      headerStyle.setFont(headerFont);

      // 기준일 생성
      String currentDate = LocalDateTime.now()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

      // RecordComponent를 사용하여 필드에서 어노테이션을 추출
      RecordComponent[] recordComponents = SellerInfo.class.getRecordComponents();
      List<RecordComponent> excelComponents = Arrays.stream(recordComponents)
          .filter(component -> component.isAnnotationPresent(ExcelColumn.class))
          .sorted(Comparator.comparingInt(
              component -> component.getAnnotation(ExcelColumn.class).order()))
          .toList();

      log.info(String.valueOf(recordComponents.length));
      
      // 헤더 행 생성
      Row header = sheet.createRow(0);
//      header.createCell(0).setCellValue("순번");
//      header.createCell(1).setCellValue("상호명");
//      header.createCell(2).setCellValue("사업자 등록번호");
//      header.createCell(3).setCellValue("연락처");
//      header.createCell(4).setCellValue("대표자");
//      header.createCell(5).setCellValue("사업장소재지");
//      header.createCell(6).setCellValue("E-mail");
//      header.createCell(7).setCellValue("통신판매업자번호");
//      header.createCell(8).setCellValue("기준일");

      for (int i = 0; i < excelComponents.size(); i++) {
        RecordComponent component = excelComponents.get(i);
        ExcelColumn excelColumn = component.getAnnotation(ExcelColumn.class);
        Cell cell = header.createCell(i);
        cell.setCellValue(excelColumn.headerName());
        cell.setCellStyle(headerStyle);
      }

//      // 헤더 스타일 적용
//      for (int i = 0; i <= 8; i++) {
//        Cell cell = header.getCell(i);
//        if (cell == null) {
//          cell = header.createCell(i);
//        }
//        cell.setCellStyle(headerStyle);
//      }

      // 데이터 기록
//      int rowIdx = 1;
//      int seqNum = 1;  // 순번
//      for (SellerInfo seller : sellerInfoList) {
//        Row row = sheet.createRow(rowIdx++);
//        row.createCell(0).setCellValue(seqNum++);  // 순번
//        row.createCell(1).setCellValue(seller.sellerName());
//        row.createCell(2).setCellValue(seller.businessId());
//        row.createCell(3).setCellValue(seller.contactInfo());
//        row.createCell(4).setCellValue(seller.representative());
//        row.createCell(5).setCellValue(seller.location());
//        row.createCell(6).setCellValue(seller.email());
//        row.createCell(7).setCellValue(seller.businessNumber());
//        row.createCell(8).setCellValue(currentDate);  // 기준일
//      }

      // 데이터 기록
      int rowIdx = 1;
      for (SellerInfo seller : sellerInfoList) {
        Row dataRow = sheet.createRow(rowIdx++);
        for (int i = 0; i < excelComponents.size(); i++) {
          RecordComponent component = excelComponents.get(i);
          try {
            // 필드 이름과 동일한 메서드 호출 (필드 이름과 메서드 이름이 동일)
            Object value = SellerInfo.class
                .getMethod(component.getName())  // 필드 이름으로 메서드 찾기
                .invoke(seller);  // 메서드 호출

            Cell cell = dataRow.createCell(i);
            log.info(value.toString());

            cell.setCellValue(value != null ? value.toString() : "");
          } catch (Exception e) {
            throw new RuntimeException("Failed to access method: " + component.getName(), e);
          }
        }
      }

      // 엑셀 파일을 ByteArray로 변환
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      workbook.write(out);
      return new ByteArrayInputStream(out.toByteArray());
    } catch (IOException e) {
      throw new IllegalStateException("Fail to create Excel file: " + e.getMessage(), e);
    }
  }
}
