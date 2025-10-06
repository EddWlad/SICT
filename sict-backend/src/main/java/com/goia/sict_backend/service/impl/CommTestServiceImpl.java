package com.goia.sict_backend.service.impl;

import com.fazecast.jSerialComm.SerialPort;
import com.goia.sict_backend.comm.MCommandType;
import com.goia.sict_backend.comm.ProtocolM;
import com.goia.sict_backend.dto.*;
import com.goia.sict_backend.entity.CommProfile;
import com.goia.sict_backend.entity.embeddable.SerialParams;
import com.goia.sict_backend.entity.enums.InterfaceType;
import com.goia.sict_backend.service.ICommProfileService;
import com.goia.sict_backend.service.ICommTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.goia.sict_backend.dto.CommFrameRequestDTO;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.TimeUnit;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;


@Service
@RequiredArgsConstructor
@Slf4j
public class CommTestServiceImpl implements ICommTestService {

    private final ICommProfileService commProfileService;

    @Override
    public CommTestResultDTO testConnection(UUID idCommProfile) throws Exception {
        CommProfile cp = commProfileService.findById(idCommProfile);
        if (cp.getStatus() != null && cp.getStatus() == 0) {
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType() != null ? cp.getInterfaceType().name() : null)
                    .elapsedMs(0)
                    .message("CommProfile está borrado (status=0)")
                    .details("Restaurar status para probar conexión")
                    .build();
        }

        if (cp.getInterfaceType() == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(null).elapsedMs(0)
                    .message("interfaceType es nulo").details(null).build();
        }

