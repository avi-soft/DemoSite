package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomTicketState;
import com.community.api.entity.CustomTicketStatus;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Service
public class TicketStatusService {
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomTicketStatus> getAllTicketStatus() {
        try {
            List<CustomTicketStatus> ticketStatusList = entityManager.createQuery(Constant.GET_ALL_TICKET_STATUS, CustomTicketStatus.class).getResultList();
            return ticketStatusList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
//            return null;
            return Collections.emptyList();
        }
    }

    public CustomTicketStatus getTicketStatusByTicketStatusId(Long ticketStatusId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_TICKET_STATE_BY_TICKET_STATUS_ID, CustomTicketStatus.class);
            query.setParameter("ticketStatusId", ticketStatusId);
            List<CustomTicketStatus> ticketState = query.getResultList();

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
