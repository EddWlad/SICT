package com.goia.sict_backend.comm;

import com.goia.sict_backend.dto.ProtocolMCommandDTO.ChecksumMode;

import java.util.ArrayList;
import java.util.List;

public final class ProtocolM {
    private ProtocolM() {}

    public static final int STX = 2;  // 0x02
    public static final int ETX = 3;  // 0x03
    public static final int ACK = 6;  // 0x06
    public static final int NAK = 0x15;

    public static boolean isAck(int b) { return (b & 0xFF) == ACK; }
    public static boolean isNak(int b) { return (b & 0xFF) == NAK; }

    public static List<Integer> buildFrame(
            List<Integer> payload, boolean wrapWithStxEtx, ChecksumMode mode) {

        if (payload == null) payload = List.of();

        List<Integer> out = new ArrayList<>();
        if (wrapWithStxEtx) out.add(STX);

        out.addAll(onlyByteRange(payload));

        // checksum opcional (entre STX y ETX; confirmar con doc)
        switch (mode == null ? ChecksumMode.NONE : mode) {
            case XOR7 -> out.add(xor7(payload));
            case SUM8 -> out.add(sum8(payload));
            case NONE -> { /* nada */ }
        }

        if (wrapWithStxEtx) out.add(ETX);
        return out;
    }

    /** Restringe a 0..255 */
    private static List<Integer> onlyByteRange(List<Integer> in) {
        List<Integer> out = new ArrayList<>(in.size());
        for (Integer v : in) out.add(((v == null ? 0 : v) & 0xFF));
        return out;
    }

    /** XOR de todos los bytes, enmascarado 0x7F (común en varias variantes). */
    public static int xor7(List<Integer> payload) {
        int x = 0;
        for (Integer v : payload) x ^= (v == null ? 0 : v) & 0xFF;
        return x & 0x7F;
    }

    /** Suma 8-bit (módulo 256). */
    public static int sum8(List<Integer> payload) {
        int s = 0;
        for (Integer v : payload) s = (s + ((v == null ? 0 : v) & 0xFF)) & 0xFF;
        return s;
    }

}
