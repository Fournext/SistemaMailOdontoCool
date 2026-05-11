package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Doctores;
import smail.sistema_mail_OdontoCool.entities.Personas;
import smail.sistema_mail_OdontoCool.repositories.DoctorRepository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

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
                list(fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Doctores.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Direccion[3], Genero[4],
            // Telefono[5], FechaNac[6], Codigo[7], Exp[8], Matricula[9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Doctor. Se requieren 10.");
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

            Doctores d = new Doctores();
            d.setCodigoDoctor(params.get(7));
            d.setTiempoExperiencia(params.get(8));
            d.setMatriculaProfesional(params.get(9));
            d.setFechaContratacion(LocalDate.now());
            d.setPersona(p);
            doctorRepository.save(d);

            sendResponse(fromEmail, "Éxito", "Doctor(a) " + p.getNombres() + " registrado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar doctor: " + e.getMessage());
        }
    }

    private void list(String fromEmail) {
        try {
            List<Doctores> lista = doctorRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Doctores:\n\n");
            for (Doctores d : lista) {
                sb.append(String.format("- [%s] Dr. %s %s (Matrícula: %s)\n",
                        d.getCodigoDoctor(), d.getPersona().getNombres(), d.getPersona().getApellidos(),
                        d.getMatriculaProfesional()));
            }
            sendResponse(fromEmail, "Listado de Doctores", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar doctores: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}
