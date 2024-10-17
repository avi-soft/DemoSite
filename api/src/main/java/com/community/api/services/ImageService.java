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
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

import static com.community.api.services.DocumentStorageService.isValidFileType;
import static com.community.api.services.ServiceProviderTestService.areImagesVisuallyIdentical;

@Service
public class ImageService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DocumentStorageService fileUploadService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxImageSize;

    public ImageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }


    @Transactional
    public Image saveImage(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalStateException("File is missing or empty");
        }

        byte[] uploadImageData = file.getBytes();
        List<Image> images = getAllRandomImages();
        for(Image image : images) {
           byte[] imageData = image.getImage_data();
           try{
               boolean areImagesIdentical=areImagesVisuallyIdentical(uploadImageData, imageData);
               if(areImagesIdentical) {
                   throw new IllegalStateException("Image already exists");
               }
           }
           catch (IOException e) {
               throw new IllegalStateException("Error comparing images", e);
           }
        }

        String db_path = "avisoftdocument/SERVICE_PROVIDER/Random/Random_Images";

        String dbPath = db_path + File.separator + file.getOriginalFilename();
        if (!isValidFileType(file)) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }
        long maxSizeInBytes = ImageSizeConfig.convertToBytes(maxImageSize);
        if (file.getSize() < Constant.MAX_FILE_SIZE || file.getSize() > maxSizeInBytes) {
            String minImageSize= ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
            throw new IllegalArgumentException("Image size should be between " + minImageSize + " and " + maxImageSize);
        }

        byte[] fileBytes = file.getBytes();

        fileUploadService.uploadFileOnFileServer(file, "Random_Images", "Random", "SERVICE_PROVIDER");


        // Create and populate the Image entity
        Image image = new Image();
        image.setFile_name(file.getOriginalFilename());
        image.setFile_type(file.getContentType());
        image.setImage_data(fileBytes);
        image.setFile_path(dbPath);

        // Persist the image entity to the database
        entityManager.persist(image);
        return image;
    }

    @Transactional
    public List<Image> saveImages(List<MultipartFile> files) throws Exception {

        List<Image> savedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalStateException("File is missing or empty");
            }

            byte[] uploadImageData = file.getBytes();
            List<Image> images = getAllRandomImages();
            for(Image image : images) {
                byte[] imageData = image.getImage_data();
                try{
                    boolean areImagesIdentical=areImagesVisuallyIdentical(uploadImageData, imageData);
                    if(areImagesIdentical) {
                        throw new IllegalStateException("Image already exists");
                    }
                }
                catch (IOException e) {
                    throw new IllegalStateException("Error comparing images", e);
                }
            }
            // Construct file path
            String db_path = "avisoftdocument/SERVICE_PROVIDER/Random/Random_Images";
            String dbPath = db_path + File.separator + file.getOriginalFilename();

            // Validate the file type
            if (!isValidFileType(file)) {
                throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
            }

            // Validate the file size
            long maxSizeInBytes = ImageSizeConfig.convertToBytes(maxImageSize);
            if (file.getSize() < Constant.MAX_FILE_SIZE || file.getSize() > maxSizeInBytes) {
                String minImageSize = ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
                throw new IllegalArgumentException("Image size should be between " + minImageSize + " and " + maxImageSize);
            }

            byte[] fileBytes = file.getBytes();

            fileUploadService.uploadFileOnFileServer(file, "Random_Images", "Random", "SERVICE_PROVIDER");

            // Create and populate the Image entity
            Image image = new Image();
            image.setFile_name(file.getOriginalFilename());
            image.setFile_type(file.getContentType());
            image.setImage_data(fileBytes);
            image.setFile_path(dbPath);

            // Persist the image entity to the database
            entityManager.persist(image);
            savedImages.add(image);
        }

        return savedImages;
    }

    @Transactional
    public List<Image> deleteAllImages()
    {
        List<Image> images =getAllRandomImages();
        for(Image image :images)
        {
            entityManager.remove(image);
        }
        return images;
    }


    @Transactional
    public List<Image> getAllRandomImages()
    {
        TypedQuery<Image> typedQuery= entityManager.createQuery(Constant.GET_ALL_RANDOM_IMAGES,Image.class);
        List<Image> images = typedQuery.getResultList();
        return images;
    }

    @Transactional
    public Image deleteImageById(Long imageId)
    {
        Image image = entityManager.find(Image.class, imageId);
        if(image == null)
        {
            throw new EntityNotFoundException("Image not found with id : " + imageId);
        }
        entityManager.remove(image);
        return image;
    }
}
