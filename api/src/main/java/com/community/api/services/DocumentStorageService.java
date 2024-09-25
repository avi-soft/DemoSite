package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.TypingText;
import com.community.api.services.exception.ExceptionHandlingService;
import com.community.api.utils.Document;
import com.community.api.utils.DocumentType;
import com.community.api.utils.ServiceProviderDocument;
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

            if (!DocumentStorageService.isValidFileType(file)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", ApiConstants.STATUS_ERROR,
                        "status_code", HttpStatus.BAD_REQUEST.value(),
                        "message", "Invalid file type: " + file.getOriginalFilename()
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

        try{
            String currentDir = System.getProperty("user.dir");

            String testDirPath = currentDir + "/../test/";
//        String testResourcesPath = testDirPath + "src/main/resources/";

            File avisoftDir = new File(testDirPath + "avisoftdocument");
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
        }catch(Exception e){
            exceptionHandlingService.handleException(e);
            throw new IOException("Error saving document: " + e.getMessage());
        }
    }


    public static boolean isValidFileType(MultipartFile file) {
        String[] allowedFileTypes = {"application/pdf", "image/jpeg", "image/png", "image/jpg"};
        String contentType = file.getContentType();

        boolean isContentTypeValid = Arrays.asList(allowedFileTypes).contains(contentType);

        String fileName = file.getOriginalFilename();

        boolean isExtensionValid = fileName != null && (fileName.endsWith(".pdf") || fileName.endsWith(".jpeg") || fileName.endsWith(".jpg") || fileName.endsWith(".png"));

        return isContentTypeValid && isExtensionValid;
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
            
       
                new DocumentType(14,"MATRICULATION", "Completed secondary education or equivalent"),
                new DocumentType( 15,"INTERMEDIATE", "Completed higher secondary education or equivalent"),
                new DocumentType(16,"BACHELORS", "Completed undergraduate degree program education "),
                new DocumentType(17,"MASTERS", "Completed postgraduate degree program education"),
                new DocumentType( 18,"DOCTORATE", "Completed doctoral degree program education"),
                new DocumentType(19,"DOMICILE", "The permanent home or principal residence of a person."),
                new DocumentType( 20,"HANDICAPED", "An outdated term for individuals with physical or mental disabilities; \"person with a disability\" is preferred today"),
                new DocumentType(21,"C-FORM-PHOTO", "A C Form photo is a standardized ID photo for official documents."),
                new DocumentType(23,"BUSSINESS_PHOTO", "A Standard proof of Running Bussiness"),
                new DocumentType(24,"PERSONAL_PHOTO", "A Personal Photgraph of SP")
            };



        for (DocumentType document : documents) {
            saveDocumentType(document);
        }
    }


    @Transactional
    public void deleteDocument(Document document) {

        String filePath = document.getFilePath();
        System.out.println(filePath + " filePath");
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
        em.remove(document);
    }

    @Transactional
    public void updateOrCreateDocument(Document existingDocument, MultipartFile file, DocumentType documentTypeObj, Long customerId, String role) {
        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + documentTypeObj.getDocument_type_name()
                + File.separator + file.getOriginalFilename();

        existingDocument.setFilePath(newFilePath);
        existingDocument.setName(file.getOriginalFilename());
        em.merge(existingDocument);
    }

    @Transactional
    public void createDocument(MultipartFile file, DocumentType documentTypeObj, CustomCustomer customCustomer, Long customerId, String role) {
        Document newDocument = new Document();
        newDocument.setName(file.getOriginalFilename());
        newDocument.setCustom_customer(customCustomer);
        newDocument.setDocumentType(documentTypeObj);


        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + documentTypeObj.getDocument_type_name()
                + File.separator + file.getOriginalFilename();


        newDocument.setFilePath(newFilePath);
        em.persist(newDocument);
    }
    @Transactional
    public void createDocumentServiceProvider(MultipartFile file, DocumentType documentTypeObj, ServiceProviderEntity serviceProviderEntity, Long customerId, String role) {
        ServiceProviderDocument newDocument = new ServiceProviderDocument();
        newDocument.setName(file.getOriginalFilename());
        newDocument.setServiceProviderEntity(serviceProviderEntity);
        newDocument.setDocumentType(documentTypeObj);


        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + documentTypeObj.getDocument_type_name()
                + File.separator + file.getOriginalFilename();


        newDocument.setFilePath(newFilePath);
        em.persist(newDocument);
    }
    @Transactional
    public void updateOrCreateServiceProvider(ServiceProviderDocument existingDocument, MultipartFile file, DocumentType documentTypeObj, Long customerId, String role) {
        String newFilePath = "avisoftdocument"
                + File.separator + role + File.separator + customerId
                + File.separator + documentTypeObj.getDocument_type_name()
                + File.separator + file.getOriginalFilename();

        existingDocument.setFilePath(newFilePath);
        existingDocument.setName(file.getOriginalFilename());
        em.merge(existingDocument);
    }
    public String findRoleName(DocumentType documentTypeId) {
        return entityManager.createQuery("SELECT dt.document_type_name FROM DocumentType dt WHERE dt.document_type_id = :documentTypeId", String.class)
                .setParameter("documentTypeId", documentTypeId.getDocument_type_id())
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    @Transactional
    public void saveAllTypingTexts() {
        TypingText[] typingTexts = {
                new TypingText(1L, "The sun sets over the horizon, painting the sky with vibrant hues of orange and pink. Birds fly home, and the world quietly transitions into the peaceful calm of evening."),
                new TypingText(2L, "A gentle breeze rustles the leaves, carrying the sweet scent of blooming flowers through the air. The world feels alive and at peace."),
                new TypingText(3L, "The mountain stood tall, its peak covered in snow, contrasting sharply with the clear blue sky above. Nature's beauty was on full display."),
                new TypingText(4L, "Waves crash against the shore, their rhythmic motion soothing to the soul. The ocean stretches endlessly, its mysteries hidden beneath the surface."),
                new TypingText(5L, "In the heart of the forest, sunlight filters through the canopy, casting dappled shadows on the ground. A sense of tranquility fills the air.")
        };

        for (TypingText text : typingTexts) {
            saveTypingText(text);
        }
    }

    @Transactional
    public void saveTypingText(TypingText typingText) {
        entityManager.persist(typingText);
    }
}