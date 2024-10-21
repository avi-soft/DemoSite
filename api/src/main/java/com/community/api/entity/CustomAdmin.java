package com.community.api.entity;

import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name ="custom_admin")
public class CustomAdmin
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long admin_id;

    private int role;
    private String password;
    private String user_name;
    private String otp;
    @Size(min = 9, max = 13)
    private String mobileNumber;
    private String country_code;
    private String token;
    private int signedUp=0;
    private String created_at,updated_at,created_by, modified_by;


    public CustomAdmin(Long admin_id, int role, String password,String user_name, String mobileNumber,String country_code,int signedUp, String created_at, String created_by) {
        this.admin_id = admin_id;
        this.role = role;
        this.password = password;
        this.user_name=user_name;
        this.mobileNumber = mobileNumber;
        this.country_code = country_code;
        this.signedUp = signedUp;
        this.created_at = created_at;
        this.created_by = created_by;
    }
}
