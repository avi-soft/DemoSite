package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
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

    @Autowired
    private EntityManager entityManager;

    public ResponseEntity<Map<String, Object>> saveDocuments(MultipartFile file, String documentTypeStr, Long customerId, String role) {
        try {

            if (!isValidFileType(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid file type: "
                ));
            }

            if (file.getSize() > Constant.MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "File size exceeds the maximum allowed size: " + file.getOriginalFilename()
                ));
            }

            String fileName = file.getOriginalFilename();
            try (InputStream fileInputStream = file.getInputStream()) {
                this.saveDocumentOndirctory(customerId.toString(), documentTypeStr, fileName, fileInputStream, role);
            }catch(Exception e){
                exceptionHandlingService.handleException(e);
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid file : " + file
                ));
            }

            Map<String, Object> responseBody = Map.of(
                    "message", "Document uploaded successfully",
                    "status", "OK",
                    "data",documentTypeStr +" uploaded successfully",
                    "status_code", HttpStatus.OK.value()
            );

            return ResponseEntity.ok(responseBody);
        } catch (Exception e) {
            exceptionHandlingService.handleException(e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", ApiConstants.STATUS_ERROR,
                    "message", "Error uploading document: " + e.getMessage(),
                    "status_code", HttpStatus.INTERNAL_SERVER_ERROR.value()
            ));
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

        /*File baseDir = new File(BASE_DIRECTORY);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }*/

        File avisoftDir = new File("avisoftdocument");
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


    @Transactional
    public void saveDocumentType(DocumentType document) {
        entityManager.persist(document);
    }

    @Transactional
    public void saveAllDocumentTypes() {

                DocumentType[] documents = {
                new DocumentType(5, "EWS_CERTIFICATE", "Certificate for individuals and families below a certain income threshold to access various benefits and concessions."),
                new DocumentType(6, "DIPLOMA", "Official academic certificate awarded upon completion of an undergraduate or vocational course, certifying knowledge and skills in a specific field."),
                new DocumentType(7, "GRADUATION", "Awarded upon completion of a degree program, signifying fulfillment of academic requirements in a specific discipline."),
                new DocumentType(8, "POST_GRADUATION", "Issued after completing a postgraduate degree, acknowledging advanced training in a specialized field."),
                new DocumentType(9, "CASTE_CERTIFICATE", "Certifies an individual's caste for reservations and benefits in education and employment."),
                new DocumentType(10, "ADDRESS_CERTIFICATE", "Verifies an individual’s residential address for identity verification and other purposes."),
                new DocumentType(11, "INCOME_CERTIFICATE", "Confirms an individual’s or family’s annual income for applying for government benefits and financial assistance."),
                new DocumentType(12, "DRIVING_LICENSE", "Authorizes an individual to operate motor vehicles, confirming knowledge of traffic laws and vehicle operation skills."),
                new DocumentType(13, "OTHERS", "Includes other document types not listed above, tailored to specific needs or contexts.")
        };



        for (DocumentType document : documents) {
            saveDocumentType(document);
        }
    }
}
