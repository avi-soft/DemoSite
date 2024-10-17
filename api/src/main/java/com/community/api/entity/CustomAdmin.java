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
    private Long id;

    private int role_id;
    private String password;
    private String otp;
    @Size(min = 9, max = 13)
    private String phoneNumber;
    private String created_at,updated_at,created_by, modified_by;


    public CustomAdmin(Long id, int role_id, String password, String phoneNumber, String created_at, String created_by) {
        this.id = id;
        this.role_id = role_id;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.created_at = created_at;
        this.created_by = created_by;
    }
}
