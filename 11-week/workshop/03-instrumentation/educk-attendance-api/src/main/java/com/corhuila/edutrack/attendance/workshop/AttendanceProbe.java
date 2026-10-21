package com.corhuila.edutrack.attendance.workshop;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.amqp.AmqpException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;
@RestController
public class AttendanceProbe {
    public record Absence(UUID studentId, UUID sessionId, LocalDate sessionDate, String status, long sequenceNumber) {}
    private final AbsencePublisher publisher; private final ObjectMapper mapper;
    public AttendanceProbe(AbsencePublisher publisher, ObjectMapper mapper) { this.publisher = publisher; this.mapper = mapper; }
    @PostMapping("/api/v1/attendance")
    public ResponseEntity<Map<String, String>> register(@RequestBody Absence input, HttpServletRequest request) throws java.io.IOException {
        if (input.studentId() == null || input.sessionId() == null || input.sessionDate() == null
                || input.sessionDate().isAfter(LocalDate.now(java.time.ZoneOffset.UTC))
                || !"ABSENT".equals(input.status()) || input.sequenceNumber() < 1)
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_absence"));
        var ctx = (TraceContext) request.getAttribute(TraceContext.ATTRIBUTE);
        var metadata = new LinkedHashMap<String, Object>(); metadata.put("correlationId", ctx.correlationId());
        metadata.put("causationId", UUID.randomUUID().toString()); metadata.put("actorId", null); metadata.put("orderingKey", input.studentId());
        var payload = new LinkedHashMap<String, Object>(); payload.put("studentId", input.studentId()); payload.put("sessionId", input.sessionId());
        payload.put("sessionDate", input.sessionDate().toString()); payload.put("status", "ABSENT"); payload.put("reason", null); payload.put("sequenceNumber", input.sequenceNumber());
        var event = Map.of("eventId", UUID.randomUUID().toString(), "eventType", "StudentAbsent", "aggregateId", UUID.randomUUID().toString(),
            "aggregateType", "AttendanceRecord", "occurredAt", Instant.now().toString(), "version", 1, "payload", payload, "metadata", metadata);
        try { publisher.publish(mapper.writeValueAsBytes(event), ctx); }
        catch (AmqpException e) { return ResponseEntity.status(503).body(Map.of("error", "broker_unavailable")); }
        return ResponseEntity.accepted().body(Map.of("mode", "publication-probe", "eventType", "StudentAbsent"));
    }
}
