package com.community.api.services;

import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class FileService {



    @Autowired
    ExceptionHandlingService exceptionHandling;

    @Value("${file.server.url}")
    private String fileServerUrl;

    /**
     * Generates a public URL for the file.
     *
     * @param filePath The relative path to the file.
     * @return The URL to access the file.
     */


    public String getFileUrl(String filePath, HttpServletRequest request) {
        try{
            String normalizedFilePath = filePath.replace("\\", "/");
            return   this.getFileUrl(normalizedFilePath);
//        return fileServerUrl + "/"  + normalizedFilePath;
        }catch (Exception e){
            exceptionHandling.handleException(e);
            return "Error fetching urls:  " + e.getMessage();
        }
    }

    public String getDownloadFileUrl(String filePath, HttpServletRequest request) {
        try{
            String normalizedFilePath = filePath.replace("\\", "/");

            String[] pathSegments = normalizedFilePath.split("/");
            StringBuilder encodedFilePath = new StringBuilder();

            for (String segment : pathSegments) {
                if (encodedFilePath.length() > 0) {
                    encodedFilePath.append("/");
                }
                String encodedSegment = URLEncoder.encode(segment, StandardCharsets.UTF_8).replace("+", "%20");
                encodedFilePath.append(encodedSegment);
            }

//        return fileServerUrl + "/" + encodedFilePath.toString();

            return   this.getFileUrl(encodedFilePath.toString());
        }catch (Exception e){
            exceptionHandling.handleException(e);
            return "Error fetching urls:  " + e.getMessage();
        }
    }

/*    public String getFileUrl(String filename) {
            try{
                String fileUrlApi = fileServerUrl + "/files/file-url";
                RestTemplate restTemplate = new RestTemplate();
                String fileUrl = restTemplate.getForObject(fileUrlApi + filename, String.class);
                return fileUrl;
            }catch (Exception e){
            exceptionHandling.handleException(e);
            return "Error fetching urls:  " + e.getMessage();
        }
    }*/

    public String getFileUrl(String fullFilePath) {
        try {
            String fileUrlApi = fileServerUrl + "/file-url?filePath=" + URLEncoder.encode(fullFilePath, StandardCharsets.UTF_8);
            RestTemplate restTemplate = new RestTemplate();
            String fileUrl = restTemplate.getForObject(fileUrlApi, String.class);
            return fileUrl;
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return "Error fetching URLs: " + e.getMessage();
        }
    }

}
