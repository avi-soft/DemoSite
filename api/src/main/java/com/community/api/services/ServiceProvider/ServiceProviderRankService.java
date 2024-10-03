package com.community.api.services.ServiceProvider;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.IndividualServiceProvider;
import com.community.api.entity.ProfessionalServiceProvider;
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

    public List<ServiceProviderRank> getAllRank() {
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

        if (serviceProviderEntity instanceof ProfessionalServiceProvider) {
            ProfessionalServiceProvider professionalServiceProvider = (ProfessionalServiceProvider) serviceProviderEntity;

            // Update scores
            updateScores(professionalServiceProvider, scoreMap);

            // Calculate total score
            Integer totalScore = calculateProfessionalServiceProviderScore(scoreMap);
            if (professionalServiceProvider.getTotalSkillTestPoints() != null) {
                totalScore += professionalServiceProvider.getTotalSkillTestPoints();
            }
            professionalServiceProvider.setTotalScore(totalScore);

            // Assign ranking
            ServiceProviderRank serviceProviderRank = assignRankingForProfessional(totalScore);
            if (serviceProviderRank == null) {
                throw new IllegalArgumentException("Service Provider Rank is not found for assigning a rank to the Professional ServiceProvider");
            }
            professionalServiceProvider.setRanking(serviceProviderRank);

            // Let JPA handle the versioning
            entityManager.flush();
        }
//        else
//        {
//            IndividualServiceProvider individualServiceProviderToGiveScore= (IndividualServiceProvider) serviceProviderEntity;
//            if (scoreMap.containsKey("business_unit_infra_score")) {
//                individualServiceProviderToGiveScore.setBusiness_unit_infra_score(scoreMap.get("business_unit_infra_score"));
//            }
//            if (scoreMap.containsKey("qualification_score")) {
//                individualServiceProviderToGiveScore.setQualification_score(scoreMap.get("qualification_score"));
//            }
//            if (scoreMap.containsKey("work_experience_score")) {
//                individualServiceProviderToGiveScore.setWork_experience_score(scoreMap.get("work_experience_score"));
//            }
//            if (scoreMap.containsKey("technical_expertise_score")) {
//                individualServiceProviderToGiveScore.setTechnical_expertise_score(scoreMap.get("technical_expertise_score"));
//            }
//            if (scoreMap.containsKey("staff_score")) {
//                individualServiceProviderToGiveScore.setPart_time_or_full_time_score(scoreMap.get("part_time_or_full_time_score"));
//            }
//
//            entityManager.merge(individualServiceProviderToGiveScore);
//
//            Integer totalScore = calculateProfessionalServiceProviderScore(scoreMap);
//            totalScore= totalScore+individualServiceProviderToGiveScore.getTotalSkillTestPoints();
//            serviceProviderEntity.setTotalScore(totalScore);
//            ServiceProviderRank serviceProviderRank= assignRankingForIndividual(totalScore);
//            if(serviceProviderRank==null)
//            {
//                throw new IllegalArgumentException("Service Provider Rank is not found for assigning a rank to the Individual ServiceProvider");
//            }
//            serviceProviderEntity.setRanking(serviceProviderRank);
//
//        }
        // Persist the updated service provider entity
//        entityManager.merge(serviceProviderEntity);
//          entityManager.persist();
    }

    private void updateScores(ProfessionalServiceProvider provider, Map<String, Integer> scoreMap) {
        if (scoreMap.containsKey("business_unit_infra_score")) {
            provider.setBusiness_unit_infra_score(scoreMap.get("business_unit_infra_score"));
        }
        if (scoreMap.containsKey("qualification_score")) {
            provider.setQualification_score(scoreMap.get("qualification_score"));
        }
        if (scoreMap.containsKey("work_experience_score")) {
            provider.setWork_experience_score(scoreMap.get("work_experience_score"));
        }
        if (scoreMap.containsKey("technical_expertise_score")) {
            provider.setTechnical_expertise_score(scoreMap.get("technical_expertise_score"));
        }
        if (scoreMap.containsKey("staff_score")) {
            provider.setStaff_score(scoreMap.get("staff_score"));
        }
    }

    private Integer calculateProfessionalServiceProviderScore(Map<String, Integer> scoreMap) {

        Integer businessUnitScore = scoreMap.getOrDefault("business_unit_infra_score", 0);
        Integer workExperienceScore = scoreMap.getOrDefault("work_experience_score", 0);
        Integer qualificationScore = scoreMap.getOrDefault("qualification_score", 0);
        Integer technicalExpertiseScore = scoreMap.getOrDefault("technical_expertise_score", 0);
        Integer staffScore = scoreMap.getOrDefault("staff_score", 0);

        return businessUnitScore + workExperienceScore + qualificationScore + technicalExpertiseScore + staffScore ;
    }
//
//    private ServiceProviderRank assignRankingForProfessional(Integer totalScore) {
//        List<ServiceProviderRank> professionalServiceProviderRanks= getAllRank();
//
//        if (totalScore >= 75) {
//            return searchServiceProviderRank(professionalServiceProviderRanks,"1a");
//        } else if (totalScore >= 50) {
//            return searchServiceProviderRank(professionalServiceProviderRanks,"1b");
//        } else if (totalScore >= 25) {
//            return searchServiceProviderRank(professionalServiceProviderRanks,"1c");
//        } else {
//            return searchServiceProviderRank(professionalServiceProviderRanks,"1d");
//        }
//    }
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

