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
            qualificationToBeSaved.setQualification_id(id);
            qualificationToBeSaved.setQualification_name(qualification.getQualification_name());
            if (qualification.getQualification_name() == null || qualification.getQualification_name().isEmpty()) {
            throw new IllegalArgumentException();
            }
        entityManager.persist(qualificationToBeSaved);
        return qualification;
    }
    public long findCount() {
        String queryString = Constant.GET_QUALIFICATIONS_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
}
