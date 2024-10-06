package com.example.hotdealcrawling.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class SellerInfo {
    private String sellerName;
    private String businessId;
    private String contactInfo;
    private String representative;
    private String location;
    private String email;

    @Override
    public String toString() {
        return "SellerInfo [sellerName=" + sellerName + ", businessId=" + businessId + ", contactInfo=" + contactInfo + ", representative=" + representative + ", location=" + location + ", email=" + email + "]";
    }
}
