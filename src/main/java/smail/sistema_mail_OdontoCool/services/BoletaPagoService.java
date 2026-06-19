package smail.sistema_mail_OdontoCool.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.DTO.CrearQR;
import smail.sistema_mail_OdontoCool.DTO.RespuestaQR;
import smail.sistema_mail_OdontoCool.DTO.RespuestaEstadoQR;
import smail.sistema_mail_OdontoCool.entities.ServicioPrestado;
import smail.sistema_mail_OdontoCool.entities.MetodoPago;
import smail.sistema_mail_OdontoCool.entities.BoletaPago;
import smail.sistema_mail_OdontoCool.entities.CuotaBoleta;
import smail.sistema_mail_OdontoCool.entities.CuotaMulta;
import smail.sistema_mail_OdontoCool.entities.ModoPago;
import smail.sistema_mail_OdontoCool.entities.Paciente;
import smail.sistema_mail_OdontoCool.entities.Secretaria;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.BoletaPagoRepository;
import smail.sistema_mail_OdontoCool.repositories.CuotaBoletaRepository;
import smail.sistema_mail_OdontoCool.repositories.CuotaMultaRepository;
import smail.sistema_mail_OdontoCool.repositories.MetodoPagoRespository;
import smail.sistema_mail_OdontoCool.repositories.ModoPagoRespository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioPrestadoRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class BoletaPagoService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SecretariaRepository secretariaRepository;

    @Autowired
    private ModoPagoRespository modoPagoRespository;

    @Autowired
    private BoletaPagoRepository boletaPagoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PagoFacilService pagoFacilService;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private CuotaBoletaRepository cuotaBoletaRepository;

    @Autowired
    private CuotaMultaRepository cuotaMultaRepository;

    @Autowired
    private ServicioPrestadoRepository servicioPrestadoRepository;

    @Autowired
    private MetodoPagoRespository metodoPagoRespository;

    @Autowired
    private CloudinaryServices cloudinaryServices;

    @Transactional
    public void handle(String action, List<String> params, String fromEmail, List<String> imagenesBase64) {
        switch (action) {
            case "GEN":
                generate(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "EVC":
                enviarComprobante(params, fromEmail, imagenesBase64);
                break;
            case "GPC":
                generarPagoCuota(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Boleta de Pago.");
        }
    }

    @Transactional
    private void generate(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parámetros: Descuento[0], DetallesRaw[1], CI_Paciente[2], CI_Secretaria[3],
            // Nombre_ModoPago[4], CantCuotas[5], PlazoCuota[6], MetodoPago[7],
            if (params.size() < 8) {
                sendResponse(fromEmail, "Error",
                        "Error al registrar Boleta de Pago: Parámetros insuficientes. Se requieren 8.");
                return;
            }

            double descuento = Double.parseDouble(params.get(0));
            int cantCuotas = Integer.parseInt(params.get(5));
            String plazoCuota = params.get(6);

            Paciente paciente = pacienteRepository.findById(params.get(2)).orElse(null);
            if (paciente == null) {
                sendResponse(fromEmail, "Error", "El paciente con CI " + params.get(2) + " no existe.");
                return;
            }

            Secretaria secretaria = secretariaRepository.findById(params.get(3)).orElse(null);
            if (secretaria == null) {
                sendResponse(fromEmail, "Error", "La secretaria con CI " + params.get(3) + " no existe.");
                return;
            }

            String modoPagoParam = params.get(4).trim();
            ModoPago modoPago = modoPagoRespository.findByNombre(modoPagoParam);

            if (modoPago == null) {
                sendResponse(fromEmail, "Error", "El modo de pago '" + modoPagoParam + "' no existe.");
                return;
            }

            String metodoPagoParam = params.get(7);
            MetodoPago metodoPago = metodoPagoRespository.findByNombre(metodoPagoParam);

            if (metodoPago == null) {
                sendResponse(fromEmail, "Error", "El metodo de pago '" + metodoPagoParam + "' no existe.");
                return;
            }

            // Validaciones necesarias

            // String validationMsg = boletaPagoVal.insertValid(params);
            // if (!validationMsg.isEmpty()) {
            // sendResponse(fromEmail, "Error en la Validación", validationMsg);
            // return;
            // }

            BoletaPago boletaPago = new BoletaPago();
            boletaPago.setDescuento(descuento);
            boletaPago.setFechaEmicion(LocalDate.now());
            boletaPago.setEstadoPago("PENDIENTE");
            boletaPago.setPaciente(paciente);
            boletaPago.setSecretaria(secretaria);
            boletaPago.setModoPago(modoPago);

            List<ServicioPrestado> serviciosPrestados = new ArrayList<>();
            String detailsRaw = params.get(1);
            double totalDetalles = 0.0;

            if (detailsRaw != null && !detailsRaw.trim().isEmpty()) {
                String[] detailsArray = detailsRaw.split("\\s*\\|\\s*");
                for (String idStr : detailsArray) {
                    if (idStr.trim().isEmpty()) {
                        continue;
                    }
                    try {
                        Long spId = Long.parseLong(idStr.trim());
                        ServicioPrestado sp = servicioPrestadoRepository.findById(spId).orElse(null);
                        if (sp == null) {
                            sendResponse(fromEmail, "Error", "El servicio prestado con ID " + spId + " no existe.");
                            return;
                        }
                        serviciosPrestados.add(sp);
                        if (sp.getSubtotal() != null) {
                            totalDetalles += sp.getSubtotal().doubleValue();
                        }
                    } catch (NumberFormatException e) {
                        sendResponse(fromEmail, "Error", "ID de servicio prestado inválido: " + idStr);
                        return;
                    }
                }
            }

            boletaPago.setTotal(totalDetalles - descuento);

            Usuario user = usuarioRepository.findByPersonaCiAndSuffix(paciente.getCi(), "PAC").orElse(null);
            if (user == null) {
                sendResponse(fromEmail, "Error", "El usuario con CI " + paciente.getCi() + " no existe.");
                return;
            }

            // Guardamos la boleta de pago primero para que obtenga su ID autogenerado
            boletaPago = boletaPagoRepository.save(boletaPago);

            // Asociamos cada servicio prestado con la boleta guardada y los actualizamos
            for (ServicioPrestado sp : serviciosPrestados) {
                sp.setBoletaPago(boletaPago);
                servicioPrestadoRepository.save(sp);
            }

            // Asignamos la lista de servicios prestados a la boleta y guardamos la relación bidireccional
            boletaPago.setServiciosPrestados(serviciosPrestados);
            boletaPago = boletaPagoRepository.save(boletaPago);

            // ==============================================================================
            // CREAR MAS DE UNA CUOTA SI ES AL CREDITO Y SOLO CREAR 1 CUOTA SI ES AL CONTADO
            // ==============================================================================
            double montoCuota = (modoPago.getNombre().equals("Crédito")) ? (boletaPago.getTotal() / cantCuotas)
                    : (modoPago.getNombre().equals("Contado")) ? (boletaPago.getTotal())
                            : (0);
            int dias = (plazoCuota.equals("MES")) ? (30)
                    : (plazoCuota.equals("SEMANA")) ? (7)
                            : (0);

            String primeraCuota = "";
            for (int i = 0; i < cantCuotas; i++) {
                CuotaBoleta cuota = new CuotaBoleta();
                cuota.setNumeroCuota(i + 1);
                cuota.setMontoCuota(montoCuota);
                cuota.setBoletaPago(boletaPago);
                cuota.setEstado("PENDIENTE");
                cuotaBoletaRepository.save(cuota);
                if (i == 0) {
                    cuota.setFechaVencimiento(LocalDate.now().plusDays(0));
                    primeraCuota = cuota.getIdCuota().toString();
                } else {
                    cuota.setFechaVencimiento(LocalDate.now().plusDays(dias));
                }
                cuotaBoletaRepository.save(cuota);
            }

            // ============================================
            // PROCESAMIENTO DE PAGO (QR)
            // ============================================
            RespuestaQR response = procesarPago(metodoPago.getNombre(), paciente, user, primeraCuota, montoCuota);

            CuotaBoleta cuotaBoleta = cuotaBoletaRepository.findById(UUID.fromString(primeraCuota)).orElse(null);
            if (cuotaBoleta != null) {
                cuotaBoleta.setId_transaccion(Long.valueOf(response.getValues().getTransactionId()));
                cuotaBoleta.setMetodoPago(metodoPago);
                cuotaBoletaRepository.save(cuotaBoleta);
            }

            String img = response.getValues().getQrBase64();
            sendResponse(fromEmail, "Éxito",
                    "Boleta Nro: " + "[" + boletaPago.getIdBoleta() + "] " + "registrada correctamente.", img);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar Boleta de Pago: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    private void generarPagoCuota(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria o Paciente
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA")
                    || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error",
                        "Error al generar pago de cuota: Parámetros insuficientes. Se requieren 3 (ID_Boleta, Nro_Cuota, MetodoPago).");
                return;
            }

            Long idBoletaPago = Long.valueOf(params.get(0));
            Integer nroCuota = Integer.valueOf(params.get(1));
            String metodoPago = params.get(2);

            BoletaPago boletaPago = boletaPagoRepository.findById(idBoletaPago).orElse(null);
            if (boletaPago == null) {
                sendResponse(fromEmail, "Error", "La boleta con ID " + idBoletaPago + " no existe.");
                return;
            }

            CuotaBoleta cuotaBoleta = cuotaBoletaRepository.findByNumeroCuotaAndBoletaPago(nroCuota, boletaPago);
            if (cuotaBoleta == null) {
                sendResponse(fromEmail, "Error",
                        "La cuota Nro: " + nroCuota + " para la boleta " + idBoletaPago + " no existe.");
                return;
            }

            if ("PAGADA".equals(cuotaBoleta.getEstado())) {
                sendResponse(fromEmail, "Error",
                        "La cuota Nro: " + nroCuota + " de la boleta " + idBoletaPago + " ya se encuentra PAGADA.");
                return;
            }

            Paciente paciente = boletaPago.getPaciente();
            Usuario user = usuarioRepository.findByPersonaCiAndSuffix(paciente.getCi(), "PAC").orElse(null);
            if (user == null) {
                sendResponse(fromEmail, "Error", "El usuario con CI " + paciente.getCi() + " no existe.");
                return;
            }

            double montoAPagar = cuotaBoleta.getMontoCuota();
            boolean tieneMulta = false;
            double montoMulta = 0.0;

            if (cuotaBoleta.getCuotaMulta() != null) {
                montoMulta = cuotaBoleta.getCuotaMulta().getMontoMulta();
                montoAPagar += montoMulta;
                tieneMulta = true;
            } else if (cuotaBoleta.getFechaVencimiento().isBefore(LocalDate.now())) {
                montoMulta = cuotaBoleta.getMontoCuota() * 0.05;

                CuotaMulta cuotaMulta = new CuotaMulta();
                cuotaMulta.setMontoMulta(montoMulta);
                cuotaMulta.setMotivo("Multa del 5% por retraso en el pago de la cuota Nro: " + nroCuota);
                cuotaMulta.setEstado("PENDIENTE");
                cuotaMulta = cuotaMultaRepository.save(cuotaMulta);

                cuotaBoleta.setCuotaMulta(cuotaMulta);
                cuotaBoleta.setEstado("MULTADA");
                cuotaBoleta.setObservacion("Asignada multa del 5% por vencimiento de cuota Nro: " + nroCuota);
                cuotaBoletaRepository.save(cuotaBoleta);

                montoAPagar += montoMulta;
                tieneMulta = true;
            }

            RespuestaQR response = procesarPago(metodoPago, paciente, user, cuotaBoleta.getIdCuota().toString(),
                    montoAPagar);

            cuotaBoleta.setId_transaccion(Long.valueOf(response.getValues().getTransactionId()));
            cuotaBoletaRepository.save(cuotaBoleta);

            String img = response.getValues().getQrBase64();

            String msgExito = "Pago para la Cuota Nro: " + nroCuota + " de la Boleta Nro: [" + boletaPago.getIdBoleta()
                    + "] generado correctamente.";
            if (tieneMulta) {
                msgExito += " ATENCIÓN: El pago incluye una multa del 5% por vencimiento de la fecha de pago (Monto original: "
                        + String.format("%.2f", cuotaBoleta.getMontoCuota()) + " Bs, Multa: "
                        + String.format("%.2f", montoMulta) + " Bs, Total a pagar: "
                        + String.format("%.2f", montoAPagar) + " Bs).";
            }

            sendResponse(fromEmail, "Éxito", msgExito, img);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al generar pago de cuota: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Falta el ID de la boleta de pago.");
                return;
            }

            Long idBoleta = Long.valueOf(params.get(0));
            BoletaPago boletaPago = boletaPagoRepository.findById(idBoleta).orElse(null);

            if (boletaPago == null) {
                sendResponse(fromEmail, "Error", "La boleta con ID " + idBoleta + " no existe o ya ha sido eliminada.");
                return;
            }

            boletaPagoRepository.delete(boletaPago);

            sendResponse(fromEmail, "Éxito", "Boleta de pago Nro: [" + idBoleta + "] eliminada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo eliminar la boleta de pago: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    private void list(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria o Paciente
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA")
                    || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Falta especificar el CI del paciente para listar sus boletas.");
                return;
            }

            String ci = params.get(0).trim();

            Paciente paciente = pacienteRepository.findById(ci).orElse(null);
            if (paciente == null) {
                sendResponse(fromEmail, "Error", "El paciente con CI " + ci + " no existe.");
                return;
            }

            List<BoletaPago> boletas = boletaPagoRepository.findByPaciente_Ci(ci);

            if (boletas.isEmpty()) {
                sendResponse(fromEmail, "Listado de Boletas de Pago",
                        "El paciente " + paciente.getNombres() + " " + paciente.getApellidos() + " (CI: " + ci
                                + ") no tiene boletas de pago registradas.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("==================================================\n");
            sb.append("BOLETAS DE PAGO - PACIENTE: ").append(paciente.getNombres()).append(" ")
                    .append(paciente.getApellidos()).append(" (CI: ").append(ci).append(")\n");
            sb.append("==================================================\n\n");

            for (BoletaPago b : boletas) {
                sb.append("ID Boleta: ").append(b.getIdBoleta()).append("\n");
                sb.append("  Fecha Emisión: ").append(b.getFechaEmicion()).append("\n");
                sb.append("  Descuento: ").append(String.format("%.2f", b.getDescuento())).append(" Bs\n");
                sb.append("  Total: ").append(String.format("%.2f", b.getTotal())).append(" Bs\n");
                sb.append("  Estado de Pago: ").append(b.getEstadoPago()).append("\n");
                sb.append("  Modo de Pago: ").append(b.getModoPago() != null ? b.getModoPago().getNombre() : "N/A")
                        .append("\n");
                sb.append("  Secretaria: ")
                        .append(b.getSecretaria() != null
                                ? b.getSecretaria().getNombres() + " " + b.getSecretaria().getApellidos() + " (CI: "
                                        + b.getSecretaria().getCi() + ")"
                                : "N/A")
                        .append("\n");

                // Detalles (Servicios Prestados)
                sb.append("  Detalles:\n");
                if (b.getServiciosPrestados() == null || b.getServiciosPrestados().isEmpty()) {
                    sb.append("    Sin detalles.\n");
                } else {
                    int num = 1;
                    for (ServicioPrestado sp : b.getServiciosPrestados()) {
                        String servicioNombre = sp.getServicio() != null ? sp.getServicio().getNombre()
                                : "Servicio Desconocido";
                        sb.append("    ").append(num++).append(". Servicio: ").append(servicioNombre)
                                .append(" | Cantidad: ").append(sp.getCantidad())
                                .append(" | Precio Unit.: ")
                                .append(sp.getPrecio() != null ? String.format("%.2f", sp.getPrecio()) : "0.00")
                                .append(" Bs")
                                .append(" | Descuento: ")
                                .append(sp.getDescuento() != null ? String.format("%.2f", sp.getDescuento()) : "0.00")
                                .append(" Bs")
                                .append(" | Subtotal: ")
                                .append(sp.getSubtotal() != null ? String.format("%.2f", sp.getSubtotal()) : "0.00")
                                .append(" Bs\n");
                    }
                }

                // Cuotas
                sb.append("  Cuotas:\n");
                if (b.getCuotasBoleta() == null || b.getCuotasBoleta().isEmpty()) {
                    sb.append("    Sin cuotas.\n");
                } else {
                    for (CuotaBoleta cb : b.getCuotasBoleta()) {
                        sb.append("    - Cuota Nro ").append(cb.getNumeroCuota()).append(":\n")
                                .append("        Monto: ").append(String.format("%.2f", cb.getMontoCuota()))
                                .append(" Bs\n")
                                .append("        Vencimiento: ").append(cb.getFechaVencimiento()).append("\n")
                                .append("        Fecha Pago: ")
                                .append(cb.getFechaPago() != null ? cb.getFechaPago() : "PENDIENTE").append("\n")
                                .append("        Estado: ").append(cb.getEstado()).append("\n");

                        if (cb.getObservacion() != null && !cb.getObservacion().trim().isEmpty()) {
                            sb.append("        Observación: ").append(cb.getObservacion()).append("\n");
                        }
                        if (cb.getId_transaccion() != null) {
                            sb.append("        ID Transacción: ").append(cb.getId_transaccion()).append("\n");
                        }
                        if (cb.getCuotaMulta() != null) {
                            CuotaMulta cm = cb.getCuotaMulta();
                            sb.append("        MULTA:\n")
                                    .append("          Monto Multa: ").append(String.format("%.2f", cm.getMontoMulta()))
                                    .append(" Bs\n")
                                    .append("          Fecha Generada: ").append(cm.getFechaGenerada()).append("\n")
                                    .append("          Motivo: ").append(cm.getMotivo()).append("\n")
                                    .append("          Estado Multa: ").append(cm.getEstado()).append("\n");
                        }
                    }
                }
                sb.append("--------------------------------------------------\n\n");
            }

            sendResponse(fromEmail, "Listado de Boletas de Pago", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar boletas de pago: " + e.getMessage());
        }
    }

    private void enviarComprobante(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            // Verificar si es Secretaria o Paciente
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA")
                    || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PACIENTE");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parametros: ID_BoletaPago[0], Nro_Cuota[1],comprobante[img1]
            if (params.size() < 2 || imagenesBase64.size() < 1) {
                sendResponse(fromEmail, "Error",
                        "Error al pagar Boleta de Pago: Parámetros insuficientes. Se requieren 3: [ID_BoletaPago, Nro_Cuota, comprobante(imagen)]");
                return;
            }
            Long idBoletaPago = Long.valueOf(params.get(0));
            Integer nroCuota = Integer.valueOf(params.get(1));

            BoletaPago boletaPago = boletaPagoRepository.findById(idBoletaPago).orElse(null);
            if (boletaPago == null) {
                sendResponse(fromEmail, "Error", "La boleta con ID " + idBoletaPago + " no existe.");
                return;
            }

            // Obtener la cuota específica usando el número de cuota y la boleta
            CuotaBoleta cuotaBoleta = cuotaBoletaRepository
                    .findByNumeroCuotaAndBoletaPago(nroCuota, boletaPago);

            if (cuotaBoleta == null) {
                sendResponse(fromEmail, "Error",
                        "La cuota Nro: " + nroCuota + " para la boleta " + idBoletaPago + " no existe.");
                return;
            }

            if (cuotaBoleta.getId_transaccion() == null) {
                sendResponse(fromEmail, "Error", "La cuota Nro: " + nroCuota + " no tiene una transacción asociada.");
                return;
            }

            // Verficar si ya estaba pagada
            if ("PAGADA".equals(cuotaBoleta.getEstado())) {
                sendResponse(fromEmail, "Aviso",
                        "La cuota Nro: " + nroCuota + " de la boleta Nro: [" + boletaPago.getIdBoleta()
                                + "] ya se encuentra pagada.");
                return;
            }

            // Verificar si esa cuota se pago
            RespuestaEstadoQR response = pagoFacilService
                    .consultarEstadoTransaccion(cuotaBoleta.getId_transaccion().toString());

            if (response != null && response.getValues() != null && response.getValues().getEstadoTransaccion() == 2) {
                // Registrar pago de la cuota
                cuotaBoleta.setEstado("PAGADA");
                String fotoUrl = (imagenesBase64 != null && !imagenesBase64.isEmpty())
                        ? cloudinaryServices.subirImagen(imagenesBase64.get(0))
                        : "null";
                cuotaBoleta.setComprobante(fotoUrl);

                LocalDate fechaPago = LocalDate.parse(response.getValues().getPaymentDate().replace('/', '-'));
                cuotaBoleta.setFechaPago(fechaPago);

                // Si la cuota tiene una multa asociada, marcarla como pagada
                if (cuotaBoleta.getCuotaMulta() != null) {
                    CuotaMulta cuotaMulta = cuotaBoleta.getCuotaMulta();
                    cuotaMulta.setEstado("PAGADA");
                    cuotaMultaRepository.save(cuotaMulta);
                }

                cuotaBoletaRepository.save(cuotaBoleta);

                // Verificar si todas las cuotas de la boleta están pagadas
                boolean allPaid = true;
                if (boletaPago.getCuotasBoleta() != null) {
                    for (CuotaBoleta cb : boletaPago.getCuotasBoleta()) {
                        if (cb.getIdCuota().equals(cuotaBoleta.getIdCuota())) {
                            continue;
                        }
                        if (!"PAGADA".equals(cb.getEstado())) {
                            allPaid = false;
                            break;
                        }
                    }
                }

                if (allPaid) {
                    boletaPago.setEstadoPago("PAGADA");
                } else {
                    boletaPago.setEstadoPago("PAGO PARCIAL");
                }
                boletaPagoRepository.save(boletaPago);

                sendResponse(fromEmail, "Éxito",
                        "Cuota Nro: " + nroCuota + " de la Boleta Nro: [" + boletaPago.getIdBoleta()
                                + "] pagada correctamente.");
            } else {
                int estadoTransaccion = (response != null && response.getValues() != null)
                        ? response.getValues().getEstadoTransaccion()
                        : 0;
                String desc = (response != null && response.getValues() != null)
                        ? response.getValues().getDescrpEstadoTransaccion()
                        : "Estado desconocido";

                if (estadoTransaccion != 4) {
                    if (cuotaBoleta.getComprobante() == null) {
                        System.out.println("------------------------Subiendo foto---------------");
                        String fotoUrl = (imagenesBase64 != null && !imagenesBase64.isEmpty())
                                ? cloudinaryServices.subirImagen(imagenesBase64.get(0))
                                : "null";
                        System.out.println(fotoUrl);
                        cuotaBoleta.setComprobante(fotoUrl);
                    }
                    cuotaBoleta.setEstado(desc.toUpperCase());
                    cuotaBoletaRepository.save(cuotaBoleta);

                    sendResponse(fromEmail, "Aviso",
                            "La cuota Nro: " + nroCuota + " de la boleta de pago Nro: [" + boletaPago.getIdBoleta()
                                    + "] "
                                    + "se encuentra en estado: " + desc);
                } else {
                    cuotaBoleta.setEstado(desc.toUpperCase());
                    cuotaBoletaRepository.save(cuotaBoleta);

                    sendResponse(fromEmail, "Error",
                            "La transacción para la cuota Nro: " + nroCuota + " de la boleta de pago Nro: ["
                                    + boletaPago.getIdBoleta() + "] "
                                    + "se encuentra en estado " + desc.toUpperCase() + ".");
                }
                return;
            }
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al pagar la Boleta de Pago: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body, String... base64Image) {
        try {
            smtpService.sendEmail(to, subject, body, base64Image);
        } catch (Exception e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

    private RespuestaQR procesarPago(String metodoPago, Paciente paciente, Usuario user, String primeraCuota,
            double montoCuota) {
        if (!"QR".equalsIgnoreCase(metodoPago)) {
            throw new RuntimeException("Método de pago '" + metodoPago + "' no soportado. Solo se admite pago por QR.");
        }

        CrearQR qr = new CrearQR();
        qr.setMetodoPago(34);
        qr.setNombreCliente(paciente.getNombres() + " " + paciente.getApellidos());
        qr.setTipoDocumento(1);
        qr.setCI(paciente.getCi());
        qr.setNumeroTelefono(paciente.getTelefono());
        qr.setEmail(user.getCorreoElectronico());

        qr.setIdBoletaPago(primeraCuota);
        qr.setMonto(montoCuota / 1000); // PagoFacil espera montos divididos por 1000

        qr.setMoneda(2); // 2 = Bs
        qr.setCodCliente(user.getCodigoUsuario());
        qr.setCallBack("https://tu-dominio.com/callback");

        RespuestaQR response = pagoFacilService.generarQR(qr);

        if (response == null || response.getError() != 0 || response.getValues() == null) {
            String errMsg = (response != null && response.getMessage() != null)
                    ? response.getMessage()
                    : "Error desconocido al generar el código QR en PagoFácil";
            throw new RuntimeException(errMsg);
        }
        return response;
    }
}
