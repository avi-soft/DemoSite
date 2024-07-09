package com.community.controller.catalog;

import org.broadleafcommerce.common.config.service.SystemPropertiesService;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.web.catalog.ProductHandlerMapping;
import org.broadleafcommerce.core.web.controller.catalog.BroadleafProductController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class ProductCustomController  {

    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    @Resource(name = "blSystemPropertiesService")
    protected SystemPropertiesService systemPropertiesService;


    // Custom controller Created for get product
    @RequestMapping(value = "/getproduct-id", params = {"id"})
    @ResponseBody
    public Product getProductApi(@RequestParam("id") final Long productId) throws Exception {
        try {
            final Product product = catalogService.findProductById(productId);
            return product;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}