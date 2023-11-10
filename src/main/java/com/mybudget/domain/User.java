package com.mybudget.domain;

import com.mybudget.config.UserRole;
import com.mybudget.dto.UserSignUpRequestDto;
import com.mybudget.enums.UserStatus;
import lombok.*;

import javax.persistence.*;

import static com.mybudget.config.UserRole.ROLE_USER;
import static com.mybudget.enums.UserStatus.INACTIVE;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Setter
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    public static User from(UserSignUpRequestDto userSignUpRequestDto, String password) {
        return User.builder()
                .email(userSignUpRequestDto.getEmail())
                .password(password)
                .phoneNumber(userSignUpRequestDto.getPhoneNumber())
                .userRole(ROLE_USER)
                .userStatus(INACTIVE)
                .build();
    }
}
