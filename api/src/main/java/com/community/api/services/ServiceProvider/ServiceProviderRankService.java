package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ServiceProviderRankService {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private QualificationService qualificationService;
    @Autowired
    private ResponseService responseService;

    public  List<ServiceProviderRank> getAllRank()
    {
        try
        {
            TypedQuery<ServiceProviderRank> query = entityManager.createQuery(Constant.FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY, ServiceProviderRank.class);
            List<ServiceProviderRank> serviceProviderRankList = query.getResultList();
            return serviceProviderRankList;
        }
        catch (Exception e) {
            exceptionHandling.handleException(e);
        }
        return null;
    }

    @Transactional
    public Map<String, Integer>  getScoreCard(Long serviceProviderId)
    {

        if(!(serviceProviderId instanceof Long))
        {
            throw new IllegalArgumentException("Service Provider Id must be a Long");
        }
        ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProviderEntity == null) {
            throw new IllegalArgumentException("The service provider with id " + serviceProviderId + " does not exist");
        }
        Map<String,Integer> scoreCard = new HashMap<>();
        scoreCard.put("qualificationScore",serviceProviderEntity.getQualificationScore());
        scoreCard.put("workExperienceScore",serviceProviderEntity.getWorkExperienceScore());
        scoreCard.put("technicalExpertiseScore",serviceProviderEntity.getTechnicalExpertiseScore());
        scoreCard.put("writtenTestScore",serviceProviderEntity.getWrittenTestScore());
        scoreCard.put("imageUploadScore",serviceProviderEntity.getImageUploadScore());
        if(serviceProviderEntity.getType().equalsIgnoreCase("PROFESSIONAL"))
        {
            scoreCard.put("businessUnitScore",serviceProviderEntity.getBusinessUnitInfraScore());
            scoreCard.put("staffScore",serviceProviderEntity.getStaffScore());
        }
        else {
            scoreCard.put("InfraScore",serviceProviderEntity.getInfraScore());
            scoreCard.put("partTimeOrFullTimeScore",serviceProviderEntity.getStaffScore());
        }
        scoreCard.put("totalScore",serviceProviderEntity.getTotalScore());
        return scoreCard;
    }
}

