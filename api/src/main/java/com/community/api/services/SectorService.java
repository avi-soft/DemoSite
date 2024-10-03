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

@Service
public class SectorService {

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    protected ExceptionHandlingService exceptionHandlingService;

    public List<CustomSector> getAllSector() {
        try {
            List<CustomSector> sectorList = entityManager.createQuery(Constant.GET_ALL_SECTOR, CustomSector.class).getResultList();
            return sectorList;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return Collections.emptyList();
        }
    }

    public CustomSector getSectorBySectorId(Long sectorId) {
        try {

            Query query = entityManager.createQuery(Constant.GET_SECTOR_BY_SECTOR_ID, CustomSector.class);
            query.setParameter("sectorId", sectorId);
            List<CustomSector> sector = query.getResultList();

            if (!sector.isEmpty()) {
                return sector.get(0);
            } else {
                return null;
            }

        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return null;
        }
    }
    public Boolean validateAddSubjectDto(AddSectorDto addSectorDto) throws Exception {
        try{
            if(addSectorDto.getSectorName() == null || addSectorDto.getSectorDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("SECTOR NAME CANNOT BE NULL OR EMPTY");
            }
            if(addSectorDto.getSectorDescription() != null && addSectorDto.getSectorDescription().trim().isEmpty()) {
                throw new IllegalArgumentException("SECTOR DESCRIPTION CANNOT BE EMPTY");
            }
            return true;
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }

    public void saveSector(AddSectorDto addSectorDto) throws Exception {
        try{
            Query query = entityManager.createQuery("INSERT INTO custom_sector (sector_name, sector_description) VALUES (:sectorName, :sectorDescription");
            query.setParameter("subjectName", addSectorDto.getSectorName());
            query.setParameter("subjectDescription", addSectorDto.getSectorDescription());

            int affectedRow = query.executeUpdate();
            if(affectedRow <= 0){
                throw new IllegalArgumentException("ENTRY NOT ADDED IN THE DB");
            }
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            throw new Exception("SOME EXCEPTION OCCURRED: "+ exception.getMessage());
        }
    }
}
