package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PropietarioService {

    @Autowired
    private PropietarioRepository propietarioRepository;

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
                sendResponse(fromEmail, "Error", "Acción no soportada para Propietarios.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], Porcentaje[7]
            if (params.size() < 8) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Propietario. Se requieren 9.");
                return;
            }

            Propietario prop = new Propietario();
            prop.setCi(params.get(0));
            prop.setNombres(params.get(1));
            prop.setApellidos(params.get(2));
            prop.setDireccion(params.get(3));
            prop.setGenero(params.get(4));
            prop.setTelefono(params.get(5));
            prop.setFechaNacimiento(LocalDate.parse(params.get(6)));

            prop.setPorcentajeParticipacion(new BigDecimal(params.get(7)));
            propietarioRepository.save(prop);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + prop.getNombres() + " registrado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar propietario: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Propietarios.");
                }

            }
            sendResponse(fromEmail, "Listado de Propietarios", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar propietarios: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Propietario> lista = propietarioRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Propietarios:\n\n");
        for (Propietario p : lista) {
            sb.append(String.format("- [%s] %s %s %s (Participación: %s%%)\n",
                    p.getCi(),
                    p.getNombres(),
                    p.getApellidos(),
                    p.getFechaRegistro(),
                    p.getPorcentajeParticipacion()));
        }
        return sb;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en PropietarioService: " + e.getMessage());
        }
    }
}
