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
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.validations.ServicioPrestadoVal;

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

    @Autowired
    private ServicioPrestadoVal servicioPrestadoVal;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
                sendResponse(fromEmail, "Error", "Acción no permitida para Servicios Prestados.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = servicioPrestadoVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String detallesRaw = params.get(0).trim();
            String[] registros = detallesRaw.split("\\s*\\|\\s*");

            int cantidadInsertada = 0;
            BigDecimal totalGeneral = BigDecimal.ZERO;

            for (String registro : registros) {
                String[] campos = registro.split("\\s*;\\s*", -1);

                int cantidad = Integer.parseInt(campos[0].trim());
                BigDecimal precioRegistrado = new BigDecimal(campos[1].trim());
                LocalDate fechaServicio = LocalDate.parse(campos[2].trim().replace('/', '-'));
                String estado = campos[3].trim().toUpperCase();
                Long tratamientoId = Long.parseLong(campos[4].trim());
                Long servicioId = Long.parseLong(campos[5].trim());

                Servicio servicio = servicioRepository.findById(servicioId)
                        .orElseThrow(() -> new RuntimeException("Servicio con ID: " + servicioId + " no encontrado."));

                Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(
                                () -> new RuntimeException("Tratamiento con ID: " + tratamientoId + " no encontrado."));

                AsignacionPrecio asignacion = asignacionPrecioRepository
                        .findTopByServicioIdAndEstadoOrderByFechaInicioDesc(servicioId, "ACTIVO")
                        .orElseThrow(
                                () -> new RuntimeException("No hay precio activo para el servicio ID: " + servicioId));

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

                cantidadInsertada++;
                totalGeneral = totalGeneral.add(subtotal);
            }

            sendResponse(fromEmail, "Éxito",
                    "Se registraron correctamente " + cantidadInsertada
                            + " servicios prestados. Total: " + totalGeneral);

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar servicios prestados: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = servicioPrestadoVal.listValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String parametro = params.get(0).trim();
            StringBuilder sb;

            if ("*".equals(parametro)) {
                sb = listAll();
            } else {
                Long tratamientoId = Long.parseLong(
                        parametro.substring("Tratamiento:".length()).trim());
                sb = findByTratamiento(tratamientoId);
            }

            sendResponse(fromEmail, "Listado de Servicios Prestados", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar servicios prestados: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            String validationMsg = servicioPrestadoVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            ServicioPrestado servicioPrestado = servicioPrestadoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servicio prestado con ID: " + id + " no encontrado."));

            if (!params.get(1).trim().isEmpty()) {
                servicioPrestado.setCantidad(Integer.parseInt(params.get(1).trim()));
            }

            if (!params.get(2).trim().isEmpty()) {
                servicioPrestado.setPrecio(new BigDecimal(params.get(2).trim()));
            }

            if (!params.get(3).trim().isEmpty()) {
                servicioPrestado.setFechaServicio(LocalDate.parse(params.get(3).trim().replace('/', '-')));
            }

            if (!params.get(4).trim().isEmpty()) {
                servicioPrestado.setEstado(params.get(4).trim().toUpperCase());
            }

            if (!params.get(5).trim().isEmpty()) {
                Long tratamientoId = Long.parseLong(params.get(5).trim());
                Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                        .orElseThrow(
                                () -> new RuntimeException("Tratamiento con ID: " + tratamientoId + " no encontrado."));
                servicioPrestado.setTratamiento(tratamiento);
            }

            if (!params.get(6).trim().isEmpty()) {
                Long servicioId = Long.parseLong(params.get(6).trim());
                Servicio servicio = servicioRepository.findById(servicioId)
                        .orElseThrow(() -> new RuntimeException("Servicio con ID: " + servicioId + " no encontrado."));
                servicioPrestado.setServicio(servicio);
            }

            recalcularMontos(servicioPrestado);

            servicioPrestadoRepository.save(servicioPrestado);

            sendResponse(fromEmail, "Éxito",
                    "Servicio prestado con ID: " + servicioPrestado.getId()
                            + " actualizado correctamente. Subtotal: " + servicioPrestado.getSubtotal()
                            + " Descuento: " + servicioPrestado.getDescuento());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar servicio prestado: " + e.getMessage());
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
            String validationMsg = servicioPrestadoVal.deleteValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            ServicioPrestado servicioPrestado = servicioPrestadoRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servicio prestado con ID: " + id + " no encontrado."));

            servicioPrestado.setEstado("INACTIVO");
            servicioPrestadoRepository.save(servicioPrestado);

            sendResponse(fromEmail, "Éxito",
                    "Servicio prestado con ID: " + id + " desactivado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar servicio prestado: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<ServicioPrestado> serviciosPrestados = servicioPrestadoRepository.findAll();

        StringBuilder sb = new StringBuilder("Listado de Servicios Prestados\n");

        for (ServicioPrestado servicio : serviciosPrestados) {
            sb.append(formatearServicioPrestado(servicio));
        }

        return sb;
    }

    private StringBuilder findByTratamiento(Long tratamientoId) {
        List<ServicioPrestado> serviciosPrestados = servicioPrestadoRepository.findByTratamientoId(tratamientoId);

        StringBuilder sb = new StringBuilder();
        sb.append("Listado de Servicios Prestados para Tratamiento ID: ")
                .append(tratamientoId)
                .append("\n");

        if (serviciosPrestados.isEmpty()) {
            sb.append("No se encontraron servicios prestados para este tratamiento.\n");
            return sb;
        }

        for (ServicioPrestado servicio : serviciosPrestados) {
            sb.append(formatearServicioPrestado(servicio));
        }

        return sb;
    }

    private void recalcularMontos(ServicioPrestado servicioPrestado) {
        int cantidad = servicioPrestado.getCantidad();
        BigDecimal precioRegistrado = servicioPrestado.getPrecio();
        Long servicioId = servicioPrestado.getServicio().getId();

        AsignacionPrecio asignacion = asignacionPrecioRepository
                .findTopByServicioIdAndEstadoOrderByFechaInicioDesc(servicioId, "ACTIVO")
                .orElseThrow(() -> new RuntimeException("No hay precio activo para este servicio."));

        BigDecimal precioSistema = asignacion.getPrecio().getMonto();

        BigDecimal descuento = precioSistema
                .subtract(precioRegistrado)
                .multiply(BigDecimal.valueOf(cantidad));

        BigDecimal subtotal = precioRegistrado
                .multiply(BigDecimal.valueOf(cantidad));

        servicioPrestado.setDescuento(descuento);
        servicioPrestado.setSubtotal(subtotal);
    }

    private String formatearServicioPrestado(ServicioPrestado servicio) {
        return String.format(
                "- [%s] Cantidad: %s | Precio: %s | Descuento: %s | Subtotal: %s | Fecha: %s | Estado: %s | Servicio: %s | Tratamiento: %s\n",
                servicio.getId(),
                servicio.getCantidad(),
                servicio.getPrecio(),
                servicio.getDescuento(),
                servicio.getSubtotal(),
                servicio.getFechaServicio(),
                servicio.getEstado(),
                servicio.getServicio() != null ? servicio.getServicio().getNombre() : "N/A",
                servicio.getTratamiento() != null ? servicio.getTratamiento().getId() : "N/A");
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}