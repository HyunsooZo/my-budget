package com.mybudget.domain;

import com.mybudget.config.UserRole;
import com.mybudget.dto.UserSignUpRequestDto;
import com.mybudget.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static com.mybudget.config.UserRole.*;
import static com.mybudget.enums.UserStatus.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String phoneNumber;

    private UserRole userRole;

    private UserStatus userStatus;

    public static User from(UserSignUpRequestDto userSignUpRequestDto, String password){
        return User.builder()
                .email(userSignUpRequestDto.getEmail())
                .password(password)
                .phoneNumber(userSignUpRequestDto.getPhoneNumber())
                .userRole(ROLE_USER)
                .userStatus(INACTIVE)
                .build();
    }
}
