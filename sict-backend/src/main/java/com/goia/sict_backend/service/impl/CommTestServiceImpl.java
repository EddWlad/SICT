package com.goia.sict_backend.service.impl;

import com.fazecast.jSerialComm.SerialPort;
import com.goia.sict_backend.dto.CommTestRequestDTO;
import com.goia.sict_backend.dto.CommTestResultDTO;
import com.goia.sict_backend.entity.CommProfile;
import com.goia.sict_backend.entity.embeddable.SerialParams;
import com.goia.sict_backend.entity.enums.InterfaceType;
import com.goia.sict_backend.service.ICommProfileService;
import com.goia.sict_backend.service.ICommTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Override
    public CommTestResultDTO testFrame(UUID idCommProfile, CommTestRequestDTO req) throws Exception {
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
                    byte[] buf = new byte[toRead];
                    int n = port.getInputStream().read(buf);
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
            String hex = req.getHexPayload().trim();
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
}
