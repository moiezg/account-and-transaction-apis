package com.moiez.pismo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moiez.pismo.api.dto.request.CreateAccountRequest;
import com.moiez.pismo.api.dto.response.AccountResponse;
import com.moiez.pismo.exception.BadRequestException;
import com.moiez.pismo.exception.NotFoundException;
import com.moiez.pismo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
public class AccountControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountService accountService;

    @Test
    void shouldCreateAccount_return200() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("123");

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenReturn(AccountResponse.builder()
                                .id(1L)
                                .documentNumber("123")
                                .build());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("123"));
    }

    @Test
    void shouldReturn400_whenAccountAlreadyExists() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest("123");

        when(accountService.createAccount(any(CreateAccountRequest.class)))
                .thenThrow(new BadRequestException("Account already exists"));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetAccount_return200() throws Exception {
        when(accountService.getAccount(1L)).thenReturn(AccountResponse.builder()
                .id(1L)
                .documentNumber("12345678900")
                .build());

        mockMvc.perform(get("/accounts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.documentNumber").value("12345678900"));
    }

    @Test
    void shouldReturn404_whenAccountNotFound() throws Exception {
        when(accountService.getAccount(99L))
                .thenThrow(new NotFoundException("Account not found"));

        mockMvc.perform(get("/accounts/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}