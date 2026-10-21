package com.corhuila.edutrack.attendance.workshop;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;
@RestController
public class AttendanceProbe {
    @PostMapping("/api/v1/attendance")
    public ResponseEntity<Map<String, String>> register() {
        return ResponseEntity.accepted().body(Map.of("mode", "trace-only", "eventType", "StudentAbsent"));
    }
}
