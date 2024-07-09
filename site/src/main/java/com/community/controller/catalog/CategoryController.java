/*-
 * #%L
 * Community Demo Site
 * %%
 * Copyright (C) 2009 - 2023 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */

package com.community.controller.catalog;

import org.broadleafcommerce.core.catalog.domain.Category;
import org.broadleafcommerce.core.catalog.domain.CategoryImpl;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.web.controller.catalog.BroadleafCategoryController;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * This class works in combination with the CategoryHandlerMapping which finds a category based upon
 * the passed in URL.
 */
@Controller("blCategoryController")
public class CategoryController extends BroadleafCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = super.handleRequest(request, response);
        if (isAjaxRequest(request)) {
            modelAndView.setViewName(modelAndView.getViewName() + " :: ajax");
        }

        return modelAndView;
    }

    @PutMapping("/save")
    @ResponseBody
    public String createCategory (){

        Category category = null;
        category.setId(2001L);
        category.setName("Electronics");
        catalogService.saveCategory(category);
        return "hi";
    }

    @RequestMapping(value = "/categories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getProductById(final HttpServletRequest request, final HttpServletResponse response) throws Exception {

        List<Category> categories = catalogService.findAllCategories();
        return "All Categories Found";
    }

    @RequestMapping(value = "/category", method = RequestMethod.GET, params = {"id"},  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getCategoryById(final HttpServletRequest request, final HttpServletResponse response,
                                  @RequestParam("id") final Long categoryId) throws Exception {
        Category category = catalogService.findCategoryById(categoryId);
        return "category Found with ID";
    }

    @RequestMapping(value = "/category", method = RequestMethod.GET, params = {"name"},  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getCategoryByName(@RequestParam("name") final String categoryName) throws Exception {
        Category category = catalogService.findCategoryByName(categoryName);
        logger.info(category.getExternalId());
        logger.info(category.getName());
        logger.info(String.valueOf(category.getId()));
        return "category Found with Name";
    }

    @RequestMapping(value = "/category/subCategories", method = RequestMethod.GET, params = {"id"},  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getSubCategoriesByCategoryId(@RequestParam("id") final Long categoryId) throws Exception {
        Category category = catalogService.findCategoryById(categoryId);
        List<Category> subCategories = catalogService.findAllSubCategories(category);

        logger.info(category.getName());
        logger.info(String.valueOf(category.getId()));
        for(Category category1: subCategories){
            logger.info(category1.getName());
        }
        return "Sub Category Found with Category";
    }

    /*@RequestMapping(value = "/category/attributes", method = RequestMethod.GET, params = {"id"},  produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getCategoryByName(@RequestParam("id") final Long categoryId) throws Exception {
        Category category = catalogService.fin(categoryId);
        logger.info(category.getName());
        logger.info(String.valueOf(category.getId()));
        return "category Attributes Found";
    }*/
}
