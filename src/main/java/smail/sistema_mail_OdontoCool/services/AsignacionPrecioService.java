package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
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

@Service
public class AsignacionPrecioService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private AsignacionPrecioRepository asignacionPrecioRepository;
    @Autowired
    private PrecioRepository precioRepository;
    @Autowired
    private ServicioRepository servicioRepository;

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

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            //Parametros: fecha_inicio[0], fecha_fin[1], servicio_id[2], precio_id[3], estado[4]
            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para registrar un turno.");
                return;
            }

            LocalDate fechaInicio = LocalDate.parse(params.get(0));
            LocalDate fechaFin = LocalDate.parse(params.get(1));
            String estado = params.get(2);
            Long servicioId = Long.parseLong(params.get(3));
            Long precioId = Long.parseLong(params.get(4));

            Precio precio = precioRepository.findById(precioId).orElse(null);
            Servicio servicio = servicioRepository.findById(servicioId).orElse(null);
            AsignacionPrecio asignacionPrecio = new AsignacionPrecio();

            asignacionPrecio.setFechaInicio(fechaInicio);
            asignacionPrecio.setFechaFin(fechaFin);
            asignacionPrecio.setEstado(estado);
            asignacionPrecio.setPrecio(precio);
            asignacionPrecio.setServicio(servicio);
            asignacionPrecioRepository.save(asignacionPrecio);
            sendResponse(fromEmail, "Éxito", "Asignación de precio: " + asignacionPrecio.getServicio().getNombre() + " registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar una asignación de precio: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.size() == 0) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }
            if (params.size() == 1) {

                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido.");
                }
            }
            sendResponse(fromEmail, "Listado de Asignaciones de Precios a servicios", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "No se pudo listar asignaciones de precios a servicios: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar una asignación de precio.");
                return;
            }
            Long id = Long.parseLong(params.get(0));
            LocalDate fechaInicio = null;
            LocalDate fechaFin = null;
            if (!params.get(1).trim().isEmpty()) {
                fechaInicio = LocalDate.parse(params.get(1));
            }

            if (!params.get(2).trim().isEmpty()) {
                fechaFin = LocalDate.parse(params.get(2));
            }
            String estado = params.get(3);
            Long servicioId = null;
            Long precioId = null;
            if (!params.get(4).trim().isEmpty()) {
                servicioId = Long.parseLong(params.get(4));
            }
            if (!params.get(5).trim().isEmpty()) {
                precioId = Long.parseLong(params.get(5));
            }

            AsignacionPrecio asignacionPrecio = asignacionPrecioRepository.findById(id).orElseThrow(() -> new RuntimeException("Asignación de precio con ID: " + id + " no encontrada."));

            if (servicioId != null) {
                Servicio servicio = servicioRepository.findById(servicioId).orElseThrow(() -> new RuntimeException("Servicio  no encontrado."));
                asignacionPrecio.setServicio(servicio);
            }
            if (precioId != null) {
                Precio precio = precioRepository.findById(precioId).orElseThrow(() -> new RuntimeException("Precio no encontrado."));
                asignacionPrecio.setPrecio(precio);
            }

            if (fechaInicio != null) {
                asignacionPrecio.setFechaInicio(fechaInicio);
            }
            if (fechaFin != null) {
                asignacionPrecio.setFechaFin(fechaFin);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                asignacionPrecio.setEstado(estado);
            }
            asignacionPrecioRepository.save(asignacionPrecio);
            sendResponse(fromEmail, "Éxito", "Asignación de precio: " + asignacionPrecio.getServicio().getNombre() + " actualizada correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo actualizar la asignación de precio: " + e.getMessage());
        }

    }

    // TODO: Implementar método delete para Asignación de precio a servicios
    private void delete(List<String> params, String fromEmail) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private StringBuilder listAll() {
        List<AsignacionPrecio> atds = asignacionPrecioRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Asignaciones de Precios a servicios:\n\n");
        for (AsignacionPrecio atd : atds) {
            sb.append(String.format(
                    "- [%s] Nombre: %s - Monto: %s -FechaInicio: %s -FechaFin: %s -Estado: %s)\n",
                    atd.getId(),
                    atd.getServicio().getNombre(),
                    atd.getPrecio().getMonto(),
                    atd.getFechaInicio(),
                    atd.getFechaFin(),
                    atd.getEstado()));
        }
        return sb;
    }

}
