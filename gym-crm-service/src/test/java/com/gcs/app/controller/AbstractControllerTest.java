package com.gcs.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcs.app.facade.GymFacade;
import com.gcs.app.security.JwtAuthFilter;
import com.gcs.app.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@TestPropertySource(properties = {"app.api.base-path=/gym-crm-core/api/v1", "metrics.enabled=false"})
public abstract class AbstractControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected GymFacade gymFacade;

    @MockitoBean
    protected JwtService jwtService;

    @MockitoBean
    protected JwtAuthFilter jwtAuthFilter;

    @Value("${app.api.base-path}")
    protected String basePath;
}