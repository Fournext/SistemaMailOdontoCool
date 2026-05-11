package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Pacientes;
import smail.sistema_mail_OdontoCool.entities.Personas;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private SmtpClientService smtpService;

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS": insert(params, fromEmail); break;
            case "LIS": list(fromEmail); break;
            default: sendResponse(fromEmail, "Error", "Acción no soportada para Pacientes.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5], FNac[6], CodPac[7], ContactoEmerg[8], TelfEmerg[9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Paciente. Se requieren 10.");
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

            Pacientes pac = new Pacientes();
            pac.setCodigoPaciente(params.get(7));
            pac.setNombreContactoEmergencia(params.get(8));
            pac.setTelefonoEmergencia(params.get(9));
            pac.setPersona(p);
            pacienteRepository.save(pac);

            sendResponse(fromEmail, "Éxito", "Paciente " + p.getNombres() + " registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar paciente: " + e.getMessage());
        }
    }

    private void list(String fromEmail) {
        try {
            List<Pacientes> lista = pacienteRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Pacientes:\n\n");
            for (Pacientes p : lista) {
                sb.append(String.format("- [%s] %s %s (Emergencia: %s)\n", 
                    p.getCodigoPaciente(), p.getPersona().getNombres(), p.getPersona().getApellidos(), p.getNombreContactoEmergencia()));
            }
            sendResponse(fromEmail, "Listado de Pacientes", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar pacientes: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en PacienteService: " + e.getMessage());
        }
    }
}
