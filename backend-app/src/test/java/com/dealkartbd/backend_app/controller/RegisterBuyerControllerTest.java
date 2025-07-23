package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.dto.RegisterBuyerRequest;
import com.dealkartbd.backend_app.service.RegisterBuyerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(RegisterBuyerController.class)
class RegisterBuyerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterBuyerService registerBuyerService;

    @Test
    void registerBuyer_success() throws Exception {
        String requestJson = "{" +
                "\"fullName\":\"Test Buyer\"," +
                "\"email\":\"buyer@example.com\"," +
                "\"password\":\"password123\"}";

        mockMvc.perform(post("/api/buyers/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Registration successful")));

        Mockito.verify(registerBuyerService).registerBuyer(Mockito.any(RegisterBuyerRequest.class));
    }
}