        return switch (cp.getInterfaceType()) {
            case RS232 -> testSerial(cp);
            default -> CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType().name())
                    .elapsedMs(0)
                    .message("Test no implementado para " + cp.getInterfaceType())
                    .details(null)
                    .build();
        };
    }

    // ----------------- PRIVADO: RS-232 DEC -----------------
    private CommTestResultDTO sendSerialDec(CommProfile cp, CommFrameRequestDTO req) {
        SerialParams sp = cp.getSerialParams();
        if (sp == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams es nulo").details(null).build();
        }

        String portName = nvl(sp.getPortName());
        Integer baud     = defaultIfNull(sp.getBaudRate(), 9600);
        Integer dataBits = defaultIfNull(sp.getDataBits(), 8);
        String parityS   = nvl(sp.getParity());      // "N","E","O","M","S"
        Integer stop     = defaultIfNull(sp.getStopBits(), 1);
        String flowS     = nvl(sp.getFlowControl()); // "NONE","RTSCTS","XONXOFF"

        int cfgReadTimeoutMs  = defaultIfNull(sp.getReadTimeoutMs(), 3000);
        int writeTimeoutMs    = defaultIfNull(sp.getWriteTimeoutMs(), 3000);
        int retries           = defaultIfNull(sp.getRetries(), 1);

        // overrides desde el request
        int readTimeoutMs = (req != null && req.readTimeoutMs() != null)
                ? req.readTimeoutMs()
                : cfgReadTimeoutMs;

        int expectBytes = (req != null && req.expectBytes() != null)
                ? Math.max(0, req.expectBytes())
                : 0; // 0 = lee lo disponible hasta timeout

        if (req == null || req.decimals() == null || req.decimals().isEmpty()) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("Lista DEC vacía").details(null).build();
        }
        if (portName.isBlank()) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams.portName vacío").details(null).build();
        }

        int parity   = mapParity(parityS);
        int stopBits = mapStopBits(stop);
        int flowCtrl = mapFlowControl(flowS);

        byte[] tx = bytesFromDecimals(req.decimals());

        Instant t0 = Instant.now();
        String lastError = null;

        for (int attempt = 1; attempt <= Math.max(1, retries); attempt++) {
            SerialPort port = null;
            try {
                port = SerialPort.getCommPort(portName);

                if (!port.setComPortParameters(baud, dataBits, stopBits, parity)) {
                    lastError = "No se pudieron aplicar parámetros al puerto";
                    log.warn("[RS232] setComPortParameters falló en {}", portName);
                    continue;
                }

                port.setFlowControl(flowCtrl);

                // Lectura BLOQUEANTE (más estable)
                port.setComPortTimeouts(
                        SerialPort.TIMEOUT_READ_BLOCKING,
                        readTimeoutMs,
                        writeTimeoutMs
                );

                if (!port.openPort()) {
                    lastError = "No se pudo abrir el puerto " + portName;
                    log.warn("[RS232] openPort() falló en {}", portName);
                    continue;
                }

                // Limpia buffers antes de usar
                purge(port);
                assertControlLines(port, true, true);
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}

                // -------- TX --------
                port.getOutputStream().write(tx);
                port.getOutputStream().flush();

                // Pequeño delay para dar tiempo a contestar (ajustable si hace falta)
                try { TimeUnit.MILLISECONDS.sleep(50); } catch (InterruptedException ignored) {}

                // -------- RX --------
                byte[] rx = (expectBytes > 0)
                        ? readExact(port, expectBytes, readTimeoutMs)   // lee N bytes o hasta timeout
                        : readUntilTimeout(port, readTimeoutMs, 4096);  // lee disponible hasta timeout (límite 4KB)

                // Validaciones opcionales
                boolean ok = true;
                StringBuilder msg = new StringBuilder("Trama enviada");
                if (rx.length == 0) {
                    ok = false;
                    msg = new StringBuilder("Sin respuesta (RX=0)");
                }
                if (ok && req.expectPrefix() != null && !req.expectPrefix().isEmpty()) {
                    if (!startsWith(rx, bytesFromDecimals(req.expectPrefix()))) {
                        ok = false;
                        msg = new StringBuilder("La respuesta no inicia con el prefijo esperado");
                    }
                }
                if (ok && req.expectContains() != null && !req.expectContains().isEmpty()) {
                    if (!containsSubsequence(rx, bytesFromDecimals(req.expectContains()))) {
                        ok = false;
                        msg = new StringBuilder("La respuesta no contiene el patrón esperado");
                    }
                }

                long elapsed = Duration.between(t0, Instant.now()).toMillis();
                return CommTestResultDTO.builder()
                        .ok(ok)
                        .interfaceType(InterfaceType.RS232.name())
                        .elapsedMs(elapsed)
                        .message(msg.toString())
                        .details("Port=" + portName + ", baud=" + baud + ", dataBits=" + dataBits
                                + ", parity=" + parityS + ", stopBits=" + stop + ", flow=" + flowS
                                + ", TX_DEC=" + toDecList(tx)
                                + ", RX_DEC=" + toDecList(rx) + " (len=" + rx.length + ")")
                        .build();

            } catch (Exception ex) {
                lastError = ex.getMessage();
                log.error("[RS232] Excepción en intento {}: {}", attempt, ex.toString());
            } finally {
                try { if (port != null && port.isOpen()) port.closePort(); }
                catch (Exception closeEx) { log.warn("[RS232] Error al cerrar puerto {}: {}", portName, closeEx.toString()); }
            }
        }

        long elapsed = Duration.between(t0, Instant.now()).toMillis();
        return CommTestResultDTO.builder()
                .ok(false)
                .interfaceType(InterfaceType.RS232.name())
                .elapsedMs(elapsed)
                .message("Fallo en envío/lectura")
                .details(lastError)
                .build();
    }

    // --------- Helpers DEC/lectura/validación ---------
    private static byte[] bytesFromDecimals(List<Integer> decs) {
        byte[] b = new byte[decs.size()];
        for (int i = 0; i < decs.size(); i++) {
            int v = (decs.get(i) == null ? 0 : decs.get(i)) & 0xFF;
            b[i] = (byte) v;
        }
        return b;
    }

    private static boolean startsWith(byte[] haystack, byte[] prefix) {
        if (prefix.length == 0 || haystack.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (haystack[i] != prefix[i]) return false;
        }
        return true;
    }

    private static boolean containsSubsequence(byte[] haystack, byte[] needle) {
        if (needle.length == 0) return true;
        outer: for (int i = 0; i <= haystack.length - needle.length; i++) {
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) continue outer;
            }
            return true;
        }
        return false;
    }

    private static String toDecList(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bytes.length; i++) {
            sb.append(bytes[i] & 0xFF);
            if (i < bytes.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /** Lee exactamente N bytes o hasta timeout (devuelve lo que alcanzó) */
    private static byte[] readExact(SerialPort port, int toRead, int timeoutMs) throws Exception {
        byte[] buf = new byte[toRead];
        int total = 0;
        long t0 = System.currentTimeMillis();
        InputStream in = port.getInputStream();
        while (total < toRead && (System.currentTimeMillis() - t0) < timeoutMs) {
            int n = in.read(buf, total, toRead - total); // bloquea (por TIMEOUT_READ_BLOCKING)
            if (n > 0) {
                total += n;
            } else {
                try { TimeUnit.MILLISECONDS.sleep(10); } catch (InterruptedException ignored) {}
            }
        }
        if (total == toRead) return buf;
        byte[] out = new byte[total];
        System.arraycopy(buf, 0, out, 0, total);
        return out;
    }

    /** Lee todo lo disponible hasta vencer timeout (límite superior para no desbordar) */
    private static byte[] readUntilTimeout(SerialPort port, int timeoutMs, int hardLimit) throws Exception {
        long t0 = System.currentTimeMillis();
        InputStream in = port.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((System.currentTimeMillis() - t0) < timeoutMs && out.size() < hardLimit) {
            int b = in.read(); // bloquea hasta que llegue 1 byte o venza el timeout interno
            if (b < 0) break;
            out.write(b);
            // pequeña pausa para agrupar ráfagas
            if (in.available() == 0) { try { TimeUnit.MILLISECONDS.sleep(5); } catch (InterruptedException ignored) {} }
        }
        return out.toByteArray();
    }

    @Override
    public CommTestResultDTO testFrame(UUID idCommProfile, CommFrameRequestDTO req) throws Exception {
        CommProfile cp = commProfileService.findById(idCommProfile);

        if (cp.getStatus() != null && cp.getStatus() == 0) {
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType() != null ? cp.getInterfaceType().name() : null)
                    .elapsedMs(0)
                    .message("CommProfile está borrado (status=0)")
                    .details("Restaurar status para probar")
                    .build();
        }
        if (cp.getInterfaceType() == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(null).elapsedMs(0)
                    .message("interfaceType es nulo").details(null).build();
        }

        return switch (cp.getInterfaceType()) {
            case RS232 -> sendSerialDec(cp, req);
            default -> CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType().name())
                    .elapsedMs(0)
                    .message("testFrame no implementado para " + cp.getInterfaceType())
                    .details(null)
                    .build();
        };
    }

    @Override
    public CommTestResultDTO sendMCommand(UUID idCommProfile, ProtocolMCommandDTO cmd) throws Exception {
        CommProfile cp = commProfileService.findById(idCommProfile);

        if (cp.getStatus() != null && cp.getStatus() == 0) {
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType() != null ? cp.getInterfaceType().name() : null)
                    .elapsedMs(0)
                    .message("CommProfile está borrado (status=0)")
                    .details("Restaurar status para probar")
                    .build();
        }
        if (cp.getInterfaceType() != InterfaceType.RS232) {
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType() != null ? cp.getInterfaceType().name() : null)
                    .elapsedMs(0)
                    .message("sendMCommand implementado solo para RS232 en este paso")
                    .details(null)
                    .build();
        }

        SerialParams sp = cp.getSerialParams();
        if (sp == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams es nulo").details(null).build();
        }

        // Parámetros del puerto
        final String portName = nvl(sp.getPortName());
        if (portName.isBlank()) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams.portName vacío").details(null).build();
        }

        final int baud           = defaultIfNull(sp.getBaudRate(), 115200);
        final int dataBits       = defaultIfNull(sp.getDataBits(), 8);
        final int stopBits       = mapStopBits(defaultIfNull(sp.getStopBits(), 1));
        final int parity         = mapParity(nvl(sp.getParity()));
        final int flowCtrl       = mapFlowControl(nvl(sp.getFlowControl()));
        final int retries        = Math.max(1, defaultIfNull(sp.getRetries(), 1));

        final int readTimeoutMs  = cmd.readTimeoutMs() != null ? cmd.readTimeoutMs() : defaultIfNull(sp.getReadTimeoutMs(), 3000);
        final int writeTimeoutMs = defaultIfNull(sp.getWriteTimeoutMs(), 3000);
        final int expectBytes    = cmd.expectBytes() != null ? Math.max(0, cmd.expectBytes()) : 0;
        final boolean expectAck  = cmd.expectAck() != null ? cmd.expectAck() : true;
        final int ackTimeoutMs   = Math.min(readTimeoutMs, 300); // ACK suele ser rápido

        // Construir TRAMA final (payload + STX/ETX + checksum si lo activas)
        var frameDec = ProtocolM.buildFrame(
                cmd.payloadDecimals(),
                cmd.wrapWithStxEtx(),
                cmd.checksumMode()
        );
        byte[] tx = bytesFromDecimals(frameDec);

        Instant t0 = Instant.now();
        SerialPort port = null;
        String lastError = null;
        java.util.ArrayList<Integer> rxAll = new java.util.ArrayList<>();

        try {
            port = SerialPort.getCommPort(portName);

            if (!port.setComPortParameters(baud, dataBits, stopBits, parity)) {
                return CommTestResultDTO.builder()
                        .ok(false)
                        .interfaceType(InterfaceType.RS232.name())
                        .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                        .message("No se pudieron aplicar parámetros al puerto")
                        .details("Port=" + portName)
                        .build();
            }
            port.setFlowControl(flowCtrl);
            // SEMI_BLOCKING nos sirve para ACK corto y lecturas posteriores
            port.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, readTimeoutMs, writeTimeoutMs);

            if (!port.openPort()) {
                return CommTestResultDTO.builder()
                        .ok(false)
                        .interfaceType(InterfaceType.RS232.name())
                        .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                        .message("No se pudo abrir el puerto " + portName)
                        .details(null).build();
            }

            purge(port);
            assertControlLines(port, true, true);
            try { Thread.sleep(20); } catch (InterruptedException ignored) {}

            for (int attempt = 1; attempt <= retries; attempt++) {
                // TX
                port.getOutputStream().write(tx);
                port.getOutputStream().flush();
                if (!expectAck && expectBytes == 0) {
                    long elapsed = Duration.between(t0, Instant.now()).toMillis();
                    return CommTestResultDTO.builder()
                            .ok(true)
                            .interfaceType(InterfaceType.RS232.name())
                            .elapsedMs(elapsed)
                            .message("Trama M enviada (sin esperar respuesta)")
                            .details("Port=" + portName + ", baud=" + baud + ", dataBits=" + dataBits
                                    + ", parity=" + nvl(sp.getParity()) + ", stopBits=" + defaultIfNull(sp.getStopBits(),1)
                                    + ", flow=" + nvl(sp.getFlowControl())
                                    + ", TX_DEC=" + frameDec + ", RX_DEC=[]")
                            .build();
                }

                // ACK/NAK (si aplica)
                if (expectAck) {
                    Integer first = readOneByte(port, ackTimeoutMs);
                    if (first == null) {
                        lastError = "No llegó ACK/NAK en " + ackTimeoutMs + "ms (intento " + attempt + ")";
                        if (attempt < retries) continue;
                        return CommTestResultDTO.builder()
                                .ok(false)
                                .interfaceType(InterfaceType.RS232.name())
                                .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                                .message(lastError)
                                .details("TX_DEC=" + frameDec + ", RX_DEC=" + rxAll)
                                .build();
                    }
                    if ((first & 0xFF) == ProtocolM.NAK) {
                        lastError = "NAK recibido (0x15) en intento " + attempt;
                        if (attempt < retries) continue;
                        return CommTestResultDTO.builder()
                                .ok(false)
                                .interfaceType(InterfaceType.RS232.name())
                                .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                                .message(lastError)
                                .details("TX_DEC=" + frameDec + ", RX_DEC=" + rxAll)
                                .build();
                    }
                    if ((first & 0xFF) != ProtocolM.ACK) {
                        // Algunos equipos no usan ACK y devuelven datos directo; conservamos ese byte
                        rxAll.add(first);
                    }
                }

                // Lectura de respuesta
                if (expectBytes > 0) {
                    byte[] rx = readExact(port, expectBytes, readTimeoutMs);
                    for (byte b : rx) rxAll.add(b & 0xFF);
                    if (rx.length < expectBytes) {
                        lastError = "Timeout lectura: esperados " + expectBytes + ", recibidos " + rx.length;
                        if (attempt < retries) {
                            try { while (port.bytesAvailable() > 0) port.getInputStream().read(); } catch (Exception ignore) {}
                            continue;
                        }
                        return CommTestResultDTO.builder()
                                .ok(false)
                                .interfaceType(InterfaceType.RS232.name())
                                .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                                .message(lastError)
                                .details("TX_DEC=" + frameDec + ", RX_DEC=" + rxAll)
                                .build();
                    }
                } else {
                    byte[] rx;
                    if (Boolean.TRUE.equals(cmd.wrapWithStxEtx())) {
                        // +++ NUEVO: si la respuesta M viene con STX/ETX y no sabes longitud, lee hasta ETX
                        rx = readUntilETX(port, readTimeoutMs);
                    } else {
                        var burst = readBurst(port, readTimeoutMs);
                        rx = new byte[burst.size()];
                        for (int i = 0; i < burst.size(); i++) rx[i] = (byte) (burst.get(i) & 0xFF);
                    }
                    for (byte b : rx) rxAll.add(b & 0xFF);
                }

                // Éxito
                long elapsed = Duration.between(t0, Instant.now()).toMillis();
                return CommTestResultDTO.builder()
                        .ok(true)
                        .interfaceType(InterfaceType.RS232.name())
                        .elapsedMs(elapsed)
                        .message("Trama enviada" + (expectAck ? " (ACK manejado)" : ""))
                        .details("Port=" + portName + ", baud=" + baud + ", dataBits=" + dataBits
                                + ", parity=" + nvl(sp.getParity()) + ", stopBits=" + defaultIfNull(sp.getStopBits(),1)
                                + ", flow=" + nvl(sp.getFlowControl())
                                + ", TX_DEC=" + frameDec
                                + ", RX_DEC=" + rxAll + " (len=" + rxAll.size() + ")")
                        .build();
            }

            // si agotó reintentos
            lastError = (lastError != null) ? lastError : "Se agotaron los reintentos";
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                    .message(lastError)
                    .details("TX_DEC=" + frameDec + ", RX_DEC=" + rxAll)
                    .build();

        } catch (Exception ex) {
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(Duration.between(t0, Instant.now()).toMillis())
                    .message("Excepción: " + ex.getMessage())
                    .details("TX_DEC=" + ProtocolM.buildFrame(cmd.payloadDecimals(), cmd.wrapWithStxEtx(), cmd.checksumMode())
                            + ", RX_DEC=" + rxAll)
                    .build();
        } finally {
            try { if (port != null && port.isOpen()) port.closePort(); } catch (Exception ignore) {}
        }
    }


    @Override
    public CommTestResultDTO testFrame1(UUID idCommProfile, CommTestRequestDTO req) throws Exception {
        CommProfile cp = commProfileService.findById(idCommProfile);

        if (cp.getStatus() != null && cp.getStatus() == 0) {
            return CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType() != null ? cp.getInterfaceType().name() : null)
                    .elapsedMs(0)
                    .message("CommProfile está borrado (status=0)")
                    .details("Restaurar status para probar conexión")
                    .build();
        }

        if (cp.getInterfaceType() == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(null).elapsedMs(0)
                    .message("interfaceType es nulo").details(null).build();
        }

        return switch (cp.getInterfaceType()) {
            case RS232 -> sendSerial(cp, req); // <- NUEVO
            default -> CommTestResultDTO.builder()
                    .ok(false)
                    .interfaceType(cp.getInterfaceType().name())
                    .elapsedMs(0)
                    .message("Test de trama no implementado para " + cp.getInterfaceType())
                    .details(null)
                    .build();
        };
    }

    // ========================= RS232 =========================
    private CommTestResultDTO testSerial(CommProfile cp) {
        SerialParams sp = cp.getSerialParams();
        if (sp == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams es nulo").details(null).build();
        }

        String portName = nvl(sp.getPortName());
        Integer baud    = defaultIfNull(sp.getBaudRate(), 9600);
        Integer dataBits= defaultIfNull(sp.getDataBits(), 8);
        String parityS  = nvl(sp.getParity());      // "N","E","O"
        Integer stop    = defaultIfNull(sp.getStopBits(), 1);
        String flowS    = nvl(sp.getFlowControl()); // "NONE","RTSCTS","XONXOFF"

        int readTimeoutMs  = defaultIfNull(sp.getReadTimeoutMs(), 3000);
        int writeTimeoutMs = defaultIfNull(sp.getWriteTimeoutMs(), 3000);
        int retries        = defaultIfNull(sp.getRetries(), 1);

        if (portName.isBlank()) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams.portName vacío").details(null).build();
        }

        int parity     = mapParity(parityS);
        int stopBits   = mapStopBits(stop);
        int flowCtrl   = mapFlowControl(flowS);

        Instant t0 = Instant.now();
        String lastError = null;

        for (int attempt = 1; attempt <= Math.max(1, retries); attempt++) {
            SerialPort port = null;
            try {
                port = SerialPort.getCommPort(portName);

                if (!port.setComPortParameters(baud, dataBits, stopBits, parity)) {
                    lastError = "No se pudieron aplicar parámetros al puerto";
                    log.warn("[RS232] setComPortParameters falló en {}", portName);
                    continue;
                }

                port.setFlowControl(flowCtrl);

                port.setComPortTimeouts(
                        SerialPort.TIMEOUT_READ_SEMI_BLOCKING,
                        readTimeoutMs,
                        writeTimeoutMs
                );

                boolean opened = port.openPort();
                if (!opened) {
                    lastError = "No se pudo abrir el puerto " + portName;
                    log.warn("[RS232] openPort() falló en {}", portName);
                    continue;
                }
                purge(port);
                assertControlLines(port, true, true);
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}

                try {
                    byte[] probe = "\r\n".getBytes(StandardCharsets.US_ASCII);
                    port.getOutputStream().write(probe);
                    port.getOutputStream().flush();
                } catch (Exception e) {
                    log.debug("[RS232] write probe falló (ignorable): {}", e.getMessage());
                }

                long elapsed = Duration.between(t0, Instant.now()).toMillis();
                return CommTestResultDTO.builder()
                        .ok(true)
                        .interfaceType(InterfaceType.RS232.name())
                        .elapsedMs(elapsed)
                        .message("Puerto abierto y configurado")
                        .details("Port=" + portName + ", baud=" + baud + ", dataBits=" + dataBits
                                + ", parity=" + parityS + ", stopBits=" + stop + ", flow=" + flowS)
                        .build();

            } catch (Exception ex) {
                lastError = ex.getMessage();
                log.error("[RS232] Excepción en intento {}: {}", attempt, ex.toString());
            } finally {
                // Cerrar si quedó abierto
                try {
                    if (port != null && port.isOpen()) {
                        port.closePort();
                    }
                } catch (Exception closeEx) {
                    log.warn("[RS232] Error al cerrar puerto {}: {}", portName, closeEx.toString());
                }
            }
        }

        long elapsed = Duration.between(t0, Instant.now()).toMillis();
        return CommTestResultDTO.builder()
                .ok(false)
                .interfaceType(InterfaceType.RS232.name())
                .elapsedMs(elapsed)
                .message("Fallo al abrir/configurar el puerto")
                .details(lastError)
                .build();
    }

    private static int mapParity(String p) {
        String v = nvl(p).toUpperCase(Locale.ROOT);
        return switch (v) {
            case "E", "EVEN"  -> SerialPort.EVEN_PARITY;
            case "O", "ODD"   -> SerialPort.ODD_PARITY;
            case "M", "MARK"  -> SerialPort.MARK_PARITY;
            case "S", "SPACE" -> SerialPort.SPACE_PARITY;
            default           -> SerialPort.NO_PARITY; // "N" o vacío
        };
    }

    private static int mapStopBits(Integer stop) {
        int s = defaultIfNull(stop, 1);
        return switch (s) {
            case 2  -> SerialPort.TWO_STOP_BITS;
            case 15 -> SerialPort.ONE_POINT_FIVE_STOP_BITS; // por si acaso
            default -> SerialPort.ONE_STOP_BIT;
        };
    }

    private static int mapFlowControl(String flow) {
        String v = nvl(flow).toUpperCase(Locale.ROOT);
        return switch (v) {
            case "RTSCTS", "HARDWARE" -> SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED;
            case "XONXOFF", "SOFTWARE"-> SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED;
            default                    -> SerialPort.FLOW_CONTROL_DISABLED; // NONE
        };
    }

    private static String nvl(String s) {
        return s == null ? "" : s.trim();
    }

    private static <T> T defaultIfNull(T v, T def) {
        return Objects.isNull(v) ? def : v;
    }

    private CommTestResultDTO sendSerial(CommProfile cp, CommTestRequestDTO req) {
        SerialParams sp = cp.getSerialParams();
        if (sp == null) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams es nulo").details(null).build();
        }

        String portName = nvl(sp.getPortName());
        Integer baud    = defaultIfNull(sp.getBaudRate(), 9600);
        Integer dataBits= defaultIfNull(sp.getDataBits(), 8);
        String parityS  = nvl(sp.getParity());
        Integer stop    = defaultIfNull(sp.getStopBits(), 1);
        String flowS    = nvl(sp.getFlowControl());

        int readTimeoutMs  = defaultIfNull(sp.getReadTimeoutMs(), 3000);
        int writeTimeoutMs = defaultIfNull(sp.getWriteTimeoutMs(), 3000);
        int retries        = defaultIfNull(sp.getRetries(), 1);

        if (req != null && req.getReadTimeoutMsOverride() != null) {
            readTimeoutMs = req.getReadTimeoutMsOverride();
        }
        int toRead = (req != null && req.getBytesToRead() != null) ? req.getBytesToRead() : 0;
        int delayBeforeReadMs = (req != null && req.getDelayBeforeReadMs() != null) ? req.getDelayBeforeReadMs() : 0;

        if (portName.isBlank()) {
            return CommTestResultDTO.builder()
                    .ok(false).interfaceType(InterfaceType.RS232.name())
                    .elapsedMs(0).message("serialParams.portName vacío").details(null).build();
        }

        int parity   = mapParity(parityS);
        int stopBits = mapStopBits(stop);
        int flowCtrl = mapFlowControl(flowS);

        Instant t0 = Instant.now();
        String lastError = null;

        for (int attempt = 1; attempt <= Math.max(1, retries); attempt++) {
            SerialPort port = null;
            try {
                port = SerialPort.getCommPort(portName);

                if (!port.setComPortParameters(baud, dataBits, stopBits, parity)) {
                    lastError = "No se pudieron aplicar parámetros al puerto";
                    continue;
                }

                port.setFlowControl(flowCtrl);
                port.setComPortTimeouts(
                        (toRead > 0) ? SerialPort.TIMEOUT_READ_SEMI_BLOCKING
                                : SerialPort.TIMEOUT_NONBLOCKING,
                        readTimeoutMs,
                        writeTimeoutMs
                );

                if (!port.openPort()) {
                    lastError = "No se pudo abrir el puerto " + portName;
                    continue;
                }
                purge(port);
                assertControlLines(port, true, true);
                try { Thread.sleep(20); } catch (InterruptedException ignored) {}

                // --- ENVIAR (sin probe automático) ---
                byte[] out = buildPayload(req);
                if (out != null && out.length > 0) {
                    port.getOutputStream().write(out);
                    port.getOutputStream().flush();
                }

                // --- DELAY opcional antes de leer ---
                if (toRead > 0 && delayBeforeReadMs > 0) {
                    try { TimeUnit.MILLISECONDS.sleep(delayBeforeReadMs); } catch (InterruptedException ignored) {}
                }

                // --- LEER (si se solicitó) ---
                String rxAscii = "";
                if (toRead > 0) {
                    byte[] buf = readExact(port, toRead, readTimeoutMs); // lectura bloqueante y confiable
                    int n = buf.length;
                    if (n > 0) {
                        rxAscii = new String(buf, 0, n, StandardCharsets.US_ASCII);
                    }
                }

                // --- Validar patrón opcional ---
                if (req != null && req.getExpectAsciiContains() != null) {
                    boolean ok = rxAscii.contains(req.getExpectAsciiContains());
                    long elapsed = Duration.between(t0, Instant.now()).toMillis();
                    return CommTestResultDTO.builder()
                            .ok(ok)
                            .interfaceType(InterfaceType.RS232.name())
                            .elapsedMs(elapsed)
                            .message(ok ? "Echo/Respuesta válida" : "No se encontró el patrón esperado")
                            .details("TX=" + describePayload(req) + ", RX=" + printable(rxAscii))
                            .build();
                }

                long elapsed = Duration.between(t0, Instant.now()).toMillis();
                return CommTestResultDTO.builder()
                        .ok(true)
                        .interfaceType(InterfaceType.RS232.name())
                        .elapsedMs(elapsed)
                        .message("Trama enviada" + (toRead > 0 ? " y leída" : ""))
                        .details("Port=" + portName + ", baud=" + baud + ", dataBits=" + dataBits
                                + ", parity=" + parityS + ", stopBits=" + stop + ", flow=" + flowS
                                + ", TX=" + describePayload(req)
                                + (toRead > 0 ? ", RX=" + printable(rxAscii) : ""))
                        .build();

            } catch (Exception ex) {
                lastError = ex.getMessage();
            } finally {
                try { if (port != null && port.isOpen()) port.closePort(); } catch (Exception ignore) {}
            }
        }

        long elapsed = Duration.between(t0, Instant.now()).toMillis();
        return CommTestResultDTO.builder()
                .ok(false)
                .interfaceType(InterfaceType.RS232.name())
                .elapsedMs(elapsed)
                .message("Fallo en envío/lectura")
                .details(lastError)
                .build();
    }

    // Helpers para payload/strings (si no los tienes ya):
    private static byte[] buildPayload(CommTestRequestDTO req) {
        if (req == null) return null;
        if (req.getHexPayload() != null && !req.getHexPayload().isBlank()) {
            String hex = req.getHexPayload()
                    .replace(" ", "")
                    .replace("0x","")
                    .replace("0X","")
                    .trim();
            int len = hex.length();
            if (len % 2 != 0) return null;
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
            return data;
        }
        if (req.getAsciiPayload() != null) {
            return req.getAsciiPayload().getBytes(StandardCharsets.US_ASCII);
        }
        return null;
    }
    private static String describePayload(CommTestRequestDTO req) {
        if (req.getHexPayload() != null && !req.getHexPayload().isBlank()) {
            return "HEX:" + req.getHexPayload();
        }
        if (req.getAsciiPayload() != null) {
            return "ASCII:" + req.getAsciiPayload().replace("\r", "\\r").replace("\n", "\\n");
        }
        return "(none)";
    }
    private static String printable(String s) {
        if (s == null) return "";
        return s.replace("\r", "\\r").replace("\n", "\\n");
    }
    // Lee 1 byte (0..255) con timeout suave. Devuelve null si no llega nada.
    /*private Integer readOneByte(SerialPort port, int timeoutMs) throws Exception {
        final long deadline = System.currentTimeMillis() + Math.max(1, timeoutMs);
        final var in = port.getInputStream();

        while (System.currentTimeMillis() < deadline) {
            int b = in.read();                 // con TIMEOUT_READ_BLOCKING devolverá -1 o esperará el interno
            if (b >= 0) {
                return b & 0xFF;
            }
            // pequeño respiro para no ciclar la CPU si no hay dato aún
            try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        }
        return null; // timeout sin recibir nada
    }*/

    private java.util.List<Integer> readBurst(SerialPort port, int timeoutMs) throws Exception {
        final long deadline = System.currentTimeMillis() + Math.max(1, timeoutMs);
        final var in = port.getInputStream();
        final var out = new java.util.ArrayList<Integer>(64);

        // Tiempo de calma para dar por cerrada la ráfaga (ajustable)
        final int quietGapMs = 20;

        while (System.currentTimeMillis() < deadline) {
            // si hay al menos 1 byte disponible, leer todo lo disponible en esta pasada
            int available = port.bytesAvailable();
            if (available > 0) {
                byte[] buf = new byte[available];
                int n = in.read(buf);
                for (int i = 0; i < n; i++) {
                    out.add(buf[i] & 0xFF);
                }
                // tras leer un chunk, espera un pequeño periodo a ver si llegan más bytes de la misma ráfaga
                long quietDeadline = System.currentTimeMillis() + quietGapMs;
                while (System.currentTimeMillis() < quietDeadline) {
                    int more = port.bytesAvailable();
                    if (more > 0) break; // llegó más, volvemos al while principal para leerlo
                    try { Thread.sleep(2); } catch (InterruptedException ignored) {}
                }
            } else {
                // nada disponible; espera un poco antes de volver a chequear hasta vencer el timeout
                try { Thread.sleep(5); } catch (InterruptedException ignored) {}
            }
        }
        return out;
    }

    public CommTestResultDTO sendMCommandType(UUID idCommProfile, MCommandType type, Map<String,Object> args) throws Exception {
        ProtocolMCommandDTO dto = type.build(args == null ? Map.of() : args);
        return this.sendMCommand(idCommProfile, dto);
    }

    // Limpia buffers y baja líneas de control (pre-apertura segura)
    private static void purge(SerialPort port) {
        try { port.clearRTS(); port.clearDTR(); } catch (Throwable ignored) {}
        try {
            var in = port.getInputStream();
            while (port.bytesAvailable() > 0) { in.read(); }
        } catch (Exception ignored) {}
    }

    // Sube/baja RTS/DTR según se necesite
    private static void assertControlLines(SerialPort port, boolean rts, boolean dtr) {
        try { if (rts) port.setRTS(); else port.clearRTS(); } catch (Throwable ignored) {}
        try { if (dtr) port.setDTR(); else port.clearDTR(); } catch (Throwable ignored) {}
    }

    // Lectura hasta ETX (0x03) o timeout, útil para Protocolo M cuando no conoces la longitud
    private static byte[] readUntilETX(SerialPort port, int overallTimeoutMs) throws Exception {
        var out = new java.io.ByteArrayOutputStream();
        long end = System.currentTimeMillis() + Math.max(1, overallTimeoutMs);
        var in = port.getInputStream();
        while (System.currentTimeMillis() < end) {
            int b = in.read();
            if (b >= 0) {
                out.write(b);
                if ((b & 0xFF) == ProtocolM.ETX) break;
            } else {
                Thread.sleep(5);
            }
        }
        return out.toByteArray();
    }

    // Lectura de 1 byte con timeout corto (ACK/NAK)
    private static Integer readOneByte(SerialPort port, int timeoutMs) throws Exception {
        long end = System.currentTimeMillis() + Math.max(1, timeoutMs);
        var in = port.getInputStream();
        while (System.currentTimeMillis() < end) {
            int n = in.read();
            if (n >= 0) return n & 0xFF;
            Thread.sleep(5);
        }
        return null;
    }

}
