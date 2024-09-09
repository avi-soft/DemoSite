package com.community.api.dto;

import com.community.api.utils.DocumentType;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Component
public class DocumentDTO {
    private MultipartFile file;
    private DocumentType documentType;


}