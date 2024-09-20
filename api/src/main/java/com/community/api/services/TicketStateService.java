package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomJobGroup;
import com.community.api.entity.CustomTicketState;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Service
public class TicketStateService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomTicketState> getAllTicketState() {
        try {
            List<CustomTicketState> ticketStateList = entityManager.createQuery(Constant.GET_ALL_TICKET_STATE, CustomTicketState.class).getResultList();
            return ticketStateList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
//            return null;
            return Collections.emptyList();
        }
    }

    public CustomTicketState getTicketStateByTicketId(Long ticketStateId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_TICKET_STATE_BY_TICKET_STATE_ID, CustomTicketState.class);
            query.setParameter("ticketStateId", ticketStateId);
            List<CustomTicketState> ticketState = query.getResultList();

            if (!ticketState.isEmpty()) {
                return ticketState.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
