package com.mybudget.service;

import com.mybudget.component.EmailSender;
import com.mybudget.component.SmsSender;
import com.mybudget.config.JwtProvider;
import com.mybudget.config.UserRole;
import com.mybudget.domain.User;
import com.mybudget.dto.UserSignInDto;
import com.mybudget.dto.UserSignInRequestDto;
import com.mybudget.enums.UserStatus;
import com.mybudget.exception.CustomException;
import com.mybudget.repository.TokenRedisRepository;
import com.mybudget.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.mybudget.exception.ErrorCode.INVALID_PASSWORD;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("사용자 로그인 테스트")
class UserLoginTest {
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

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private TokenRedisRepository tokenRedisRepository;

    private UserService userService;

    public static UserSignInRequestDto userSignInRequestDto =
            new UserSignInRequestDto("email@test.com", "12341233");
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
        userService =
                new UserService(
                        smsSender,
                        emailSender,
                        redisTemplate,
                        passwordEncoder,
                        userRepository,
                        jwtProvider,
                        tokenRedisRepository
                );
    }

    @Test
    @DisplayName("실패 - 비밀번호 오류")
    public void testSignIn_InvalidPassword() {
        UserSignInRequestDto requestDto = new UserSignInRequestDto("test@example.com", "wrong_password");

        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword()))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.signIn(requestDto))
                .isInstanceOf(CustomException.class)
                .hasMessage(INVALID_PASSWORD.getMessage());
    }

    @Test
    @DisplayName("성공")
    public void testSignIn_ValidUser() {
        //given
        UserSignInRequestDto requestDto =
                new UserSignInRequestDto("test@example.com", "correct_password");

        when(userRepository.findByEmail(requestDto.getEmail()))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDto.getPassword(), user.getPassword()))
                .thenReturn(true);

        //when
        UserSignInDto signInDto = userService.signIn(requestDto);

        //then
        Assertions.assertThat(signInDto.getUserId()).isEqualTo(user.getId());
    }
}