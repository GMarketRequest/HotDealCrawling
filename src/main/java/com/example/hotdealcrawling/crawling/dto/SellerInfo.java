package com.example.hotdealcrawling.crawling.dto;

import com.example.hotdealcrawling.annotation.ExcelColumn;
import lombok.Builder;

@Builder
public record SellerInfo(
    @ExcelColumn(headerName = "상호명", order = 1) String sellerName,
    @ExcelColumn(headerName = "사업자 등록번호", order = 2) String businessId,
    @ExcelColumn(headerName = "연락처", order = 3) String contactInfo,
    @ExcelColumn(headerName = "대표자", order = 4) String representative,
    @ExcelColumn(headerName = "사업장 소재지", order = 5) String location,
    @ExcelColumn(headerName = "E-mail", order = 6) String email,
    @ExcelColumn(headerName = "통신판매업자번호", order = 7) String businessNumber
) {

  @Override
  public String toString() {
    return "SellerInfo [sellerName=" + sellerName +
        ", businessId=" + businessId +
        ", contactInfo=" + contactInfo +
        ", representative=" + representative +
        ", location=" + location +
        ", email=" + email + "]";
  }
}
