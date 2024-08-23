package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.avisoft.controller.Qualification.ExaminationController;
import com.community.api.endpoint.customer.CustomCustomer;
import com.community.api.endpoint.customer.Qualification;
import com.community.api.entity.Examination;
import com.community.api.services.exception.CustomerDoesNotExistsException;
import com.community.api.services.exception.EntityDoesNotExistsException;
import com.community.api.services.exception.EntityAlreadyExistsException;
import com.community.api.services.exception.ExaminationDoesNotExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@Service
public class QualificationService
{
    EntityManager entityManager;
    ExaminationController examinationController;

    public QualificationService(EntityManager entityManager, ExaminationController examinationController)
    {
        this.entityManager=entityManager;
        this.examinationController= examinationController;
    }
    @Transactional
    public Qualification addQualification(Long customCustomerId, Qualification qualification)
            throws MethodArgumentNotValidException, EntityDoesNotExistsException, EntityAlreadyExistsException, ExaminationDoesNotExistsException {

        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
        if (customCustomer == null) {
            throw new EntityDoesNotExistsException("Customer does not exist with id " + customCustomerId);
        }
        TypedQuery<Qualification> query = entityManager.createQuery(
                "SELECT q FROM Qualification q WHERE q.customCustomer.id = :customerId AND q.examinationName = :examinationName",
                Qualification.class);
        query.setParameter("customerId", customCustomerId);
        query.setParameter("examinationName", qualification.getExaminationName());
        Qualification existingQualification = query.getResultStream().findFirst().orElse(null);

        if (existingQualification != null ) {
            throw new EntityAlreadyExistsException("Qualification with name " + qualification.getExaminationName() + " already exists");
        }
        List<Examination> examinations=examinationController.getAllExaminations();
        String examinationToAdd=null;

        for(Examination examination: examinations)
        {
            if(examination.getExamination_name().equalsIgnoreCase(qualification.getExaminationName()) ) {
                examinationToAdd=examination.getExamination_name();
                break;
            }
        }

        if (examinationToAdd == null) {
            throw new ExaminationDoesNotExistsException("Examination with name " + qualification.getExaminationName() + " does not exist");
        }
        qualification.setExaminationName(examinationToAdd);
        qualification.setCustomCustomer(customCustomer);
        customCustomer.getQualificationList().add(qualification);
        entityManager.merge(customCustomer);
        entityManager.persist(qualification);
        return qualification;
    }

    public List<Qualification> getAllQualifications() {
        // Create a TypedQuery using the predefined JPQL query
        TypedQuery<Qualification> query = entityManager.createQuery(FIND_ALL_QUALIFICATIONS_QUERY, Qualification.class);

        // Execute the query and get the result list
        List<Qualification> qualifications = query.getResultList();

        return qualifications;
    }

    public Qualification getQualificationById(Long customCustomerId,Long qualificationId) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
        if(customCustomer==null)
        {
            throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
        }
       List<Qualification> qualifications= customCustomer.getQualificationList();
        Qualification qualificationToGet=null;
        for(Qualification qualification1 : qualifications)
        {
            if(qualification1.getId()==qualificationId)
            {
                qualificationToGet=qualification1;
                break;
            }
        }
        if (qualificationToGet == null) {
            throw new EntityDoesNotExistsException("Qualification with id " + qualificationId+ " does not exists");
        }
        return qualificationToGet;
    }

    @Transactional
    public Qualification deleteQualification(Long qualificationId) throws EntityDoesNotExistsException {
        Qualification qualification = entityManager.find(Qualification.class, qualificationId);
        if (qualification == null) {
            throw new EntityDoesNotExistsException("Qualification with id " + qualificationId+ " does not exist");
        }
        entityManager.remove(qualification);
        return qualification;
    }

   @Transactional
    public Qualification updateQualification(Long customCustomerId,Long qualificationId,Qualification qualification) throws EntityDoesNotExistsException, EntityAlreadyExistsException, CustomerDoesNotExistsException, ExaminationDoesNotExistsException {
       CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
       if(customCustomer==null)
       {
           throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
       }
       List<Qualification> qualifications= customCustomer.getQualificationList();
       Qualification qualificationToUpdate=null;
       for(Qualification qualification1 : qualifications)
       {
           if(qualification1.getId()==qualificationId)
           {
               qualificationToUpdate=qualification1;
               break;
           }
       }
       if (qualificationToUpdate == null) {
           throw new EntityDoesNotExistsException("Qualification with id " + qualificationId+ " does not exists");
       }
       if (Objects.nonNull(qualification.getExaminationName())) {
           List<Examination> examinations = examinationController.getAllExaminations();
           String examinationToAdd = null;

           for (Examination examination : examinations) {
               if (examination.getExamination_name().equalsIgnoreCase(qualification.getExaminationName())) {
                   examinationToAdd = examination.getExamination_name();
                   break;
               }
           }

           if (examinationToAdd == null) {
               throw new ExaminationDoesNotExistsException("Examination with name " + qualification.getExaminationName() + " does not exist");
           }
           qualification.setExaminationName(examinationToAdd);
       }
        if (Objects.nonNull(qualification.getInstitutionName())) {
            qualificationToUpdate.setInstitutionName(qualification.getInstitutionName());
        }
        if (Objects.nonNull(qualification.getBoardOrUniversity())) {
            qualificationToUpdate.setBoardOrUniversity(qualification.getBoardOrUniversity());
        }
       if (Objects.nonNull(qualification.getMarksObtained())) {
           qualificationToUpdate.setBoardOrUniversity(String.valueOf(qualification.getMarksObtained()));
       }
       if (Objects.nonNull(qualification.getMarksTotal())) {
           qualificationToUpdate.setBoardOrUniversity(String.valueOf(qualification.getMarksTotal()));
       }
       if (Objects.nonNull(qualification.getSubjectStream())) {
           qualificationToUpdate.setSubjectStream(qualification.getSubjectStream());
       }
       if (Objects.nonNull(qualification.getGradeOrPercentageValue())) {
           qualificationToUpdate.setGradeOrPercentageValue(qualification.getGradeOrPercentageValue());
       }
       if (Objects.nonNull(qualification.getYearOfPassing())) {
           qualificationToUpdate.setYearOfPassing(qualification.getYearOfPassing());
       }
       return entityManager.merge(qualificationToUpdate);
    }

}
