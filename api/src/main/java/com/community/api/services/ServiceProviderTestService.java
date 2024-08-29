package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.util.Random;

import static com.community.api.services.ImageValidator.validateImage;

@Service
public class ServiceProviderTestService {

    @Autowired
    private EntityManager entityManager;

    public ServiceProviderTestService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Transactional
    public ServiceProviderTest startTest(Long serviceProviderId) {
        ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if(serviceProvider==null)
        {
            throw new EntityNotFoundException("Service Provider not found");
        }
        ServiceProviderTest test = new ServiceProviderTest();
        test.setServiceProvider(serviceProvider);
        // Generate URLs or paths for the random image, etc.
        test.setDownloadedImageUrl(generateRandomImageUrl());
        entityManager.persist(test);
        return test;
    }

    @Transactional
    public ServiceProviderTest uploadResizedImage(Long testId, String resizedImageUrl) {
        ServiceProviderTest test = entityManager.find(ServiceProviderTest.class,testId);
                if(test==null)
                {
                    throw new EntityNotFoundException("Test not found");
                }
        // Validate the resized image
        validateImage(resizedImageUrl);
        test.setResizedImageUrl(resizedImageUrl);
         entityManager.persist(test); //may be here merge is used
         return test;
    }

    @Transactional
    public ServiceProviderTest submitTypingTest(Long testId, String typedText) {
        ServiceProviderTest test = entityManager.find(ServiceProviderTest.class,testId);
        if(test==null)
        {
            throw new EntityNotFoundException("Test not found");
        }
        boolean isPassed = validateTypedText(test.getTypingTestText(), typedText);
        test.setIsTypingTestPassed(isPassed);
        return entityManager.merge(test);
    }

    private String generateRandomImageUrl() {
        // Example using Lorem Picsum
        int width = 400;  // width of the image
        int height = 300; // height of the image
        return "https://picsum.photos/" + width + "/" + height + "?random=" + new Random().nextInt(1000);
    }


    private boolean validateTypedText(String originalText, String typedText) {
        // Check for null or empty inputs
        if (originalText == null || typedText == null) {
            return false;
        }

        // Trim and convert both texts to lower case
        String trimmedOriginalText = originalText.trim().toLowerCase();
        String trimmedTypedText = typedText.trim().toLowerCase();

        // Compare the texts for case-insensitive match
        return trimmedOriginalText.equals(trimmedTypedText);
    }


}

