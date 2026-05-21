package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Secretaria;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class SecretariaService {

    @Autowired
    private SecretariaRepository secretariaRepository;

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
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Secretarias.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], FContrat[7]
            if (params.size() < 8) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Secretaria. Se requieren 8.");
                return;
            }

            Secretaria s = new Secretaria();
            s.setCi(params.get(0));
            s.setNombres(params.get(1));
            s.setApellidos(params.get(2));
            s.setDireccion(params.get(3));
            s.setGenero(params.get(4));
            s.setTelefono(params.get(5));
            s.setFechaNacimiento(LocalDate.parse(params.get(6)));

            s.setFechaContratacion(LocalDate.parse(params.get(8)));
            secretariaRepository.save(s);

            sendResponse(fromEmail, "Éxito", "Secretaria " + s.getNombres() + " registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar secretaria: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Secretarias.");
                }

            }
            sendResponse(fromEmail, "Listado de Secretarias", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar secretarias: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Secretaria> lista = secretariaRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Secretarias:\n\n");
        for (Secretaria s : lista) {
            sb.append(
                    String.format("- [%s] %s %s Direccion: %s Genero %s Telefono %s Nacimiento %s (Contratación: %s)\n",
                            s.getCi(),
                            s.getNombres(),
                            s.getApellidos(),
                            s.getDireccion(),
                            s.getGenero(),
                            s.getTelefono(),
                            s.getFechaNacimiento(),
                            s.getFechaContratacion()));
        }
        return sb;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en SecretariaService: " + e.getMessage());
        }
    }
}
