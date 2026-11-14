package com.example.task.helpers.auth.service;

import com.example.task.helpers.auth.dto.AuthRequestDto;
import com.example.task.helpers.auth.dto.AdminResDto;
import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.auth.enums.Role;
import com.example.task.helpers.auth.exception.UserNotFoundException;
import com.example.task.helpers.auth.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdminService {
    @Autowired
    private UserRepo userRepo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public List<AdminResDto> getAllUsers(){
        return userRepo.findAll().stream().map(this::convertToResponse).toList();
    }

    public AdminResDto getUserByUsername (String username){
        return convertToResponse(
                userRepo.findByUsername(username)
                        .orElseThrow(()-> {
                            log.warn("Username {} not found", username);
                            return new UserNotFoundException("Username " + username + " not found");
                        })
        );
    }

    public AdminResDto getUserById (Long id){
        return convertToResponse(
                userRepo.findById(id)
                        .orElseThrow(()-> {
                            log.warn("User not found for id {}", id);
                            return new UserNotFoundException("User not found for id {}" + id);
                        })
        );
    }

    public AdminResDto makeAdmin(String username){
        User user = userRepo.findByUsername(username).orElseThrow(()->{
            log.warn("Username {} not found. Try crating a new admin user", username);
            return new UserNotFoundException("Username " + username + " not found");
        });
        user.setRole(Role.ADMIN);
        return convertToResponse(userRepo.save(user));
    }

    public AdminResDto makeAdmin(AuthRequestDto request){
        User user = user = new User(
                request.getUsername(),
                encoder.encode(request.getPassword()),
                request.getEmail(),
                request.getPfp(),
                Role.ADMIN
        );
        return convertToResponse(userRepo.save(user));
    }

    private AdminResDto convertToResponse(User user){
        return new AdminResDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPfp(),
                user.getRole() == Role.ADMIN ? "Admin" : "User"
        );
    }

}
