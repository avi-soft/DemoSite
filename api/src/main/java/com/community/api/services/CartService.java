package com.community.api.services;

import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.util.Iterator;
import java.util.List;

@Service
public class CartService {

private EntityManager entityManager;

   public  CartService(EntityManager entityManager)
   {
       this.entityManager= entityManager;
   }
    public boolean removeItemFromCart(Order cart, Long orderItemId) {
        List<OrderItem> items = cart.getOrderItems();
        Iterator<OrderItem> iterator = items.iterator();

        while (iterator.hasNext()) {
            OrderItem item = iterator.next();
            if (item.getId().equals(orderItemId)) {
                iterator.remove();
                entityManager.remove(item);
                entityManager.merge(cart);
                return true;
            }
        }
        return false;
    }
}
