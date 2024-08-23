package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.endpoint.customer.Qualification;
import com.community.api.entity.Examination;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.FIND_ALL_EXAMINATIONS_QUERY;
import static com.community.api.component.Constant.FIND_ALL_QUALIFICATIONS_QUERY;

@RestController
@RequestMapping("/examinations")
public class ExaminationController {

    private EntityManager entityManager;

    public ExaminationController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping
    public List<Examination> getAllExaminations() {
        TypedQuery<Examination> query = entityManager.createQuery(FIND_ALL_EXAMINATIONS_QUERY, Examination.class);
        List<Examination> examinations = query.getResultList();
        return examinations;
    }

    @GetMapping("/byName")
    public ResponseEntity<Map<String,Object>> getExaminationByName(@RequestParam String e) {
        TypedQuery<Examination> query = entityManager.createQuery(Constant.FIND_EXAMINATION_BY_NAME_QUERY, Examination.class);
        query.setParameter("examination_name", e);

        // Retrieve the examination if it exists
        Examination existingExamination = query.getResultStream().findFirst().orElse(null);

        // Prepare the response
        Map<String, Object> response = new HashMap<>();
        if (existingExamination != null) {
            response.put("status", "success");
            response.put("examination", existingExamination);
        } else {
            response.put("status", "error");
            response.put("message", "Examination not found");
        }
        return ResponseEntity.ok(response);
    }
}

