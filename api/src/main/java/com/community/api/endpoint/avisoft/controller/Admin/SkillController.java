package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.component.Constant;
import com.community.api.entity.Skill;
import com.community.api.entity.StateCode;
import com.community.api.services.SkillService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping("/service-provider-skill")
public class SkillController {
        @Autowired
        private EntityManager entityManager;
        @Autowired
        private ExceptionHandlingImplement exceptionHandling;
        @Autowired
        private SkillService skillService;
        @Transactional
        @PostMapping("/addSkill")
        public ResponseEntity<?> addSkill(@RequestBody Map<String,Object> skill) {
            try{
               return skillService.addSkill(skill);
        }catch (Exception exception)
            {
                exceptionHandling.handleException(exception);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error saving skill : " + exception.getMessage());
            }
        }
    @GetMapping("/getSkillLists")
    public ResponseEntity<?> getSkillList() {
        try{
            return new ResponseEntity<>(skillService.findAllSkillList(),HttpStatus.OK);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error saving skill : " + exception.getMessage());
        }
    }
}
