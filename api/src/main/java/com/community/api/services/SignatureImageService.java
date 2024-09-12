package com.community.api.services;

import com.community.api.entity.SignatureImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ByteArrayInputStream;

@Service
public class SignatureImageService
{
    @Autowired
    private EntityManager entityManager;

    public SignatureImageService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public SignatureImage saveSignatureImage(MultipartFile file) throws IOException {
        SignatureImage image = new SignatureImage();
        image.setSignature_file_name(file.getOriginalFilename());
        image.setSignature_content_type(file.getContentType());
        image.setSignature_image_data(file.getBytes());

        entityManager.persist(image);
        return image;
    }

    public SignatureImage getSignatureImage(Long id) {
        SignatureImage image = entityManager.find(SignatureImage.class, id);
        if (image == null) {
            throw new EntityNotFoundException("SignatureImage is not found");
        }
        return image;
    }
    public static boolean compareSignatures(byte[] uploadedImage, byte[] downloadedImage) throws IOException {
        int hash1 = calculateImageHash(uploadedImage);
        int hash2 = calculateImageHash(downloadedImage);

        // need to adjust this threshold based on your specific requirements
        int threshold = 10;
        return Math.abs(hash1 - hash2) <= threshold;
    }

    private static int calculateImageHash(byte[] imageData) throws IOException {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
        BufferedImage resized = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.drawImage(img, 0, 0, 8, 8, null);
        g.dispose();

        int hash = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                hash = (hash << 1) | (resized.getRGB(x, y) & 1);
            }
        }
        return hash;
    }
}