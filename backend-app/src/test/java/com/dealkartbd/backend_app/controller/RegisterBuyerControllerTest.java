package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.RegisterBuyerRequest;
import com.dealkartbd.backend_app.service.RegisterBuyerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RegisterBuyerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RegisterBuyerService registerBuyerService;

    @InjectMocks
    private RegisterBuyerController registerBuyerController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(registerBuyerController).build();
    }

    @Test
    void registerBuyer_success() throws Exception {
        // Given
        doNothing().when(registerBuyerService).registerBuyer(any(RegisterBuyerRequest.class));

        String requestJson = "{" +
                "\"fullName\":\"Test Buyer\"," +
                "\"email\":\"buyer@example.com\"," +
                "\"password\":\"password123\"," +
                "\"phone\":\"+1234567890\"," +
                "\"addressStreet\":\"123 Test St\"," +
                "\"addressCity\":\"Test City\"," +
                "\"addressState\":\"Test State\"," +
                "\"addressPostalCode\":\"12345\"," +
                "\"addressCountry\":\"Test Country\"}";

        // When & Then
        mockMvc.perform(post("/api/auth/buyers/register")  // Fixed URL to match controller mapping
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Registration successful. Please check your email to confirm your account."));  // Updated message to match controller

        verify(registerBuyerService).registerBuyer(any(RegisterBuyerRequest.class));
    }
}
