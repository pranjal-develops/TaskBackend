package com.example.task.helpers.auth.controller;

import com.example.task.helpers.auth.dto.AdminResDto;
import com.example.task.helpers.auth.dto.AuthRequestDto;
import com.example.task.helpers.auth.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public List<AdminResDto> getAllUsers (){
        return adminService.getAllUsers();
    }

    @GetMapping("/users/{username}")
    public AdminResDto getUser (@PathVariable String username){
        return adminService.getUserByUsername(username);
    }

    @PostMapping("/users/{username}")
    public AdminResDto makeAdmin(@PathVariable String username){
        return adminService.makeAdmin(username);
    }

    @PostMapping("/users/createAdmin")
    public AdminResDto createAdmin(@RequestBody AuthRequestDto request){
        return adminService.makeAdmin(request);
    }

}
