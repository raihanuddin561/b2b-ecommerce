package com.dealkartbd.backend_app.service.impl;

import com.dealkartbd.backend_app.dto.UserDto;
import com.dealkartbd.backend_app.repository.UserRepository;
import com.dealkartbd.backend_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    @Override
    public UserDto getUserByEmail(String email) {
    return userRepository.findByEmail(email)
            .map(user -> new ModelMapper().map(user, UserDto.class))
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
