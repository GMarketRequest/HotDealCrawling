package com.example.hotdealcrawling.crawling.dto;

import lombok.Builder;

@Builder
public record SellerInfo(
    String sellerName,
    String businessId,
    String contactInfo,
    String representative,
    String location,
    String email
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
