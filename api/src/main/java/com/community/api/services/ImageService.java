package com.community.api.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.community.api.entity.Image;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Iterator;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;

import static com.community.api.services.DocumentStorageService.isValidFileType;

@Service
public class ImageService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private Cloudinary cloudinary;

    public ImageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Transactional
    public Image saveImage(MultipartFile file) throws IOException {

            if(!isValidFileType(file))
            {
                throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
            }
            if (file.getSize() < 2 * 1024 * 1024) {
                throw new IllegalArgumentException("File size must be larger than 2 MB.");
            }
            // Get the byte array of the file
            byte[] fileBytes = file.getBytes();

            // Upload to Cloudinary
            Map<String, String> params = ObjectUtils.asMap(
                    "public_id", "random_image_" + System.currentTimeMillis(),
                    "folder", "random_images"
            );

            Map uploadResult = cloudinary.uploader().upload(fileBytes, params);

            // Get the URL of the uploaded image
            String imageUrl = (String) uploadResult.get("secure_url");


            // Create and populate the Image entity
            Image image = new Image();
            image.setFile_name(file.getOriginalFilename());
            image.setFile_type(file.getContentType());
            image.setFile_path(imageUrl); // Store the Cloudinary URL
            image.setImage_data(fileBytes); // Store the byte array in the database

            // Persist the image entity to the database
            entityManager.persist(image);
            return image;
        }
    }