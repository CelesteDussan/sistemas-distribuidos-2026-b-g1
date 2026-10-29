package com.corhuila.edutrack.attendance.infrastructure.web;

import com.corhuila.edutrack.attendance.domain.model.AttendanceEvent;
import com.corhuila.edutrack.attendance.domain.model.AttendanceStatus;
import com.corhuila.edutrack.attendance.domain.port.in.GetAttendanceUseCase;
import com.corhuila.edutrack.attendance.domain.port.in.RegisterAttendanceUseCase;
import com.corhuila.edutrack.attendance.infrastructure.config.SecurityConfig;
import com.corhuila.edutrack.attendance.infrastructure.security.JwtAuthenticationFilter;
import com.corhuila.edutrack.attendance.infrastructure.security.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AttendanceAuthorizationTest {
    @Autowired MockMvc mvc;
    @MockBean JwtProvider jwt;
    @MockBean RegisterAttendanceUseCase register;
    @MockBean GetAttendanceUseCase queries;
    static final UUID ID = UUID.fromString("00000000-0000-4000-8000-000000000001");
    static final String BODY = """
        {"studentId":"00000000-0000-4000-8000-000000000001",
         "schoolId":"00000000-0000-4000-8000-000000000001",
         "teacherId":"00000000-0000-4000-8000-000000000001",
         "date":"2026-10-28","status":"ABSENT","sequenceNum":1}
        """;
    void principal(String role) {
        when(jwt.validateToken("fixture")).thenReturn(true);
        when(jwt.getAuthentication("fixture")).thenReturn(
            new UsernamePasswordAuthenticationToken(ID.toString(), null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))));
        when(register.registerAttendance(any(), any(), any(), anyString(), any(), any()))
            .thenReturn(AttendanceEvent.register(ID, ID, ID,
                LocalDate.of(2026, 10, 28), AttendanceStatus.ABSENT, ID, 1L));
    }
    @Test void parentCannotPostAttendance() throws Exception {
        principal("PARENT");
        mvc.perform(post("/api/v1/attendance").header("Authorization", "Bearer fixture")
            .contentType("application/json").content(BODY)).andExpect(status().isForbidden());
        verifyNoInteractions(register);
    }
    @Test void teacherRequestReachesTheWriteUseCase() throws Exception {
        principal("TEACHER");
        mvc.perform(post("/api/v1/attendance").header("Authorization", "Bearer fixture")
            .contentType("application/json").content(BODY)).andExpect(status().isCreated());
        verify(register).registerAttendance(any(), any(), any(), anyString(), any(), any());
    }
}
