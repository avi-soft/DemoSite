package com.community.api.endpoint.avisoft.controller.cart;
import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.community.api.services.CartService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.OrderItemService;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.OrderItemRequest;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/cart-custom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class CartEndPoint extends BaseEndpoint {
    private final CustomerService customerService;
    private final OrderService orderService;
    private final CatalogService catalogService;
    private final ExceptionHandlingImplement exceptionHandling;
    private final EntityManager entityManager;
    private final OrderItemService orderItemService;
    private final CartService cartService;
    @Autowired
    public CartEndPoint(CustomerService customerService, OrderService orderService, CatalogService catalogService,
                        ExceptionHandlingImplement exceptionHandling, EntityManager entityManager,
                        OrderItemService orderItemService, CartService cartService) {
        this.customerService = customerService;
        this.orderService = orderService;
        this.catalogService = catalogService;
        this.exceptionHandling = exceptionHandling;
        this.entityManager = entityManager;
        this.orderItemService = orderItemService;
        this.cartService = cartService;
    }

    @RequestMapping(value = "empty", method = RequestMethod.DELETE)
    public ResponseEntity<String> emptyTheCart(@RequestParam Long customerId) {
        try {
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
            Order cart = null;
            if (customer == null) {
                return new ResponseEntity<>("Customer not found for this Id", HttpStatus.NOT_FOUND);
            } else {
                cart = this.orderService.findCartForCustomer(customer);
                if (cart == null) {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
                if (cart.getStatus().equals(OrderStatus.IN_PROCESS)) {//ensuring its cart and not an order
                    orderService.deleteOrder(cart);
                    return new ResponseEntity<>("Order Deleted", HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.OK);

            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error deleting!", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "addToCart", method = RequestMethod.POST)
    public ResponseEntity<String> addToCart(@RequestParam long customerId, @RequestParam long productId) {
        try {
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
            Order cart = null;
            if (customer == null) {
                return new ResponseEntity<>("Customer not found for this Id", HttpStatus.NOT_FOUND);
            } else {
                cart = this.orderService.findCartForCustomer(customer);
                if (cart == null) {
                    cart = orderService.createNewCartForCustomer(customer);
                }
                Product product = catalogService.findProductById(productId);
                if (product == null)
                    return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
                OrderItemRequest orderItemRequest = new OrderItemRequest();
                orderItemRequest.setProduct(product);
                OrderItem orderItem = orderItemService.createOrderItem(orderItemRequest);
                orderItem.setName(product.getName());
                List<OrderItem> items = new ArrayList<>();
                orderItem.setOrder(cart);
                orderItem.setSalePrice(product.getPrice());
                cart.setCustomer(customer);
                cart.setOrderItems(items);
                items.add(orderItem);
                entityManager.persist(cart);
                return new ResponseEntity<>("Cart updated", HttpStatus.OK);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error adding item to cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value = "numberOfItems", method = RequestMethod.GET)
        public ResponseEntity<?> retrieveCartItemsCount(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer != null) {
                if (orderService.findOrderById(orderId) != null) {
                    return new ResponseEntity<>(orderService.findOrderById(orderId).getOrderItems().size(), HttpStatus.OK);
                } else
                    return new ResponseEntity<>("No items found", HttpStatus.NOT_FOUND);
            } else
                return new ResponseEntity<>("Customer not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @JsonBackReference
    @RequestMapping(value = "preview-cart", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItems(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null)
            {
                return new ResponseEntity<>("customer does not exist",HttpStatus.NOT_FOUND);
            }
            Order cart=orderService.findOrderById(orderId);
            List<OrderItem>orderItemList=cart.getOrderItems();
            List<OrderItem>neworderItemList=new ArrayList<>();
            if (cart!=null) {
                for(OrderItem orderItem:cart.getOrderItems())
                {
                    OrderItem current=orderItem;
                    current.setOrder(null);
                    neworderItemList.add(current);
                }
                orderItemList=neworderItemList;
                cart.setOrderItems(orderItemList);
                   return new ResponseEntity<>(cart.getOrderItems().toArray(),HttpStatus.OK);
            } else
                return new ResponseEntity<>("No items found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "remove-item", method = RequestMethod.DELETE)
    public ResponseEntity<String> removeCartItems(
            @RequestParam long customerId,
            @RequestParam Long orderId,
            @RequestParam Long orderItemId) {
        try {
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("Invalid request: Customer does not exist", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Order cart = orderService.findOrderById(orderId);
            if (cart == null || cart.getOrderItems() == null) {
                return new ResponseEntity<>("Cart Empty", HttpStatus.NOT_FOUND);
            }

            boolean itemRemoved = cartService.removeItemFromCart(cart, orderItemId);
            if (itemRemoved) {
                return new ResponseEntity<>("Item Removed", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Error removing item from cart: item not present in cart", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return new ResponseEntity<>("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private boolean isAnyServiceNull() {
        return customerService == null || orderService == null|| catalogService == null;
    }

}
