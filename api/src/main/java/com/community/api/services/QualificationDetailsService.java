package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.UpdateQualificationDto;
import com.community.api.endpoint.avisoft.controller.Qualification.QualificationController;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Qualification;
import com.community.api.entity.QualificationDetails;
import com.community.api.entity.ScoringCriteria;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import com.community.api.services.exception.*;
import com.community.api.utils.DocumentType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.*;

@Service
public class QualificationDetailsService {
    EntityManager entityManager;
    QualificationController qualificationController;
    QualificationService qualificationService;
    SharedUtilityService sharedUtilityService;
    ServiceProviderServiceImpl serviceProviderService;

    public QualificationDetailsService(EntityManager entityManager, QualificationController qualificationController, QualificationService qualificationService, SharedUtilityService sharedUtilityService, ServiceProviderServiceImpl serviceProviderService) {
        this.entityManager = entityManager;
        this.qualificationController = qualificationController;
        this.qualificationService = qualificationService;
        this.sharedUtilityService = sharedUtilityService;
        this.serviceProviderService=serviceProviderService;
    }

    @Transactional
    public QualificationDetails addQualificationDetails(Long userId, QualificationDetails qualificationDetails, String roleName)
            throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException {

        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
            List<DocumentType> qualifications = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
            qualificationDetails.setQualification_id(qualificationToAdd);
            qualificationDetails.setService_provider(serviceProviderEntity);
            serviceProviderEntity.getQualificationDetailsList().add(qualificationDetails);
            entityManager.persist(qualificationDetails);
            giveQualificationScore(userId);
            return qualificationDetails;
        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        checkIfQualificationAlreadyExists(userId, qualificationDetails.getQualification_id(), roleName);
        List<DocumentType> qualifications = qualificationService.getAllQualifications();
        Integer qualificationToAdd = null;
        qualificationToAdd = findQualificationId(qualificationDetails.getQualification_id(), qualifications);
        qualificationDetails.setQualification_id(qualificationToAdd);
        qualificationDetails.setCustom_customer(customCustomer);
        customCustomer.getQualificationDetailsList().add(qualificationDetails);
        entityManager.persist(qualificationDetails);
        return qualificationDetails;
    }

