package com.gcc.app.controller.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithAnonymousUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SwaggerSecurityControllerTest extends AbstractSecurityControllerTest {

    @Test
    @WithAnonymousUser
    void openapi_shouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get(basePath + "/openapi"))
                .andExpect(status().isOk());
    }
}