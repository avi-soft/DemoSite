package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.Image;
import com.community.api.entity.TypingText;
import com.community.api.services.ResponseService;
import com.community.api.services.TypingTextService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/typing-text")
public class TypingTextController
{
    private final TypingTextService typingTextService;
    private final ExceptionHandlingImplement exceptionHandling;

    public TypingTextController(TypingTextService typingTextService,ExceptionHandlingImplement exceptionHandling) {
        this.typingTextService = typingTextService;
        this.exceptionHandling=exceptionHandling;
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllRandomImages()
    {
        List<TypingText> randomTypingTexts= typingTextService.getAllRandomTypingTexts();
        if(randomTypingTexts.isEmpty())
        {
            return ResponseService.generateSuccessResponse("Typing Text list is empty",randomTypingTexts, HttpStatus.OK);
        }
        return ResponseService.generateSuccessResponse("Typing Text list is found",randomTypingTexts,HttpStatus.OK);
    }

    @PostMapping("/add-all")
    public ResponseEntity<?> addAllRandomImages(@RequestBody List<TypingText> typingTexts)
    {
        try
        {
            List<?> randomTypingTexts= typingTextService.addAllRandomTypingTexts(typingTexts);
            return ResponseService.generateSuccessResponse("The Typing Texts are added successfully",randomTypingTexts, HttpStatus.CREATED);
        }
        catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
        catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Something went wrong",HttpStatus.BAD_REQUEST);
        }
    }
}
