package com.community.api.endpoint.avisoft.controller.cart;
import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.community.api.component.Constant;
import com.community.api.services.CartService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/cart",
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
    private SharedUtilityService sharedUtilityService;

    // Setter-based injection
    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setSharedUtilityService(SharedUtilityService sharedUtilityService) {
        this.sharedUtilityService=sharedUtilityService;
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
    public ResponseEntity<?> emptyTheCart(@RequestParam Long customer_id) {
        try {
            if (isAnyServiceNull()) {
                return responseService.generateErrorResponse("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customer_id);//finding the customer to get cart associated with it
            Order cart = null;
            if (customer == null) {
                return responseService.generateErrorResponse("Customer not found for this Id",HttpStatus.NOT_FOUND);
            } else {
                cart = this.orderService.findCartForCustomer(customer);
                if (cart == null) {
                    return responseService.generateErrorResponse("Cart Not Found",HttpStatus.NOT_FOUND);
                }
                if (cart.getStatus().equals(OrderStatus.IN_PROCESS)) {//ensuring its cart and not an order
                    orderService.deleteOrder(cart);
                    return responseService.generateSuccessResponse("Cart is empty now",null,HttpStatus.OK);
                }
                else
                    return responseService.generateErrorResponse("Error removing all items from cart",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error removing all items from cart : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "add-to-cart", method = RequestMethod.POST)
    public ResponseEntity<?> addToCart(@RequestParam long customerId, @RequestParam long productId) {
        try {
            if (isAnyServiceNull()) {
                return responseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null) {
                cart = orderService.createNewCartForCustomer(customer);
            }
            Product product = catalogService.findProductById(productId);
            if (product == null) {
                return responseService.generateErrorResponse("Product not found", HttpStatus.NOT_FOUND);
            }
            OrderItemRequest orderItemRequest = new OrderItemRequest();
            orderItemRequest.setProduct(product);
            orderItemRequest.setOrder(cart);
            orderItemRequest.setQuantity(1);
            orderItemRequest.setCategory(product.getCategory());
            orderItemRequest.setItemName(product.getName());
            Map<String,String> atrtributes=orderItemRequest.getItemAttributes();
            atrtributes.put("productId",product.getId().toString());
            orderItemRequest.setItemAttributes(atrtributes);
            OrderItem orderItem = orderItemService.createOrderItem(orderItemRequest);
            List<OrderItem> items = cart.getOrderItems();
            boolean flag=false;
            for(OrderItem existingOrderItem:items)
            {
                if(Long.parseLong(existingOrderItem.getOrderItemAttributes().get("productId").getValue())==productId) {
                    flag=true;
                    return responseService.generateErrorResponse(Constant.CANNOT_ADD_MORE_THAN_ONE_FORM,HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
            if(!flag)
                items.add(orderItem);
            cart.setOrderItems(items);
            return responseService.generateSuccessResponse("Cart updated",orderItem.getOrderItemAttributes().toString(), HttpStatus.OK);
        } catch (Exception e) {
            return responseService.generateErrorResponse("Error adding item to cart : "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "number-of-items", method = RequestMethod.GET)
        public ResponseEntity<?> retrieveCartItemsCount(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            if (isAnyServiceNull()) {
                return responseService.generateErrorResponse("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer != null) {
                if (orderService.findOrderById(orderId) != null) {
                    return responseService.generateSuccessResponse("Items in cart :",orderService.findOrderById(orderId).getOrderItems().size(), HttpStatus.OK);
                } else
                    return responseService.generateErrorResponse("No items found", HttpStatus.NOT_FOUND);
            } else
                return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @JsonBackReference
    @RequestMapping(value = "preview-cart", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItems(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            Double subTotal=0.0;
            if (isAnyServiceNull()) {
                return responseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("customer does not exist", HttpStatus.NOT_FOUND);
            }
            Order cart = orderService.findOrderById(orderId);
            if(cart==null)
                return responseService.generateErrorResponse("Cart not found",HttpStatus.NOT_FOUND);
            List<Product>listOfProducts=new ArrayList<>();
            List<OrderItem>orderItemList=cart.getOrderItems();
            if(orderItemList!=null&&(!orderItemList.isEmpty()))
            {
                Map<String,Object>response=new HashMap<>();
                List<Map<String,Object>>products=new ArrayList<>();
                for(OrderItem orderItem:orderItemList) {
                  Long productId=Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
                    Product product = catalogService.findProductById(productId);
                    if (product != null) {
                        Map<String,Object>productDetails=sharedUtilityService.createProductResponseMap(product);
                        products.add(productDetails);
                            subTotal += product.getDefaultSku().getCost().doubleValue();
                    }
                }
                response.put("products",products.toArray());
                response.put("sub_total",subTotal);
                return responseService.generateSuccessResponse("Cart items",response,HttpStatus.OK);
            }
            else
                return responseService.generateErrorResponse("No items in cart",HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "remove-item", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeCartItems(
            @RequestParam long customerId,
            @RequestParam Long orderId,
            @RequestParam Long orderItemId) {
        try {
            if (isAnyServiceNull()) {
                return responseService.generateErrorResponse("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return responseService.generateErrorResponse("Invalid request: Customer does not exist", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Order cart = orderService.findOrderById(orderId);
            if (cart == null || cart.getOrderItems() == null) {
                return responseService.generateErrorResponse("Cart Empty", HttpStatus.NOT_FOUND);
            }

            boolean itemRemoved = cartService.removeItemFromCart(cart, orderItemId);
            /*OrderItem orderItem=entityManager.find(OrderItem.class,orderItemId);*/

            if (itemRemoved) {
                return responseService.generateSuccessResponse("Item Removed",null, HttpStatus.OK);
            } else {
                return responseService.generateErrorResponse("Error removing item from cart: item not present in cart", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private boolean isAnyServiceNull() {
        return customerService == null || orderService == null|| catalogService == null;
    }

}
