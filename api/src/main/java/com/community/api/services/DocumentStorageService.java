package com.community.api.services;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class DocumentStorageService {

    public static final String BASE_DIRECTORY = "C:\\Documents";


    /**
     * Saves a file to a dynamic directory structure.
     *
     * @param customerId The ID of the customer.
     * @param documentType The type of document (e.g., "aadhar", "pan", "signature").
     * @param fileName The name of the file to be saved.
     * @param fileInputStream InputStream of the file data.
     * @throws IOException If an I/O error occurs.
     */
    public void saveDocument(String customerId, String documentType, String fileName, InputStream fileInputStream,String Role) throws IOException {
        File baseDir = new File(BASE_DIRECTORY);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        File customerDir = new File(baseDir, Role +"/" + customerId);
        if (!customerDir.exists()) {
            customerDir.mkdirs();
        }

        File documentTypeDir = new File(customerDir, documentType);
        if (!documentTypeDir.exists()) {
            documentTypeDir.mkdirs();
        }

        File file = new File(documentTypeDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }


        public static String getDocumentTypeFromMultipartFile(MultipartFile file) {

            if (file.getOriginalFilename().endsWith(".pdf")) {
                return "PDF";
            } else if (file.getOriginalFilename().endsWith(".jpg")) {
                return "JPG";
            }

            return "UNKNOWN";
        }

}
