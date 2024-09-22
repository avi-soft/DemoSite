package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.dto.UpdateTestStatusRank;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.Qualification;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;

@Service
public class ServiceProviderTestRankService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private QualificationService qualificationService;
    @Autowired
    private ResponseService responseService;

    public List<ServiceProviderRank> getAllTestStatus() {
        TypedQuery<ServiceProviderRank> query = entityManager.createQuery(Constant.FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY, ServiceProviderRank.class);
        List<ServiceProviderRank> serviceProviderTestRankList = query.getResultList();
        return serviceProviderTestRankList;
    }
}
