package com.community.api.services;

import com.community.api.component.Constant;
import com.community.api.endpoint.serviceProvider.ServiceProviderEntity;
import com.community.api.services.ServiceProvider.ServiceProviderServiceImpl;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

@Service
public class CustomOrderService {
    private OrderService orderService;
    private EntityManager entityManager;
    private ServiceProviderServiceImpl serviceProviderService;
    @Autowired
    public void setOrderService(OrderService orderService)
    {
        this.orderService=orderService;
    }
    @Autowired
    public void setEntityManager(EntityManager entityManager)
    {
        this.entityManager=entityManager;
    }
    @Autowired
    public void setServiceProviderService(ServiceProviderServiceImpl serviceProviderService)
    {
        this.serviceProviderService=serviceProviderService;
    }
    @Transactional
    public List<ServiceProviderEntity> availableSp(@PathVariable Long orderId, int page, int limit)
    {
        Order order=orderService.findOrderById(orderId);
        Query query=entityManager.createNativeQuery(Constant.NOT_ELIGIBLE_SP);
        List<ServiceProviderEntity>allSp=(serviceProviderService.getAllSp(page,limit));
        query.setParameter("orderId",orderId);
        List<BigInteger>nonEligibleSp=query.getResultList();
        for(BigInteger id:nonEligibleSp)
        {
            ServiceProviderEntity serviceProvider=entityManager.find(ServiceProviderEntity.class,id.longValue());
            if(serviceProvider!=null)
                allSp.remove(serviceProvider);
        }
        return allSp;
    }
}
