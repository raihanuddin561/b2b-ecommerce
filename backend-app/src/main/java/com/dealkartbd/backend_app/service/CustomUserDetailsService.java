package com.dealkartbd.backend_app.service;

import com.dealkartbd.backend_app.entity.AccountStatus;
import com.dealkartbd.backend_app.entity.User;
import com.dealkartbd.backend_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static com.dealkartbd.backend_app.exception.ErrorMessages.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND.getMessage()));

        boolean accountNonLocked = !user.isLocked() &&
            (user.getLockedUntil() == null || user.getLockedUntil().isBefore(LocalDateTime.now()));

        boolean enabled = user.isEnabled() && user.getAccountStatus() == AccountStatus.ACTIVE;

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            enabled,
            true, // accountNonExpired
            true, // credentialsNonExpired
            accountNonLocked,
            user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList())
        );
    }
}
