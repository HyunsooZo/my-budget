package com.mybudget.domain;

import com.mybudget.config.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User extends BaseEntity{
    @Id
    private Long id;

    private String email;

    private String password;

    private String phoneNumber;

    private UserRole userRole;
}
