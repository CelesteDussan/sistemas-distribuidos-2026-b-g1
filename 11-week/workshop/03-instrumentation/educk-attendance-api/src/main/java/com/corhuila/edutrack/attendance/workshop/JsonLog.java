package com.corhuila.edutrack.attendance.workshop;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.LinkedHashMap;
@Component
public class JsonLog {
    private final ObjectMapper mapper;
    public JsonLog(ObjectMapper mapper) { this.mapper = mapper; }
    public void write(TraceContext ctx, String level, String event, String message, double durationMs) {
        var fields = new LinkedHashMap<String, Object>();
        fields.put("timestamp", Instant.now().toString()); fields.put("level", level);
        fields.put("service", "attendance-service"); fields.put("version", "1.0.0-workshop");
        fields.put("environment", "dev"); fields.put("trace_id", ctx.traceId());
        fields.put("span_id", ctx.spanId()); fields.put("correlation_id", ctx.correlationId());
        fields.put("event", event); fields.put("message", message);
        fields.put("duration_ms", Math.max(0, durationMs));
        try { System.out.println(mapper.writeValueAsString(fields)); }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new IllegalStateException("Log serialization failed"); }
    }
}
