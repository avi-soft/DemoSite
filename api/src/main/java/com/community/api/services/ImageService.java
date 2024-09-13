package com.community.api.services;

import com.community.api.entity.Image;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import org.apache.commons.codec.binary.Hex;

@Service
public class ImageService {
    @Autowired
    private EntityManager entityManager;

    public ImageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Image saveImage(MultipartFile file) throws Exception {
        // Define the directory where you want to store the images
        String uploadDir = "api/avisoftdocument/Random Images";

        // Create the directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        byte[] fileBytes = file.getBytes();
        // Generate the file path (append the filename properly with a separator)
        String filePath = uploadDir + File.separator + file.getOriginalFilename();

        try {
            File destFile = new File(filePath);
            FileUtils.writeByteArrayToFile(destFile, file.getBytes());
        } catch (IOException e) {
            throw new Exception("Failed to save the file", e);
        }

        // Create and populate the Image entity
        Image image = new Image();
        image.setFile_name(file.getOriginalFilename());
        image.setFile_type(file.getContentType());
        image.setImage_data(fileBytes);
        image.setFile_path(filePath); // Store the file path in the database

        // Persist the image entity to the database
        entityManager.persist(image);
        return image;
    }
}