package com.example.task.helpers.auth.service;

import com.example.task.common.security.service.JWTService;
import com.example.task.helpers.auth.dto.AuthRequestDto;
import com.example.task.helpers.auth.dto.AuthResponseDto;
import com.example.task.helpers.auth.dto.SearchResponseDto;
import com.example.task.helpers.auth.dto.UserPreferenceDto;
import com.example.task.helpers.auth.entity.RefreshToken;
import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.auth.enums.Role;
import com.example.task.helpers.auth.exception.UserNotFoundException;
import com.example.task.helpers.auth.repo.RefreshTokenRepo;
import com.example.task.helpers.auth.repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final RefreshTokenRepo refreshTokenRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Transactional
    public void updatePreferences(String username, UserPreferenceDto preferences) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        
        user.setPreferredTimezone(preferences.getPreferredTimezone());
        user.setEmailNotification(preferences.isEmailNotification());
        user.setUpdatedAt(Instant.now());
        userRepo.save(user);
    }

    public AuthResponseDto register(AuthRequestDto request){
        User user = new User(
                request.getUsername(),
                encoder.encode(request.getPassword()),
                request.getEmail(),
                request.getPfp(),
                Role.USER
        );
        return convertToResponse(userRepo.save(user));
    }

    public AuthResponseDto getUserByUsername (AuthRequestDto request){
        return convertToResponse(
                userRepo.findByUsername(request.getUsername())
                        .orElseThrow(()-> {
                            log.warn("Username {} not found", request.getUsername());
                            return new UserNotFoundException("Username " + request.getUsername() + " not found");
                        })
        );
    }
    public AuthResponseDto getUserByUsername (String request){
        return convertToResponse(
                userRepo.findByUsername(request)
                        .orElseThrow(()-> {
                            log.warn("User {} not found", request);
                            return new UserNotFoundException("Username " + request + " not found");
                        })
        );
    }

    public List<SearchResponseDto> searchUsername(String username){
        return userRepo.findByUsernameContainingIgnoreCase(username)
                .stream()
                .map(this::searchResponse)
                .toList();
    }

    public String verify(AuthRequestDto request){
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        return auth.isAuthenticated() ? jwtService.generateToken(request.getUsername()) : "Fail";
    }

    private AuthResponseDto convertToResponse(User user){
        return new AuthResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPfp(),
                user.getBanner(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt(),
                user.getRole(),
                user.getPreferredTimezone()
        );
    }

    private SearchResponseDto searchResponse(User user){
        return new SearchResponseDto(
                user.getId(),
                user.getUsername(),
                user.getPfp(),
                user.getBanner(),
                user.getFirstName(),
                user.getLastName()
        );
    }

//    @Transactional
//    public String createRefreshToken(String username){
//        User user = userRepo.findByUsername(username).orElseThrow(()-> new UserNotFoundException("User not found"));
//        refreshTokenRepo.deleteByUser(user);
//        refreshTokenRepo.flush();
//
//        String token = UUID.randomUUID().toString();
//        RefreshToken refreshToken = RefreshToken.builder()
//                .user(user)
//                .token(token)
//                .expiryDate(Instant.now().plusMillis(1000L * 60 * 60 * 24 * 30))
//                .build();
//        refreshTokenRepo.save(refreshToken);
//        return token;
//    }

    public String getRefreshToken(String username){
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        RefreshToken refreshToken = refreshTokenRepo.findByUser(user).orElseGet(() -> createRefreshToken(user));
        return refreshToken.getExpiryDate().isAfter(Instant.now()) ? refreshToken.getToken() : createRefreshToken(refreshToken);
    }

//    @Transactional
//    private String createRefreshToken(String username) {
//        User user = userRepo.findByUsername(username)
//                .orElseThrow(() -> new UserNotFoundException("User not found"));
//
//        RefreshToken refreshToken = refreshTokenRepo.findByUser(user)
//                .orElseGet(() -> RefreshToken.builder()
//                        .user(user)
//                        .build());
//
//        String token = UUID.randomUUID().toString();
//
//        refreshToken.setToken(token);
//        refreshToken.setExpiryDate(
//                Instant.now().plus(30, ChronoUnit.DAYS)
//        );
//
//        refreshTokenRepo.save(refreshToken);
//
//        return token;
//    }

    @Transactional
    private RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .build();

        String token = UUID.randomUUID().toString();

        refreshToken.setToken(token);
        refreshToken.setExpiryDate(
                Instant.now().plus(30, ChronoUnit.DAYS)
        );

        return refreshTokenRepo.save(refreshToken);
    }

    @Transactional
    private String createRefreshToken(RefreshToken refreshToken) {
        String token = UUID.randomUUID().toString();
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(
                Instant.now().plus(30, ChronoUnit.DAYS)
        );
        refreshTokenRepo.save(refreshToken);

        return token;
    }

//    public LoginResponseDto refreshJwtToken(String token) {
//        return refreshTokenRepo.findByToken(token)
//                .map(this::verifyExpiration)
//                .map(com.example.task.helpers.auth.entity.RefreshToken::getUser)
//                .map(user -> jwtService.generateToken(user.getUsername()))
//                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
//                .map(refreshToken -> {
////                    AuthResponseDto user = new AuthResponseDto(
////                            refreshToken.getUser().getId(),
////                            refreshToken.getUser().getUsername(),
////                            refreshToken.getUser().getEmail(),
////                            refreshToken.getUser().getPfp(),
////                            refreshToken.getUser().getBanner(),
////                            refreshToken.getUser().getFirstName(),
////                            refreshToken.getUser().getLastName(),
////                            refreshToken.getUser().isActive(),
////                            refreshToken.getUser().getCreatedAt(),
////                            refreshToken.getUser().getUpdatedAt(),
////                            refreshToken.getUser().getLastLoginAt(),
////                            refreshToken.getUser().getRole(),
////                            refreshToken.getUser().getPreferredTimezone()
////                    );
    /// /                    return new LoginResponseDto(jwtToken, user);
//                })

    public String refreshJwtToken(String token){
        return refreshTokenRepo.findByToken(token)
                .map(this::verifyExpiration)
                .map(refreshToken -> {
                    return jwtService.generateToken(refreshToken.getUser().getUsername());
                })
                .orElseThrow(() ->
                        new RuntimeException("Refresh token is not in database!")
                );
    }

    private RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepo.delete(token);
            throw new RuntimeException("Refresh token expired. Please make a new sign in request");
        }
        return token;
    }

}
