package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.ServiceProviderRank;
import com.community.api.services.QualificationService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
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

    public  List<ServiceProviderRank> getAllRank() {
        TypedQuery<ServiceProviderRank> query = entityManager.createQuery(Constant.FIND_ALL_SERVICE_PROVIDER_TEST_RANK_QUERY, ServiceProviderRank.class);
        List<ServiceProviderRank> serviceProviderRankList = query.getResultList();
        return serviceProviderRankList;
    }

    @Transactional
    public void giveScoresToServiceProvider(Long serviceProviderId, Map<String, Integer> scoreMap) {
        ServiceProviderEntity serviceProviderEntity = entityManager.find(ServiceProviderEntity.class, serviceProviderId);
        if (serviceProviderEntity == null) {
            throw new IllegalArgumentException("The service provider with id " + serviceProviderId + " does not exist");
        }

        List<String> commonRequiredKeys = List.of(
                "businessUnitInfraScore",
                "qualificationScore",
                "workExperienceScore",
                "technicalExpertiseScore"
        );

        // Define specific required keys for PROFESSIONAL
        List<String> professionalRequiredKeys = List.of("staffScore");

        // Define specific required keys for INDIVIDUAL
        List<String> individualRequiredKeys = List.of("partTimeOrFullTimeScore");

        // Check that all common required keys are present
        for (String key : commonRequiredKeys) {
            if (!scoreMap.containsKey(key)) {
                throw new IllegalArgumentException("Missing required score: " + key);
            }
            if (scoreMap.get(key) < 0) {
                throw new IllegalArgumentException("Score " + key + " cannot be negative");
            }
        }

        if(scoreMap.get("businessUnitInfraScore")>20)
        {
            throw new IllegalArgumentException("Business Unit Infra Score cannot be more than 20 points");
        }
        if(scoreMap.get("qualificationScore")>10)
        {
            throw new IllegalArgumentException("Qualification Score cannot be more than 10 points");
        }
        if(scoreMap.get("workExperienceScore")>20)
        {
            throw new IllegalArgumentException("Work Experience Score cannot be more than 20 points");
        }
        if(scoreMap.get("technicalExpertiseScore")>10)
        {
            throw new IllegalArgumentException("Technical Expertise Score cannot be more than 10 points");
        }

            if (serviceProviderEntity.getType().equalsIgnoreCase("PROFESSIONAL")) {

            for (String key : professionalRequiredKeys) {
                if (!scoreMap.containsKey(key)) {
                    throw new IllegalArgumentException("Missing required score for Professional: " + key);
                }
                if (scoreMap.get(key) < 0) {
                    throw new IllegalArgumentException("Score " + key + " cannot be negative");
                }
                if(scoreMap.get(key) >10)
                {
                    throw new IllegalArgumentException("Staff Score cannot be more than 10 points");
                }
            }

            updateScores(serviceProviderEntity, scoreMap);
            if(scoreMap.containsKey("staffScore"))
            {
                serviceProviderEntity.setStaffScore(scoreMap.get("staffScore"));
            }

            // Calculate total score
            Integer totalScore = calculateProfessionalServiceProviderScore(scoreMap);
                if (serviceProviderEntity.getWrittenTestScore() != null) {
                    totalScore += serviceProviderEntity.getWrittenTestScore();
                }

                if(serviceProviderEntity.getImageUploadScore() != null)
                {
                    totalScore+=serviceProviderEntity.getImageUploadScore();
                }
            serviceProviderEntity.setTotalScore(totalScore);
            // Assign ranking
            ServiceProviderRank serviceProviderRank = assignRankingForProfessional(totalScore);
            if (serviceProviderRank == null) {
                throw new IllegalArgumentException("Service Provider Rank is not found for assigning a rank to the Professional ServiceProvider");
            }
            serviceProviderEntity.setRanking(serviceProviderRank);
        }
        else
        {
            for (String key : individualRequiredKeys) {
                if (!scoreMap.containsKey(key)) {
                    throw new IllegalArgumentException("Missing required score for Individual: " + key);
                }
                if (scoreMap.get(key) < 0) {
                    throw new IllegalArgumentException("Score " + key + " cannot be negative");
                }
                if(scoreMap.get(key)>10)
                {
                    throw new IllegalArgumentException("Part Time or Full Time Score cannot be more than 10 points");
                }
            }

            updateScores(serviceProviderEntity,scoreMap);
            if (scoreMap.containsKey("partTimeOrFullTimeScore")) {
                serviceProviderEntity.setPartTimeOrFullTimeScore(scoreMap.get("partTimeOrFullTimeScore"));
            }

            Integer totalScore = calculateIndividualServiceProviderScore(scoreMap);
            if (serviceProviderEntity.getWrittenTestScore() != null) {
                totalScore += serviceProviderEntity.getWrittenTestScore();
            }

            if(serviceProviderEntity.getImageUploadScore() != null)
            {
                totalScore+=serviceProviderEntity.getImageUploadScore();
            }
            serviceProviderEntity.setTotalScore(totalScore);
            ServiceProviderRank serviceProviderRank= assignRankingForIndividual(totalScore);
            if(serviceProviderRank==null)
            {
                throw new IllegalArgumentException("Service Provider Rank is not found for assigning a rank to the Individual ServiceProvider");
            }
            serviceProviderEntity.setRanking(serviceProviderRank);
        }
        entityManager.merge(serviceProviderEntity);
    }

    private void updateScores(ServiceProviderEntity serviceProvider, Map<String, Integer> scoreMap) {
        if (scoreMap.containsKey("businessUnitInfraScore")) {
            serviceProvider.setBusinessUnitInfraScore(scoreMap.get("businessUnitInfraScore"));
        }
        if (scoreMap.containsKey("qualificationScore")) {
            serviceProvider.setQualificationScore(scoreMap.get("qualificationScore"));
        }
        if (scoreMap.containsKey("workExperienceScore")) {
            serviceProvider.setWorkExperienceScore(scoreMap.get("workExperienceScore"));
        }
        if (scoreMap.containsKey("technicalExpertiseScore")) {
            serviceProvider.setTechnicalExpertiseScore(scoreMap.get("technicalExpertiseScore"));
        }
    }

    private Integer calculateProfessionalServiceProviderScore(Map<String, Integer> scoreMap) {

        Integer businessUnitScore = scoreMap.getOrDefault("businessUnitInfraScore", 0);
        Integer workExperienceScore = scoreMap.getOrDefault("workExperienceScore", 0);
        Integer qualificationScore = scoreMap.getOrDefault("qualificationScore", 0);
        Integer technicalExpertiseScore = scoreMap.getOrDefault("technicalExpertiseScore", 0);
        Integer staffScore = scoreMap.getOrDefault("staffScore", 0);

        return businessUnitScore + workExperienceScore + qualificationScore + technicalExpertiseScore + staffScore ;
    }
    private Integer calculateIndividualServiceProviderScore(Map<String, Integer> scoreMap) {

        Integer businessUnitScore = scoreMap.getOrDefault("businessUnitInfraScore", 0);
        Integer workExperienceScore = scoreMap.getOrDefault("workExperienceScore", 0);
        Integer qualificationScore = scoreMap.getOrDefault("qualificationScore", 0);
        Integer technicalExpertiseScore = scoreMap.getOrDefault("technicalExpertiseScore", 0);
        Integer partTimeOrFullTimeScore = scoreMap.getOrDefault("partTimeOrFullTimeScore", 0);

        return businessUnitScore + workExperienceScore + qualificationScore + technicalExpertiseScore + partTimeOrFullTimeScore ;
    }

    private ServiceProviderRank assignRankingForProfessional(Integer totalScore) {
        List<ServiceProviderRank> professionalServiceProviderRanks= getAllRank();

        if (totalScore >= 75) {
            return searchServiceProviderRank(professionalServiceProviderRanks,"1a");
        } else if (totalScore >= 50) {
            return searchServiceProviderRank(professionalServiceProviderRanks,"1b");
        } else if (totalScore >= 25) {
            return searchServiceProviderRank(professionalServiceProviderRanks,"1c");
        } else {
            return searchServiceProviderRank(professionalServiceProviderRanks,"1d");
        }
    }
    private ServiceProviderRank assignRankingForIndividual(Integer totalScore) {
        List<ServiceProviderRank> professionalServiceProviderRanks= getAllRank();

        if (totalScore >= 75) {
            return searchServiceProviderRank(professionalServiceProviderRanks,"2a");
        } else if (totalScore >= 50) {
            return searchServiceProviderRank(professionalServiceProviderRanks,"2b");
        } else if (totalScore >= 25) {
            return searchServiceProviderRank(professionalServiceProviderRanks,"2c");
        } else {
            return searchServiceProviderRank(professionalServiceProviderRanks,"2d");
        }
    }

    private ServiceProviderRank searchServiceProviderRank(List<ServiceProviderRank> serviceProviderRankList,String rankValue)
    {
        for(ServiceProviderRank serviceProviderRank:serviceProviderRankList)
        {
            if(serviceProviderRank.getRank_name().equalsIgnoreCase(rankValue))
            {
                return serviceProviderRank;
            }
        }
        return null;
    }

}

