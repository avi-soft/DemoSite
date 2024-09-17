package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.entity.ServiceProviderInfra;
import com.community.api.entity.ServiceProviderLanguage;
import com.community.api.entity.Skill;
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
import java.util.Map;


@Service
public class ServiceProviderLanguageService {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ExceptionHandlingImplement exceptionHandling;
    @Autowired
    private ResponseService responseService;
    @Autowired
    private SharedUtilityService sharedUtilityService;
    @Autowired
    private SanitizerService sanitizerService;
    @Transactional
    public ResponseEntity<?> addLanguage(@RequestBody Map<String,Object> language) {
        try{
            if(!sharedUtilityService.validateInputMap(language).equals(SharedUtilityService.ValidationResult.SUCCESS))
            {
                return ResponseService.generateErrorResponse("Invalid Request Body",HttpStatus.UNPROCESSABLE_ENTITY);
            }
            language=sanitizerService.sanitizeInputMap(language);
            String languageName=(String)language.get("language_name");
            if(languageName==null||languageName.isEmpty())
                return responseService.generateErrorResponse("Error saving language : Language Name required", HttpStatus.BAD_REQUEST);
            ServiceProviderLanguage languageTobeSaved=new ServiceProviderLanguage();
            int id=(int)findCount();
            languageTobeSaved.setLanguage_id(++id);
            languageTobeSaved.setLanguage_name(languageName);
            entityManager.persist(languageTobeSaved);
            return responseService.generateSuccessResponse("Language added successfully",languageTobeSaved,HttpStatus.OK);
        }catch (Exception exception)
        {
            exceptionHandling.handleException(exception);
            return responseService.generateErrorResponse("Error saving language : " + exception.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    public long findCount() {
        String queryString = Constant.GET_LANGUAGES_COUNT;
        TypedQuery<Long> query = entityManager.createQuery(queryString, Long.class);
        return query.getSingleResult();
    }
    public List<ServiceProviderLanguage> findAllLanguageList() {
        TypedQuery<ServiceProviderLanguage> query = entityManager.createQuery(Constant.GET_ALL_LANGUAGES, ServiceProviderLanguage.class);
        return query.getResultList();
    }

}
