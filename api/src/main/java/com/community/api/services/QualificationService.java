package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Qualification;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class QualificationService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private QualificationService qualificationService;
    @Autowired
    private ResponseService responseService;
    public List<Qualification> getAllQualifications() {
        TypedQuery<Qualification> query = entityManager.createQuery(Constant.FIND_ALL_QUALIFICATIONS_QUERY, Qualification.class);
        List<Qualification> qualifications = query.getResultList();
        return qualifications;
}
    @Transactional
    public Qualification addQualification(@RequestBody Qualification qualification) {
            Qualification qualificationToBeSaved =new Qualification();
            long id = findCount() + 1;
            if (qualification.getQualification_name() == null || qualification.getQualification_name().isEmpty()) {
                throw new IllegalArgumentException("Qualification name cannot be empty");
            }
            if(qualification.getQualification_description()==null || qualification.getQualification_description().isEmpty()) {
                throw new IllegalArgumentException("Qualification description cannot be empty");
            }
            if (!qualification.getQualification_name().matches("^[a-zA-Z ]+$")) {
                throw new IllegalArgumentException("Qualification name cannot contain numeric values or special characters");
            }
            if (!(qualification.getQualification_description() instanceof String)) {
                throw new IllegalArgumentException("Qualification description must be a string");
            }
            String description = qualification.getQualification_description();
            if (description.isEmpty()) {
                throw new IllegalArgumentException("Qualification description cannot be empty");
            }

            List<Qualification> qualifications = qualificationService.getAllQualifications();
            for (Qualification existingQualification : qualifications) {
                if (existingQualification.getQualification_name().equalsIgnoreCase(qualification.getQualification_name())) {
                    throw new IllegalArgumentException("Qualification with the same name already exists");
                }
            }
            qualificationToBeSaved.setQualification_id(id);
            qualificationToBeSaved.setQualification_name(qualification.getQualification_name());
            qualificationToBeSaved.setQualification_description(qualification.getQualification_description());
        entityManager.persist(qualificationToBeSaved);
        return qualificationToBeSaved;
    }
    public long findCount() {
        String queryString = Constant.GET_QUALIFICATIONS_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
}
