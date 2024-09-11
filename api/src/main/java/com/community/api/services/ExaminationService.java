package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Examination;
import com.community.api.entity.Examination;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class ExaminationService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ExaminationService examinationService;
    @Autowired
    private ResponseService responseService;
    public List<Examination> getAllExaminations() throws RuntimeException {
        TypedQuery<Examination> query = entityManager.createQuery(Constant.FIND_ALL_EXAMINATIONS_QUERY, Examination.class);
        List<Examination> examinations = query.getResultList();
        if(query.getResultList().isEmpty())
        {
             throw new RuntimeException();
        }
        return examinations;
}
    @Transactional
    public Examination addExamination(@RequestBody Examination examination) throws Exception {
            Examination examinationToBeSaved=new Examination();
            int isSaved=0;
            long id = findCount() + 1;
            examinationToBeSaved.setExamination_id(id);
            examinationToBeSaved.setExamination_name(examination.getExamination_name());
        if (examination.getExamination_name() == null || examination.getExamination_name().isEmpty()) {
            throw new Exception();
        }
        entityManager.persist(examinationToBeSaved);
        return examination;
    }
    public long findCount() {
        String queryString = Constant.GET_EXAMINATIONS_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
}
