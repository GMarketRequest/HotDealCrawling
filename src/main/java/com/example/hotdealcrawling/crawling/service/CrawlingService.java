package com.example.hotdealcrawling.crawling.service;

import com.example.hotdealcrawling.crawling.dto.SellerInfo;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CrawlingService {

    public List<SellerInfo> crawlGmarket() {
        List<SellerInfo> sellerList = new ArrayList<>();

        // ChromeDriver 경로 설정
        System.setProperty("webdriver.chrome.driver", "chromedriver.exe");

        // Chrome 옵션 설정
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4515.107 Safari/537.36");

        ChromeOptions options2 = new ChromeOptions();
        options2.addArguments("--disable-gpu");
        options2.addArguments("--no-sandbox");
        options2.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36");

        WebDriver driver = new ChromeDriver(options);

        try {
            // Gmarket Superdeal 페이지로 이동
            System.out.println("Navigating to Gmarket Superdeal page...");
            driver.get("https://www.gmarket.co.kr/n/superdeal");

            // 페이지가 완전히 로드될 때까지 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

            System.out.println("Page title: " + driver.getTitle());

            // 쿠키 저장 (세션 유지)
            Set<Cookie> cookies = driver.manage().getCookies();

            // 랜덤 스크롤 이벤트 추가
            JavascriptExecutor js = (JavascriptExecutor) driver;
            js.executeScript("window.scrollBy(0,1000)", "");

            // 모든 item_list ul 태그들 추출
            List<WebElement> itemLists = driver.findElements(By.cssSelector(".item_list>li"));


            if (itemLists.isEmpty()) {
                System.out.println("No item lists found. Check the selector or page structure.");
                return sellerList;  // item_list가 없으면 빈 리스트 반환
            }

            System.out.println("Number of item lists found: " + itemLists.size());

            // 각 item_list 내의 상품 링크들 추출
            for (WebElement itemList : itemLists) {
                WebElement productLink = itemList.findElement(By.cssSelector(".inner>a"));
                String productUrl = productLink.getAttribute("href");
                System.out.println("Navigating to product page: " + productUrl);


                // 요청 간에 랜덤 딜레이 추가 (봇으로 감지되지 않도록)
                Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 3000));

                WebDriver driver2 = new ChromeDriver(options2);



                // 해당 상품 상세 페이지로 이동
                driver2.get(productUrl);

                // '교환/반품' 탭을 찾고 클릭
                try {
                    WebElement exchangeTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#vip-tab_exchange']")));
                    exchangeTab.click();
                    System.out.println("Clicked '교환/반품' tab.");

                    // 교환/반품 정보를 포함한 섹션 찾기
                    WebElement exchangeGuideBox = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".box__exchange-guide")));
                    List<WebElement> exchangeSections = exchangeGuideBox.findElements(By.cssSelector(".box__inner"));

                    // 교환/반품 정보가 있는 섹션 추출
                    if (exchangeSections.size() >= 5) {
                        WebElement sellerInfoSection = exchangeSections.get(4);

                        // 판매자 정보 추출
                        String sellerName = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'상호명')]//span")).getText();
                        String representative = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'대표자')]//span")).getText();
                        String contact = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'연락처')]//span")).getText();
                        String businessNumber = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'사업자 등록번호')]//span")).getText();
                        String salesNumber = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'통신판매업자번호')]//span")).getText();
                        String location = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'사업장소재지')]//span")).getText();
                        String email = sellerInfoSection.findElement(By.xpath("//li[span[contains(text(),'E-mail')]//span")).getText();

                        // SellerInfo 객체 생성 및 리스트에 추가
                        SellerInfo sellerInfo = new SellerInfo(sellerName, businessNumber, contact, representative, location, email);
                        sellerList.add(sellerInfo);

                        System.out.println("Seller info added: " + sellerInfo.toString());
                    }
                } catch (TimeoutException te) {
                    System.out.println("교환/반품 정보를 찾지 못했습니다.");
                }
                finally{
                    driver2.quit();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();

        }

        System.out.println("Crawling completed. Total sellers found: " + sellerList.size());
        return sellerList;
    }
}
