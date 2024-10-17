package com.community.api.endpoint.avisoft.controller.Sector;

import com.community.api.component.Constant;
import com.community.api.dto.AddSectorDto;
import com.community.api.entity.CustomSector;
import com.community.api.services.ResponseService;
import com.community.api.services.SectorService;
import com.community.api.services.StaticDataService;
import com.community.api.services.exception.ExceptionHandlingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SectorController {

    private final ExceptionHandlingService exceptionHandlingService;
    private final SectorService sectorService;
    private final StaticDataService staticDataService;

    @Autowired
    public SectorController(ExceptionHandlingService exceptionHandlingService, SectorService sectorService, StaticDataService staticDataService) {
        this.exceptionHandlingService = exceptionHandlingService;
        this.sectorService = sectorService;
        this.staticDataService = staticDataService;
    }

    @PostMapping("/add-sector")
    public ResponseEntity<?> addSubject(@RequestBody AddSectorDto addSectorDto, @RequestHeader(value = "Authorization") String authHeader) {
        try{
            if(!staticDataService.validiateAuthorization(authHeader)) {
                return ResponseService.generateErrorResponse("NOT AUTHORIZED TO ADD A STREAM", HttpStatus.UNAUTHORIZED);
            }

            sectorService.validateAddSubjectDto(addSectorDto);
            sectorService.saveSector(addSectorDto);

            return ResponseService.generateSuccessResponse("SUCCESSFULLY ADDED", addSectorDto, HttpStatus.OK);
        }  catch (IllegalArgumentException e) {
            return ResponseService.generateErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-all-sector")
    public ResponseEntity<?> getAllSubject() {
        try {
            List<CustomSector> subjectList = sectorService.getAllSector();
            if (subjectList.isEmpty()) {
                return ResponseService.generateErrorResponse("NO SUBJECT FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("SUBJECTS FOUND", subjectList, HttpStatus.OK);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-sector-by-sector-id/{sectorId}")
    public ResponseEntity<?> getSubjectById(@PathVariable Long sectorId) {
        try {
            CustomSector sector = sectorService.getSectorBySectorId(sectorId);
            if (sector == null) {
                return ResponseService.generateErrorResponse("NO SECTOR FOUND", HttpStatus.NOT_FOUND);
            }
            return ResponseService.generateSuccessResponse("SECTORS FOUND", sector, HttpStatus.OK);
        } catch (NumberFormatException numberFormatException) {
            exceptionHandlingService.handleException(numberFormatException);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + numberFormatException.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception exception) {
            exceptionHandlingService.handleException(exception);
            return ResponseService.generateErrorResponse(Constant.SOME_EXCEPTION_OCCURRED + ": " + exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
