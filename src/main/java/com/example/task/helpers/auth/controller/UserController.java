package com.example.task.helpers.auth.controller;

import com.example.task.helpers.auth.dto.AuthRequestDto;
import com.example.task.helpers.auth.dto.AuthResponseDto;
import com.example.task.helpers.auth.dto.LoginResponseDto;
import com.example.task.helpers.auth.dto.SearchResponseDto;
import com.example.task.helpers.auth.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

@RequestMapping("/api/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{username}")
    public AuthResponseDto getUserProfile(@PathVariable String username){
        return userService.getUserByUsername(username);
    }

    @GetMapping("/search/{username}")
    public List<SearchResponseDto> searchUser (@PathVariable String username){
        return userService.searchUsername(username);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@RequestBody AuthRequestDto request){
        return userService.register(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponseDto login(
            @RequestBody AuthRequestDto request,
            HttpServletResponse response
    ){
        AuthResponseDto user = userService.getUserByUsername(request);
        String token = userService.verify(request);
        String refreshToken = userService.getRefreshToken(request.getUsername());
        addRefreshCookie(response, refreshToken);
        return new LoginResponseDto(token, user);
    }

//    public LoginResponseDto refreshToken ( @RequestBody Map<String, String> request){
//        String refreshToken = request.get("refreshToken");

    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.CREATED)
        public String refreshToken(
        @CookieValue(name = "refresh_token", required = false) String refreshToken,
                HttpServletResponse response
    ){
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token is missing");
        }
        addRefreshCookie(response, refreshToken);
        return userService.refreshJwtToken(refreshToken);
    }

    @PatchMapping("/preferences")
    @ResponseStatus(HttpStatus.OK)
    public void updatePreferences(
            @RequestBody com.example.task.helpers.auth.dto.UserPreferenceDto preferences) {
        String username = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
        userService.updatePreferences(username, preferences);
    }

    private void addRefreshCookie(
            HttpServletResponse response,
            String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
//                .secure(true)
                .sameSite("Strict")
                .path("/api/user/refresh")
                .maxAge(Duration.ofDays(30))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

}
