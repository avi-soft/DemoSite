package com.community.api.services;

import com.community.api.dto.UpdateQualificationDto;
import com.community.api.endpoint.avisoft.controller.Qualification.QualificationController;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Qualification;
import com.community.api.entity.QualificationDetails;
import com.community.api.services.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Objects;

@Service
public class QualificationDetailsService
{
    EntityManager entityManager;
    QualificationController qualificationController;
    QualificationService qualificationService;
    public QualificationDetailsService(EntityManager entityManager, QualificationController qualificationController, QualificationService qualificationService)
    {
        this.entityManager=entityManager;
        this.qualificationController = qualificationController;
        this.qualificationService = qualificationService;
    }
    @Transactional
    public QualificationDetails addQualificationDetails(Long customCustomerId, QualificationDetails qualificationDetails)
            throws EntityAlreadyExistsException, ExaminationDoesNotExistsException, CustomerDoesNotExistsException{

        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
        if (customCustomer == null) {
            throw new CustomerDoesNotExistsException("Customer does not exist with id " + customCustomerId);
        }
        TypedQuery<QualificationDetails> query = entityManager.createQuery(
                "SELECT q FROM QualificationDetails q WHERE q.custom_customer.id = :customerId AND q.qualification_id = :qualification_id",
                QualificationDetails.class
        );
        query.setParameter("customerId", customCustomerId);
        query.setParameter("qualification_id", qualificationDetails.getQualification_id());

        QualificationDetails existingQualificationDetails = query.getResultStream().findFirst().orElse(null);

        if (existingQualificationDetails != null ) {
            throw new EntityAlreadyExistsException("Qualification with id " + qualificationDetails.getQualification_id() + " already exists");
        }
        List<Qualification> qualifications = qualificationService.getAllQualifications();
        Long examinationToAdd=null;

        for(Qualification qualification : qualifications)
        {
            if(qualification.getQualification_id()== qualificationDetails.getQualification_id()) {
                examinationToAdd= qualification.getQualification_id();
                break;
            }
        }
        if (examinationToAdd == null) {
            throw new ExaminationDoesNotExistsException("Qualification with id " + qualificationDetails.getQualification_id() + " does not exist");
        }
        qualificationDetails.setQualification_id(examinationToAdd);
        qualificationDetails.setCustom_customer(customCustomer);
        customCustomer.getQualificationDetailsList().add(qualificationDetails);
        entityManager.persist(qualificationDetails);
        return qualificationDetails;
    }

    public List<QualificationDetails> getQualificationDetailsByCustomerId(Long customCustomerId) throws  CustomerDoesNotExistsException,RuntimeException {
        CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
        if(customCustomer==null)
        {
            throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
        }
       List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
        if(qualificationDetails.isEmpty())
        {
              throw new RuntimeException();
        }
        return qualificationDetails;
    }

    @Transactional
    public QualificationDetails deleteQualificationDetail(Long customCustomerId, Long qualificationId) throws EntityDoesNotExistsException, CustomerDoesNotExistsException {
        CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
        if(customCustomer==null)
        {
            throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
        }
        List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
        QualificationDetails qualificationDetailsToDelete =null;
        for(QualificationDetails qualificationDetails1 : qualificationDetails)
        {
            if(qualificationDetails1.getId()==qualificationId)
            {
                qualificationDetailsToDelete = qualificationDetails1;
                break;
            }
        }
        if (qualificationDetailsToDelete == null) {
            throw new EntityDoesNotExistsException("QualificationDetails with id " + qualificationId+ " does not exists");
        }
        qualificationDetails.remove(qualificationDetailsToDelete);
        entityManager.remove(qualificationDetailsToDelete);
        return qualificationDetailsToDelete;
    }

   @Transactional
    public QualificationDetails updateQualificationDetail(Long customCustomerId, Long qualificationId, UpdateQualificationDto qualification) throws EntityDoesNotExistsException, EntityAlreadyExistsException, CustomerDoesNotExistsException, ExaminationDoesNotExistsException {
       CustomCustomer customCustomer= entityManager.find(CustomCustomer.class,customCustomerId);
       if(customCustomer==null)
       {
           throw new CustomerDoesNotExistsException("Customer does not exist with id "+ customCustomerId);
       }
       List<QualificationDetails> qualificationDetails = customCustomer.getQualificationDetailsList();
       QualificationDetails qualificationDetailsToUpdate =null;
       for(QualificationDetails qualificationDetails1 : qualificationDetails)
       {
           if(qualificationDetails1.getId()==qualificationId)
           {
               qualificationDetailsToUpdate = qualificationDetails1;
               break;
           }
       }
       if (qualificationDetailsToUpdate == null) {
           throw new EntityDoesNotExistsException("Qualification details with id " + qualificationId+ " does not exists");
       }

       TypedQuery<QualificationDetails> query = entityManager.createQuery(
               "SELECT q FROM QualificationDetails q WHERE q.custom_customer.id = :customerId AND q.qualification_id = :qualification_id",
               QualificationDetails.class
       );
       query.setParameter("customerId", customCustomerId);
       query.setParameter("qualification_id", qualification.getQualification_id());
       QualificationDetails existingQualificationDetails = query.getResultStream().findFirst().orElse(null);

       if (existingQualificationDetails != null && qualificationId!= existingQualificationDetails.getId()) {
           throw new EntityAlreadyExistsException("Qualification details with id " + qualification.getQualification_id() + " already exists");
       }

       if (Objects.nonNull(qualification.getQualification_id())) {
           List<Qualification> qualifications = qualificationService.getAllQualifications();
           Long examinationToAdd = null;

           for (Qualification examination : qualifications) {
               if (examination.getQualification_id()==(qualification.getQualification_id())) {
                   examinationToAdd = examination.getQualification_id();
                   break;
               }
           }

           if (examinationToAdd == null) {
               throw new ExaminationDoesNotExistsException("Qualification with id " + qualification.getQualification_id() + " does not exist");
           }
           qualificationDetailsToUpdate.setQualification_id(examinationToAdd);
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
       if (Objects.nonNull(qualification.getSubject_stream())) {
           qualificationDetailsToUpdate.setSubject_stream(qualification.getSubject_stream());
       }
       if (Objects.nonNull(qualification.getGrade_or_percentage_value())) {
           qualificationDetailsToUpdate.setGrade_or_percentage_value(qualification.getGrade_or_percentage_value());
       }
       if (Objects.nonNull(qualification.getYear_of_passing())) {
           qualificationDetailsToUpdate.setYear_of_passing(qualification.getYear_of_passing());
       }
       return entityManager.merge(qualificationDetailsToUpdate);
    }
}
