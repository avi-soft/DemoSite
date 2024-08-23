package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Privileges;
import com.community.api.entity.Role;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Service
public class PrivilegeService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;

    @Transactional
    public ResponseEntity<?> assignPrivilege(@RequestParam Integer privilege_id, @RequestParam Long id, @RequestParam Integer role_id) {
        try {
            if (privilege_id == null || id == null || role_id == null) {
                return new ResponseEntity<>("Empty details", HttpStatus.BAD_REQUEST);
            }
            Privileges privilege = entityManager.find(Privileges.class, privilege_id);
            if (privilege == null)
                return new ResponseEntity<>("Privilege not found", HttpStatus.NOT_FOUND);
            Role role = entityManager.find(Role.class, role_id);
            if (role == null)
                return new ResponseEntity<>("Specified role not found", HttpStatus.NOT_FOUND);
            if (role.getRole_name().equals("SERVICE_PROVIDER")) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, id);
                if (serviceProvider == null) {
                    return new ResponseEntity<>("Error assigning privilege : " + role.getRole_name().equals("SERVICE_PROVIDER") + " not found", HttpStatus.NOT_FOUND);
                }
                List<Privileges> spPrivileges = serviceProvider.getPrivileges();
                if (!spPrivileges.contains(privilege))
                    spPrivileges.add(privilege);
                else
                    return new ResponseEntity<>("Privilage already assigned", HttpStatus.UNAUTHORIZED);
                serviceProvider.setPrivileges(spPrivileges);
                entityManager.merge(serviceProvider);
                return new ResponseEntity<>(serviceProvider, HttpStatus.OK);
            } else
                return new ResponseEntity<>("No records found for given details", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error assigning privilege", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> removePrivilege(@RequestParam Integer privilege_id, @RequestParam Long id, @RequestParam Integer role_id) {
        try {
            if (privilege_id == null || id == null || role_id == null) {
                return new ResponseEntity<>("Empty details", HttpStatus.BAD_REQUEST);
            }
            Privileges privilege = entityManager.find(Privileges.class, privilege_id);
            if (privilege == null)
                return new ResponseEntity<>("Privilege not found", HttpStatus.NOT_FOUND);
            Role role = entityManager.find(Role.class, role_id);
            if (role == null)
                return new ResponseEntity<>("Specified role not found", HttpStatus.NOT_FOUND);
            if (role.getRole_name().equals("SERVICE_PROVIDER")) {
                ServiceProviderEntity serviceProvider = entityManager.find(ServiceProviderEntity.class, id);
                if (serviceProvider == null) {
                    return new ResponseEntity<>("Error removing privilege : " + role.getRole_name().equals("SERVICE_PROVIDER") + " not found", HttpStatus.NOT_FOUND);
                }
                List<Privileges> spPrivileges = serviceProvider.getPrivileges();
                if (spPrivileges.contains(privilege))
                    spPrivileges.remove(privilege);
                else
                    return new ResponseEntity<>("Privilege not assigned", HttpStatus.UNAUTHORIZED);
                serviceProvider.setPrivileges(spPrivileges);
                entityManager.merge(serviceProvider);
                return new ResponseEntity<>(serviceProvider, HttpStatus.OK);
            } else
                return new ResponseEntity<>("No records found for given details", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    public ResponseEntity<?> createPrivilege(Privileges privilege) {
        try {
            if (privilege.getPrivilege_name() == null || privilege.getDescription() == null)
                return new ResponseEntity<>("Incomplete details", HttpStatus.BAD_REQUEST);
            entityManager.persist(privilege);
            return new ResponseEntity<>(privilege, HttpStatus.OK);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error removing ", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Integer> getPrivilege(Long userId) {
        try {

            Query query = entityManager.createNativeQuery(Constant.serviceProviderRoles);
            query.setParameter("serviceProviderId", userId);

            return query.getResultList();

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return Collections.emptyList();
        }
    }
}
