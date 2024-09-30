package com.community.api.endpoint.avisoft.controller;

import com.community.api.entity.Image;
import com.community.api.entity.TypingText;
import com.community.api.services.ResponseService;
import com.community.api.services.TypingTextService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/typing-text")
public class TypingTextController
{
    private final TypingTextService typingTextService;

    public TypingTextController(TypingTextService typingTextService) {
        this.typingTextService = typingTextService;
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
}
