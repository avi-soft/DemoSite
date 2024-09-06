package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.ErrorResponse;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.services.exception.FileSizeExceededException;
import com.community.api.services.exception.InvalidFileTypeException;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class DocumentStorageService {

    public static final String BASE_DIRECTORY = "C:\\Documents";

    @Autowired
    private  ResponseService responseService;

    @Autowired
    private EntityManager em;

    @Autowired
    private ExceptionHandlingService exceptionHandlingService;

    @Autowired
    private DocumentStorageService documentStorageService;

    public ResponseEntity<?> saveDocuments(MultipartFile file, String documentTypeStr, Long customerId, String role) {
        try {
            if (!DocumentStorageService.isValidFileType(file)) {
                throw new InvalidFileTypeException("Invalid file type: " + file.getOriginalFilename());
            }

            if (file.getSize() > Constant.MAX_FILE_SIZE) {
                throw new FileSizeExceededException("File size exceeds the maximum allowed size: " + file.getOriginalFilename());
            }

            CustomCustomer customCustomer = em.find(CustomCustomer.class, customerId);
            if (customCustomer == null) {
                throw new EntityNotFoundException("Customer not found with ID: " + customerId);
            }


       DocumentType documentType = em.createQuery(
                            "SELECT dt FROM DocumentType dt WHERE dt.document_type_name = :document_type_name", DocumentType.class)
                    .setParameter("document_type_name", documentTypeStr)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            if (documentType == null) {
                throw new EntityNotFoundException("DocumentType not found : " + documentTypeStr);
            }

            String fileName = file.getOriginalFilename();
            try (InputStream fileInputStream = file.getInputStream()) {
                documentStorageService.saveDocumentOndirctory(customerId.toString(), documentTypeStr, fileName, fileInputStream, role);
            }

            Document doc = new Document();
            doc.setName(fileName);
            String filePath = DocumentStorageService.BASE_DIRECTORY
                    + File.separator
                    + "avisoft"
                    + File.separator
                    + role
                    + File.separator
                    + customerId
                    + File.separator
                    + documentTypeStr
                    + File.separator
                    + fileName;
            doc.setFilePath(filePath);
            doc.setData(file.getBytes());
            doc.setCustomCustomer(customCustomer);
            doc.setDocumentType(documentType);
            em.persist(doc);

            return responseService.generateSuccessResponse("Documents uploaded", doc, HttpStatus.OK);

        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return responseService.generateErrorResponse("Error uploading document", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * Saves a file to a dynamic directory structure.
     *
     * @param customerId The ID of the customer.
     * @param documentType The type of document (e.g., "aadhar", "pan", "signature").
     * @param fileName The name of the file to be saved.
     * @param fileInputStream InputStream of the file data.
     * @throws IOException If an I/O error occurs.
     */
    public void saveDocumentOndirctory(String customerId, String documentType, String fileName, InputStream fileInputStream, String role) throws IOException {
        File baseDir = new File(BASE_DIRECTORY);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        File avisoftDir = new File(baseDir, "avisoft");
        if (!avisoftDir.exists()) {
            avisoftDir.mkdirs();
        }

        File roleDir = new File(avisoftDir, role);
        if (!roleDir.exists()) {
            roleDir.mkdirs();
        }

        File customerDir = new File(roleDir, customerId);
        if (!customerDir.exists()) {
            customerDir.mkdirs();
        }

        File documentTypeDir = new File(customerDir, documentType);
        if (!documentTypeDir.exists()) {
            documentTypeDir.mkdirs();
        }

        File file = new File(documentTypeDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }
    }


    public static boolean isValidFileType(MultipartFile file) {

        String[] allowedFileTypes = {"application/pdf", "image/jpeg", "image/png"};
        return Arrays.asList(allowedFileTypes).contains(file.getContentType());
    }

    public List<DocumentType> getAllDocumentTypes() {
        return em.createQuery("SELECT dt FROM DocumentType dt", DocumentType.class).getResultList();
    }
    public String getDocumentTypeFromMultipartFile(MultipartFile file, List<DocumentType> allDocumentTypes) {
        String fileName = file.getOriginalFilename();

        if (fileName != null) {
            for (DocumentType docType : allDocumentTypes) {
                if (fileName.toLowerCase().contains(docType.getDocument_type_name().toLowerCase())) {
                    return docType.getDocument_type_name();
                }
            }
        }
        return "Unknown Document Type";
    }

}
