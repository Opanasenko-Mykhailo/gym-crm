package com.gcc.app.controller.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gcc.app.facade.GymFacade;
import com.gcc.app.security.BruteForceProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class AbstractSecurityControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected GymFacade gymFacade;

    @MockitoBean
    protected BruteForceProtectionService bruteForceService;

    @Value("${app.api.base-path}")
    protected String basePath;
}