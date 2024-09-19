package com.community.api.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.entity.Image;
import com.community.api.services.exception.EntityDoesNotExistsException;
import com.twilio.base.Page;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Random;

@Service
public class ServiceProviderTestService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DocumentStorageService documentStorageService;
    @Autowired
    private FileService fileService;
    private static final long MAX_IMAGE_SIZE_MB = 2L * 1024 * 1024;   //(2MB)

    public ServiceProviderTestService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public ServiceProviderTest startTest(Long serviceProviderId) throws EntityDoesNotExistsException{
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if(serviceProvider==null)
        {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }
        if(serviceProvider.getTestStatus()!=null)
        {
            if(serviceProvider.getTestStatus().getTest_status_id()==2L )
            {
                throw new IllegalArgumentException("Skill Test has already been submitted.You cannot start a new test.");
            }
            if(serviceProvider.getTestStatus().getTest_status_id()==3L)
            {
                throw new IllegalArgumentException("Skill Test has already been approved. No need to start test again.");
            }
        }

        Image randomImage = getRandomImage();
        if(randomImage==null )
        {
            throw new IllegalArgumentException("There is no any random image present. Add a image to be selected randomly.");
        }
        String randomText= getRandomTypingText();
        if(randomText==null)
        {
            throw new IllegalArgumentException("There is no any random typing text present. Add a typing text to be selected randomly.");
        }

        ServiceProviderTest test = new ServiceProviderTest();
        test.setService_provider(serviceProvider);
        test.setDownloaded_image(randomImage);
        test.setTyping_test_text(randomText);
        entityManager.persist(test);
        serviceProvider.getServiceProviderTests().add(test);
        entityManager.merge(serviceProvider);
        return test;
    }

    @Transactional
    public ServiceProviderTest uploadResizedImages(Long serviceProviderId, Long testId, MultipartFile resizedFile, HttpServletRequest request) throws Exception {
        // Retrieve the service provider entity

        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProvider == null) {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }

        // Find the specific test for the service provider
        ServiceProviderTest test = null;
        List<ServiceProviderTest> serviceProviderTestList = serviceProvider.getServiceProviderTests();
        for (ServiceProviderTest serviceProviderTest : serviceProviderTestList) {
            if (testId.equals(serviceProviderTest.getTest_id())) {
                test = serviceProviderTest;
                break;
            }
        }
        if (test == null) {
            throw new EntityNotFoundException("Test not found with id: " + testId);
        }

        test.setIs_image_test_passed(false);
        if (resizedFile.getSize() > MAX_IMAGE_SIZE_MB) {
            test.setIs_image_test_passed(false);
            entityManager.merge(test);
            throw new IllegalArgumentException("Image size exceeds 2 MB");
        }

        // Validate the image size using saveDocuments method logic
        ResponseEntity<Map<String, Object>> savedResponse = documentStorageService.saveDocuments(resizedFile, "Resized Images", serviceProviderId, "SERVICE_PROVIDER");
        Map<String, Object> responseBody = savedResponse.getBody();

        if (savedResponse.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error uploading resized image: " + responseBody.get("message"));
        }

        // If successful, update the ServiceProviderTest with the image details
        String fileName = resizedFile.getOriginalFilename();
        ResizedImage resizedImage = test.getResized_image();
        if (resizedImage == null) {
            resizedImage = new ResizedImage();
            resizedImage = entityManager.merge(resizedImage); // Persist the new Image entity
            test.setResized_image(resizedImage);
        }

        String currentDir = System.getProperty("user.dir");

        String testDirPath = currentDir + "/../test/";
        String dbPath="avisoftdocument/service_provider/" + serviceProviderId + "/Resized Images";

        // Ensure the directory exists, and create it if it doesn't
        File baseDir = new File(dbPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs(); // Create the directory structure if it doesn't exist
        }

        // Full file path for the signature image
        String fullFilePath = dbPath + File.separator + fileName;
        String fileUrl = fileService.getFileUrl(fullFilePath, request);

        // Set file metadata in the ResizedImage object
        resizedImage.setFile_name(fileName);
        resizedImage.setFile_type(resizedFile.getContentType());
        resizedImage.setFile_path(fullFilePath);
        resizedImage.setFile_url(fileUrl);
        resizedImage.setImage_data(resizedFile.getBytes());
        resizedImage.setServiceProvider(serviceProvider);

        try {
            File destFile = new File(fullFilePath);
            FileUtils.writeByteArrayToFile(destFile, resizedFile.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to save the file", e);
        }

        // Set the image data and validate the resized image
        test.setResized_image_data(resizedFile.getBytes());
        entityManager.merge(test);
        boolean isImageValid = validateResizedImage(test);
        if (isImageValid) {
            test.setIs_image_test_passed(true);
        } else {
            test.setIs_image_test_passed(false);
            throw new IllegalArgumentException("Uploaded image is different from expected image");
        }
        return test;
    }

    @Transactional
    public ServiceProviderTest submitTypedText(Long serviceProviderId,Long testId, String typedText) throws Exception {
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if(serviceProvider==null)
        {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }
        ServiceProviderTest test =null;
        List<ServiceProviderTest> serviceProviderTestList = serviceProvider.getServiceProviderTests();
        for(ServiceProviderTest serviceProviderTest: serviceProviderTestList)
        {
            if(testId==serviceProviderTest.getTest_id())
            {
                test=serviceProviderTest;
                break;
            }
        }
        if (test == null) {
            throw new EntityNotFoundException();
        }
        test.setIs_typing_test_passed(false);

        boolean isPassed = validateTypedText(test.getTyping_test_text(), typedText);
        test.setSubmitted_text(typedText);
        if(isPassed) {
            test.setIs_typing_test_passed(true);
        }
        else
        {
            test.setIs_typing_test_passed(false);
            throw new IllegalArgumentException("Typed text mismatch");
        }
        // Persist the changes
        return entityManager.merge(test);
    }

    @Transactional
    public ServiceProviderTest uploadSignatureImage(Long serviceProviderId, Long testId, MultipartFile signatureFile,HttpServletRequest request) throws Exception {
        // Retrieve the service provider entity
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProvider == null) {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }

        // Find the specific test for the service provider
        ServiceProviderTest test = null;
        List<ServiceProviderTest> serviceProviderTestList = serviceProvider.getServiceProviderTests();
        for (ServiceProviderTest serviceProviderTest : serviceProviderTestList) {
            if (testId.equals(serviceProviderTest.getTest_id())) {
                test = serviceProviderTest;
                break;
            }
        }
        if (test == null) {
            throw new EntityNotFoundException("Service Provider Test not found");
        }

        if (signatureFile.getSize() > MAX_IMAGE_SIZE_MB) {
           throw new IllegalArgumentException("Signature image size exceeds 2 MB");
        }

        // Check the MIME type of the file
        if(!documentStorageService.isValidFileType(signatureFile))
        {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }
        // Use the saveDocuments method to validate and store the signature image
        ResponseEntity<Map<String, Object>> savedResponse = documentStorageService.saveDocuments(signatureFile, "Signature Image", serviceProviderId, "SERVICE_PROVIDER");
        Map<String, Object> responseBody = savedResponse.getBody();

        if (savedResponse.getStatusCode() != HttpStatus.OK) {
            throw new Exception("Error uploading signature image: " + responseBody.get("message"));
        }

        // If successful, update the ServiceProviderTest with the image details
        String fileName = signatureFile.getOriginalFilename();
        SignatureImage signatureImage = test.getSignature_image();
        if (signatureImage == null) {
            signatureImage = new SignatureImage();
            signatureImage = entityManager.merge(signatureImage); // Persist the new Image entity
            test.setSignature_image(signatureImage);
        }

        String currentDir = System.getProperty("user.dir");

        String testDirPath = currentDir + "/../test/";
        String dbPath="avisoftdocument/service_provider/" + serviceProviderId + "/Resized Images";

        // Ensure the directory exists, and create it if it doesn't
        File baseDir = new File(dbPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs(); // Create the directory structure if it doesn't exist
        }

        // Full file path for the signature image
        String fullFilePath = dbPath + File.separator + fileName;
        String fileUrl = fileService.getFileUrl(fullFilePath, request);

        // Set the file details in the signatureImage entity
        signatureImage.setFile_name(fileName);
        signatureImage.setFile_type(signatureFile.getContentType());
        signatureImage.setFile_path(fullFilePath);
        signatureImage.setFile_url(fileUrl);
        signatureImage.setImage_data(signatureFile.getBytes());
        signatureImage.setServiceProvider(serviceProvider);

