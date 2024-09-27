package com.community.api.endpoint.avisoft.controller;
import com.community.api.component.JwtUtil;
import com.community.api.services.*;
import io.github.bucket4j.Bucket;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import com.community.api.services.exception.ExceptionHandlingImplement;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.security.Key;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    private final RateLimiterService rateLimiterService;

    private ExceptionHandlingImplement exceptionHandling;

    private JwtUtil jwtUtil;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private SanitizerService sanitizerService;

    @Autowired
    private DocumentStorageService documentStorageService;


    private CustomCustomerService customCustomerService;

    public TestController(RateLimiterService rateLimiterService, ExceptionHandlingImplement exceptionHandling, JwtUtil jwtUtil, CustomCustomerService customCustomerService) {
        this.rateLimiterService = rateLimiterService;
        this.exceptionHandling = exceptionHandling;
        this.jwtUtil = jwtUtil;
        this.customCustomerService = customCustomerService;
    }


    @GetMapping("/catch-error")
    public ResponseEntity<String> catcherror() {

        try {
            int x = 5 / 0;
        } catch (Exception e) {

            String errorMessage = exceptionHandling.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);

        }
        return ResponseEntity.ok("Success");
    }


    @GetMapping("/generate-key")
    public String generateKey() {
        Key key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated Key: " + base64Key);
        return base64Key;
    }

    @GetMapping("/api/rate-limit")
    public String rateLimit(@RequestParam String userId, HttpServletRequest request) {
        Bucket bucket = rateLimiterService.resolveBucket(userId, "/api/rate-limit");

        if (bucket.tryConsume(1)) {
            return "Request successful!";
        } else {
            return "Rate limit exceeded!";
        }
    }

    @PostMapping("/api/data-entry")
    public String dataEntry(@RequestParam String userId) {
        documentStorageService.saveAllDocumentTypes();
        return "Documents inserted successfully";
    }


    @PostMapping("/alter/CustomProduct")
    @Transactional
    public String alterDocument() {
        String sql = "ALTER TABLE custom_product ALTER COLUMN admit_card_date_from SET DEFAULT NULL";

        try {
            entityManager.createNativeQuery(sql).executeUpdate();
            return "Column 'role' added to Document table successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while altering the Document table";

        }
    }

    @PostMapping("/alter/column")
    @Transactional
    public String altercolumn(@RequestParam String userId) {
        String sql = "ALTER TABLE CUSTOM_CUSTOMER ADD COLUMN token VARCHAR";
        String sql1 = "ALTER TABLE service_provider ADD COLUMN token VARCHAR";
        try {
            entityManager.createNativeQuery(sql).executeUpdate();
            entityManager.createNativeQuery(sql1).executeUpdate();
            return "Column 'role' added to Document table successfully";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while altering the Document table";

        }
    }
    @PostMapping("/add-column-to-a-table/{entityName}/{columnName}/{dataType}")
    @Transactional
    public String addColumn(@PathVariable String entityName,@PathVariable String columnName,@PathVariable String dataType) {
        String sql = "ALTER TABLE "+entityName +" ADD COLUMN "+columnName+" "+dataType;
        try {
            entityManager.createNativeQuery(sql).executeUpdate();
            return "Column "+columnName+" added successfully to "+entityName;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error occurred while altering the Document table";

        }
    }


    @PostMapping("/add/typing-text")
    public String addTypingText(@RequestParam String userId) {
        documentStorageService.saveAllTypingTexts();
        return "Typing texts inserted successfully";
    }
    @PostMapping("/test-sanitizer")
    public ResponseEntity<?> testSanitizer(@RequestBody Map<String,Object>map) {
       return ResponseService.generateSuccessResponse("Sanitized map",sanitizerService.sanitizeInputMap(map),HttpStatus.OK);
    }
    @GetMapping("/download-file-test")
    public void downloadFile( HttpServletRequest request, HttpServletResponse response) {

        try {
            String fileUrl = "http://192.168.0.138:8080/avisoftdocument/service_provider/RandomImages/pexels-fotios-photos-1540258.jpg";

            URL url = new URL(fileUrl);
            System.out.println("Downloading file: " + fileUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();



            if (responseCode == HttpURLConnection.HTTP_OK) {
                response.setContentType("application/octet-stream");

                // Extract the file name from the URL
                String fileName = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);

                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                response.setContentLength(connection.getContentLength());

                try (InputStream inputStream = connection.getInputStream();
                     OutputStream outputStream = response.getOutputStream()) {
                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush(); // Ensure all data is sent
                }
            } else {
                System.out.println("Error: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @GetMapping("/download-file")
    public void downloadFileNew(@RequestParam("filePath") String filePath, HttpServletRequest request, HttpServletResponse response) {
        try {
            URL url = new URL(filePath);

            // Use URI to handle special characters

//            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();



            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                response.setContentType("application/octet-stream");

                // Extract the file name from the URL
                String fileName = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);

                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                response.setContentLength(connection.getContentLength());

                try (InputStream inputStream = connection.getInputStream();
                     OutputStream outputStream = response.getOutputStream()) {
                    IOUtils.copy(inputStream, outputStream);
                    outputStream.flush(); // Ensure all data is sent
                }
            } else {

            }
        } catch (IOException e) {
            exceptionHandling.handleException(e);

        }catch (Exception e) {
            exceptionHandling.handleException(e);

        }

    }

}
