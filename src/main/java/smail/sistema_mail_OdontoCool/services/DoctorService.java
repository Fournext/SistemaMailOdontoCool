package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Doctor;
import smail.sistema_mail_OdontoCool.entities.Especialidad;
import smail.sistema_mail_OdontoCool.repositories.DoctorRepository;
import smail.sistema_mail_OdontoCool.repositories.EspecialidadRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EspecialidadRepository especialidadRepository;

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
            case "ASE":
                asignarEspecialidad(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Doctores.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Direccion[3], Genero[4],
            // Telefono[5], FechaNac[6], Exp[7], Matricula[8]
            if (params.size() < 9) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Doctor. Se requieren 9.");
                return;
            }

            Doctor d = new Doctor();
            d.setCi(params.get(0));
            d.setNombres(params.get(1));
            d.setApellidos(params.get(2));
            d.setDireccion(params.get(3));
            d.setGenero(params.get(4));
            d.setTelefono(params.get(5));
            d.setFechaNacimiento(LocalDate.parse(params.get(6)));

            d.setTiempoExperiencia(params.get(7));
            d.setMatriculaProfesional(params.get(8));
            d.setFechaContratacion(LocalDate.now());
            doctorRepository.save(d);

            sendResponse(fromEmail, "Éxito", "Doctor(a) " + d.getNombres() + " registrado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar doctor: " + e.getMessage());
        }
    }

    @Transactional
    public void asignarEspecialidad(List<String> params, String fromEmail) {
        try {
            if (params.size() < 2) {
                sendResponse(fromEmail, "Error", "Faltan parámetros. Se requiere CI doctor e ID especialidad.");
                return;
            }

            String doctorCi = params.get(0);
            UUID especialidadId = UUID.fromString(params.get(1));

            Doctor doctor = doctorRepository.findById(doctorCi)
                    .orElseThrow(() -> new RuntimeException("Doctor no encontrado"));

            Especialidad especialidad = especialidadRepository.findById(especialidadId)
                    .orElseThrow(() -> new RuntimeException("Especialidad no encontrada"));

            doctor.getEspecialidades().add(especialidad);
            doctorRepository.save(doctor);

            sendResponse(fromEmail, "Éxito", "Especialidad asignada correctamente al doctor.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo asignar la especialidad. Detalles: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Doctores.");
                }

            }

            sendResponse(fromEmail, "Listado de Doctores", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar doctores: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Doctor> lista = doctorRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Doctores:\n\n");
        for (Doctor d : lista) {
            sb.append(String.format(
                    "- [%s] Dr. %s %s Direccion: %s Genero %s Telefono %s Nacimiento %s (Matrícula: %s)\n",
                    d.getCi(),
                    d.getNombres(),
                    d.getApellidos(),
                    d.getDireccion(),
                    d.getGenero(),
                    d.getTelefono(),
                    d.getFechaNacimiento(),
                    d.getMatriculaProfesional()));
        }
        return sb;
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }
}
