package com.community.api.services;

import com.community.api.dto.UpdateQualificationDto;
import com.community.api.endpoint.avisoft.controller.Qualification.ExaminationController;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Qualification;
import com.community.api.entity.Examination;
import com.community.api.services.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Objects;

@Service
public class QualificationService
{
    EntityManager entityManager;
    ExaminationController examinationController;
    ExaminationService examinationService;
    public QualificationService(EntityManager entityManager, ExaminationController examinationController,ExaminationService examinationService)
    {
        this.entityManager=entityManager;
        this.examinationController= examinationController;
        this.examinationService=examinationService;
    }
    @Transactional
    public Qualification addQualification(Long customCustomerId, Qualification qualification)
            throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException{

        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
        if (customCustomer == null) {
            throw new CustomerDoesNotExistsException("Customer does not exist with id " + customCustomerId);
        }
        TypedQuery<Qualification> query = entityManager.createQuery(
                "SELECT q FROM Qualification q WHERE q.custom_customer.id = :customerId AND q.examination_id = :examination_id",
                Qualification.class
        );
        query.setParameter("customerId", customCustomerId);
        query.setParameter("examination_id", qualification.getExamination_id());

        Qualification existingQualification = query.getResultStream().findFirst().orElse(null);

        if (existingQualification != null ) {
            throw new EntityAlreadyExistsException("Qualification with id " + qualification.getExamination_id() + " already exists");
        }
        List<Examination> examinations=examinationService.getAllExaminations();
        Long examinationToAdd=null;

        for(Examination examination: examinations)
        {
            if(examination.getExamination_id()==qualification.getExamination_id()) {
                examinationToAdd=examination.getExamination_id();
                break;
            }
        }
        if (examinationToAdd == null) {
            throw new ExaminationDoesNotExistsException("Examination with id " + qualification.getExamination_id() + " does not exist");
        }
        qualification.setExamination_id(examinationToAdd);
        qualification.setCustom_customer(customCustomer);
        customCustomer.getQualificationList().add(qualification);
        entityManager.persist(qualification);
        return qualification;
    }

    public List<Qualification> getQualificationsByCustomerId(Long customCustomerId) throws  CustomerDoesNotExistsException,RuntimeException {
        CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
        if(customCustomer==null)
        {
            throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
        }
       List<Qualification> qualifications= customCustomer.getQualificationList();
        if(qualifications.isEmpty())
        {
              throw new RuntimeException();
        }
        return qualifications;
    }

    @Transactional
    public Qualification deleteQualification(Long customCustomerId, Long qualificationId) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
        if(customCustomer==null)
        {
            throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
        }
        List<Qualification> qualifications= customCustomer.getQualificationList();
        Qualification qualificationToDelete=null;
        for(Qualification qualification1 : qualifications)
        {
            if(qualification1.getId()==qualificationId)
            {
                qualificationToDelete=qualification1;
                break;
            }
        }
        if (qualificationToDelete == null) {
            throw new EntityDoesNotExistsException("Qualification with id " + qualificationId+ " does not exists");
        }
        qualifications.remove(qualificationToDelete);
        entityManager.remove(qualificationToDelete);
        return qualificationToDelete;
    }

   @Transactional
    public Qualification updateQualification(Long customCustomerId, Long qualificationId, UpdateQualificationDto qualification) throws EntityDoesNotExistsException, EntityAlreadyExistsException, CustomerDoesNotExistsException, ExaminationDoesNotExistsException {
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

       TypedQuery<Qualification> query = entityManager.createQuery(
               "SELECT q FROM Qualification q WHERE q.custom_customer.id = :customerId AND q.examination_id = :examination_id",
               Qualification.class
       );
       query.setParameter("customerId", customCustomerId);
       query.setParameter("examination_id", qualification.getExamination_id());
       Qualification existingQualification = query.getResultStream().findFirst().orElse(null);

       if (existingQualification != null && qualificationId!=existingQualification.getId()) {
           throw new EntityAlreadyExistsException("Qualification with id " + qualification.getExamination_id() + " already exists");
       }

       if (Objects.nonNull(qualification.getExamination_id())) {
           List<Examination> examinations = examinationService.getAllExaminations();
           Long examinationToAdd = null;

           for (Examination examination : examinations) {
               if (examination.getExamination_id()==(qualification.getExamination_id())) {
                   examinationToAdd = examination.getExamination_id();
                   break;
               }
           }

           if (examinationToAdd == null) {
               throw new ExaminationDoesNotExistsException("Examination with id " + qualification.getExamination_id() + " does not exist");
           }
           qualificationToUpdate.setExamination_id(examinationToAdd);
       }
        if (Objects.nonNull(qualification.getInstitution_name())) {
            qualificationToUpdate.setInstitution_name(qualification.getInstitution_name());
        }
        if (Objects.nonNull(qualification.getBoard_or_university())) {
            qualificationToUpdate.setBoard_or_university(qualification.getBoard_or_university());
        }
       if (Objects.nonNull(qualification.getMarks_obtained())) {
           qualificationToUpdate.setMarks_obtained(qualification.getMarks_obtained());
       }
       if (Objects.nonNull(qualification.getTotal_marks())) {
           qualificationToUpdate.setTotal_marks(qualification.getTotal_marks());
       }
       if (Objects.nonNull(qualification.getSubject_stream())) {
           qualificationToUpdate.setSubject_stream(qualification.getSubject_stream());
       }
       if (Objects.nonNull(qualification.getGrade_or_percentage_value())) {
           qualificationToUpdate.setGrade_or_percentage_value(qualification.getGrade_or_percentage_value());
       }
       if (Objects.nonNull(qualification.getYear_of_passing())) {
           qualificationToUpdate.setYear_of_passing(qualification.getYear_of_passing());
       }
       return entityManager.merge(qualificationToUpdate);
    }
}
