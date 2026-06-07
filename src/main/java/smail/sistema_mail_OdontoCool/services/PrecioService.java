package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Precio;
import smail.sistema_mail_OdontoCool.repositories.PrecioRepository;

@Service
public class PrecioService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private PrecioRepository precioRepository;

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
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un precio.");
                return;
            }
            Precio precio = new Precio();
            precio.setMoneda(params.get(0));
            precio.setMonto(new BigDecimal(params.get(1)));
            precio.setEstado(params.get(2));
            precioRepository.save(precio);
            sendResponse(fromEmail, "Éxito", "Precio registrado correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un turno: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            StringBuilder sb = new StringBuilder();
            if (params.size() == 0) {
                sendResponse(fromEmail, "Error", "Falta especificar tipo de listado. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }
            if (params.size() == 1) {
                switch (params.get(0)) {
                    case "*":
                        sb = listAll();
                        break;
                    default:
                        sendResponse(fromEmail, "Error", "Listado no permitido para Turnos.");
                }
            }
            sendResponse(fromEmail, "Listado de Precios", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar precios: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            //Parametros: id[0], monto[1], moneda[2], estado[3]
            // if (params.size() < 4) {
            //     sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un precio.");
            //     return;
            // }
            Long id = Long.parseLong(params.get(0));
            String moneda = params.get(1);
            BigDecimal monto = null;
            if (!params.get(2).trim().isEmpty()) {
                monto = new BigDecimal(params.get(2));
            }
            String estado = params.get(3);
            Precio precio = precioRepository.findById(id).orElse(null);
            if (precio == null) {
                sendResponse(fromEmail, "Error", "Precio con ID: " + id + " no encontrado.");
                return;
            }
            if (moneda != null && !moneda.trim().isEmpty()) {
                precio.setMoneda(moneda);
            }
            if (monto.compareTo(BigDecimal.ZERO) > 0) {
                precio.setMonto(monto);
            }
            if (estado != null && !estado.trim().isEmpty()) {
                precio.setEstado(estado);
            }
            precioRepository.save(precio);
            sendResponse(fromEmail, "Éxito", "Precio: " + precio.getId() + " actualizado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar precio: " + e.getMessage());
        }
    }

    // TODO: Implementar eliminación lógica (cambiar estado a INACTIVO) en lugar de eliminación física
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
        List<Precio> precios = precioRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Precios\n");
        for (Precio precio : precios) {
            sb.append(String.format("- [%s] Monto: %s Moneda: %s Estado: %s\n",
                    precio.getId(),
                    precio.getMonto(),
                    precio.getMoneda(),
                    precio.getEstado()));
        }
        return sb;
    }

}
