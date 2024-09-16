package com.community.api.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Cart {
   @Id
   private String cartId;
   private long customerId;
  /* List<Prod>*/
}
