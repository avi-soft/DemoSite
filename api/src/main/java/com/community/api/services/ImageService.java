package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.entity.Image;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import javax.servlet.http.HttpServletRequest;
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
    private DocumentStorageService fileUploadService;

    public ImageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Transactional
    public Image saveImage(MultipartFile file) throws Exception {

        String db_path = "avisoftdocument/SERVICE_PROVIDER/1/Random_Images/Random";


        String dbPath = db_path + File.separator + file.getOriginalFilename();
        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }
        if (file.getSize() < Constant.MAX_FILE_SIZE) {
            String maxImageSize= ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
            throw new IllegalArgumentException("File size must be larger than "+maxImageSize);
        }

        byte[] fileBytes = file.getBytes();

        fileUploadService.uploadFileOnFileServer(file, "Random_Images", "Random", "SERVICE_PROVIDER");


        // Create and populate the Image entity
        Image image = new Image();
        image.setFile_name(file.getOriginalFilename());
        image.setFile_type(file.getContentType());
        image.setImage_data(fileBytes);
        image.setFile_path(dbPath); // Store the file path in the database

        // Persist the image entity to the database
        entityManager.persist(image);
        return image;
    }
}
