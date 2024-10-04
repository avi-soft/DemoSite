package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.configuration.ImageSizeConfig;
import com.community.api.dto.GiveUploadedImageScoreDTO;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.*;
import com.community.api.entity.Image;
import com.community.api.services.exception.EntityDoesNotExistsException;
import io.swagger.models.auth.In;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.time.LocalDateTime;
import java.util.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class ServiceProviderTestService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DocumentStorageService documentStorageService;
    @Autowired
    private FileService fileService;
    @Autowired
    private DocumentStorageService fileUploadService;

    @Value("${skill.test.required.image.size.min}")
    private String minImageSize;

    public ServiceProviderTestService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public Map<String, Object> startTest(Long serviceProviderId,HttpServletRequest request) throws EntityDoesNotExistsException{
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if(serviceProvider==null)
        {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }
        if(serviceProvider.getTestStatus()!=null)
        {
            ServiceProviderTestStatus serviceProviderTestStatus= entityManager.find(ServiceProviderTestStatus.class, Constant.TEST_COMPLETED_STATUS);
            if(serviceProviderTestStatus==null)
            {
                throw new IllegalArgumentException("Test Status id "+ Constant.TEST_COMPLETED_STATUS+" Not found so cannot start test of ServiceProvider");
            }
            Long testStatus= serviceProviderTestStatus.getTest_status_id();
            if(!serviceProvider.getServiceProviderTests().isEmpty() && serviceProvider.getTestStatus().getTest_status_id().equals(Constant.INITIAL_TEST_STATUS))
            {
                ServiceProviderTest test= serviceProvider.getServiceProviderTests().get(0);
                String imageUrl = fileService.getFileUrl(test.getDownloaded_image().getFile_path(),request);
                String maxImageSize= ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
                String imageValidation = "Only images between "+minImageSize+" and "+maxImageSize+" are allowed";
                Map<String, Object> response = new HashMap<>();
                response.put("test", test);
                response.put("imageValidation", imageValidation);
                response.put("downloadImageUrl", imageUrl);
                response.put("requiredMinImageSize",minImageSize);
                response.put("requiredMaxImageSize",maxImageSize);
                return response;
            }
            if(serviceProvider.getTestStatus().getTest_status_id().equals(testStatus) )
            {
                throw new IllegalArgumentException("Skill Test has already been submitted.You cannot start a new test.");
            }

            ServiceProviderTestStatus serviceProviderTestStatusForApproved= entityManager.find(ServiceProviderTestStatus.class, Constant.APPROVED_TEST);
            if(serviceProviderTestStatus==null)
            {
                throw new IllegalArgumentException("Test Status id "+ Constant.APPROVED_TEST+" Not found so cannot start test of ServiceProvider");
            }
            if(serviceProvider.getTestStatus().getTest_status_id().equals(serviceProviderTestStatusForApproved.getTest_status_id()))
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
        test.setIs_test_completed(false);
        entityManager.persist(test);
        serviceProvider.getServiceProviderTests().add(test);
        entityManager.merge(serviceProvider);

        String imageUrl = fileService.getFileUrl(test.getDownloaded_image().getFile_path(),request);
        String maxImageSize= ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
        String imageValidation = "Only images between "+minImageSize+" and "+maxImageSize+" are allowed";
        Map<String, Object> response = new HashMap<>();
        response.put("test", test);
        response.put("imageValidation", imageValidation);
        response.put("downloadImageUrl", imageUrl);
        response.put("requiredMinImageSize",minImageSize);
        response.put("requiredMaxImageSize",maxImageSize);

        return response;
    }

    @Transactional
    public Map<String, Object> uploadResizedImages(Long serviceProviderId, Long testId, MultipartFile resizedFile, HttpServletRequest request) throws Exception {
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
        long minSizeInBytes = ImageSizeConfig.convertToBytes(minImageSize);

        if(!documentStorageService.isValidFileType(resizedFile))
        {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }

        // Validate image size
        if (resizedFile.getSize() < minSizeInBytes || resizedFile.getSize() > Constant.MAX_FILE_SIZE) {
            String maxImageSize= ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
            test.setIs_image_test_passed(false);
            entityManager.merge(test);


            throw new IllegalArgumentException("Resized image size should be between " + minImageSize + " and " + maxImageSize);


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


        String db_path ="avisoftdocument/SERVICE_PROVIDER/Resized/Resized_Images";
        String dbPath=db_path+File.separator+ resizedFile.getOriginalFilename();

        String fileUrl = fileService.getFileUrl(dbPath, request);

        fileUploadService.uploadFileOnFileServer(resizedFile, "Resized_Images", "Resized", "SERVICE_PROVIDER");
        // Set file metadata in the ResizedImage object
        resizedImage.setFile_name(fileName);
        resizedImage.setFile_type(resizedFile.getContentType());
        resizedImage.setFile_path(dbPath);
        resizedImage.setImage_data(resizedFile.getBytes());
        resizedImage.setServiceProvider(serviceProvider);


        // Set the image data and validate the resized image
        test.setResized_image_data(resizedFile.getBytes());
        boolean isImageValid = validateResizedImage(test);
        if (!isImageValid) {
            throw new IllegalArgumentException("Uploaded image is different from expected image");
        }

        // If image validation passes, mark the test as passed
        test.setIs_image_test_passed(true);
        entityManager.merge(test);


        Map<String, Object> response = new HashMap<>();
        response.put("test", test);
        response.put("resizedImageUrl", fileUrl);

        return response;
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
            if(testId.equals(serviceProviderTest.getTest_id()))
            {
                test=serviceProviderTest;
                break;
            }
        }
        if (test == null) {
            throw new EntityNotFoundException();
        }
        test.setSubmitted_text(typedText);

        // Calculate typing test score based on similarity between expected text and entered text
        int typingTestScore = calculateTypingTestScore(test.getTyping_test_text(), typedText);
        test.setTyping_test_scores(typingTestScore);
        serviceProvider.setWrittenTestScore(typingTestScore);

        entityManager.merge(serviceProvider);
        return entityManager.merge(test);

    }

    @Transactional
    public Map<String,Object> uploadSignatureImage(Long serviceProviderId, Long testId, MultipartFile signatureFile,HttpServletRequest request) throws Exception {
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

        // Check the MIME type of the file
        if(!documentStorageService.isValidFileType(signatureFile))
        {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed.");
        }

        long minSizeInBytes = ImageSizeConfig.convertToBytes(minImageSize);
//        long maxSizeInBytes = ImageSizeConfig.convertToBytes(maxImageSize);

        // Validate image size
        if (signatureFile.getSize() < minSizeInBytes || signatureFile.getSize() > Constant.MAX_FILE_SIZE) {
            String maxImageSize= ImageSizeConfig.convertBytesToReadableSize(Constant.MAX_FILE_SIZE);
            test.setIs_image_test_passed(false);
            entityManager.merge(test);

            throw new IllegalArgumentException("Signature image size should be between " + minImageSize + " and " + maxImageSize);
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

        String db_path ="avisoftdocument/SERVICE_PROVIDER/Signature/Signature_Images";
        String dbPath= db_path+File.separator+signatureFile.getOriginalFilename();

        fileUploadService.uploadFileOnFileServer(signatureFile, "Signature_Images", "Signature", "SERVICE_PROVIDER");

        String fileUrl = fileService.getFileUrl(dbPath, request);

        // Set the file details in the signatureImage entity
        signatureImage.setFile_name(fileName);
        signatureImage.setFile_type(signatureFile.getContentType());
        signatureImage.setFile_path(dbPath);
        signatureImage.setImage_data(signatureFile.getBytes());
        signatureImage.setServiceProvider(serviceProvider);

        test.setIs_test_completed(true);
        test.setSubmitted_at(LocalDateTime.now());
        entityManager.merge(test);
        ServiceProviderTestStatus serviceProviderTestStatus = entityManager.find(ServiceProviderTestStatus.class, Constant.TEST_COMPLETED_STATUS);
        if(serviceProviderTestStatus==null)
        {
            throw new IllegalArgumentException("Test status with id status 'completed test' does not exists");
        }
        serviceProvider.setTestStatus(serviceProviderTestStatus);
        entityManager.merge(serviceProvider);

        Map<String, Object> response = new HashMap<>();
        response.put("test", test);
        response.put("signatureImageUrl", fileUrl);

        return response;
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

    @Transactional
    public ResponseEntity<?> getCompletedServiceProviderTest(Long serviceProviderId,HttpServletRequest request) throws EntityDoesNotExistsException {
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProvider == null) {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }

        // query to get all tests of the service provider
        TypedQuery<ServiceProviderTest> query = entityManager.createQuery(
                "SELECT spt FROM ServiceProviderTest spt WHERE spt.service_provider.service_provider_id = :serviceProviderId",
                ServiceProviderTest.class
        );

        // Binding the parameter to the query
        query.setParameter("serviceProviderId", serviceProviderId);

        List<ServiceProviderTest> serviceProviderTests = query.getResultList();
        if (serviceProviderTests.isEmpty()) {
            return ResponseService.generateSuccessResponse("Service Provider has not given any test yet",null,HttpStatus.OK);
        }

        ServiceProviderTest serviceProviderTestToReturn = null;
        for (ServiceProviderTest serviceProviderTest : serviceProviderTests) {
            if((serviceProviderTest.getIs_test_completed()!=null))
            {
                if (Boolean.TRUE.equals(serviceProviderTest.getIs_test_completed())) {
                    if (serviceProviderTestToReturn == null || serviceProviderTest.getSubmitted_at().isAfter(serviceProviderTestToReturn.getSubmitted_at())) {
                        serviceProviderTestToReturn = serviceProviderTest;
                    }
                }
            }
        }
        if(serviceProviderTestToReturn==null)
        {
            return ResponseService.generateSuccessResponse("Service Provider has not completed any test yet",null,HttpStatus.OK);
        }
        String downloadedImageUrl= fileService.getFileUrl(serviceProviderTestToReturn.getDownloaded_image().getFile_path(),request);
        String resizedImageUrl= fileService.getFileUrl(serviceProviderTestToReturn.getResized_image().getFile_path(),request);
        String signatureImageUrl= fileService.getFileUrl(serviceProviderTestToReturn.getSignature_image().getFile_path(),request);
        Map<String,Object> completedTestMap= new HashMap<>();
        completedTestMap.put("completed_test",serviceProviderTestToReturn);
        completedTestMap.put("downloaded_image_url",downloadedImageUrl);
        completedTestMap.put("resized_image_url",resizedImageUrl);
        completedTestMap.put("signature_image_url",signatureImageUrl);

        return ResponseService.generateSuccessResponse("Completed test is found",completedTestMap,HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<?> givePointsForImageUpload(Long serviceProviderId, GiveUploadedImageScoreDTO giveUploadedImageScoreDTO) throws EntityDoesNotExistsException {
        if(giveUploadedImageScoreDTO.getImage_test_scores()==null)
        {
            return ResponseService.generateErrorResponse("Image Test Score cannot be null",HttpStatus.BAD_REQUEST);
        }
        if(giveUploadedImageScoreDTO.getImage_test_scores()<0)
        {
            return ResponseService.generateErrorResponse("Image Upload Score cannot be a negative number",HttpStatus.BAD_REQUEST);
        }
        if(giveUploadedImageScoreDTO.getImage_test_scores()>15)
        {
            return ResponseService.generateErrorResponse("Image Upload Score cannot be greater than 15",HttpStatus.BAD_REQUEST);
        }
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProvider == null) {
            throw new EntityDoesNotExistsException("Service Provider not found");
        }

        TypedQuery<ServiceProviderTest> query = entityManager.createQuery(
                "SELECT spt FROM ServiceProviderTest spt WHERE spt.service_provider.service_provider_id = :serviceProviderId",
                ServiceProviderTest.class
        );

        query.setParameter("serviceProviderId", serviceProviderId);

        List<ServiceProviderTest> serviceProviderTests = query.getResultList();
        if (serviceProviderTests.isEmpty()) {
            return ResponseService.generateSuccessResponse("Service Provider has not given any test yet",null,HttpStatus.OK);
        }

        ServiceProviderTest serviceProviderTest = null;
        for (ServiceProviderTest serviceProviderTest1 : serviceProviderTests) {
            if((serviceProviderTest1.getIs_test_completed()!=null))
            {
                if (Boolean.TRUE.equals(serviceProviderTest1.getIs_test_completed())) {
                    if (serviceProviderTest == null || serviceProviderTest1.getSubmitted_at().isAfter(serviceProviderTest.getSubmitted_at())) {
                        serviceProviderTest = serviceProviderTest1;
                    }
                }
            }
        }
        if(serviceProviderTest==null)
        {
            return ResponseService.generateSuccessResponse("Service Provider has not completed any test yet",null,HttpStatus.OK);
        }

        serviceProviderTest.setImage_test_scores(giveUploadedImageScoreDTO.getImage_test_scores());
        entityManager.merge(serviceProviderTest);
        serviceProvider.setImageUploadScore(giveUploadedImageScoreDTO.getImage_test_scores());
        serviceProvider.setTotalSkillTestPoints(serviceProviderTest.getImage_test_scores() + serviceProviderTest.getTyping_test_scores());

        Integer totalScore=serviceProvider.getWrittenTestScore()+giveUploadedImageScoreDTO.getImage_test_scores();
        if(serviceProvider.getBusinessUnitInfraScore()!=null)
        {
            Integer businessUnitInfraScore= serviceProvider.getBusinessUnitInfraScore();
            totalScore+=businessUnitInfraScore;
        }
        if(serviceProvider.getWorkExperienceScore()!=null)
        {
            Integer workExperienceScore= serviceProvider.getWorkExperienceScore();
            totalScore+=workExperienceScore;
        }
        if(serviceProvider.getQualificationScore()!=null)
        {
            Integer qualificationScore= serviceProvider.getQualificationScore();
            totalScore+=qualificationScore;
        }
        if(serviceProvider.getTechnicalExpertiseScore()!=null)
        {
            Integer technicalExpertiseScore= serviceProvider.getTechnicalExpertiseScore();
            totalScore+=technicalExpertiseScore;
        }

        if(serviceProvider.getType().equalsIgnoreCase("PROFESSIONAL"))
        {
            if(serviceProvider.getStaffScore()!=null)
            {
                Integer staffScore= serviceProvider.getStaffScore();
                totalScore+=staffScore;
            }
        }
        else {
            if(serviceProvider.getStaffScore()!=null)
            {
                Integer partTimeOrFullTimeScore= serviceProvider.getPartTimeOrFullTimeScore();
                totalScore+=partTimeOrFullTimeScore;
            }
        }
        serviceProvider.setTotalScore(totalScore);
        entityManager.merge(serviceProvider);
                return ResponseService.generateSuccessResponse("Image test scores updated successfully",serviceProviderTest,HttpStatus.OK);
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

    private int calculateTypingTestScore(String expectedText, String typedText) {
        // Handle null or empty cases
        if (expectedText == null || typedText == null || expectedText.isEmpty()) {
            return 0; // If expectedText is null or empty, no score can be given
        }

        // Split the texts into words for comparison (can be adjusted for character-level comparison if needed)
        String[] expectedWords = expectedText.split("\\s+");
        String[] typedWords = typedText.split("\\s+");

        int totalWords = expectedWords.length;
        int matchingWords = 0;

        // Compare word by word up to the length of the shortest text
        for (int i = 0; i < Math.min(expectedWords.length, typedWords.length); i++) {
            if (expectedWords[i].equalsIgnoreCase(typedWords[i])) {
                matchingWords++;
            }
        }

        // Calculate the accuracy as a percentage of matching words
        double accuracy = (double) matchingWords / totalWords;

        // Map accuracy to score between 0 and 15
        int score = (int) Math.round(accuracy * 15);

        // Return the score, ensuring it's between 0 and 15
        return Math.max(0, Math.min(15, score));
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

