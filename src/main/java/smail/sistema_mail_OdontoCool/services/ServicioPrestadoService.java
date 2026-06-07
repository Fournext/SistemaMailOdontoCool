package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.AsignacionPrecio;
import smail.sistema_mail_OdontoCool.entities.Servicio;
import smail.sistema_mail_OdontoCool.entities.ServicioPrestado;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.repositories.AsignacionPrecioRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioPrestadoRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Service
public class ServicioPrestadoService {

    @Autowired
    private ServicioPrestadoRepository servicioPrestadoRepository;
    @Autowired
    private TratamientoRepository tratamientoRepository;
    @Autowired
    private ServicioRepository servicioRepository;
    @Autowired
    private AsignacionPrecioRepository asignacionPrecioRepository;
    @Autowired
    private SmtpClientService smtpService;

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS":
                insert(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "MOD":
                update(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Doctores.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros:
            // cantidad[0], precio[1], fecha_servicio[2], estado[3], tratamientoId[4], servicioId[5]

            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un servicio prestado.");
                return;
            }

            int cantidad = Integer.parseInt(params.get(0));
            BigDecimal precioRegistrado = new BigDecimal(params.get(1));
            LocalDate fechaServicio = LocalDate.parse(params.get(2));
            String estado = params.get(3);
            Long tratamientoId = Long.parseLong(params.get(4));
            Long servicioId = Long.parseLong(params.get(5));

            if (cantidad <= 0) {
                sendResponse(fromEmail, "Error", "La cantidad debe ser mayor a 0.");
                return;
            }

            if (precioRegistrado.compareTo(BigDecimal.ZERO) <= 0) {
                sendResponse(fromEmail, "Error", "El precio debe ser mayor a 0.");
                return;
            }

            Servicio servicio = servicioRepository.findById(servicioId)
                    .orElseThrow(() -> new RuntimeException("Servicio con ID: " + servicioId + " no encontrado."));

            Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                    .orElseThrow(() -> new RuntimeException("Tratamiento con ID: " + tratamientoId + " no encontrado."));

            AsignacionPrecio asignacion = asignacionPrecioRepository
                    .findTopByServicioIdAndEstadoOrderByFechaInicioDesc(servicioId, "ACTIVO")
                    .orElseThrow(() -> new RuntimeException("No hay precio activo para este servicio."));

            BigDecimal precioSistema = asignacion.getPrecio().getMonto();

            BigDecimal descuento = precioSistema
                    .subtract(precioRegistrado)
                    .multiply(BigDecimal.valueOf(cantidad));

            BigDecimal subtotal = precioRegistrado
                    .multiply(BigDecimal.valueOf(cantidad));

            ServicioPrestado servicioPrestado = new ServicioPrestado();
            servicioPrestado.setCantidad(cantidad);
            servicioPrestado.setPrecio(precioRegistrado);
            servicioPrestado.setDescuento(descuento);
            servicioPrestado.setSubtotal(subtotal);
            servicioPrestado.setFechaServicio(fechaServicio);
            servicioPrestado.setEstado(estado);
            servicioPrestado.setTratamiento(tratamiento);
            servicioPrestado.setServicio(servicio);

            servicioPrestadoRepository.save(servicioPrestado);

            sendResponse(fromEmail, "Éxito",
                    "Servicio prestado registrado correctamente. Subtotal: " + subtotal
                    + " Descuento: " + descuento);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un servicio prestado: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }
            if (params.size() == 1) {
                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Servicios Prestados.");
                }
            }
            sendResponse(fromEmail, "Listado de Servicios Prestados", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar servicios prestados: " + e.getMessage());
        }
    }

    private void update(List<String> params, String fromEmail) {
        try {
            // Parámetros:
            // id[0], cantidad[1], precio[2], fecha_servicio[3], estado[4], tratamientoId[5], servicioId[6]

            if (params.size() < 7) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un servicio prestado.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "ID del servicio prestado es requerido para actualizar.");
                return;
            }

            Long id = Long.parseLong(params.get(0));

            ServicioPrestado servicioPrestado = servicioPrestadoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servicio prestado con ID: " + id + " no encontrado."));

            if (!params.get(1).trim().isEmpty()) {
                int cantidad = Integer.parseInt(params.get(1));

                if (cantidad <= 0) {
                    sendResponse(fromEmail, "Error", "La cantidad debe ser mayor a 0.");
                    return;
                }

                servicioPrestado.setCantidad(cantidad);
            }

            if (!params.get(2).trim().isEmpty()) {
                BigDecimal precio = new BigDecimal(params.get(2));

                if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                    sendResponse(fromEmail, "Error", "El precio debe ser mayor a 0.");
                    return;
                }

                servicioPrestado.setPrecio(precio);
            }

            if (!params.get(3).trim().isEmpty()) {
                LocalDate fechaServicio = LocalDate.parse(params.get(3));
                servicioPrestado.setFechaServicio(fechaServicio);
            }

            if (!params.get(4).trim().isEmpty()) {
                servicioPrestado.setEstado(params.get(4));
            }

            if (!params.get(5).trim().isEmpty()) {
                Long tratamientoId = Long.parseLong(params.get(5));

                Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(() -> new RuntimeException("Tratamiento con ID: " + tratamientoId + " no encontrado."));

                servicioPrestado.setTratamiento(tratamiento);
            }

            if (!params.get(6).trim().isEmpty()) {
                Long servicioId = Long.parseLong(params.get(6));

                Servicio servicio = servicioRepository.findById(servicioId)
                        .orElseThrow(() -> new RuntimeException("Servicio con ID: " + servicioId + " no encontrado."));

                servicioPrestado.setServicio(servicio);
            }

            // Recalcular descuento y subtotal usando los valores finales
            int cantidadFinal = servicioPrestado.getCantidad();
            BigDecimal precioFinal = servicioPrestado.getPrecio();
            Servicio servicioFinal = servicioPrestado.getServicio();

            if (cantidadFinal <= 0 || precioFinal == null || servicioFinal == null) {
                sendResponse(fromEmail, "Error", "No se puede calcular subtotal: faltan cantidad, precio o servicio.");
                return;
            }

            AsignacionPrecio asignacion = asignacionPrecioRepository
                    .findTopByServicioIdAndEstadoOrderByFechaInicioDesc(servicioFinal.getId(), "ACTIVO")
                    .orElseThrow(() -> new RuntimeException("No hay precio activo para este servicio."));

            BigDecimal precioSistema = asignacion.getPrecio().getMonto();

            BigDecimal descuentoFinal = precioSistema
                    .subtract(precioFinal)
                    .multiply(BigDecimal.valueOf(cantidadFinal));

            BigDecimal subtotalFinal = precioFinal
                    .multiply(BigDecimal.valueOf(cantidadFinal));

            servicioPrestado.setDescuento(descuentoFinal);
            servicioPrestado.setSubtotal(subtotalFinal);

            servicioPrestadoRepository.save(servicioPrestado);

            sendResponse(fromEmail, "Éxito",
                    "Servicio prestado con ID: " + servicioPrestado.getId()
                    + " actualizado correctamente. Subtotal: " + subtotalFinal
                    + " Descuento: " + descuentoFinal);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar el servicio prestado: " + e.getMessage());
        }
    }

    // TODO: Implementar eliminación lógica (cambiar estado a INACTIVO) en lugar de eliminación física)
    private void delete(List<String> params, String fromEmail) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<ServicioPrestado> serviciosPrestados = servicioPrestadoRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Servicios Prestados\n");
        for (ServicioPrestado servicio : serviciosPrestados) {
            sb.append(String.format("- [%s] Cantidad: %s Precio: %s Descuento: %s Subtotal: %s Fecha: %s Estado: %s Servicio: %s Tratamiento: %s\n",
                    servicio.getId(),
                    servicio.getCantidad(),
                    servicio.getPrecio(),
                    servicio.getDescuento(),
                    servicio.getSubtotal(),
                    servicio.getFechaServicio(),
                    servicio.getEstado(),
                    servicio.getServicio().getNombre(),
                    servicio.getTratamiento().getId()
            ));
        }
        return sb;
    }

}
