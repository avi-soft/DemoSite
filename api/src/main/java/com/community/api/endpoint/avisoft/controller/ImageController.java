package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.Image;
import com.community.api.services.ImageService;
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
@RequestMapping("/image")
public class ImageController
{
    @Autowired
    ImageService imageService;
    @Transactional
    @PostMapping("/upload")
    public ResponseEntity<Image> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Image savedImage = imageService.saveImage(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImage);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/get-image/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        Image image = imageService.getImage(id);

        if (image != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(image.getFile_type()));
            headers.setContentDispositionFormData(image.getFile_name(), image.getFile_name());
            return new ResponseEntity<>(image.getImage_data(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}