package com.example.task.helpers.auth.controller;

import com.example.task.helpers.auth.dto.AuthRequestDto;
import com.example.task.helpers.auth.dto.AuthResponseDto;
import com.example.task.helpers.auth.dto.LoginResponseDto;
import com.example.task.helpers.auth.dto.SearchResponseDto;
import com.example.task.helpers.auth.service.UserService;
import com.example.task.common.error.dto.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "User", description = "User management and authentication endpoints")
@RequestMapping("/api/user")
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Get user profile", description = "Retrieves the profile details of a user by their username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved profile"),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{username}")
    public AuthResponseDto getUserProfile(@PathVariable String username){
        return userService.getUserByUsername(username);
    }

    @Operation(summary = "Search users", description = "Search for users by a username fragment")
    @ApiResponse(responseCode = "200", description = "List of matching users")
    @GetMapping("/search/{username}")
    public List<SearchResponseDto> searchUser (@PathVariable String username){
        return userService.searchUsername(username);
    }

    @Operation(summary = "Register user", description = "Creates a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto register(@RequestBody AuthRequestDto request){
        return userService.register(request);
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token plus a refresh cookie")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Refresh token", description = "Generates a new JWT access token using the refresh cookie")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "New token generated"),
        @ApiResponse(responseCode = "401", description = "Invalid or missing refresh token", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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

    @Operation(summary = "Update preferences", description = "Updates the authenticated user's preferences (timezone, notifications)")
    @ApiResponse(responseCode = "200", description = "Preferences updated successfully")
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
