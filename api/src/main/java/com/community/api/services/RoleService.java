package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.Role;
import com.community.api.entity.Skill;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RoleService {
    private EntityManager entityManager;
    private SharedUtilityService sharedUtilityService;
    private ResponseService responseService;
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setSharedUtilityService(SharedUtilityService sharedUtilityService) {
        this.sharedUtilityService = sharedUtilityService;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    public String findRoleName(int role_id) {
        String response= entityManager.createQuery(Constant.FETCH_ROLE, String.class)
                .setParameter("role_id", role_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if(response==null)
            return "EMPTY";
        else return response;
    }
    @Transactional
    public ResponseEntity<?> addRole(Role role)
    {
        try{
            if(role.getRole_name()==null)
                return responseService.generateErrorResponse("Role name cannot be Empty", HttpStatus.BAD_REQUEST);
            int count=(int) sharedUtilityService.findCount(Constant.GET_COUNT_OF_ROLES);
            role.setRole_id(++count);
            role.setCreated_at(sharedUtilityService.getCurrentTimestamp());
            role.setUpdated_at(sharedUtilityService.getCurrentTimestamp());
            role.setCreated_by("SUPER_ADMIN");//@TODO- get role id from token and check role name fromm it
            entityManager.persist(role);
            return responseService.generateSuccessResponse("role added successfully",role,HttpStatus.OK);
        }catch (Exception e)
        {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error saving role : " + e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public List<Role> findAllRoleList() {
        TypedQuery<Role> query = entityManager.createQuery(Constant.GET_ALL_ROLES, Role.class);
        return query.getResultList();
    }

        public Role getRoleByRoleId(int roleId) {
        return entityManager.createQuery(Constant.GET_ROLE_BY_ROLE_ID, Role.class)
                .setParameter("roleId", roleId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

}
