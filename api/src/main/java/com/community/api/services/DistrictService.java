package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.Districts;
import com.community.api.entity.StateCode;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

@Service
public class DistrictService {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    public List<Districts> findDistrictsByStateCode(String state_code) {
            TypedQuery<Districts> query = entityManager.createQuery(Constant.DISTRICT_QUERY, Districts.class);
        query.setParameter("state_code",state_code);
        return query.getResultList();
    }
    public String findDistrictById(int district_id) {
        return entityManager.createQuery(Constant.FIND_DISTRICT, String.class)
                .setParameter("district_id", district_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
        public String findStateById(int state_id) {

        return entityManager.createQuery(Constant.FIND_STATE, String.class)
                .setParameter("state_id", state_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
    public List<StateCode> findStateList() {
        TypedQuery<StateCode> query = entityManager.createQuery(Constant.GET_STATES_LIST, StateCode.class);
        return query.getResultList();
    }
    public StateCode getStateByStateId(int stateId) throws Exception {
        try{
            Query query = entityManager.createQuery(Constant.GET_STATE_BY_STATE_ID, StateCode.class);
            query.setParameter("stateId", stateId);

            List<StateCode> stateCode = query.getResultList();
            return stateCode.get(0);
        } catch (NumberFormatException numberFormatException) {
            throw new NumberFormatException("NUMBER FORMAT EXCEPTION: " + numberFormatException.getMessage());
        } catch (Exception exception) {
            exceptionHandling.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURED: "+ exception.getMessage());
        }
    }
}
