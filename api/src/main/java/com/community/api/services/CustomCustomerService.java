package com.community.api.services;
import com.community.api.component.Constant;
import com.community.api.endpoint.customer.CustomCustomer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Service
public class CustomCustomerService {
    @Autowired
    private EntityManager em;

    public Boolean validateInput(CustomCustomer customer) {
        if (customer.getUsername().isEmpty() || customer.getUsername() == null || customer.getMobileNumber().isEmpty() || customer.getMobileNumber() == null || customer.getPassword() == null || customer.getPassword().isEmpty())
            return false;
        if (!isValidMobileNumber(customer.getMobileNumber()))
            return false;
        return true;
    }

    public boolean isValidMobileNumber(String mobileNumber) {
        String mobileNumberPattern = "^\\+?\\d{10,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber) {
        return em.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


        public String formatDate(String dateString) {
            // Define the formatter for the input date string
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

            // Define the formatter for the desired output format
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            // Parse the date string into a LocalDateTime object
            LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);

            // Format the LocalDateTime object to the desired string format
            return dateTime.format(outputFormatter);
        }
    }
