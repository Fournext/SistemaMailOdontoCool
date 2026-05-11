package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class PersonaService {

    @Autowired
    private PersonaRepository personaRepository;

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
                sendResponse(fromEmail, "Error", "Acción no soportada para Persona: " + action);
        }
    }

    private void insert(List<String> params, String fromEmail) {
        if (params.size() < 7) {
            sendResponse(fromEmail, "Error", "Faltan datos para registrar persona (se requieren 7 parámetros).");
            return;
        }
        try {
            Persona p = new Persona(params.get(0), params.get(1), params.get(2), params.get(3), params.get(4), params.get(5), params.get(6));
            personaRepository.save(p);
            sendResponse(fromEmail, "Éxito", "Persona " + p.getNombre() + " registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo insertar: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            if (params.isEmpty()) {
                sendResponse(fromEmail, "Error de Consulta", "Falta el parámetro de búsqueda ([\"*\"] o [\"CI\"]).");
                return;
            }
            String filter = params.get(0);
            List<Persona> lista = "*".equals(filter) ? personaRepository.findAll() : new ArrayList<>();
            if (!"*".equals(filter)) personaRepository.findById(filter).ifPresent(lista::add);

            if (lista.isEmpty()) {
                sendResponse(fromEmail, "Listando Personas", "No se encontró ninguna persona con el filtro: " + filter);
            } else {
                StringBuilder sb = new StringBuilder("Se han encontrado " + lista.size() + " personas:\n\n");
                for (Persona p : lista) sb.append(p.toString()).append("\n");
                sendResponse(fromEmail, "Listando Personas", sb.toString());
            }
        } catch (Exception e) {
            sendResponse(fromEmail, "Error de Base de Datos", "No se pudo realizar la consulta: " + e.getMessage());
        }
    }

    private void update(List<String> params, String fromEmail) {
        sendResponse(fromEmail, "Info", "Modificación de Persona no implementada aún.");
    }

    private void delete(List<String> params, String fromEmail) {
        sendResponse(fromEmail, "Info", "Eliminación de Persona no implementada aún.");
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error enviando respuesta: " + e.getMessage());
        }
    }
}
