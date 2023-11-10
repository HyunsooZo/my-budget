package com.mybudget.service;

import com.mybudget.component.EmailSender;
import com.mybudget.component.SmsSender;
import com.mybudget.config.UserRole;
import com.mybudget.domain.User;
import com.mybudget.dto.UserSignUpRequestDto;
import com.mybudget.enums.UserStatus;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("회원가입 테스트")
class UserSignUpTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SmsSender smsSender;

    private UserService userService;

    public static UserSignUpRequestDto requestDto = new UserSignUpRequestDto(
            "email@test.com", "12341233", "조현수", "01099998888");
    public static User user = User.builder()
            .id(1L)
            .email("email@test.com")
            .phoneNumber("112333333")
            .password("aaaaa")
            .userStatus(UserStatus.ACTIVE)
            .userRole(UserRole.ROLE_USER)
            .build();

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userService = new UserService(smsSender, emailSender, redisTemplate, passwordEncoder, userRepository);
    }

    @Test
    @DisplayName("성공")
    public void signUp_Successful() {
        // Given
        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(requestDto.getPassword()))
                .thenReturn("encodedPassword");
        when(userRepository.save(any()))
                .thenReturn(user);

        // When
        userService.signUp(requestDto);

        // Then
        verify(userRepository, times(1)).findByEmail("email@test.com");
        verify(passwordEncoder, times(1)).encode("12341233");
        verify(userRepository, times(1)).save(any());
        verify(emailSender, times(1)).sendEmail(any(), any(), any());
    }

    @Test
    @DisplayName("실패 - 이미 가입된 사용자")
    public void signUp_ExistingUser() {
        // Given
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        // When/Then
        assertThatThrownBy(() -> userService.signUp(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage("이미 가입한 사용자 입니다.");

        // Verify
        verify(userRepository, times(1))
                .findByEmail("email@test.com");
    }
}