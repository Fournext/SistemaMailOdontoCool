package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Diente;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;

@Service
public class DienteService {

    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private DienteRepository dienteRepository;

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
            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para insertar un Diente.");
                return;
            }

            String nombre = params.get(0);
            int numero = Integer.parseInt(params.get(1));
            String tipo = params.get(2);
            String ubicacion = params.get(3);
            String estado = params.get(4);

            if (numero < 1 || numero > 32) {
                sendResponse(fromEmail, "Error", "El número de diente debe estar entre 1 y 32.");
                return;
            }

            if (dienteRepository.existsByNumero(numero)) {
                sendResponse(fromEmail, "Error", "Ya existe un diente con el número " + numero + ".");
                return;
            }

            Diente diente = new Diente();
            diente.setNombre(nombre);
            diente.setNumero(numero);
            diente.setTipo(tipo);
            diente.setUbicacion(ubicacion);
            diente.setEstado(estado);

            dienteRepository.save(diente);

            sendResponse(fromEmail, "Éxito", "Diente registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un diente: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para dientes.");
                }
            }
            sendResponse(fromEmail, "Listado de Dientes", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar dientes: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Parámetros: id[0], nombre[1], numero[2], tipo[3], ubicacion[4], estado[5]

            if (params.size() < 6) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para actualizar un diente.");
                return;
            }

            if (params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El ID del diente es obligatorio.");
                return;
            }

            Long id = Long.parseLong(params.get(0));

            Diente diente = dienteRepository.findById(id).orElse(null);

            if (diente == null) {
                sendResponse(fromEmail, "Error", "Diente con ID: " + id + " no encontrado.");
                return;
            }

            String nombre = params.get(1);
            String tipo = params.get(3);
            String ubicacion = params.get(4);
            String estado = params.get(5);

            if (!params.get(2).trim().isEmpty()) {
                int numero = Integer.parseInt(params.get(2));

                if (numero < 1 || numero > 32) {
                    sendResponse(fromEmail, "Error", "El número de diente debe estar entre 1 y 32.");
                    return;
                }

                if (!diente.getNumero().equals(numero) && dienteRepository.existsByNumero(numero)) {
                    sendResponse(fromEmail, "Error", "Ya existe un diente con el número " + numero + ".");
                    return;
                }

                diente.setNumero(numero);
            }

            if (nombre != null && !nombre.trim().isEmpty()) {
                diente.setNombre(nombre);
            }

            if (tipo != null && !tipo.trim().isEmpty()) {
                diente.setTipo(tipo);
            }

            if (ubicacion != null && !ubicacion.trim().isEmpty()) {
                diente.setUbicacion(ubicacion);
            }

            if (estado != null && !estado.trim().isEmpty()) {
                diente.setEstado(estado);
            }

            dienteRepository.save(diente);

            sendResponse(fromEmail, "Éxito", "Diente: " + diente.getId() + " actualizado correctamente.");

        } catch (NumberFormatException e) {
            sendResponse(fromEmail, "Error", "El ID y el número del diente deben ser valores numéricos.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar diente: " + e.getMessage());
        }
    }

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
        List<Diente> dientes = dienteRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("listado de Dientes\n");
        for (Diente diente : dientes) {
            sb.append(String.format("- [%s] Nombre: %s Número: %s Tipo: %s Ubicación: %s Estado: %s\n",
                    diente.getId(),
                    diente.getNombre(),
                    diente.getNumero(),
                    diente.getTipo(),
                    diente.getUbicacion(),
                    diente.getEstado()));
        }
        return sb;
    }

}
