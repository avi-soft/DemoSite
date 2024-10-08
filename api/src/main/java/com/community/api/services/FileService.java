package com.community.api.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class FileService {

    @Value("${file.server.url}")
    private String fileServerUrl;

    /**
     * Generates a public URL for the file.
     *
     * @param filePath The relative path to the file.
     * @return The URL to access the file.
     */


    public String getFileUrl(String filePath, HttpServletRequest request) {
        String normalizedFilePath = filePath.replace("\\", "/");
        return fileServerUrl + "/"  + normalizedFilePath;
    }

    public String getDownloadFileUrl(String filePath, HttpServletRequest request) {
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
        return fileServerUrl + "/" + encodedFilePath.toString();
    }


}
