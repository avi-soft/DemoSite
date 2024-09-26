package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddStreamDto;
import com.community.api.dto.AddSubjectDto;
import com.community.api.entity.CustomSubject;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

public class SubjectService {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    @Autowired
    JwtUtil jwtTokenUtil;

    @Autowired
    RoleService roleService;

    public Boolean validiateAuthorization(String authHeader) throws Exception {
        try {
            String jwtToken = authHeader.substring(7);

            Integer roleId = jwtTokenUtil.extractRoleId(jwtToken);
            String role = roleService.getRoleByRoleId(roleId).getRole_name();

            if (role.equals(Constant.SUPER_ADMIN) || role.equals(Constant.ADMIN)) {
                return true;
            }
            return false;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("ERRORS WHILE VALIDATING AUTHORIZATION: " + exception.getMessage() + "\n");
        }
    }

    public List<CustomSubject> getAllSubject() {
        try {
            List<CustomSubject> subjectList = entityManager.createQuery(Constant.GET_ALL_SUBJECT, CustomSubject.class).getResultList();
            return subjectList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomSubject getSubjectBySubjectId(Long subjectId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_SUBJECT_BY_SUBJECT_ID, CustomSubject.class);
            query.setParameter("subjectId", subjectId);
            List<CustomSubject> stream = query.getResultList();

            if (!stream.isEmpty()) {
                return stream.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public Boolean validateAddSubjectDto(AddSubjectDto addSubjectDto) throws Exception {
        try{
            if(addSubjectDto.getSubjectName() == null || addSubjectDto.getSubjectDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("SUBJECT NAME CANNOT BE NULL OR EMPTY");
            }
            if(addSubjectDto.getSubjectDescription() != null && addSubjectDto.getSubjectDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("SUBJECT DESCRIPTION CANNOT BE EMPTY");
            }
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    public void saveSubject(AddSubjectDto addSubjectDto) throws Exception {
        try{
            Query query = entityManager.createQuery("INSERT INTO custom_subject (subject_name, subject_description) VALUES (:subjectName, :subjectDescription");
            query.setParameter("subjectName", addSubjectDto.getSubjectName());
            query.setParameter("subjectDescription", addSubjectDto.getSubjectDescription());

            int affectedRow = query.executeUpdate();
            if(affectedRow <= 0){
                throw new IllegalArgumentException("ENTRY NOT ADDED IN THE DB");
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

}
