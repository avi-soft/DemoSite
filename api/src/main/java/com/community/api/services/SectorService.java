package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.dto.AddSectorDto;
import com.community.api.entity.CustomSector;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collections;
import java.util.List;

import static com.community.api.component.Constant.GET_ALL_SECTOR;
import static com.community.api.component.Constant.GET_SECTOR_BY_SECTOR_ID;

@Service
public class SectorService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomSector> getAllSector() {
        try {
            List<CustomSector> sectorList = entityManager.createQuery(GET_ALL_SECTOR, CustomSector.class).getResultList();
            return sectorList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomSector getSectorBySectorId(Long sectorId) throws Exception {
        try {

            Query query = entityManager.createQuery(GET_SECTOR_BY_SECTOR_ID, CustomSector.class);
            query.setParameter("sectorId", sectorId);
            List<CustomSector> sector = query.getResultList();

            if (!sector.isEmpty()) {
                return sector.get(0);
            } else {
                throw new IllegalArgumentException("No sector found with this id.");
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while fetching sector: " + exception.getMessage());
        }
    }
    public Boolean validateAddSubjectDto(AddSectorDto addSectorDto) throws Exception {
        try{
            if(addSectorDto.getSectorName() == null || addSectorDto.getSectorName().trim().isEmpty()) {
                throw new IllegalArgumentException("Sector name cannot be null or empty.");
            }
            if(addSectorDto.getSectorDescription() != null && addSectorDto.getSectorDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("Sector description cannot be empty.");
            }
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while fetching sector: "+ exception.getMessage());
        }
    }

    public void saveSector(AddSectorDto addSectorDto) throws Exception {
        try{
            Query query = entityManager.createQuery("INSERT INTO custom_sector (sector_name, sector_description) VALUES (:sectorName, :sectorDescription");
            query.setParameter("subjectName", addSectorDto.getSectorName());
            query.setParameter("subjectDescription", addSectorDto.getSectorDescription());

            int affectedRow = query.executeUpdate();
            if(affectedRow <= 0){
                throw new IllegalArgumentException("Entry not added.");
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("Exception caught while fetching sector: "+ exception.getMessage());
        }
    }
}
