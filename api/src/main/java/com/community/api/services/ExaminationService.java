package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Examination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class ExaminationService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExaminationService examinationService;
    @Autowired
    private ResponseService responseService;
    public List<Examination> getAllExaminations() {
        TypedQuery<Examination> query = entityManager.createQuery(Constant.FIND_ALL_EXAMINATIONS_QUERY, Examination.class);
        List<Examination> examinations = query.getResultList();
        if(query.getResultList().isEmpty())
        {
            return null;
        }
        return examinations;
}
}
