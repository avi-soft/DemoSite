package com.community.api.services;

import com.community.api.dto.DocumentDto;
import com.community.api.entity.CustomCustomer;
import com.community.api.utils.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<DocumentDto> getAllDocumentsWithData(Long customCustomerId)  {
        CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customCustomerId);
        if (customCustomer == null) {
            throw new EntityNotFoundException("Customer does not exist with id " + customCustomerId);
        }

        String jpql = "SELECT d FROM Document d WHERE d.custom_customer.id = :customerId";

        List<Document> documents = entityManager.createQuery(jpql, Document.class)
                .setParameter("customerId", customCustomerId)
                .getResultList();

        if (documents.isEmpty()) {
            throw new IllegalArgumentException("No documents found for customer with id " + customCustomerId);
        }

        return documents.stream()
                .map(d -> new DocumentDto(d.getDocument_id(), d.getDocument_type(), d.getData()))
                .collect(Collectors.toList());
    }
}