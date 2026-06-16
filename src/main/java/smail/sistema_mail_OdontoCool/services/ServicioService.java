package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.AsignacionPrecio;
import smail.sistema_mail_OdontoCool.entities.Precio;
import smail.sistema_mail_OdontoCool.entities.Servicio;
import smail.sistema_mail_OdontoCool.repositories.AsignacionPrecioRepository;
import smail.sistema_mail_OdontoCool.repositories.PrecioRepository;
import smail.sistema_mail_OdontoCool.repositories.ServicioRepository;
import smail.sistema_mail_OdontoCool.validations.ServicioVal;

@Service
public class ServicioService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private ServicioRepository servicioRepository;

    @Autowired
    private AsignacionPrecioRepository asignacionPrecioRepository;

    @Autowired
    private PrecioRepository precioRepository;

    @Autowired
    private ServicioVal servicioVal;

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS":
                insert(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "BUS":
                findByNombre(params, fromEmail);
                break;
            case "MOD":
                update(params, fromEmail);
                break;
            case "APS":
                asignarPrecioAServicio(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Servicios.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            String validationMsg = servicioVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Servicio servicio = new Servicio();
            servicio.setNombre(params.get(0).trim());
            servicio.setDescripcion(params.get(1).trim());
            servicio.setEstado(params.get(2).trim().toUpperCase());
            servicio.setTipo(params.get(3).trim());

            servicioRepository.save(servicio);

            sendResponse(fromEmail, "Éxito",
                    "Servicio: " + servicio.getNombre() + " registrado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un servicio: " + e.getMessage());
        }
    }

    @Transactional
    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();

            if (params == null || params.isEmpty()) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado. Use LISER[*] o LISER[Tipo:valor].");
                return;
            }

            if (params.size() == 1) {
                String parametro = params.get(0).trim();

                switch (parametro.equals("*") ? "*"
                        : parametro.startsWith("Tipo:") ? "Tipo:"
                        : "default") {

                    case "*":
                        sb = listAll();
                        break;

                    case "Tipo:":
                        String tipo = parametro.substring("Tipo:".length()).trim();
                        if (tipo.isEmpty()) {
                            sendResponse(fromEmail, "Error", "Debe indicar el tipo del servicio.");
                            return;
                        }
                        sb = findByTipo(tipo);
                        break;

                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Servicios.");
                        return;
                }
            } else {
                sendResponse(fromEmail, "Error", "Cantidad de parámetros incorrecta para listar servicios.");
                return;
            }

            sendResponse(fromEmail, "Listado de Servicios", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar servicios: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            String validationMsg = servicioVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servicio con ID: " + id + " no encontrado."));

            String nombre = params.get(1).trim();
            String descripcion = params.get(2).trim();
            String estado = params.get(3).trim();
            String tipo = params.get(4).trim();

            if (!nombre.isEmpty()) {
                servicio.setNombre(nombre);
            }

            if (!descripcion.isEmpty()) {
                servicio.setDescripcion(descripcion);
            }

            if (!estado.isEmpty()) {
                servicio.setEstado(estado.toUpperCase());
            }

            if (!tipo.isEmpty()) {
                servicio.setTipo(tipo);
            }

            servicioRepository.save(servicio);

            sendResponse(fromEmail, "Éxito",
                    "Servicio: " + servicio.getNombre() + " actualizado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar servicio: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            if (params == null || params.size() != 1 || params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "Debe enviar el ID del servicio a eliminar.");
                return;
            }

            Long id = Long.parseLong(params.get(0).trim());

            Servicio servicio = servicioRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Servicio con ID: " + id + " no encontrado."));

            servicio.setEstado("INACTIVO");
            servicioRepository.save(servicio);

            sendResponse(fromEmail, "Éxito",
                    "Servicio: " + servicio.getNombre() + " desactivado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID del servicio debe ser numérico.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar servicio: " + e.getMessage());
        }
    }

    @Transactional
    private StringBuilder listAll() {
        List<Servicio> servicios = servicioRepository.findAllConPrecios();
        StringBuilder sb = new StringBuilder("Listado de Servicios:\n");

        for (Servicio servicio : servicios) {
            sb.append(formatearServicio(servicio));
        }

        return sb;
    }

    @Transactional
    private StringBuilder findByTipo(String tipo) {
        List<Servicio> servicios = servicioRepository.findAllConPrecios();
        StringBuilder sb = new StringBuilder("Listado de Servicios por tipo: " + tipo + "\n");

        boolean encontrado = false;

        for (Servicio servicio : servicios) {
            if (servicio.getTipo() != null && servicio.getTipo().equalsIgnoreCase(tipo)) {
                sb.append(formatearServicio(servicio));
                encontrado = true;
            }
        }

        if (!encontrado) {
            sb.append("No se encontraron servicios con tipo: ").append(tipo).append("\n");
        }

        return sb;
    }

    @Transactional
    private void asignarPrecioAServicio(List<String> params, String fromEmail) {
        try {
            String validationMsg = servicioVal.asignarPrecioValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            String nombreServicio = params.get(0).trim();
            LocalDate fechaInicio = LocalDate.parse(params.get(1).trim().replace('/', '-'));
            LocalDate fechaFin = LocalDate.parse(params.get(2).trim().replace('/', '-'));
            String moneda = params.get(3).trim().toUpperCase();
            BigDecimal monto = new BigDecimal(params.get(4).trim());

            Servicio servicio = servicioRepository.findByNombre(nombreServicio)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado: " + nombreServicio));

            Precio precio = precioRepository.findByMonto(monto).orElseGet(() -> {
                Precio nuevoPrecio = new Precio(moneda, monto);
                return precioRepository.save(nuevoPrecio);
            });

            List<AsignacionPrecio> asignacionesActivas =
                    asignacionPrecioRepository.findByServicioIdAndEstado(servicio.getId(), "ACTIVO");

            for (AsignacionPrecio asignacionAnterior : asignacionesActivas) {
                asignacionAnterior.setEstado("INACTIVO");
                asignacionAnterior.setFechaFin(fechaInicio.minusDays(1));
            }

            asignacionPrecioRepository.saveAll(asignacionesActivas);

            AsignacionPrecio nuevaAsignacion = new AsignacionPrecio();
            nuevaAsignacion.setServicio(servicio);
            nuevaAsignacion.setPrecio(precio);
            nuevaAsignacion.setFechaInicio(fechaInicio);
            nuevaAsignacion.setFechaFin(fechaFin);
            nuevaAsignacion.setEstado("ACTIVO");

            asignacionPrecioRepository.save(nuevaAsignacion);

            sendResponse(fromEmail, "Éxito",
                    String.format("Precio %.2f %s asignado al servicio %s desde %s hasta %s.",
                            monto, moneda, servicio.getNombre(), fechaInicio, fechaFin));

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al asignar precio a servicio: " + e.getMessage());
        }
    }

    @Transactional
    private void findByNombre(List<String> params, String fromEmail) {
        try {
            if (params == null || params.isEmpty() || params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "Falta el nombre del servicio para la búsqueda.");
                return;
            }

            String nombre = params.get(0).trim();

            Servicio servicio = servicioRepository.findByNombreConPrecios(nombre).orElse(null);

            if (servicio == null) {
                sendResponse(fromEmail, "No encontrado",
                        "No se encontró un servicio con el nombre: " + nombre);
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("Servicio encontrado:\n");
            sb.append(formatearServicio(servicio));

            sendResponse(fromEmail, "Servicio Encontrado", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al buscar servicio por nombre: " + e.getMessage());
        }
    }

    private String formatearServicio(Servicio servicio) {
        String precioTexto = "Sin precio asignado";

        for (AsignacionPrecio asignacion : servicio.getAsignacionesPrecio()) {
            if ("ACTIVO".equalsIgnoreCase(asignacion.getEstado())) {
                Precio precio = asignacion.getPrecio();
                precioTexto = precio.getMonto() + " " + precio.getMoneda();
                break;
            }
        }

        return String.format(
                "- [%s] Servicio: %s | Descripción: %s | Tipo: %s | Estado: %s | Precio actual: %s\n",
                servicio.getId(),
                servicio.getNombre(),
                servicio.getDescripcion(),
                servicio.getTipo(),
                servicio.getEstado(),
                precioTexto
        );
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}