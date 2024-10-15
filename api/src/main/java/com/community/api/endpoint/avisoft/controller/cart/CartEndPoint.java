package com.community.api.endpoint.avisoft.controller.cart;

import com.broadleafcommerce.rest.api.endpoint.BaseEndpoint;
import com.community.api.component.Constant;
import com.community.api.entity.CustomCustomer;
import com.community.api.entity.CustomProduct;
import com.community.api.services.CartService;
import com.community.api.services.ProductReserveCategoryFeePostRefService;
import com.community.api.services.ReserveCategoryService;
import com.community.api.services.ResponseService;
import com.community.api.services.SharedUtilityService;
import com.community.api.services.exception.ExceptionHandlingImplement;
import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.models.auth.In;
import org.broadleafcommerce.common.money.Money;
import org.broadleafcommerce.common.persistence.Status;
import org.broadleafcommerce.core.catalog.domain.Product;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.order.domain.*;
import org.broadleafcommerce.core.order.service.OrderItemService;
import org.broadleafcommerce.core.order.service.OrderService;
import org.broadleafcommerce.core.order.service.call.OrderItemRequest;
import org.broadleafcommerce.core.order.service.type.OrderStatus;
import org.broadleafcommerce.profile.core.domain.Customer;
import org.broadleafcommerce.profile.core.domain.CustomerAttribute;
import org.broadleafcommerce.profile.core.domain.CustomerAttributeImpl;
import org.broadleafcommerce.profile.core.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getIntegerList;
import static com.community.api.services.ServiceProvider.ServiceProviderServiceImpl.getLongList;

