package com.community.api.entity;
import com.community.api.entity.CustomCustomer;
import io.micrometer.core.lang.Nullable;
import lombok.*;
import org.broadleafcommerce.profile.core.domain.CustomerImpl;
import javax.persistence.*;

@Entity
@Table(name = "CUSTOM_OTP")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomOtp extends CustomerImpl {

    @NonNull
    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Nullable
    @Column(name = "otp", unique = true)
    private String otp;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "mobile_number", referencedColumnName = "mobile_number",insertable = false,updatable = false)
    private CustomCustomer customCustomer;
}

