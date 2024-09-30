package com.community.api.endpoint.avisoft.controller.ServiceProvider;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.services.ResponseService;
import com.community.api.services.ServiceProvider.ServiceProviderRankService;
import com.community.api.services.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static com.community.api.component.Constant.FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY;

@RestController
@RequestMapping("/service-provider-test-rank")
public class ServiceProviderRankController {

    private EntityManager entityManager;
    private ResponseService responseService;
    protected ExceptionHandlingImplement exceptionHandling;
    private ServiceProviderRankService serviceProviderRankService;


    public ServiceProviderRankController(EntityManager entityManager, ResponseService responseService, ExceptionHandlingImplement exceptionHandling, ServiceProviderRankService serviceProviderRankService) {
        this.responseService = responseService;
        this.entityManager = entityManager;
        this.exceptionHandling = exceptionHandling;
        this.serviceProviderRankService = serviceProviderRankService;
    }


    @GetMapping("/get-all-service-provider-test-rank")

    public ResponseEntity<?> getAllServiceProviderRank() {
        TypedQuery<ServiceProviderRank> query = entityManager.createQuery(FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY, ServiceProviderRank.class);
        List<ServiceProviderRank> serviceProviderTestRankList = query.getResultList();
        if (serviceProviderTestRankList.isEmpty()) {
            return responseService.generateResponse(HttpStatus.OK, "Service Provider Test Rank List is Empty", serviceProviderTestRankList);
        }
        return responseService.generateResponse(HttpStatus.OK, "Service Provider Test Rank List Retrieved Successfully", serviceProviderTestRankList);
    }
}
