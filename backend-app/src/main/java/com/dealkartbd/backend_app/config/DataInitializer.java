package com.dealkartbd.backend_app.config;

import com.dealkartbd.backend_app.entity.Role;
import com.dealkartbd.backend_app.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ContextRefreshedEvent> {

    private final RoleRepository roleRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initRoles();
    }

    private void initRoles() {
        if (roleRepository.findByName(Role.RoleName.ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ADMIN);
            roleRepository.save(adminRole);
        }

        if (roleRepository.findByName(Role.RoleName.EMPLOYEE).isEmpty()) {
            Role employee = new Role();
            employee.setName(Role.RoleName.EMPLOYEE);
            roleRepository.save(employee);
        }
    }
}
