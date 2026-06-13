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
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.DoctorRepository;
import smail.sistema_mail_OdontoCool.repositories.EspecialidadRepository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.validations.DoctorVal;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PersonaRepository personaRepository;

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
    @Autowired
    private DoctorVal doctorVal;

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
            // Telefono[5], FechaNac[6], Exp[7], Matricula[8], TelfProf[9], CORREO[10],
            // PASS[11],
            if (params.size() < 12) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Doctor. Se requieren 12.");
                return;
            }

            String validationMsg = doctorVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error", validationMsg);
                return;
            }

            String ci = params.get(0);
            String nombres = params.get(1);
            String apellidos = params.get(2);
            String direccion = params.get(3);
            String genero = params.get(4);
            String telefono = params.get(5);
            LocalDate fechaNac = LocalDate.parse(params.get(6).replace('/', '-'));

            Persona persona = personaRepository.findById(ci).orElse(null);
            if (persona != null) {
                // Validar que los datos de la persona coincidan con la registrada
                if (!persona.getNombres().equalsIgnoreCase(nombres.trim()) ||
                        !persona.getApellidos().equalsIgnoreCase(apellidos.trim()) ||
                        !persona.getDireccion().equalsIgnoreCase(direccion.trim()) ||
                        !persona.getGenero().equalsIgnoreCase(genero.trim()) ||
                        !persona.getTelefono().equalsIgnoreCase(telefono.trim()) ||
                        !persona.getFechaNacimiento().equals(fechaNac)) {
                    sendResponse(fromEmail, "Error",
                            "La persona con CI (" + ci + ") ya existe registrada con datos distintos.");
                    return;
                }
            } else {
                persona = new Persona();
                persona.setCi(ci);
                persona.setNombres(nombres);
                persona.setApellidos(apellidos);
                persona.setDireccion(direccion);
                persona.setGenero(genero);
                persona.setTelefono(telefono);
                persona.setFechaNacimiento(fechaNac);
                persona = personaRepository.save(persona);
            }

            // Validar si ya existe este doctor
            Doctor d = doctorRepository.findById(ci).orElse(null);
            if (d != null) {
                sendResponse(fromEmail, "Error", "Ya existe un doctor registrado con el CI: " + ci);
                return;
            }

            d = new Doctor();
            d.setPersona(persona);
            d.setTiempoExperiencia(params.get(7));
            d.setMatriculaProfesional(params.get(8));
            d.setTelefonoProfesional(params.get(9));
            d.setFechaContratacion(LocalDate.now());
            doctorRepository.save(d);

            // Codigo de usuario: AP + CI
            String firstLetter = (d.getApellidos() != null && !d.getApellidos().trim().isEmpty())
                    ? d.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";

            // Convertir a base 64 y Subir a Cloudinary
            String fotoUrl = (imagenesBase64 != null && !imagenesBase64.isEmpty())
                    ? cloudinaryServices.subirImagen(imagenesBase64.get(0))
                    : "null";

            Usuario u = new Usuario();
            u.setCodigoUsuario(firstLetter + d.getCi() + "DOC");
            u.setCorreoElectronico(params.get(10));
            u.setContraseña(passwordService.hashPassword(params.get(11)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(persona);
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

            doctor.addEspecialidad(especialidad);
            doctorRepository.save(doctor);

            sendResponse(fromEmail, "Éxito", "Especialidad asignada correctamente al doctor.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo asignar la especialidad. Detalles: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            if (params.size() == 0 || params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado o término de búsqueda. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }

            String query = params.get(0).trim();
            StringBuilder sb = new StringBuilder();
            List<Doctor> lista;

            if ("*".equals(query)) {
                lista = doctorRepository.findAll();
                sb.append("Lista de Doctores:\n\n");
            } else {
                lista = doctorRepository.searchByNameOrCi(query);
                sb.append("Resultados de búsqueda de Doctores para '").append(query).append("':\n\n");
            }

            java.util.List<String> base64Images = new java.util.ArrayList<>();
            if (lista.isEmpty()) {
                sb.append("No se encontraron doctores.\n");
            } else {
                for (Doctor d : lista) {
                    String codigo = "Sin código";
                    String email = "Sin correo";
                    String foto = "Sin foto";
                    Usuario u = usuarioRepository.findByPersonaCiAndSuffix(d.getCi(), "DOC").orElse(null);
                    if (u != null) {
                        if (u.getCodigoUsuario() != null && !u.getCodigoUsuario().trim().isEmpty()) {
                            codigo = u.getCodigoUsuario();
                        }
                        if (u.getCorreoElectronico() != null && !u.getCorreoElectronico().trim().isEmpty()) {
                            email = u.getCorreoElectronico();
                        }
                        if (u.getFotoUrl() != null && !u.getFotoUrl().equalsIgnoreCase("null") && !u.getFotoUrl().trim().isEmpty()) {
                            foto = u.getFotoUrl();
                            if (lista.size() == 1) {
                                String b64 = descargarImagenBase64(foto);
                                if (b64 != null) {
                                    base64Images.add(b64);
                                }
                            }
                        }
                    }
                    sb.append("- Doctor:\n")
                      .append("  * CI: ").append(d.getCi()).append("\n")
                      .append("  * Nombre: Dr. ").append(d.getNombres()).append(" ").append(d.getApellidos()).append("\n")
                      .append("  * Dirección: ").append(d.getDireccion() != null ? d.getDireccion() : "No especificada").append("\n")
                      .append("  * Género: ").append(d.getGenero() != null ? d.getGenero() : "No especificado").append("\n")
                      .append("  * Teléfono: ").append(d.getTelefono() != null ? d.getTelefono() : "No especificado").append("\n")
                      .append("  * Fecha Nacimiento: ").append(d.getFechaNacimiento() != null ? d.getFechaNacimiento() : "No especificada").append("\n")
                      .append("  * Matrícula: ").append(d.getMatriculaProfesional() != null ? d.getMatriculaProfesional() : "No especificada").append("\n")
                      .append("  * Teléfono Prof.: ").append(d.getTelefonoProfesional() != null ? d.getTelefonoProfesional() : "No especificado").append("\n")
                      .append("  * Usuario: ").append(codigo).append("\n")
                      .append("  * Email: ").append(email).append("\n")
                      .append("  * Foto: ").append(foto).append("\n\n");
                }
            }
            sendResponse(fromEmail, "Listado de Doctores", sb.toString(), base64Images.toArray(new String[0]));
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar doctores: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {

            // Parámetros: CI[0], Nombres[1], Apellidos[2], Direccion[3], Genero[4],
            // Telefono[5], FechaNac[6], Exp[7], Matricula[8], TelfProf[9], CORREO[10],
            // PASS[11],
            if (params.size() < 12) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Doctor. Se requieren 12.");
                return;
            }

            String validationMsg = doctorVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error", validationMsg);
                return;
            }

            String ci = params.get(0);
            String nombres = params.get(1);
            String apellidos = params.get(2);
            String direccion = params.get(3);
            String genero = params.get(4);
            String telefono = params.get(5);
            String fechaNacimiento = params.get(6);
            String tiempoExperiencia = params.get(7);
            String matriculaProfesional = params.get(8);
            String telefonoProfesional = params.get(9);
            String correoElectronico = params.get(10);
            String contraseña = params.get(11);

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
            d.setTelefonoProfesional(telefonoProfesional);
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

    private void sendResponse(String to, String subject, String body, String... base64Image) {
        try {
            smtpService.sendEmail(to, subject, body, base64Image);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

    private String descargarImagenBase64(String urlStr) {
        if (urlStr == null || urlStr.trim().isEmpty() || "null".equalsIgnoreCase(urlStr.trim())) {
            return null;
        }
        try {
            java.net.URL url = java.net.URI.create(urlStr).toURL();
            try (java.io.InputStream in = url.openStream();
                 java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int n;
                while (-1 != (n = in.read(buffer))) {
                    out.write(buffer, 0, n);
                }
                return java.util.Base64.getEncoder().encodeToString(out.toByteArray());
            }
        } catch (Exception e) {
            System.err.println("Error al descargar imagen: " + e.getMessage());
            return null;
        }
    }
}
