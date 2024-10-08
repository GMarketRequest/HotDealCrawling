package com.example.hotdealcrawling.crawling.service;

import com.example.hotdealcrawling.crawling.dto.SellerInfo;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CrawlingService {

  public List<SellerInfo> crawlGmarket() throws InterruptedException {
    List<SellerInfo> sellerList = new ArrayList<>();

    // ChromeDriver 경로 설정
    System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

    // Chrome 옵션 설정
    ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");
    options.addArguments(
        "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4515.107 Safari/537.36");

    ChromeOptions options2 = new ChromeOptions();
//        options2.addArguments("--headless");
    options2.addArguments("--disable-gpu");
    options2.addArguments("--no-sandbox");
    options2.addArguments(
        "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36");
    options2.addArguments("--disable-javascript");

    WebDriver driver = new ChromeDriver(options);

    // Gmarket Superdeal 페이지로 이동
    log.info("Navigating to Gmarket Superdeal page...");
    driver.get("https://www.gmarket.co.kr/n/superdeal");

    // 페이지가 완전히 로드될 때까지 대기
    WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript(
        "return document.readyState").equals("complete"));

    log.info("Page title: {}", driver.getTitle());

    // 쿠키 저장 (세션 유지)
    Set<Cookie> cookies = driver.manage().getCookies();

    // 랜덤 스크롤 이벤트 추가
    JavascriptExecutor js = (JavascriptExecutor) driver;
    js.executeScript("window.scrollBy(0,1000)", "");

    // 모든 item_list ul 태그들 추출
    List<WebElement> itemLists = driver.findElements(By.cssSelector(".item_list>li"));

    if (itemLists.isEmpty()) {
      log.warn("No item lists found. Check the selector or page structure.");
      return sellerList;  // item_list가 없으면 빈 리스트 반환
    }

    log.info("Number of item lists found: {}", itemLists.size());

    // 각 item_list 내의 상품 링크들 추출
    for (WebElement itemList : itemLists) {
      WebElement productLink = itemList.findElement(By.cssSelector(".inner>a"));
      String productUrl = productLink.getAttribute("href");
      log.info("Navigating to product page: {}", productUrl);

      WebDriver driver2 = new ChromeDriver(options2);
      WebDriverWait wait2 = new WebDriverWait(driver2, Duration.ofSeconds(20));

      int retryCount = 3;
      boolean success = false;
      while (retryCount > 0 && !success) {
        driver2.get(productUrl);

        try {
          WebDriverWait alertWait = new WebDriverWait(driver2, Duration.ofSeconds(3));
          Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
          log.info("Alert detected with message: {}", alert.getText());
          alert.accept();  // 알림 창을 닫음
          log.info("Alert closed.");
        } catch (TimeoutException e) {
          log.info("No alert found.");
        }

        // 교환/반품 정보가 있는 섹션 추출
        List<WebElement> exchangeSections = driver2.findElements(
            By.cssSelector(".box__exchange-guide>div"));
        log.info("box__exchange-guide found");

        if (exchangeSections.size() >= 5) {
          WebElement sellerInfoSection = exchangeSections.get(4);
          log.info(sellerInfoSection.getAttribute("innerHTML"));

          String innerHTML = sellerInfoSection.getAttribute("innerHTML");

          // 판매자 정보 추출
          String sellerName = extractDataFromHtml(innerHTML, "상호명");
          String representative = extractDataFromHtml(innerHTML, "대표자");
          String contact = extractDataFromHtml(innerHTML, "연락처");
          String businessNumber = extractDataFromHtml(innerHTML, "사업자 등록번호");
          String salesNumber = extractDataFromHtml(innerHTML, "통신판매업자번호");
          String location = extractDataFromHtml(innerHTML, "사업장소재지");
          String email = extractDataFromHtml(innerHTML, "E-mail");

          // SellerInfo 객체 생성 및 리스트에 추가
          SellerInfo sellerInfo = SellerInfo.builder()
              .sellerName(sellerName)
              .businessId(businessNumber)
              .representative(representative)
              .contactInfo(contact)
              .location(location)
              .email(email)
              .businessNumber(salesNumber)
              .build();
          sellerList.add(sellerInfo);

          log.info("Seller info added: {}", sellerInfo.toString());
          success = true;
        }
        retryCount--;
      }
      driver2.quit();
    }

    driver.quit();
    log.info("Crawling completed. Total sellers found: {}", sellerList.size());
    return sellerList;
  }

  private String extractDataFromHtml(String html, String key) {
    String regex = key + "\\s*:\\s*<span[^>]*>([^<]*)</span>";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(html);

    if (matcher.find()) {
      return matcher.group(1).trim();
    }
    return "";
  }
}