@RestController
@RequestMapping(value = "/cart",
        produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE}
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
    private ReserveCategoryService reserveCategoryService;
    private ProductReserveCategoryFeePostRefService reserveCategoryFeePostRefService;

    // Setter-based injection
    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Autowired
    public void setSharedUtilityService(SharedUtilityService sharedUtilityService) {
        this.sharedUtilityService = sharedUtilityService;
    }

    @Autowired
    private ProductReserveCategoryFeePostRefService productReserveCategoryFeePostRefService;

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }
    @Autowired
    public void setReserveCategoryService(ReserveCategoryService reserveCategoryService){
        this.reserveCategoryService=reserveCategoryService;
    }
    @Autowired
    public void setReserveCategoryFeePostRefService(ProductReserveCategoryFeePostRefService reserveCategoryFeePostRefService)
    {
        this.reserveCategoryFeePostRefService=reserveCategoryFeePostRefService;
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
    @RequestMapping(value = "empty/{customerId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> emptyTheCart(@PathVariable Long customerId) { //@TODO-empty cart should remove each item one by one
        try {
            Long id = Long.valueOf(customerId);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            Customer customer = customerService.readCustomerById(customerId);//finding the customer to get cart associated with it
            Order cart = null;
            if (customer == null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            } else {
                CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
                cart = this.orderService.findCartForCustomer(customer);
                if (cart == null) {
                    return ResponseService.generateErrorResponse("Cart Not Found", HttpStatus.NOT_FOUND);
                }
                if (cart.getStatus().equals(OrderStatus.IN_PROCESS)) {//ensuring its cart and not an order
                    List<OrderItem> items = cart.getOrderItems();
                    Iterator<OrderItem> iterator = items.iterator();

                    while (iterator.hasNext()) {
                        OrderItem item = iterator.next();
                        iterator.remove();
                        Product product = findProductFromItemAttribute(item);
                        CustomProduct customProduct = entityManager.find(CustomProduct.class, product.getId());
                        if (customCustomer != null && customProduct != null) {
                            if (!customCustomer.getCartRecoveryLog().contains(customProduct))
                                customCustomer.getCartRecoveryLog().add(customProduct);
                        }
                        entityManager.remove(item);
                    }
                    entityManager.merge(cart);
                    entityManager.merge(customCustomer);
                    return ResponseService.generateSuccessResponse("Cart is empty now", null, HttpStatus.OK);
                } else
                    return ResponseService.generateErrorResponse("Error removing all items from cart", HttpStatus.INTERNAL_SERVER_ERROR);
            }

        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error removing all items from cart : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "add-to-cart/{customerId}/{productId}", method = RequestMethod.POST)
    public ResponseEntity<?> addToCart(@PathVariable long customerId, @PathVariable long productId) {
        try {
            Long id = Long.valueOf(customerId);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            Customer customer = customerService.readCustomerById(customerId);
            CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
            if (customer == null||customCustomer==null) {
                return ResponseService.generateErrorResponse("Customer not found for this Id", HttpStatus.NOT_FOUND);
            }
            if (customer.getFirstName() == null ||
                    customer.getLastName() == null ||
                    customer.getEmailAddress() == null ||
                    customCustomer.getCategory() == null ||
                    customer.getUsername() == null ||
                    customer.getPassword() == null)
            {
                return ResponseService.generateErrorResponse(
                        "All fields must be completed: First Name, Last Name, Primary Email, Username, Password, and Category are required before setting up the cart.",
                        HttpStatus.BAD_REQUEST
                );
            }

            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null) {
                cart = orderService.createNewCartForCustomer(customer);
                }
            Product product = catalogService.findProductById(productId);
            if (product == null) {
                return ResponseService.generateErrorResponse("Product not found", HttpStatus.NOT_FOUND);
            }
            CustomProduct customProduct=entityManager.find(CustomProduct.class,productId);
            Long reserveCategoryId=reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId();
            if(reserveCategoryService.getReserveCategoryFee(productId,reserveCategoryId)==null)
                return ResponseService.generateErrorResponse("Cannot add product to cart :Fee not specified for your category",HttpStatus.UNPROCESSABLE_ENTITY);
            /*if(productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(),.getFee()==null)
            {

            }*/
            if ((((Status) customProduct).getArchived() == 'Y' || !customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                return ResponseService.generateErrorResponse("Cannot add an archieved product",HttpStatus.BAD_REQUEST);
            }
            OrderItemRequest orderItemRequest = new OrderItemRequest();
            orderItemRequest.setProduct(product);
            orderItemRequest.setOrder(cart);
            orderItemRequest.setQuantity(1);
            orderItemRequest.setCategory(product.getCategory());
            orderItemRequest.setItemName(product.getName());
            Map<String, String> atrtributes = orderItemRequest.getItemAttributes();
            atrtributes.put("productId", product.getId().toString());
            orderItemRequest.setItemAttributes(atrtributes);
            OrderItem orderItem = orderItemService.createOrderItem(orderItemRequest);
            List<OrderItem> items = cart.getOrderItems();
            Map<String, Object> responseBody = new HashMap<>();
            boolean flag = false;
            for (OrderItem existingOrderItem : items) {
                if (Long.parseLong(existingOrderItem.getOrderItemAttributes().get("productId").getValue()) == productId) {
                    flag = true;
                    return ResponseService.generateErrorResponse(Constant.CANNOT_ADD_MORE_THAN_ONE_FORM, HttpStatus.UNPROCESSABLE_ENTITY);
                }
            }
            if (!flag)
                items.add(orderItem);
            cart.setOrderItems(items);
            responseBody.put("cart_id", cart.getId());
            responseBody.put("added_product_id", orderItem.getOrderItemAttributes().get("productId").getValue());
            return ResponseService.generateSuccessResponse("Cart updated", responseBody, HttpStatus.OK);

        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            return ResponseService.generateErrorResponse("Error adding item to cart : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "number-of-items/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItemsCount(@PathVariable long customerId) {
        try {
            Long id = Long.valueOf(customerId);
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            Map<String, Object> responseBody = new HashMap<>();
            if (customer != null) {
                if (orderService.findCartForCustomer(customer) != null) {
                    responseBody.put("number_of_items", orderService.findCartForCustomer(customer).getOrderItems().size());
                    return ResponseService.generateSuccessResponse("Items in cart :", responseBody, HttpStatus.OK);
                } else
                    return ResponseService.generateErrorResponse("No items found", HttpStatus.NOT_FOUND);
            } else
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);

        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @JsonBackReference
    @RequestMapping(value = "preview-cart/{customerId}", method = RequestMethod.GET)
    public ResponseEntity<?> retrieveCartItems(@PathVariable long customerId) {
        try {
            List<OrderItem>archievedItems=new ArrayList<>();
            Long id = Long.valueOf(customerId);
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            Double subTotal = 0.0;
            double platformFee=0.0;
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Serivces not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);

            if (customer == null) {
                return ResponseService.generateErrorResponse("customer does not exist", HttpStatus.NOT_FOUND);
            }
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null)
                return ResponseService.generateErrorResponse("Cart not found", HttpStatus.NOT_FOUND);
            List<Product> listOfProducts = new ArrayList<>();
            List<OrderItem> orderItemList = cart.getOrderItems();
            if (orderItemList != null && (!orderItemList.isEmpty())) {
                Map<String, Object> response = new HashMap<>();
                List<Map<String, Object>> products = new ArrayList<>();
                for (OrderItem orderItem : orderItemList) {
                    Product product = findProductFromItemAttribute(orderItem);
                    CustomProduct customProduct=entityManager.find(CustomProduct.class,product.getId());
                    if (product != null) {
                        if ((((Status) customProduct).getArchived() == 'Y' || !customProduct.getDefaultSku().getActiveEndDate().after(new Date()))) {
                            archievedItems.add(orderItem);
                            continue;
                        }
                        Map<String, Object> productDetails = sharedUtilityService.createProductResponseMap(product, orderItem,customCustomer);
                        products.add(productDetails);
                        if(customProduct!=null)
                            platformFee=platformFee+customProduct.getPlatformFee();
                        subTotal = subTotal + productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(), 1L).getFee();
                    }
                }
                response.put("cart_id", cart.getId());
                response.put("products", products.toArray());
                response.put("sub_total", subTotal);
                response.put("total_platform_fee", platformFee);
                for(OrderItem orderItem:archievedItems)
                {
                    cart.getOrderItems().remove(orderItem);
                }
                archievedItems.clear();
                return ResponseService.generateSuccessResponse("Cart items", response, HttpStatus.OK);
            } else
                return ResponseService.generateErrorResponse("No items in cart", HttpStatus.NOT_FOUND);

        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error retrieving cart Items", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "remove-item/{customerId}/{orderItemId}", method = RequestMethod.DELETE)
    public ResponseEntity<?> removeCartItems(
            @PathVariable long customerId,
            @PathVariable Long orderItemId) {
        try {
            Long id = Long.valueOf(customerId);
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            if (isAnyServiceNull()) {
                return ResponseService.generateErrorResponse("One or more Services not initialized", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Customer customer = customerService.readCustomerById(customerId);
            if (customer == null) {
                return ResponseService.generateErrorResponse("Invalid request: Customer does not exist", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            CustomCustomer customCustomer = entityManager.find(CustomCustomer.class, customer.getId());
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null || cart.getOrderItems() == null) {
                return ResponseService.generateErrorResponse("Cart Empty", HttpStatus.NOT_FOUND);
            }
            OrderItem orderItemToRemove=null;
            for(OrderItem orderItem:cart.getOrderItems())
            {
                if(orderItem.getId().equals(orderItemId)) {
                    orderItemToRemove = orderItem;
                    break;
                }
            }
            if(orderItemToRemove==null)
            {
                return ResponseService.generateErrorResponse("Item to remove not found",HttpStatus.NOT_FOUND);
            }
            long pid=Long.parseLong(orderItemToRemove.getOrderItemAttributes().get("productId").getValue());
            CustomProduct customProduct=entityManager.find(CustomProduct.class,pid);
            if (customProduct != null) {
                if (!customCustomer.getCartRecoveryLog().contains(customProduct))
                    customCustomer.getCartRecoveryLog().add(customProduct);
            }
            boolean itemRemoved = cartService.removeItemFromCart(cart, orderItemId);
            /*OrderItem orderItem=entityManager.find(OrderItem.class,orderItemId);*/
            if(itemRemoved)
            {
                return ResponseService.generateSuccessResponse("Item Removed", null, HttpStatus.OK);
            } else {
                return ResponseService.generateErrorResponse("Error removing item from cart: item not present in cart", HttpStatus.NOT_FOUND);
            }

        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error deleting", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional
    @RequestMapping(value = "place-order/{customerId}", method = RequestMethod.POST)
    public ResponseEntity<?> placeOrder(@PathVariable long customerId,@RequestBody Map<String,Object>map) {
        try {
            CustomProduct customProduct=null;
            Long id = Long.valueOf(customerId);
            List<Long>orderItemIds=getLongList(map,"orderItemIds");
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
            Map<String, Object> responseMap = new HashMap<>();
            List<Order> individualOrders = new ArrayList<>();
            Customer customer = customerService.readCustomerById(customerId);
            CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
            if (customer == null|| customCustomer==null)
                return ResponseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
            Order cart = orderService.findCartForCustomer(customer);
            if (cart == null)
                return ResponseService.generateErrorResponse("Cart not found", HttpStatus.NOT_FOUND);
            if(cart.getOrderItems().isEmpty())
                return ResponseService.generateErrorResponse("Cart is empty",HttpStatus.NOT_FOUND);
            List<Long>cartItemIds=new ArrayList<>();
            List<String>errors=new ArrayList<>();
            int batchNumber=customCustomer.getNumberOfOrders();
            ++batchNumber;
            for(OrderItem orderItem : cart.getOrderItems())
            {
                cartItemIds.add(orderItem.getId());
            }
            if(orderItemIds.isEmpty())
                return ResponseService.generateErrorResponse("No items Selected",HttpStatus.BAD_REQUEST);

                for (Long orderItemId:orderItemIds)
            {
                if(!cartItemIds.contains(orderItemId))
                {
                    errors.add("Order Item Id : "+orderItemId+" does not belong to cart");
                }
            }
            if(!errors.isEmpty())
                return ResponseService.generateErrorResponse("Error Placing order : "+errors.toString(),HttpStatus.BAD_REQUEST);
            for (OrderItem orderItem : cart.getOrderItems()) {
                if (orderItemIds.contains(orderItem.getId())) {
                    Product product = findProductFromItemAttribute(orderItem);
                    if(product!=null)
                        customProduct=entityManager.find(CustomProduct.class,product.getId());
                    Order individualOrder = orderService.createNamedOrderForCustomer(orderItem.getName(), customer);
                    individualOrder.setCustomer(customer);
                    individualOrder.setEmailAddress(customer.getEmailAddress());
                    individualOrder.setStatus(new OrderStatus("ORDER_PLACED", "order placed"));
                    OrderItemRequest orderItemRequest = new OrderItemRequest();
                    orderItemRequest.setProduct(product);
                    orderItemRequest.setOrder(individualOrder);
                    orderItemRequest.setQuantity(1);
                    orderItemRequest.setCategory(product.getCategory());
                    orderItemRequest.setItemName(product.getName());
                    Map<String, String> atrtributes = orderItemRequest.getItemAttributes();
                    atrtributes.put("productId", product.getId().toString());
                    orderItemRequest.setItemAttributes(atrtributes);
                    OrderItem orderItemForIndividualOrder = orderItemService.createOrderItem(orderItemRequest);
                    individualOrder.addOrderItem(orderItemForIndividualOrder);
                    Money subTotal=new Money(productReserveCategoryFeePostRefService.getCustomProductReserveCategoryFeePostRefByProductIdAndReserveCategoryId(product.getId(),reserveCategoryService.getCategoryByName(customCustomer.getCategory()).getReserveCategoryId()).getFee());
                    individualOrder.setSubTotal(subTotal);
                    individualOrder.setOrderNumber("O-"+customer.getId()+"-B-"+batchNumber);
                    Money total=new Money(customProduct.getPlatformFee());
                    individualOrder.setTotal(total);
                    LocalDateTime localDateTime = LocalDateTime.now();
                    Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                    individualOrder.setSubmitDate(date);
                    individualOrder.setSubmitDate(date);
                    entityManager.persist(individualOrder);
                    individualOrders.add(individualOrder);
                }
            }
                responseMap.put("Orders", individualOrders);
                List<OrderItem> items = cart.getOrderItems();
                Iterator<OrderItem> iterator = items.iterator();
                while (iterator.hasNext()) {
                    OrderItem item = iterator.next();
                    if(orderItemIds.contains(item.getId())) {
                        iterator.remove();
                        entityManager.remove(item);
                    }
                }
                customCustomer.setNumberOfOrders(batchNumber);
                entityManager.merge(customCustomer);
                entityManager.merge(cart);
                return ResponseService.generateSuccessResponse("Order Placed", cart.getId(), HttpStatus.OK);
        }catch (NumberFormatException e) {
            return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error placing order "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @RequestMapping(value ="cart-recovery-log/{customerId}",method = RequestMethod.GET)
    public ResponseEntity<?>getCartRecoveryLog(@PathVariable Long customerId)
    {
        try{
            Long id = Long.valueOf(customerId);
            if(id==null)
                return ResponseService.generateErrorResponse("Customer Id not specified",HttpStatus.BAD_REQUEST);
                CustomCustomer customCustomer=entityManager.find(CustomCustomer.class,customerId);
                if(customCustomer==null)
                    return ResponseService.generateErrorResponse("Cannot find customer for this id",HttpStatus.NOT_FOUND);
                List<Map<String,Object>>productList=new ArrayList<>();
                for(Product product:customCustomer.getCartRecoveryLog())
                {
                    productList.add(sharedUtilityService.createProductResponseMap(product,null,customCustomer));
                }
                return ResponseService.generateSuccessResponse("Cart Recovery Log : ",productList,HttpStatus.OK);

            }catch (NumberFormatException e) {
                return ResponseService.generateErrorResponse("Invalid customerId: expected a Long", HttpStatus.BAD_REQUEST);
            }catch (Exception e) {

            exceptionHandling.handleException(e);
            return ResponseService.generateErrorResponse("Error fetching recovery log", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean isAnyServiceNull() {
        return customerService == null || orderService == null || catalogService == null;
    }

    public Product findProductFromItemAttribute(OrderItem orderItem) {
        Long productId = Long.parseLong(orderItem.getOrderItemAttributes().get("productId").getValue());
        System.out.println(productId);
        Product product = catalogService.findProductById(productId);
        System.out.println(product.getName());
        return product;
    }
    public class OrderRequest {
        private List<Long> orderItemIds;
    }
}
