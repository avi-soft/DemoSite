package com.community.api.dto;

import com.community.api.utils.DocumentType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDto {
    private Long documentId;
    private DocumentType documentType;
    private byte[] data;
}