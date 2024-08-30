package com.community.api.endpoint.avisoft.controller.Qualification;

import com.community.api.component.Constant;
import com.community.api.entity.Examination;
import com.community.api.services.ResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.community.api.component.Constant.FIND_ALL_EXAMINATIONS_QUERY;

@RestController
@RequestMapping("/examination")
public class ExaminationController {

    private EntityManager entityManager;
    private ResponseService responseService;
    public ExaminationController(EntityManager entityManager,ResponseService responseService) {
        this.responseService=responseService;
        this.entityManager = entityManager;
    }

    @GetMapping("/get-all-examinations")
    public ResponseEntity<?> getAllExaminations() {
        TypedQuery<Examination> query = entityManager.createQuery(FIND_ALL_EXAMINATIONS_QUERY, Examination.class);
        List<Examination> examinations = query.getResultList();
        if(query.getResultList().isEmpty())
        {
            return null;
        }
        return responseService.generateSuccessResponse("List Retrieved Successfully",examinations, HttpStatus.OK);
    }
}

