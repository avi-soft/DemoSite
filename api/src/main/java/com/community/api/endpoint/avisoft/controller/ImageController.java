package com.community.api.endpoint.avisoft.controller;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Image;
import com.community.api.services.ImageService;
import com.community.api.services.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/image")
public class ImageController
{
    @Autowired
    ImageService imageService;
    @Autowired
    EntityManager entityManager;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            Image savedImage = imageService.saveImage(file);
            return ResponseService.generateSuccessResponse("Image is saved",savedImage,HttpStatus.OK);
        } catch (IOException e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRandomImages()
    {
       List<Image> randomImages= imageService.getAllRandomImages();
       if(randomImages.isEmpty())
       {
           return ResponseService.generateSuccessResponse("Image list is empty",randomImages,HttpStatus.OK);
       }
       return ResponseService.generateSuccessResponse("Image list is found",randomImages,HttpStatus.OK);
    }



}
