package com.community.api.services;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.entity.IndividualServiceProvider;
import com.community.api.entity.ProfessionalServiceProvider;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Map;

@Service
public class RankingService
{
    private EntityManager entityManager;
    public RankingService(EntityManager entityManager)
    {
        this.entityManager = entityManager;
    }
    @Transactional
    public void giveScoresToServiceProvider(Long serviceProviderId, Map<String, Integer> scoreMap)
    {
        ServiceProviderEntity serviceProviderEntity= entityManager.find(ServiceProviderEntity.class,serviceProviderId);
        if(serviceProviderEntity==null)
        {
            throw new IllegalArgumentException("The service provider with id "+serviceProviderId+" does not exist");
        }
        if(serviceProviderEntity.getType().equalsIgnoreCase("PROFESSIONAL"))
        {
            ProfessionalServiceProvider professionalServiceProviderToGiveScore= (ProfessionalServiceProvider) serviceProviderEntity;
            if (scoreMap.containsKey("business_unit_infra_score")) {
                professionalServiceProviderToGiveScore.setBusiness_unit_infra_score(scoreMap.get("business_unit_infra_score"));
            }
            if (scoreMap.containsKey("qualification_score")) {
                professionalServiceProviderToGiveScore.setQualification_score(scoreMap.get("qualification_score"));
            }
            if (scoreMap.containsKey("work_experience_score")) {
                professionalServiceProviderToGiveScore.setWork_experience_score(scoreMap.get("work_experience_score"));
            }
            if (scoreMap.containsKey("technical_expertise_score")) {
                professionalServiceProviderToGiveScore.setTechnical_expertise_score(scoreMap.get("technical_expertise_score"));
            }
            if (scoreMap.containsKey("staff_score")) {
                professionalServiceProviderToGiveScore.setStaff_score(scoreMap.get("staff_score"));
            }

            Integer totalScore = calculateProfessionalServiceProviderScore(scoreMap);
            totalScore= totalScore+professionalServiceProviderToGiveScore.getTotalSkillTestPoints();
            serviceProviderEntity.setTotalScore(totalScore);
            serviceProviderEntity.setRank(assignRankingForProfessional(totalScore));
        }
        else
        {
            IndividualServiceProvider individualServiceProviderToGiveScore= (IndividualServiceProvider) serviceProviderEntity;
            if (scoreMap.containsKey("business_unit_infra_score")) {
                individualServiceProviderToGiveScore.setBusiness_unit_infra_score(scoreMap.get("business_unit_infra_score"));
            }
            if (scoreMap.containsKey("qualification_score")) {
                individualServiceProviderToGiveScore.setQualification_score(scoreMap.get("qualification_score"));
            }
            if (scoreMap.containsKey("work_experience_score")) {
                individualServiceProviderToGiveScore.setWork_experience_score(scoreMap.get("work_experience_score"));
            }
            if (scoreMap.containsKey("technical_expertise_score")) {
                individualServiceProviderToGiveScore.setTechnical_expertise_score(scoreMap.get("technical_expertise_score"));
            }
            if (scoreMap.containsKey("staff_score")) {
                individualServiceProviderToGiveScore.setPart_time_or_full_time_score(scoreMap.get("part_time_or_full_time_score"));
            }

            Integer totalScore = calculateProfessionalServiceProviderScore(scoreMap);
            totalScore= totalScore+individualServiceProviderToGiveScore.getTotalSkillTestPoints();
            serviceProviderEntity.setTotalScore(totalScore);
            serviceProviderEntity.setRank(assignRankingForProfessional(totalScore));

        }
        // Persist the updated service provider entity
        entityManager.merge(serviceProviderEntity);

    }

    private Integer calculateProfessionalServiceProviderScore(Map<String, Integer> scoreMap) {

        Integer businessUnitScore = scoreMap.getOrDefault("business_unit_infra_score", 0);
        Integer workExperienceScore = scoreMap.getOrDefault("work_experience_score", 0);
        Integer qualificationScore = scoreMap.getOrDefault("qualification_score", 0);
        Integer technicalExpertiseScore = scoreMap.getOrDefault("technical_expertise_score", 0);
        Integer staffScore = scoreMap.getOrDefault("staff_score", 0);

        return businessUnitScore + workExperienceScore + qualificationScore + technicalExpertiseScore + staffScore ;
    }

    private String assignRankingForProfessional(Integer totalScore) {
        if (totalScore >= 75) {
            return "1a";
        } else if (totalScore >= 50) {
            return "1b";
        } else if (totalScore >= 25) {
            return "1c";
        } else {
            return "1d";
        }
    }
}
