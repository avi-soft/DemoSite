package com.community.api.services;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;

@Service
public class FileService {

    /**
     * Generates a public URL for the file.
     *
     * @param filePath The relative path to the file.
     * @return The URL to access the file.
     */
    public String getFileUrl(String filePath) {
        // Normalize the file path for URL
        String normalizedFilePath = filePath.replace("\\", "/");

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(normalizedFilePath)
                .toUriString();
    }

    /**
     * Retrieves a file object.
     *
     * @param basePath The base path for the files.
     * @return The File object for the specified path.
     */
    public File getFile(String basePath) {
        String normalizedFilePath = basePath.replace("\\", "/");
        return new File( normalizedFilePath);
    }
}
