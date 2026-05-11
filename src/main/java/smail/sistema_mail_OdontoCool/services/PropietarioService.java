package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Personas;
import smail.sistema_mail_OdontoCool.entities.Propietarios;
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
            case "INS": insert(params, fromEmail); break;
            case "LIS": list(fromEmail); break;
            default: sendResponse(fromEmail, "Error", "Acción no soportada para Propietarios.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5], FNac[6], FInicio[7], Porcentaje[8]
            if (params.size() < 9) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Propietario. Se requieren 9.");
                return;
            }

            Personas p = new Personas();
            p.setCi(params.get(0));
            p.setNombres(params.get(1));
            p.setApellidos(params.get(2));
            p.setDireccion(params.get(3));
            p.setGenero(params.get(4));
            p.setTelefono(params.get(5));
            p.setFechaNacimiento(LocalDate.parse(params.get(6)));
            personaRepository.save(p);

            Propietarios prop = new Propietarios();
            prop.setFechaInicio(LocalDate.parse(params.get(7)));
            prop.setPorcentajeParticipacion(new BigDecimal(params.get(8)));
            prop.setPersona(p);
            propietarioRepository.save(prop);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + p.getNombres() + " registrado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar propietario: " + e.getMessage());
        }
    }

    private void list(String fromEmail) {
        try {
            List<Propietarios> lista = propietarioRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Propietarios:\n\n");
            for (Propietarios p : lista) {
                sb.append(String.format("- %s %s (Participación: %s%%)\n", 
                    p.getPersona().getNombres(), p.getPersona().getApellidos(), p.getPorcentajeParticipacion()));
            }
            sendResponse(fromEmail, "Listado de Propietarios", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar propietarios: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en PropietarioService: " + e.getMessage());
        }
    }
}
