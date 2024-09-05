package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Image;
import com.community.api.entity.ServiceProviderTest;
import com.community.api.entity.SignatureImage;
import com.community.api.entity.TypingText;
import com.community.api.services.exception.EntityDoesNotExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

@Service
public class ServiceProviderTestService {

    @Autowired
    private EntityManager entityManager;
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
        Image randomImage = getRandomImage();
        String randomText= getRandomTypingText();
        SignatureImage randomSignatureImage = getRandomSignatureImage();

        ServiceProviderTest test = new ServiceProviderTest();
        test.setService_provider(serviceProvider);
        test.setDownloaded_image(randomImage);
        test.setTyping_test_text(randomText);
        test.setDownloaded_signature_image(randomSignatureImage);
        entityManager.persist(test);
        return test;
    }

    @Transactional
    public ServiceProviderTest uploadResizedImage(Long testId, MultipartFile resizedFile) throws Exception {
        ServiceProviderTest test = entityManager.find(ServiceProviderTest.class, testId);
        if (test == null) {
            throw new EntityNotFoundException();
        }

        // Validate the image size
        if (resizedFile.getSize() > MAX_IMAGE_SIZE_MB) {
            test.setIs_image_test_passed(false);
            entityManager.merge(test);
            throw new IllegalArgumentException("Image size exceeds 2 MB");
        }
        byte[] resizedImageData;
        try (InputStream inputStream = resizedFile.getInputStream()) {
            resizedImageData = inputStream.readAllBytes();
        }
        test.setResized_image_data(resizedImageData);
        entityManager.merge(test);
        // Validate the resized image
        boolean isImageValid = validateResizedImage(test);
        if (isImageValid) {
            test.setIs_image_test_passed(true); // Set the image test to false if validation fails
        }
        else {
            test.setIs_image_test_passed(false);
            throw new Exception();
        }

        // Persist the changes
        return test;
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

    private SignatureImage getRandomSignatureImage() {
        // Fetch the count of available signature images in the database
        long count = (long) entityManager.createQuery("SELECT COUNT(s) FROM SignatureImage s").getSingleResult();
        if (count == 0) {
            throw new EntityNotFoundException("No signature images available");
        }
        // Get a random index within the range of available signature images
        int randomIndex = new Random().nextInt((int) count);
        return (SignatureImage) entityManager.createQuery("SELECT s FROM SignatureImage s")
                .setFirstResult(randomIndex)
                .setMaxResults(1)
                .getSingleResult();
    }

    @Transactional
    public ServiceProviderTest submitTypedText(Long testId, String typedText) throws EntityNotFoundException{
        ServiceProviderTest test = entityManager.find(ServiceProviderTest.class, testId);
        if (test == null) {
            throw new EntityNotFoundException();
        }

        boolean isPassed = validateTypedText(test.getTyping_test_text(), typedText);
        test.setSubmitted_text(typedText);
        test.setIs_typing_test_passed(isPassed);
        // Persist the changes
        return entityManager.merge(test);
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

    @Transactional
    public ServiceProviderTest uploadSignatureImage(Long testId, MultipartFile signatureFile) throws Exception {
        ServiceProviderTest test = entityManager.find(ServiceProviderTest.class, testId);
        if (test == null) {
            throw new EntityNotFoundException();
        }

        // Validate the image size
        if (signatureFile.getSize() > MAX_IMAGE_SIZE_MB) {
           test.setIs_signature_test_passed(false);
            entityManager.merge(test);
            throw new IllegalArgumentException("Signature image size exceeds 2 MB");
        }
        byte[] resizedSignatureData;
        try (InputStream inputStream = signatureFile.getInputStream()) {
            resizedSignatureData = inputStream.readAllBytes();
        }
        test.setResized_signature_image_data(resizedSignatureData);
        entityManager.merge(test);
        // Validate the resized image
        boolean isImageValid = compareSignatures(signatureFile,testId);
        if (isImageValid) {
            test.setIs_signature_test_passed(true); // Set the image test to false if validation fails
        }
        else {
            test.setIs_signature_test_passed(false);
            throw new Exception();
        }

        // Persist the changes
        return test;
    }
    public boolean compareSignatures(MultipartFile uploadedFile, Long serviceProviderId) throws IOException {
        ServiceProviderTest test = entityManager.find(ServiceProviderTest.class,serviceProviderId);
        if(test ==null)
        {
            throw new EntityNotFoundException("Service Provider not found");
        }
        byte[] uploadedImageData = uploadedFile.getBytes();
        byte[] downloadedImageData = test.getDownloaded_signature_image().getSignature_image_data();

        boolean isSignatureMatched = SignatureImageService.compareSignatures(uploadedImageData, downloadedImageData);

        test.setIs_signature_test_passed(isSignatureMatched);
        entityManager.persist(test);

        return isSignatureMatched;
    }
}

