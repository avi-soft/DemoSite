package com.community.api.services;

import com.community.api.entity.Image;
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

//    public Image saveImage(MultipartFile file) throws IOException {
//        Image image = new Image();
//        image.setFile_name(file.getOriginalFilename());
//        image.setFile_type(file.getContentType());
//        image.setImage_data(file.getBytes());
//
//        entityManager.persist(image);
//        return image;
//    }
public Image saveImage(MultipartFile file) throws IOException {
    // Define the directory where you want to store the images
    String uploadDir = "C:/Users/91774/Documents/RandomImages";

    // Create the directory if it doesn't exist
    File directory = new File(uploadDir);
    if (!directory.exists()) {
        directory.mkdirs();
    }

    // Generate the file path
    String filePath = uploadDir + file.getOriginalFilename();

    // Save the file to the directory
    File destinationFile = new File(filePath);
    file.transferTo(destinationFile);

    // Create and populate the Image entity
    Image image = new Image();
    image.setFile_name(file.getOriginalFilename());
    image.setFile_type(file.getContentType());
    image.setImage_data(file.getBytes()); // Optionally, you can store the file as byte data or skip this if you only want to store the path
    image.setFile_path(filePath); // Store the file path

    // Persist the image entity to the database
    entityManager.persist(image);
    return image;
}


    public Image getImage(Long id) {
        Image image = entityManager.find(Image.class, id);
        if (image == null) {
            throw new EntityNotFoundException("Image is not found");
        }
        return image;
    }

    private static String calculateImageHash(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length == 0) {
            throw new IllegalArgumentException("Image data is null or empty.");
        }

        String format = detectImageFormat(imageData);
        System.out.println("Detected image format: " + format);
        System.out.println("Image data length: " + imageData.length);
        System.out.println("First few bytes: " + bytesToHex(imageData, 20));

        BufferedImage image;
        if ("Unknown".equals(format)) {
            image = readUnknownFormat(imageData);
        } else {
            try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(imageData))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
                if (!readers.hasNext()) {
                    System.err.println("No ImageReader found. Available readers: " + getAvailableImageReaders());
                    throw new IllegalArgumentException("No ImageReader found for the image data");
                }
                ImageReader reader = readers.next();
                System.out.println("Using ImageReader: " + reader.getClass().getName());
                reader.setInput(iis);
                image = reader.read(0);
            }
        }

        if (image == null) {
            throw new IllegalArgumentException("Failed to decode image data, resulting in a null BufferedImage.");
        }

        return generateImageHash(image);
    }

    private static String getAvailableImageReaders() {
        String[] readerFormatNames = ImageIO.getReaderFormatNames();
        if (readerFormatNames == null || readerFormatNames.length == 0) {
            return "None";
        }
        return String.join(", ", readerFormatNames);
    }

    private static BufferedImage readUnknownFormat(byte[] imageData) throws IOException {
        if (imageData == null || imageData.length < 8) {  // 8 bytes is arbitrary, adjust as needed
            throw new IllegalArgumentException("Image data is null or too small");
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
             ImageInputStream iis = ImageIO.createImageInputStream(bais)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            while (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    String format = reader.getFormatName();
                    System.out.println("Attempting to read image as format: " + format);
                    if ("wbmp".equalsIgnoreCase(format)) {
                        System.out.println("Skipping WBMP format");
                        continue;
                    }
                    reader.setInput(iis);
                    BufferedImage image = reader.read(0);
                    if (image != null) {
                        System.out.println("Successfully read image using format: " + format);
                        return image;
                    }
                } catch (IOException e) {
                    System.out.println("Failed to read with " + reader.getFormatName() + ": " + e.getMessage());
                } finally {
                    reader.dispose();
                }
            }
        }
        throw new IOException("Unable to read image data with any available ImageReader");
    }

    private static String detectImageFormat(byte[] imageData) {
        if (imageData.length >= 2) {
            if (imageData[0] == (byte) 0xFF && imageData[1] == (byte) 0xD8) {
                return "JPEG";
            } else if (imageData[0] == (byte) 0x89 && imageData[1] == (byte) 0x50) {
                return "PNG";
            } else if (imageData[0] == (byte) 0x47 && imageData[1] == (byte) 0x49) {
                return "GIF";
            } else if (imageData[0] == (byte) 0x42 && imageData[1] == (byte) 0x4D) {
                return "BMP";
            } else if (imageData[0] == (byte) 0x49 && imageData[1] == (byte) 0x49) {
                return "TIFF";
            }
        }
        return "Unknown";
    }

    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(bytes.length, length); i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString();
    }

    private static String generateImageHash(BufferedImage image) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            byte[] hash = digest.digest(imageBytes);
            return Hex.encodeHexString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate image hash", e);
        }
    }
}