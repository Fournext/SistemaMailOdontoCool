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
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CloudinaryServices cloudinaryServices;

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private PasswordService passwordService;

    public void handle(String action, List<String> params, String fromEmail, List<String> imagenesBase64) {
        switch (action) {
            case "INS":
                insert(params, fromEmail, imagenesBase64);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "MOD":
                update(params, fromEmail, imagenesBase64);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            case "ASE":
                asignarEspecialidad(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para Doctores.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Direccion[3], Genero[4],
            // Telefono[5], FechaNac[6], Exp[7], Matricula[8], CORREO[9], PASS[10],
            if (params.size() < 11) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Doctor. Se requieren 11.");
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

            // Codigo de usuario: AP + CI
            String firstLetter = (d.getApellidos() != null && !d.getApellidos().trim().isEmpty())
                    ? d.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";

            // Convertir a base 64 y Subir a Cloudinary
            String fotoUrl = cloudinaryServices.subirImagen(imagenesBase64.get(0));

            Usuario u = new Usuario();
            u.setCodigoUsuario(firstLetter + d.getCi());
            u.setCorreoElectronico(params.get(9));
            u.setContraseña(passwordService.hashPassword(params.get(10)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(d);
            usuarioRepository.save(u);

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

    @Transactional
    private void update(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            String ci = params.get(0);
            String nombres = params.get(1);
            String apellidos = params.get(2);
            String direccion = params.get(3);
            String genero = params.get(4);
            String telefono = params.get(5);
            String fechaNacimiento = params.get(6);
            String tiempoExperiencia = params.get(7);
            String matriculaProfesional = params.get(8);
            String correoElectronico = params.get(9);
            String contraseña = params.get(10);

            Doctor d = doctorRepository.findById(ci).orElse(null);
            if (d == null) {
                sendResponse(fromEmail, "Error", "Doctor no encontrado.");
                return;
            }
            d.setNombres(nombres);
            d.setApellidos(apellidos);
            d.setDireccion(direccion);
            d.setGenero(genero);
            d.setTelefono(telefono);
            d.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            d.setTiempoExperiencia(tiempoExperiencia);
            d.setMatriculaProfesional(matriculaProfesional);
            doctorRepository.save(d);

            Usuario u = usuarioRepository.findByPersona_Ci(ci).orElse(null);
            if (u == null) {
                sendResponse(fromEmail, "Error", "Usuario no encontrado.");
                return;
            }
            u.setCorreoElectronico(correoElectronico);
            u.setContraseña(passwordService.hashPassword(contraseña));
            if (imagenesBase64 != null && !imagenesBase64.isEmpty()) {
                String fotoUrl = cloudinaryServices.subirImagen(imagenesBase64.get(0));
                u.setFotoUrl(fotoUrl);
            }
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Doctor(a) " + nombres + " actualizado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo actualizar doctor: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {
            String ci = params.get(0);
            Doctor d = doctorRepository.findById(ci).orElse(null);

            if (d == null) {
                sendResponse(fromEmail, "Error", "Doctor no encontrado.");
                return;
            }

            Usuario u = usuarioRepository.findByPersona_Ci(ci).orElse(null);

            if (u != null) {
                usuarioRepository.delete(u);
            }
            doctorRepository.delete(d);

            sendResponse(fromEmail, "Éxito", "Doctor(a) " + d.getNombres() + " eliminado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo eliminar doctor: " + e.getMessage());
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