    @Transactional
    public List<Map<String, Object>> getQualificationDetailsByCustomerId(Long userId, String roleName) throws CustomerDoesNotExistsException, RuntimeException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
            return sharedUtilityService.mapQualifications(qualificationDetails);
        }
        CustomCustomer customCustomer = findCustomCustomerById(userId);
        qualificationDetails = customCustomer.getQualificationDetailsList();
        return sharedUtilityService.mapQualifications(qualificationDetails);
    }

    @Transactional
    public QualificationDetails deleteQualificationDetail(Long userId, Long qualificationId, String roleName) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
        } else {
            CustomCustomer customCustomer = findCustomCustomerById(userId);
            qualificationDetails = customCustomer.getQualificationDetailsList();
        }

        QualificationDetails qualificationDetailsToDelete = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getId().equals(qualificationId)) {
                qualificationDetailsToDelete = qualificationDetails1;
                break;
            }
        }
        if (qualificationDetailsToDelete == null) {
            throw new EntityDoesNotExistsException("QualificationDetails with id " + qualificationId + " does not exists");
        }
        qualificationDetails.remove(qualificationDetailsToDelete);
        entityManager.remove(qualificationDetailsToDelete);
        giveQualificationScore(userId);
        return qualificationDetailsToDelete;
    }

    @Transactional
    public QualificationDetails updateQualificationDetail(Long userId, Long qualificationId, UpdateQualificationDto qualification, String roleName) throws EntityDoesNotExistsException, EntityAlreadyExistsException, CustomerDoesNotExistsException, ExaminationDoesNotExistsException {
        List<QualificationDetails> qualificationDetails;
        if (roleName.equals(Constant.SERVICE_PROVIDER)) {
            ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
            qualificationDetails = serviceProviderEntity.getQualificationDetailsList();
        } else {
            CustomCustomer customCustomer = findCustomCustomerById(userId);
            qualificationDetails = customCustomer.getQualificationDetailsList();
        }

        QualificationDetails qualificationDetailsToUpdate = null;
        for (QualificationDetails qualificationDetails1 : qualificationDetails) {
            if (qualificationDetails1.getId().equals(qualificationId)) {
                qualificationDetailsToUpdate = qualificationDetails1;
                break;
            }
        }
        if (qualificationDetailsToUpdate == null) {
            throw new EntityDoesNotExistsException("Qualification details with id " + qualificationId + " does not exists");
        }
        String queryStr;

        // Build the query string based on the entity type
        if ("SERVICE_PROVIDER".equalsIgnoreCase(roleName)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.service_provider.service_provider_id = :entityId AND q.qualification_id = :qualification_id";
        } else if ("CUSTOMER".equalsIgnoreCase(roleName)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.custom_customer.id = :entityId AND q.qualification_id = :qualification_id";
        } else {
            throw new IllegalArgumentException("Invalid entity type specified.");
        }

        // Create the dynamic query based on the entity type
        TypedQuery<QualificationDetails> query = entityManager.createQuery(queryStr, QualificationDetails.class);
        query.setParameter("entityId", userId);
        query.setParameter("qualification_id", qualification.getQualification_id());

        // Execute the query and check if qualification already exists
        QualificationDetails existingQualificationDetails = query.getResultStream().findFirst().orElse(null);

        if (existingQualificationDetails != null && !qualificationId.equals(existingQualificationDetails.getId())) {
            throw new EntityAlreadyExistsException("Qualification details with id " + qualification.getQualification_id() + " already exists");
        }

        if (Objects.nonNull(qualification.getQualification_id())) {
            List<DocumentType> qualificationDetailsList = qualificationService.getAllQualifications();
            Integer qualificationToAdd = findQualificationId(qualification.getQualification_id(), qualificationDetailsList);
            qualificationDetailsToUpdate.setQualification_id(qualificationToAdd);

            //Qualification scoring
            if(roleName.equalsIgnoreCase(Constant.SERVICE_PROVIDER))
            {
                giveQualificationScore(userId);
            }
        }

        if (Objects.nonNull(qualification.getInstitution_name())) {
            qualificationDetailsToUpdate.setInstitution_name(qualification.getInstitution_name());
        }
        if (Objects.nonNull(qualification.getBoard_or_university())) {
            qualificationDetailsToUpdate.setBoard_or_university(qualification.getBoard_or_university());
        }
        if (Objects.nonNull(qualification.getMarks_obtained())) {
            qualificationDetailsToUpdate.setMarks_obtained(qualification.getMarks_obtained());
        }
        if (Objects.nonNull(qualification.getTotal_marks())) {
            qualificationDetailsToUpdate.setTotal_marks(qualification.getTotal_marks());
        }
        if (Objects.nonNull(qualification.getSubject_name())) {
            qualificationDetailsToUpdate.setSubject_name(qualification.getSubject_name());
        }
        if (Objects.nonNull(qualification.getStream())) {
            qualificationDetailsToUpdate.setStream(qualification.getStream());
        }
        if (Objects.nonNull(qualification.getGrade_or_percentage_value())) {
            qualificationDetailsToUpdate.setGrade_or_percentage_value(qualification.getGrade_or_percentage_value());
        }
        if (Objects.nonNull(qualification.getYear_of_passing())) {
            qualificationDetailsToUpdate.setYear_of_passing(qualification.getYear_of_passing());
        }
        if (Objects.nonNull(qualification.getExamination_role_number())) {
            qualificationDetailsToUpdate.setExamination_role_number(qualification.getExamination_role_number());
        }
        if (Objects.nonNull(qualification.getExamination_registration_number())) {
            qualificationDetailsToUpdate.setExamination_registration_number(qualification.getExamination_registration_number());
        }
        return entityManager.merge(qualificationDetailsToUpdate);
    }

    private CustomCustomer findCustomCustomerById(Long customCustomerId) throws CustomerDoesNotExistsException {
        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
        if (customCustomer == null) {
            throw new CustomerDoesNotExistsException("Customer does not exist with id " + customCustomerId);
        }
        return customCustomer;
    }

    private ServiceProviderEntity findServiceProviderById(Long serviceProviderId) throws CustomerDoesNotExistsException {
        ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProviderEntity == null) {
            throw new CustomerDoesNotExistsException("ServiceProvider does not exist with id " + serviceProviderId);
        }
        return serviceProviderEntity;
    }

    private void checkIfQualificationAlreadyExists(Long entityId, Integer qualificationId, String entityType) throws EntityAlreadyExistsException {
        String queryStr;

        // Build the query string based on the entity type
        if ("SERVICE_PROVIDER".equalsIgnoreCase(entityType)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.service_provider.service_provider_id = :entityId AND q.qualification_id = :qualification_id";
        } else if ("CUSTOMER".equalsIgnoreCase(entityType)) {
            queryStr = "SELECT q FROM QualificationDetails q WHERE q.custom_customer.id = :entityId AND q.qualification_id = :qualification_id";
        } else {
            throw new IllegalArgumentException("Invalid entity type specified.");
        }

        // Create the dynamic query based on the entity type
        TypedQuery<QualificationDetails> query = entityManager.createQuery(queryStr, QualificationDetails.class);
        query.setParameter("entityId", entityId);
        query.setParameter("qualification_id", qualificationId);

        // Execute the query and check if qualification already exists
        QualificationDetails existingQualification = query.getResultStream().findFirst().orElse(null);

        if (existingQualification != null) {
            throw new EntityAlreadyExistsException("Qualification with id " + qualificationId + " already exists for " + entityType.toLowerCase());
        }
    }

    public Integer findQualificationId(Integer qualificationId, List<DocumentType> qualifications) throws ExaminationDoesNotExistsException {
        for (DocumentType qualification : qualifications) {
            if (qualification.getDocument_type_id().equals(qualificationId)) {
                return qualification.getDocument_type_id();
            }
        }
        throw new ExaminationDoesNotExistsException("Qualification with id " + qualificationId + " does not exist");
    }

    public void giveQualificationScore(Long userId) throws CustomerDoesNotExistsException {
        ServiceProviderEntity serviceProviderEntity = findServiceProviderById(userId);
        TypedQuery<ScoringCriteria> typedQuery=  entityManager.createQuery(Constant.GET_ALL_SCORING_CRITERIA,ScoringCriteria.class);
        List<ScoringCriteria> scoringCriteriaList = typedQuery.getResultList();

        Integer totalScore=0;
        ScoringCriteria scoringCriteriaToMap =null;
        if(!serviceProviderEntity.getQualificationDetailsList().isEmpty())
        {
            QualificationDetails qualificationDetail= serviceProviderEntity.getQualificationDetailsList().get(serviceProviderEntity.getQualificationDetailsList().size()-1);
            DocumentType qualification1 = entityManager.find(DocumentType.class, qualificationDetail.getQualification_id());
            if (qualification1 != null) {
                if (qualification1.getDocument_type_name().equalsIgnoreCase("BACHELORS") || qualification1.getDocument_type_name().equalsIgnoreCase("MASTERS") || qualification1.getDocument_type_name().equalsIgnoreCase("DOCTORATE")) {
                    scoringCriteriaToMap=serviceProviderService.traverseListOfScoringCriteria(6L,scoringCriteriaList,serviceProviderEntity);
                    if(scoringCriteriaToMap==null)
                    {
                        throw new IllegalArgumentException("Scoring Criteria is not found for scoring Qualification Score");
                    }
                    else {
                        serviceProviderEntity.setQualificationScore(scoringCriteriaToMap.getScore());
                    }
                }
                else if(qualification1.getDocument_type_name().equalsIgnoreCase("INTERMEDIATE")) {
                    scoringCriteriaToMap=serviceProviderService.traverseListOfScoringCriteria(7L,scoringCriteriaList,serviceProviderEntity);
                    if(scoringCriteriaToMap==null)
                    {
                        throw new IllegalArgumentException("Scoring Criteria is not found for scoring Qualification Score");
                    }
                    else {
                        serviceProviderEntity.setQualificationScore(scoringCriteriaToMap.getScore());
                    }
                }
                else if(qualification1.getDocument_type_name().equalsIgnoreCase("MATRICULATION")) {
                    serviceProviderEntity.setQualificationScore(0);
                }
            }
            else {
                throw new IllegalArgumentException("Unknown Qualification is found");
            }
        }
        else if(serviceProviderEntity.getQualificationDetailsList().isEmpty()) {
            serviceProviderEntity.setQualificationScore(0);
        }

        if(serviceProviderEntity.getType().equalsIgnoreCase("PROFESSIONAL"))
        {
            totalScore=serviceProviderEntity.getBusinessUnitInfraScore()+serviceProviderEntity.getWorkExperienceScore()+serviceProviderEntity.getTechnicalExpertiseScore()+ serviceProviderEntity.getQualificationScore()+ serviceProviderEntity.getStaffScore();
        }
        else {
            totalScore=serviceProviderEntity.getInfraScore()+serviceProviderEntity.getWorkExperienceScore()+serviceProviderEntity.getTechnicalExpertiseScore()+serviceProviderEntity.getQualificationScore()+serviceProviderEntity.getPartTimeOrFullTimeScore();
        }
        if(serviceProviderEntity.getWrittenTestScore()!=null)
        {
            totalScore=totalScore+serviceProviderEntity.getWrittenTestScore();
        }
        if(serviceProviderEntity.getImageUploadScore()!=null)
        {
            totalScore=totalScore+serviceProviderEntity.getImageUploadScore();
        }
        serviceProviderEntity.setTotalScore(0);
        serviceProviderEntity.setTotalScore(totalScore);
        serviceProviderService.assignRank(serviceProviderEntity,totalScore);
        entityManager.merge(serviceProviderEntity);
    }
}