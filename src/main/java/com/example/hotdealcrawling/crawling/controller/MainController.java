package com.example.hotdealcrawling.crawling.controller;

import com.example.hotdealcrawling.crawling.dto.SellerInfo;
import com.example.hotdealcrawling.crawling.service.CrawlingService;
import com.example.hotdealcrawling.crawling.service.ExcelService;
import java.io.ByteArrayInputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

  private final CrawlingService crawlingService;

  private final ExcelService excelService;

  @GetMapping("/")
  public String index() {
    return "index";  // index.jsp로 이동
  }

  @GetMapping("/download-excel")
  public ResponseEntity<InputStreamResource> downloadExcel(Model model) throws Exception {
    List<SellerInfo> sellerInfoList = crawlingService.crawlGmarket();  // 크롤링 데이터 수집
    ByteArrayInputStream in = excelService.createExcel(sellerInfoList);  // 엑셀 파일 생성

    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Disposition", "attachment; filename=seller_info.xlsx");

    return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
  }
}
