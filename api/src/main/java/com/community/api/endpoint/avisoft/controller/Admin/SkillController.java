package com.community.api.endpoint.avisoft.controller.Admin;

import com.community.api.entity.Skill;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Map;

@RestController
@RequestMapping("/service-provider-skill")
public class SkillController {
        @Autowired
        private EntityManager entityManager;
        @Autowired
        private ExceptionHandlingImplement exceptionHandling;
        @Transactional
        @PostMapping("/add-skill")
        public ResponseEntity<?> addSkill(@RequestBody Map<String,Object> skill) {
            try{
                String skillName=(String)skill.get("skill_name");
            if(skillName==null||skillName.isEmpty())
                return new ResponseEntity<>("Error saving skill : Skill Name required",HttpStatus.BAD_REQUEST);
            Skill skillToBeSaved=new Skill();
            skillToBeSaved.setSkill_name(skillName);
            entityManager.persist(skillToBeSaved);
            return new ResponseEntity<>(skill,HttpStatus.OK);
        }catch (Exception exception)
            {
                exceptionHandling.handleException(exception);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error saving skill : " + exception.getMessage());
            }
    }
}
