package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.component.JwtUtil;
import com.community.api.dto.AddProductDto;
import com.community.api.dto.AddStreamDto;
import com.community.api.entity.CustomStream;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.Privileges;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Service
public class StreamService {

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
    public List<CustomStream> getAllStream() {
        try {
            List<CustomStream> streamList = entityManager.createQuery(Constant.GET_ALL_STREAM, CustomStream.class).getResultList();
            return streamList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomStream getStreamByStreamId(Long streamId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_STREAM_BY_STREAM_ID, CustomStream.class);
            query.setParameter("streamId", streamId);
            List<CustomStream> stream = query.getResultList();

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

    public Boolean validateAddStreamDto(AddStreamDto addStreamDto) throws Exception {
        try{
            if(addStreamDto.getStreamName() == null || addStreamDto.getStreamDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("STREAM NAME CANNOT BE NULL OR EMPTY");
            }
            if(addStreamDto.getStreamDescription() != null && addStreamDto.getStreamDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("STREAM DESCRIPTION CANNOT BE EMPTY");
            }
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    public void saveStream(AddStreamDto addStreamDto) throws Exception {
        try{
            Query query = entityManager.createQuery("INSERT INTO custom_stream (stream_name, stream_description) VALUES (:streamName, :streamDescription");
            query.setParameter("streamName", addStreamDto.getStreamName());
            query.setParameter("streamDescription", addStreamDto.getStreamDescription());

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
