package com.community.api.utils;

import com.community.api.endpoint.customer.CustomCustomer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String value;

    @Lob
    private byte[] data;

    @OneToOne
    @JoinColumn(name = "custom_customer_id")
    private CustomCustomer customCustomer;
}
