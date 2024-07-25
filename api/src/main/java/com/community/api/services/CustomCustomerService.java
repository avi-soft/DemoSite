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

        if (mobileNumber.startsWith("0")) {
            mobileNumber = mobileNumber.substring(1);
        }
        String mobileNumberPattern = "^\\d{9,13}$";
        return Pattern.compile(mobileNumberPattern).matcher(mobileNumber).matches();
    }

    public CustomCustomer findCustomCustomerByPhone(String mobileNumber,String countryCode) {
        return em.createQuery(Constant.PHONE_QUERY, CustomCustomer.class)
                .setParameter("mobileNumber", mobileNumber)
                .setParameter("countryCode", countryCode)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

        public String formatDate(String dateString) {

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

            LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);

            return dateTime.format(outputFormatter);
        }
    }
