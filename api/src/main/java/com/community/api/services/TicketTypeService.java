package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.CustomTicketType;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

@Service
public class TicketTypeService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomTicketType> getAllTicketType() {
        try {
            List<CustomTicketType> ticketTypeList = entityManager.createQuery(Constant.GET_ALL_TICKET_TYPE, CustomTicketType.class).getResultList();
            return ticketTypeList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
//            return null;
            return Collections.emptyList();
        }
    }

    public CustomTicketType getTicketTypeByTicketTypeId(Long ticketTypeId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_TICKET_TYPE_BY_TICKET_TYPE_ID, CustomTicketType.class);
            query.setParameter("ticketTypeId", ticketTypeId);
            List<CustomTicketType> ticketType = query.getResultList();

            if (!ticketType.isEmpty()) {
                return ticketType.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
}
