package com.community.api.endpoint.avisoft.controller.cart;
import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.services.CartService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
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
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

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
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;
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


    @Transactional
    @RequestMapping(value = "empty", method = RequestMethod.DELETE)
    public ResponseEntity<?> emptyTheCart(@RequestParam Long customer_id) { //@TODO-empty cart should remove each item one by one
        try {
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customer_id);//finding the customer to get cart associated with it
            Order cart = null;
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id",HttpStatus.NOT_FOUND);
            } else {
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customer.getId());
                cart = this.orderService.findCartForCustomer(customer);
                System.out.println(cart.getId());
                if (cart == null) {
                    return ResponseService.generateErrorResponse("Cart Not Found", HttpStatus.NOT_FOUND);
                }
                if (cart.getStatus().equals(OrderStatus.IN_PROCESS)) {//ensuring its cart and not an order
                    List<OrderItem> items = cart.getOrderItems();
                    Iterator<OrderItem> iterator = items.iterator();

                    while (iterator.hasNext()) {
                        OrderItem item = iterator.next();
                        iterator.remove();
                        Product product=findProductFromItemAttribute(item);
                        CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
                        if(customCustomer!=null && customProduct!=null)
                        {
                            if(!customCustomer.getCartRecoveryLog().contains(customProduct))
                                customCustomer.getCartRecoveryLog().add(customProduct);
                        }
                        entityManager.remove(item);
                    }
                    entityManager.merge(cart);
                    entityManager.merge(customCustomer);
                    return ResponseService.generateSuccessResponse("Cart is empty now",null,HttpStatus.OK);
                }
                else
                    return ResponseService.generateErrorResponse("Error removing all items from cart",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error removing all items from cart : "+e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "add-to-cart", method = RequestMethod.POST)
    public ResponseEntity<?> addToCart(@RequestParam long customerId, @RequestParam long productId) {
        try {
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null) {
                cart = orderService.createNewCartForCustomer(customer);
            }
            Product product = catalogService.findProductById(productId);
            if (product == null) {
                return ResponseService.generateErrorResponse("Product not found", HttpStatus.NOT_FOUND);
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
            Map<String,Object>responseBody=new HashMap<>();
            boolean flag=false;
            for(OrderItem existingOrderItem:items)
            {
                if(Long.parseLong(existingOrderItem.getOrderItemAttributes().get("productId").getValue())==productId) {
                    flag=true;
                    return ResponseService.generateErrorResponse(Constant.CANNOT_ADD_MORE_THAN_ONE_FORM,HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
            if(!flag)
                items.add(orderItem);
            cart.setOrderItems(items);
            responseBody.put("order_id",cart.getId());
            responseBody.put("added_product_id",orderItem.getOrderItemAttributes().get("productId").getValue());
            return ResponseService.generateSuccessResponse("Cart updated",responseBody, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseService.generateErrorResponse("Error adding item to cart : "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "number-of-items", method = RequestMethod.GET)
        public ResponseEntity<?> retrieveCartItemsCount(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            Map<String,Object>responseBody=new HashMap<>();
            if (customer != null) {
                if (orderService.findOrderById(orderId) != null) {
                    responseBody.put("number_of_items",orderService.findOrderById(orderId).getOrderItems().size());
                    return ResponseService.generateSuccessResponse("Items in cart :",responseBody, HttpStatus.OK);
                } else
                    return ResponseService.generateErrorResponse("No items found", HttpStatus.NOT_FOUND);
            } else
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @JsonBackReference
    @RequestMapping(value = "preview-cart", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItems(@RequestParam long customerId, @RequestParam Long orderId) {
        try {
            Double subTotal=0.0;
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("customer does not exist", HttpStatus.NOT_FOUND);
            }
            Order cart = orderService.findOrderById(orderId);
            if(cart==null)
                return ResponseService.generateErrorResponse("Cart not found",HttpStatus.NOT_FOUND);
            List<Product>listOfProducts=new ArrayList<>();
            List<OrderItem>orderItemList=cart.getOrderItems();
            if(orderItemList!=null&&(!orderItemList.isEmpty()))
            {
                Map<String,Object>response=new HashMap<>();
                List<Map<String,Object>>products=new ArrayList<>();
                for(OrderItem orderItem:orderItemList) {
                    Product product=findProductFromItemAttribute(orderItem);
                    if (product != null) {
                        Map<String,Object>productDetails=sharedUtilityService.createProductResponseMap(product,orderItem);
                        products.add(productDetails);
                            subTotal =subTotal+ productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(),1L).getFee();
                    }
                }
                response.put("cart_id",cart.getId());
                response.put("products",products.toArray());
                response.put("sub_total",subTotal);
                return ResponseService.generateSuccessResponse("Cart items",response,HttpStatus.OK);
            }
            else
                return ResponseService.generateErrorResponse("No items in cart",HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
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
                return ResponseService.generateErrorResponse("One or more Services not initialized",HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Invalid request: Customer does not exist", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customer.getId());
            Order cart = orderService.findOrderById(orderId);
            if (cart == null || cart.getOrderItems() == null) {
                return ResponseService.generateErrorResponse("Cart Empty", HttpStatus.NOT_FOUND);
            }

            boolean itemRemoved = cartService.removeItemFromCart(cart, orderItemId);
            /*OrderItem orderItem=entityManager.find(OrderItem.class,orderItemId);*/

            if (itemRemoved) {
                return ResponseService.generateSuccessResponse("Item Removed",null, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("Error removing item from cart: item not present in cart", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @Transactional
    @RequestMapping(value ="place-order/{customerId}",method = RequestMethod.POST)
    public ResponseEntity<?>placeOrder(@PathVariable long customerId,@RequestParam long orderId) {
        try {
            Map<String,Object>responseMap=new HashMap<>();
            List<Order>individualOrders=new ArrayList<>();
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null)
                ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            Order order = orderService.findOrderById(orderId);
            if (order == null)
                ResponseService.generateErrorResponse("Cart items not found", HttpStatus.NOT_FOUND);
            Order cart = orderService.findCartForCustomer(customer);
            if(cart==null)
                ResponseService.generateErrorResponse("Cart not found",HttpStatus.NOT_FOUND);
            if (cart!=null &&cart.getId() == orderId) {
                for (OrderItem orderItem : cart.getOrderItems())
                {
                    Product product=findProductFromItemAttribute(orderItem);
                    Order individualOrder=orderService.createNamedOrderForCustomer(orderItem.getName(),customer);
                    individualOrder.setCustomer(customer);
                    if(customer!=null)
                        individualOrder.setEmailAddress(customer.getEmailAddress());
                    individualOrder.setStatus(new OrderStatus("ORDER_PLACED","order placed"));
                    OrderItemRequest orderItemRequest = new OrderItemRequest();
                    orderItemRequest.setProduct(product);
                    orderItemRequest.setOrder(individualOrder);
                    orderItemRequest.setQuantity(1);
                    orderItemRequest.setCategory(product.getCategory());
                    orderItemRequest.setItemName(product.getName());
                    Map<String,String> atrtributes=orderItemRequest.getItemAttributes();
                    atrtributes.put("productId",product.getId().toString());
                    orderItemRequest.setItemAttributes(atrtributes);
                    OrderItem orderItemForIndividualOrder = orderItemService.createOrderItem(orderItemRequest);
                    individualOrder.addOrderItem(orderItemForIndividualOrder);
                    entityManager.persist(individualOrder);
                    individualOrders.add(individualOrder);
                }
                responseMap.put("Orders",individualOrders);
                List<OrderItem> items = cart.getOrderItems();
                Iterator<OrderItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    OrderItem item = iterator.next();
                    iterator.remove();
                    entityManager.remove(item);
                }
                entityManager.merge(cart);
                return ResponseService.generateSuccessResponse("Order Placed", cart.getId(), HttpStatus.OK);
            }
            return ResponseService.generateErrorResponse("Error placing order", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error placing order", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value ="cart-recovery-log/{customerId}",method = RequestMethod.GET)
    public ResponseEntity<?>getCartRecoveryLog(@PathVariable long customerId)
    {
        CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
        if(customCustomer==null)
            return ResponseService.generateErrorResponse("Cannot find customer for this id",HttpStatus.NOT_FOUND);
        List<Map<String,Object>>productList=new ArrayList<>();
        for(Product product:customCustomer.getCartRecoveryLog())
        {
            productList.add(sharedUtilityService.createProductResponseMap(product,null));
        }
        return ResponseService.generateSuccessResponse("Cart Recovery Log : ",productList,HttpStatus.OK);
    }

    private boolean isAnyServiceNull() {
        return customerService == null || orderService == null|| catalogService == null;
    }
    public Product findProductFromItemAttribute(OrderItem orderItem)
    {
        Long productId=Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        System.out.println(productId);
        Product product=catalogService.findProductById(productId);
        System.out.println(product.getName());
        return product;
    }

}