//         Save the file to the specified path
        try {
            File destFile = new File(fullFilePath);
            FileUtils.writeByteArrayToFile(destFile, signatureFile.getBytes());
        } catch (IOException e) {
            throw new Exception("Failed to save the file", e);
        }
        ServiceProviderTestStatus serviceProviderTestStatus = entityManager.find(ServiceProviderTestStatus.class, 2L);
        serviceProvider.setTestStatus(serviceProviderTestStatus);
        // Merge and persist changes
        entityManager.merge(test);

        return test;
    }

    @Transactional
    public List<ServiceProviderTest> getServiceProviderTestByServiceProviderId(Long serviceProviderId, int page, int limit) throws EntityDoesNotExistsException {
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProvider == null) {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }

        // Calculate the start position for pagination
        int startPosition = page * limit;

        // Create the query
        TypedQuery<ServiceProviderTest> query = entityManager.createQuery(
                "SELECT spt FROM ServiceProviderTest spt WHERE spt.service_provider.service_provider_id = :serviceProviderId",
                ServiceProviderTest.class
        );
        query.setParameter("serviceProviderId", serviceProviderId);


        // Apply pagination
        query.setFirstResult(startPosition);
        query.setMaxResults(limit);

        return query.getResultList();
    }


    private boolean validateResizedImage(ServiceProviderTest test) throws IOException {
        Image downloadedImage = test.getDownloaded_image();
        if (downloadedImage == null || downloadedImage.getImage_data() == null) {
            throw new IllegalStateException("Downloaded image or its data is missing");
        }

        byte[] downloadedImageData = downloadedImage.getImage_data();
        byte[] resizedImageData = test.getResized_image_data();

        try {
            return areImagesVisuallyIdentical(downloadedImageData, resizedImageData);
        } catch (IOException e) {
            throw new IllegalStateException("Error comparing images", e);
        }
    }

    private Image getRandomImage() {
        // Fetch a random Image entity from the database
        long count = (long) entityManager.createQuery("SELECT COUNT(i) FROM Image i").getSingleResult();
        if (count == 0) {
            throw new EntityNotFoundException("No images available");
        }
        int randomIndex = new Random().nextInt((int) count);
        return (Image) entityManager.createQuery("SELECT i FROM Image i")
                .setFirstResult(randomIndex)
                .setMaxResults(1)
                .getSingleResult();
    }

    private String getRandomTypingText() {
        // Fetch a random TypingText entity from the database
        long count = (long) entityManager.createQuery("SELECT COUNT(t) FROM TypingText t").getSingleResult();
        if (count == 0) {
            throw new EntityNotFoundException("No typing texts available");
        }
        int randomIndex = new Random().nextInt((int) count);
        TypingText typingText = (TypingText) entityManager.createQuery("SELECT t FROM TypingText t")
                .setFirstResult(randomIndex)
                .setMaxResults(1)
                .getSingleResult();
        return typingText.getText();
    }

    private boolean validateTypedText(String originalText, String typedText) {
        if (originalText == null || typedText == null) {
            return false;
        }
        String trimmedOriginalText = originalText.trim();
        String trimmedTypedText = typedText.trim();
        return trimmedOriginalText.equals(trimmedTypedText);
    }

    private static final double SIMILARITY_THRESHOLD = 0.95; //can adjust this value

    public static boolean areImagesVisuallyIdentical(byte[] originalImageData, byte[] resizedImageData) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageData));
        BufferedImage resizedImage = ImageIO.read(new ByteArrayInputStream(resizedImageData));

        // Normalize images to a common size for comparison
        int commonWidth = 100; //adjust this value
        int commonHeight = 100;

        BufferedImage normalizedOriginal = normalizeImage(originalImage, commonWidth, commonHeight);
        BufferedImage normalizedResized = normalizeImage(resizedImage, commonWidth, commonHeight);

        return calculateSimilarity(normalizedOriginal, normalizedResized) >= SIMILARITY_THRESHOLD;
    }

    private static BufferedImage normalizeImage(BufferedImage image, int width, int height) {
        BufferedImage normalized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = normalized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(image, 0, 0, width, height, null);
        g2d.dispose();
        return normalized;
    }

    private static double calculateSimilarity(BufferedImage img1, BufferedImage img2) {
        long diff = 0;
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                diff += pixelDifference(img1.getRGB(x, y), img2.getRGB(x, y));
            }
        }
        long maxDiff = 3L * 255 * img1.getWidth() * img1.getHeight();
        return 1.0 - ((double) diff / maxDiff);
    }

    private static int pixelDifference(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >> 8) & 0xff;
        int b1 = rgb1 & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >> 8) & 0xff;
        int b2 = rgb2 & 0xff;
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }


}

