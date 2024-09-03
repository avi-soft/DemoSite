package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Districts;
import com.community.api.entity.Skill;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class SkillService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Transactional
    public ResponseEntity<?> addSkill(@RequestBody Map<String,Object> skill) {
        try{
            String skillName=(String)skill.get("skill_name");
            if(skillName==null||skillName.isEmpty())
                return new ResponseEntity<>("Error saving skill : Skill Name required", HttpStatus.BAD_REQUEST);
            Skill skillToBeSaved=new Skill();
            int id=(int)findCount();
            skillToBeSaved.setSkill_id(++id);
            skillToBeSaved.setSkill_name(skillName);
            entityManager.persist(skillToBeSaved);
            return new ResponseEntity<>(skillToBeSaved,HttpStatus.OK);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error saving skill : " + exception.getMessage());
        }
    }
    public long findCount() {
        String queryString = Constant.GET_SKILLS_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public List<Skill> findAllSkillList() {
        TypedQuery<Skill> query = entityManager.createQuery(Constant.GET_ALL_SKILLS, Skill.class);
        return query.getResultList();
    }
}
