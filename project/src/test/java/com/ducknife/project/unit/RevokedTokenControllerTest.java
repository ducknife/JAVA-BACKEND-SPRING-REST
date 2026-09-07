package com.ducknife.project.unit;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ducknife.project.modules.token.RevokedToken;
import com.ducknife.project.modules.token.RevokedTokenController;
import com.ducknife.project.modules.token.RevokedTokenService;

@WebMvcTest(RevokedTokenController.class)
public class RevokedTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RevokedTokenService revokedTokenService;

    @Test
    @DisplayName("Lấy danh sách revoked tokens")
    @WithMockUser
    public void layDanhSachRevokedToken() throws Exception {

        // LocalDate date = LocalDate.of(2026, 9, 3);
        // LocalTime time = LocalTime.of(8, 0);

        // LocalDateTime dateTime = new LocalDateTime(date, time);

        RevokedToken token = RevokedToken.builder()
                .jti("REVOKED TOKEN TEST") // 18
                .expiresAt(LocalDateTime.now())
                .build();

        List<RevokedToken> tokens = List.of(token);

        // System.out.println(tokens.get(0));

        when(revokedTokenService.getRevokedTokens()).thenReturn(tokens);

        mockMvc.perform(get("/api/revoked-tokens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].jti").value("REVOKED TOKEN TEST"));
    }

    @Test
    @WithMockUser
    public void truyCapDuongUrlKhongTonTai() throws Exception {

        mockMvc.perform(get("/api/revoked-tokens/1"))
                .andExpect(status().isNotFound());
    }

}
