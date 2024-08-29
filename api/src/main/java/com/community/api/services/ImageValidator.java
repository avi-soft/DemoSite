package com.community.api.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageValidator {

    // Allowed file extensions
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "gif"};

    // Max allowed file size in bytes (e.g., 2 MB)
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    // Desired image width and height
    private static final int MAX_WIDTH = 1024;
    private static final int MAX_HEIGHT = 768;

    public static void validateImage(String imageUrl) {
        try {
            // Step 1: Validate file extension
            String fileExtension = FilenameUtils.getExtension(imageUrl);
            boolean isValidExtension = false;
            for (String extension : ALLOWED_EXTENSIONS) {
                if (extension.equalsIgnoreCase(fileExtension)) {
                    isValidExtension = true;
                    break;
                }
            }
            if (!isValidExtension) {
                throw new IllegalArgumentException("Invalid image format. Allowed formats are: " + String.join(", ", ALLOWED_EXTENSIONS));
            }

            // Step 2: Validate image content type
            RestTemplate restTemplate = new RestTemplate();
            MediaType mediaType = restTemplate.headForHeaders(imageUrl).getContentType();
            if (mediaType == null || !isImageContentType(mediaType)) {
                throw new IllegalArgumentException("Invalid image content type. Content type must be an image.");
            }

            // Step 3: Download and validate image dimensions and size
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            if (image == null) {
                throw new IllegalArgumentException("Could not read image from the URL.");
            }

            int width = image.getWidth();
            int height = image.getHeight();
            if (width > MAX_WIDTH || height > MAX_HEIGHT) {
                throw new IllegalArgumentException("Image dimensions are too large. Max allowed dimensions are " + MAX_WIDTH + "x" + MAX_HEIGHT + " pixels.");
            }

            // Optional: Check the image file size
            long imageSize = restTemplate.headForHeaders(imageUrl).getContentLength();
            if (imageSize > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Image file size is too large. Max allowed size is " + (MAX_FILE_SIZE / 1024 / 1024) + " MB.");
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("An error occurred while validating the image: " + e.getMessage());
        }
    }

    private static boolean isImageContentType(MediaType mediaType) {
        return mediaType.equals(MediaType.IMAGE_JPEG) ||
               mediaType.equals(MediaType.IMAGE_PNG) ||
               mediaType.equals(MediaType.IMAGE_GIF);
    }
}
