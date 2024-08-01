package com.community.api.services.ServiceProvider;

import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Service
public class ServiceProviderServiceImpl implements ServiceProviderService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ServiceProviderEntity saveServiceProvider(ServiceProviderEntity serviceProviderEntity) {
        try {
            entityManager.persist(serviceProviderEntity);
            return serviceProviderEntity;
        } catch (Exception e) {
            throw new RuntimeException("Error saving service provider entity", e);
        }
    }

    @Override
    @Transactional
    public ServiceProviderEntity updateServiceProvider(Long userId, ServiceProviderEntity serviceProviderEntity) throws Exception {
        ServiceProviderEntity existingServiceProvider = entityManager.find(ServiceProviderEntity.class, userId);
        if (existingServiceProvider == null) {
            throw new Exception("ServiceProvider with ID " + userId + " not found");
        }

        // Copy properties from the input entity to the existing entity
        existingServiceProvider.setUsername(serviceProviderEntity.getUsername());
        existingServiceProvider.setFirstName(serviceProviderEntity.getFirstName());
        existingServiceProvider.setLastName(serviceProviderEntity.getLastName());
        existingServiceProvider.setFatherName(serviceProviderEntity.getFatherName());
        existingServiceProvider.setDateOfBirth(serviceProviderEntity.getDateOfBirth());
        existingServiceProvider.setAadhaarNumber(serviceProviderEntity.getAadhaarNumber());
        existingServiceProvider.setPanNumber(serviceProviderEntity.getPanNumber());
        existingServiceProvider.setPersonalPhoto(serviceProviderEntity.getPersonalPhoto());
        existingServiceProvider.setResidentialAddress(serviceProviderEntity.getResidentialAddress());
        existingServiceProvider.setState(serviceProviderEntity.getState());
        existingServiceProvider.setDistrict(serviceProviderEntity.getDistrict());
        existingServiceProvider.setCity(serviceProviderEntity.getCity());
        existingServiceProvider.setPinCode(serviceProviderEntity.getPinCode());
        existingServiceProvider.setPrimaryMobileNumber(serviceProviderEntity.getPrimaryMobileNumber());
        existingServiceProvider.setSecondaryMobileNumber(serviceProviderEntity.getSecondaryMobileNumber());
        existingServiceProvider.setWhatsappNumber(serviceProviderEntity.getWhatsappNumber());
        existingServiceProvider.setPrimaryEmail(serviceProviderEntity.getPrimaryEmail());
        existingServiceProvider.setSecondaryEmail(serviceProviderEntity.getSecondaryEmail());
        existingServiceProvider.setRunningBusinessUnit(serviceProviderEntity.getRunningBusinessUnit());
        existingServiceProvider.setBusinessName(serviceProviderEntity.getBusinessName());
        existingServiceProvider.setBusinessLocation(serviceProviderEntity.getBusinessLocation());
        existingServiceProvider.setBusinessEmail(serviceProviderEntity.getBusinessEmail());
        existingServiceProvider.setNumberOfEmployees(serviceProviderEntity.getNumberOfEmployees());
        existingServiceProvider.setBusinessPhoto(serviceProviderEntity.getBusinessPhoto());
        existingServiceProvider.setCFormAvailable(serviceProviderEntity.getCFormAvailable());
        existingServiceProvider.setRegistrationNumber(serviceProviderEntity.getRegistrationNumber());
        existingServiceProvider.setcFormPhoto(serviceProviderEntity.getcFormPhoto());
        existingServiceProvider.setEquipment(serviceProviderEntity.getEquipment());
        existingServiceProvider.setHasTechnicalKnowledge(serviceProviderEntity.getHasTechnicalKnowledge());
        existingServiceProvider.setWorkExperienceInMonths(serviceProviderEntity.getWorkExperienceInMonths());
        existingServiceProvider.setHighestQualification(serviceProviderEntity.getHighestQualification());
        existingServiceProvider.setSkills(serviceProviderEntity.getSkills());

        entityManager.merge(existingServiceProvider);
        return existingServiceProvider;
    }

    @Override
    public ServiceProviderEntity getServiceProviderById(Long userId) {
        return entityManager.find(ServiceProviderEntity.class, userId);
    }
}