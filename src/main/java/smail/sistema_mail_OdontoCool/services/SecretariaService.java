package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Personas;
import smail.sistema_mail_OdontoCool.entities.Secretarias;
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
            case "INS": insert(params, fromEmail); break;
            case "LIS": list(fromEmail); break;
            default: sendResponse(fromEmail, "Error", "Acción no soportada para Secretarias.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5], FNac[6], CodSec[7], FContrat[8]
            if (params.size() < 9) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Secretaria. Se requieren 9.");
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

            Secretarias s = new Secretarias();
            s.setCodigoSecretaria(params.get(7));
            s.setFechaContratacion(LocalDate.parse(params.get(8)));
            s.setPersona(p);
            secretariaRepository.save(s);

            sendResponse(fromEmail, "Éxito", "Secretaria " + p.getNombres() + " registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar secretaria: " + e.getMessage());
        }
    }

    private void list(String fromEmail) {
        try {
            List<Secretarias> lista = secretariaRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Secretarias:\n\n");
            for (Secretarias s : lista) {
                sb.append(String.format("- [%s] %s %s (Contratación: %s)\n", 
                    s.getCodigoSecretaria(), s.getPersona().getNombres(), s.getPersona().getApellidos(), s.getFechaContratacion()));
            }
            sendResponse(fromEmail, "Listado de Secretarias", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar secretarias: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en SecretariaService: " + e.getMessage());
        }
    }
}
