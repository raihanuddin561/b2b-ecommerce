package com.dealkartbd.backend_app.controller;

import com.dealkartbd.backend_app.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Debug test to see actual response format
 */
@WebMvcTest(FallbackController.class)
@Import(GlobalExceptionHandler.class)
class DebugWrongPathTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void debugResponseFormat() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/nonexistent"))
                .andReturn();

        System.out.println("Status: " + result.getResponse().getStatus());
        System.out.println("Response Body: " + result.getResponse().getContentAsString());
        System.out.println("Content Type: " + result.getResponse().getContentType());
    }
}
