package com.corhuila.edutrack.attendance.workshop;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.UUID;
public record TraceContext(String traceId, String spanId, String flags, String correlationId) {
    public static final String ATTRIBUTE = "attendance.trace";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static String id(int bytes) {
        byte[] value = new byte[bytes]; String result;
        do { RANDOM.nextBytes(value); result = HexFormat.of().formatHex(value); }
        while (result.equals("0".repeat(bytes * 2)));
        return result;
    }
    public static TraceContext incoming(String header, String correlation) {
        boolean valid = header != null && header.matches("00-[0-9a-f]{32}-[0-9a-f]{16}-[0-9a-f]{2}")
            && !header.substring(3, 35).equals("0".repeat(32))
            && !header.substring(36, 52).equals("0".repeat(16));
        String businessId;
        try {
            businessId = UUID.fromString(correlation).toString();
            if (!businessId.equals(correlation)) throw new IllegalArgumentException();
        } catch (IllegalArgumentException | NullPointerException ignored) { businessId = UUID.randomUUID().toString(); }
        return new TraceContext(valid ? header.substring(3, 35) : id(16), id(8),
            valid ? header.substring(53, 55) : "01", businessId);
    }
    public TraceContext child() { return new TraceContext(traceId, id(8), flags, correlationId); }
    public String traceparent() { return "00-" + traceId + "-" + spanId + "-" + flags; }
}
