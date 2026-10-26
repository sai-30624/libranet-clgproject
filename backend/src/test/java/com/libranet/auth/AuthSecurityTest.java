package com.libranet.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression tests for the authentication and authorization fixes.
 * Uses an in-memory H2 database (see src/test/resources/application-test.yml).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private JsonNode register(String email, String extraJson) throws Exception {
        String body = "{\"name\":\"Test User\",\"email\":\"" + email
                + "\",\"password\":\"secret123\"" + extraJson + "}";
        String response = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private String tokenFor(String email) throws Exception {
        return register(email, "").get("token").asText();
    }

    @Test
    void registerIgnoresRoleSentByClient() throws Exception {
        JsonNode response = register("eve@test.dev", ",\"role\":\"ROLE_ADMIN\"");

        assertThat(response.get("role").asText()).isEqualTo("ROLE_USER");
        assertThat(userRepository.findByEmail("eve@test.dev").orElseThrow().getRole())
                .isEqualTo(Role.ROLE_USER);
    }

    @Test
    void regularUserCannotReachAdminApi() throws Exception {
        String token = tokenFor("user@test.dev");

        mvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicPasswordResetEndpointNoLongerChangesPasswords() throws Exception {
        register("victim@test.dev", "");
        String hashBefore = userRepository.findByEmail("victim@test.dev").orElseThrow().getPassword();

        mvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"victim@test.dev\",\"newPassword\":\"hacked123\"}"))
                .andExpect(status().is4xxClientError());

        String hashAfter = userRepository.findByEmail("victim@test.dev").orElseThrow().getPassword();
        assertThat(hashAfter).isEqualTo(hashBefore);
    }

    @Test
    void changingPasswordRequiresCorrectCurrentPassword() throws Exception {
        String token = tokenFor("cp@test.dev");

        mvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong-password\",\"newPassword\":\"newsecret123\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(put("/api/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"secret123\",\"newPassword\":\"newsecret123\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"cp@test.dev\",\"password\":\"newsecret123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointRejectsMissingOrForgedToken() throws Exception {
        mvc.perform(get("/api/users/me"))
                .andExpect(status().is4xxClientError());

        mvc.perform(get("/api/users/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void registerRejectsShortPassword() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Shorty\",\"email\":\"short@test.dev\",\"password\":\"abc\"}"))
                .andExpect(status().isBadRequest());
    }
}
