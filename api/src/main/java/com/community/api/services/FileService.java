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

    /**
     * Generates a public URL for the file.
     *
     * @param filePath The relative path to the file.
     * @return The URL to access the file.
     */

    private static String baseURL = "http://192.168.0.138:8080";
    public String getFileUrl(String filePath, HttpServletRequest request) {
        String normalizedFilePath = filePath.replace("\\", "/");
        String encodedFilePath = URLEncoder.encode(normalizedFilePath, StandardCharsets.UTF_8);


        return baseURL + "/"  + normalizedFilePath;
    }


}
