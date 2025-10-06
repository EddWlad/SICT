package com.goia.sict_backend.comm;

import com.goia.sict_backend.dto.ProtocolMCommandDTO;
import com.goia.sict_backend.dto.ProtocolMCommandDTO.ChecksumMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum MCommandType {
    // ---- ASCII (no M) ----
    PING {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            // "PING\r\n" => 80,73,78,71,13,10
            List<Integer> payload = List.of(80,73,78,71,13,10);
            // No es M, por eso no se envuelve y solo esperamos ACK si el equipo lo usa (puedes forzar false vía args)
            boolean expectAck = getBoolArg(args, "expectAck", true);
            Integer expectBytes = getIntArg(args, "expectBytes", 0);
            Integer timeout = getIntArg(args, "readTimeoutMs", 2000);
            return base(payload, false, ChecksumMode.NONE, expectAck, expectBytes, timeout);
        }
    },

    // ---- Preset M que usamos en pruebas (con STX/ETX) ----
    MPING {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            // Ejemplo usado en pruebas: 22 dec dentro de STX/ETX
            List<Integer> payload = List.of(
                    128,217,134,128,128,133,128,131,133,137,
                    128,132,129,131,129,130,131,132,133,134,128,212
            );
            boolean expectAck = getBoolArg(args, "expectAck", true);
            // Si no indicas expectBytes, la impl leerá HASTA ETX
            Integer expectBytes = (Integer) args.getOrDefault("expectBytes", null);
            Integer timeout = getIntArg(args, "readTimeoutMs", 3000);
            return base(payload, true, ChecksumMode.NONE, expectAck, expectBytes, timeout);
        }
    },

    // ---- Taller: solo enviar (sin ACK y sin lectura) ----
    M_NOACK {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            List<Integer> payload = List.of(
                    128,217,134,128,128,133,128,131,133,137,
                    128,132,129,131,129,130,131,132,133,134,128,212
            );
            Integer timeout = getIntArg(args, "readTimeoutMs", 2000);
            return base(payload, true, ChecksumMode.NONE, /*expectAck*/ false, /*expectBytes*/ 0, timeout);
        }
    },

    // ---- Taller: espera ACK y termina (no lee datos) ----
    M_ACK_ONLY {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            // Puede ser el mismo payload; el objetivo es validar handshake ACK
            List<Integer> payload = List.of(
                    128,217,134,128,128,133,128,131,133,137,
                    128,132,129,131,129,130,131,132,133,134,128,212
            );
            Integer timeout = getIntArg(args, "readTimeoutMs", 3000);
            // wrapWithStxEtx=false para que la impl NO intente leer hasta ETX si expectBytes==null
            return base(payload, /*wrapWithStxEtx*/ false, ChecksumMode.NONE, /*expectAck*/ true, /*expectBytes*/ 0, timeout);
        }
    },

    // ---- Ejemplos que ya tenías (con overrides por args) ----
    GET_STATUS {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            List<Integer> payload = List.of(16, 1); // ficticio
            Integer expect = getIntArg(args, "expectBytes", 0);
            Integer timeout = getIntArg(args, "readTimeoutMs", 3000);
            return base(payload, true, ChecksumMode.XOR7, true, expect, timeout);
        }
    },

    GET_CLOCK {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            List<Integer> payload = List.of(32, 1); // ficticio
            Integer expect = getIntArg(args, "expectBytes", 7);
            Integer timeout = getIntArg(args, "readTimeoutMs", 3000);
            return base(payload, true, ChecksumMode.XOR7, true, expect, timeout);
        }
    },

    SET_CLOCK {
        @Override
        public ProtocolMCommandDTO build(Map<String, Object> args) {
            LocalDateTime dt = (LocalDateTime) args.getOrDefault("datetime", LocalDateTime.now());
            List<Integer> payload = new ArrayList<>();
            payload.add(0x21); // ficticio SET_CLOCK
            payload.add(dt.getYear() % 100);
            payload.add(dt.getMonthValue());
            payload.add(dt.getDayOfMonth());
            payload.add(dt.getHour());
            payload.add(dt.getMinute());
            payload.add(dt.getSecond());
            Integer timeout = getIntArg(args, "readTimeoutMs", 3000);
            return base(payload, true, ChecksumMode.XOR7, true, /*expectBytes*/ 0, timeout);
        }
    };

    public abstract ProtocolMCommandDTO build(Map<String, Object> args);

    // ---------- helpers ----------
    protected static ProtocolMCommandDTO base(
            List<Integer> payload,
            boolean wrapWithStxEtx,
            ChecksumMode checksum,
            boolean expectAck,
            Integer expectBytes,
            Integer readTimeoutMs
    ) {
        return new ProtocolMCommandDTO(payload, wrapWithStxEtx, checksum, expectAck, expectBytes, readTimeoutMs);
    }

    private static Integer getIntArg(Map<String, Object> args, String key, Integer def) {
        Object v = args != null ? args.get(key) : null;
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private static boolean getBoolArg(Map<String, Object> args, String key, boolean def) {
        Object v = args != null ? args.get(key) : null;
        if (v instanceof Boolean b) return b;
        return def;
    }
}
