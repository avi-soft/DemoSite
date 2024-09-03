package com.community.api.endpoint.avisoft.controller.cart;
import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.community.api.services.CartService;
import com.community.api.services.ResponseService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.*;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;

@RestController
@RequestMapping(value = "/cart-custom",
        produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE }
)
public class CartEndPoint extends BaseEndpoint {
    private CustomerService customerService;
    private OrderService orderService;
    private CatalogService catalogService;
    private ExceptionHandlingImplement exceptionHandling;
    private EntityManager entityManager;
    private OrderItemService orderItemService;
    private CartService cartService;
    private ResponseService responseService;

    // Setter-based injection
    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService=responseService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Autowired
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Autowired
    public void setOrderItemService(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    @Autowired
    public void setCartService(CartService cartService) {
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
    @RequestMapping(value = "add-to-cart", method = RequestMethod.POST)
    public ResponseEntity<?> addToCart(@RequestParam long customerId, @RequestParam long productId) {
        try {
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }

            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null) {
                cart = orderService.createNewCartForCustomer(customer);
            }

            Product product = catalogService.findProductById(productId);
            if (product == null) {
                return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
            }
            OrderItemRequest orderItemRequest = new OrderItemRequest();
            orderItemRequest.setProduct(product);
            orderItemRequest.setOrder(cart);
            orderItemRequest.setQuantity(1);
            orderItemRequest.setCategory(product.getCategory());
            orderItemRequest.setItemName(product.getName());
            Map<String,String>atrtributes=orderItemRequest.getItemAttributes();
            atrtributes.put("productId",product.getId().toString());
            orderItemRequest.setItemAttributes(atrtributes);
            OrderItem orderItem = orderItemService.createOrderItem(orderItemRequest);
            List<OrderItem> items = cart.getOrderItems();
            boolean flag=false;
            for(OrderItem existingOrderItem:items)
            {
                if(Long.parseLong(existingOrderItem.getOrderItemAttributes().get("productId").getValue())==productId) {
                    flag=true;
                    entityManager.remove(orderItem);
                    int quantity = existingOrderItem.getQuantity();
                    existingOrderItem.setQuantity(++quantity);
                    entityManager.merge(existingOrderItem);
                    break;
                }
            }
            if(!flag)
                items.add(orderItem);
            cart.setOrderItems(items);
            return responseService.generateSuccessResponse("Cart updated",orderItem.getOrderItemAttributes().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "number-of-items", method = RequestMethod.GET)
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
    @JsonBackReference
    @RequestMapping(value = "preview-cart", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItems(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            Double subTotal=0.0;
            Map<String, Object>responseMap=new HashMap<>();
            if (isAnyServiceNull()) {
                return new ResponseEntity<>("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return new ResponseEntity<>("customer does not exist", HttpStatus.NOT_FOUND);
            }
            Order cart = orderService.findOrderById(orderId);
            if(cart==null)
                return responseService.generateErrorResponse("Cart not found",HttpStatus.NOT_FOUND);
            List<Product>listOfProducts=new ArrayList<>();
            List<OrderItem>orderItemList=cart.getOrderItems();
            if(orderItemList!=null&&(!orderItemList.isEmpty()))
            {
                for(OrderItem orderItem:orderItemList) {
                  Long productId=Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
                    Product product = catalogService.findProductById(productId);
                    if (product != null) {
                        listOfProducts.add(product);
                        for(int quantity=orderItem.getQuantity();quantity>1;quantity--)
                            subTotal += product.getDefaultSku().getCost().doubleValue();
                    }
                }
                responseMap.put("products",listOfProducts);
                responseMap.put("sub_total",subTotal);
                return responseService.generateSuccessResponse("Cart items",responseMap,HttpStatus.OK);
            }
            else
                return responseService.generateErrorResponse("No items in cart",HttpStatus.NOT_FOUND);
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
