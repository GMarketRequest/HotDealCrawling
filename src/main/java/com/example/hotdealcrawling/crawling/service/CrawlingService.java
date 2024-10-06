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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
//        options2.addArguments("--headless");
        options2.addArguments("--disable-gpu");
        options2.addArguments("--no-sandbox");
        options2.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36");
        options2.addArguments("--disable-javascript");

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
                WebDriverWait wait2 = new WebDriverWait(driver2, Duration.ofSeconds(20));



                // 해당 상품 상세 페이지로 이동

                int retryCount = 3;
                boolean success = false;
                while (retryCount > 0 && !success) {
// '교환/반품' 탭을 찾고 클릭
                    try {

                        driver2.get(productUrl);

//                    WebElement exchangeTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href='#vip-tab_exchange']")));
//                    exchangeTab.click();
//                    System.out.println("Clicked '교환/반품' tab.");
                        try {
                            // 팝업을 확인하고 닫기
                            WebDriverWait alertWait = new WebDriverWait(driver2, Duration.ofSeconds(5));
                            Alert alert = alertWait.until(ExpectedConditions.alertIsPresent());
                            System.out.println("Alert detected with message: " + alert.getText());
                            alert.accept();  // 알림 창을 닫음
                            System.out.println("Alert closed.");
                        } catch (TimeoutException e) {
                            System.out.println("No alert found.");
                        }

                        // 교환/반품 정보를 포함한 섹션 찾기
//                        WebElement exchangeGuideBox = wait2.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".box__exchange-guide")));
                        List<WebElement> exchangeSections = driver2.findElements(By.cssSelector(".box__exchange-guide>div"));
                        System.out.println("box__exchange-guide found");


                        // 교환/반품 정보가 있는 섹션 추출
                        if (exchangeSections.size() >= 5) {
                            WebElement sellerInfoSection = exchangeSections.get(4);
                            System.out.println(sellerInfoSection.getAttribute("innerHTML"));

                            // sellerInfoSection의 innerHTML을 추출
                            String innerHTML = sellerInfoSection.getAttribute("innerHTML");

                            // 판매자 정보 추출
                            String sellerName = extractDataFromHtml(innerHTML, "상호명");
                            String representative = extractDataFromHtml(innerHTML, "대표자");
                            String contact = extractDataFromHtml(innerHTML, "연락처");
                            String businessNumber = extractDataFromHtml(innerHTML, "사업자 등록번호");
                            String salesNumber = extractDataFromHtml(innerHTML, "통신판매업자번호");
                            String location = extractDataFromHtml(innerHTML, "사업장소재지");
                            String email = extractDataFromHtml(innerHTML, "E-mail");



                            System.out.println("sellerName: " + sellerName);

                            // SellerInfo 객체 생성 및 리스트에 추가
                            SellerInfo sellerInfo = new SellerInfo(sellerName, businessNumber, contact, representative, location, email);
                            sellerList.add(sellerInfo);

                            System.out.println("Seller info added: " + sellerInfo.toString());
                            success = true;
                        }
                    } catch (TimeoutException | StaleElementReferenceException e) {
                        retryCount--;
                        if(retryCount == 0) {
                            System.out.println("로딩 실패. 재시도 횟수 초과");
                        }
                        else{
                            System.out.println("오류 발생, 재시도 중" + retryCount);
                            continue;
                        }

                    }
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
    private String extractDataFromHtml(String html, String key) {
        // key에 해당하는 데이터를 추출하는 정규 표현식
        String regex = key + "\\s*:\\s*<span[^>]*>([^<]*)</span>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        // 일치하는 데이터가 있으면 반환
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 일치하는 데이터가 없으면 빈 문자열 반환
        return "";
    }
}
