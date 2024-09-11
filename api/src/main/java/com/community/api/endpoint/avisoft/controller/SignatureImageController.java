package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.SignatureImage;
import com.community.api.services.SignatureImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@RestController
@RequestMapping("/signature-signatureImage")
public class SignatureImageController
{
    @Autowired
    SignatureImageService signatureImageService;
    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<SignatureImage> uploadSignatureImage(@RequestParam("file") MultipartFile file) {
        try {
            SignatureImage savedSignatureImage = signatureImageService.saveSignatureImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedSignatureImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-signatureImage/{id}")
    public ResponseEntity<byte[]> getSignatureImage(@PathVariable Long id) {
        SignatureImage signatureImage = signatureImageService.getSignatureImage(id);

        if (signatureImage != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(signatureImage.getSignature_content_type()));
            headers.setContentDispositionFormData(signatureImage.getSignature_file_name(), signatureImage.getSignature_file_name());
            return new ResponseEntity<>(signatureImage.getSignature_image_data(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
