package com.example.task.helpers.auth.service;

import com.example.task.helpers.auth.entity.User;
import com.example.task.helpers.auth.entity.UserPrinciple;
import com.example.task.helpers.auth.exception.UserNotFoundException;
import com.example.task.helpers.auth.repo.UserRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MyUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username).orElseThrow(()->{
            log.debug("Username {} not found", username);
            return new UserNotFoundException("Username " + username + " not found");
        });
        log.debug("The user is {}", user.getUsername());
        return new UserPrinciple(user);
    }
}
