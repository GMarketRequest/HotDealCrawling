package com.example.hotdealcrawling.crawling.advice;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.StaleElementReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class CrawlingExceptionHandler {

  //크롤링 부분 에러처리
  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<String> handleNoSuchElementException(NoSuchElementException ex) {
    log.error("NoSuchElementException occurred: {}", ex.getMessage());
    return new ResponseEntity<>("Element not found during crawling", HttpStatus.NOT_FOUND);
  }

//  @ExceptionHandler(TimeoutException.class)
//  public ResponseEntity<String> handleTimeoutException(TimeoutException ex) {
//    log.error("TimeoutException occurred: {}", ex.getMessage());
//    return new ResponseEntity<>("Request timed out during crawling", HttpStatus.REQUEST_TIMEOUT);
//  }

  @ExceptionHandler(StaleElementReferenceException.class)
  public ResponseEntity<String> handleStaleElementReferenceException(
      StaleElementReferenceException ex) {
    log.error("StaleElementReferenceException occurred: {}", ex.getMessage());
    return new ResponseEntity<>("Stale element reference during crawling", HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleGenericException(Exception ex) {
    log.error("Generic Exception occurred: {}", ex.getMessage());
    return new ResponseEntity<>("An error occurred during crawling",
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

  //엑셀 부분 에러 처리
  // IllegalArgumentException 처리 (ExcelService에서 사용)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error("IllegalArgumentException occurred: {}", ex.getMessage());
    return new ResponseEntity<>("Invalid input: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  // IOException 처리 (파일 생성, 다운로드 관련)
  @ExceptionHandler(IOException.class)
  public ResponseEntity<String> handleIOException(IOException ex) {
    log.error("IOException occurred: {}", ex.getMessage());
    return new ResponseEntity<>("Error occurred while processing the file: " + ex.getMessage(),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }

}
