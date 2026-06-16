package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Paciente;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.repositories.RolRepository;
import smail.sistema_mail_OdontoCool.validations.PacienteVal;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private CloudinaryServices cloudinaryServices;

    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private PasswordService passwordService;
    @Autowired
    private PacienteVal pacienteVal;

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
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Pacientes.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], ContactoEmerg[7], TelfEmerg[8], CORREO[9], PASS[10]
            if (params.size() < 11) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Paciente. Se requieren 11.");
                return;
            }

            String validationMsg = pacienteVal.insertValid(params);
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

            // Validar si ya existe este paciente
            Paciente pac = pacienteRepository.findById(ci).orElse(null);
            if (pac != null) {
                sendResponse(fromEmail, "Error", "Ya existe un paciente registrado con el CI: " + ci);
                return;
            }

            pac = new Paciente();
            pac.setPersona(persona);
            pac.setNombreContactoEmergencia(params.get(7));
            pac.setTelefonoEmergencia(params.get(8));
            pacienteRepository.save(pac);

            String firstLetter = (pac.getApellidos() != null && !pac.getApellidos().trim().isEmpty())
                    ? pac.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";

            String fotoUrl = (imagenesBase64 != null && !imagenesBase64.isEmpty())
                    ? cloudinaryServices.subirImagen(imagenesBase64.get(0))
                    : "null";

            Usuario u = new Usuario();
            u.setCodigoUsuario(firstLetter + pac.getCi() + "PAC");
            u.setCorreoElectronico(params.get(9));
            u.setContraseña(passwordService.hashPassword(params.get(10)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(persona);
            rolRepository.findByNombreIgnoreCase("PACIENTE").ifPresent(u::setRol);
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Paciente " + pac.getNombres() + " registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar paciente: " + e.getMessage());
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
            List<Paciente> lista;

            if ("*".equals(query)) {
                lista = pacienteRepository.findAll();
                sb.append("Lista de Pacientes:\n\n");
            } else {
                lista = pacienteRepository.searchByNameOrCi(query);
                sb.append("Resultados de búsqueda de Pacientes para '").append(query).append("':\n\n");
            }

            List<String> base64Images = new ArrayList<>();
            if (lista.isEmpty()) {
                sb.append("No se encontraron pacientes.\n");
            } else {
                for (Paciente p : lista) {
                    String codigo = "Sin código";
                    String email = "Sin correo";
                    String foto = "Sin foto";
                    Usuario u = usuarioRepository.findByPersonaCiAndSuffix(p.getCi(), "PAC").orElse(null);
                    if (u != null) {
                        if (u.getCodigoUsuario() != null && !u.getCodigoUsuario().trim().isEmpty()) {
                            codigo = u.getCodigoUsuario();
                        }
                        if (u.getCorreoElectronico() != null && !u.getCorreoElectronico().trim().isEmpty()) {
                            email = u.getCorreoElectronico();
                        }
                        if (u.getFotoUrl() != null && !u.getFotoUrl().equalsIgnoreCase("null")
                                && !u.getFotoUrl().trim().isEmpty()) {
                            foto = u.getFotoUrl();
                            if (lista.size() == 1) {
                                String b64 = descargarImagenBase64(foto);
                                if (b64 != null) {
                                    base64Images.add(b64);
                                }
                            }
                        }
                    }
                    sb.append("- Paciente:\n")
                            .append("  * CI: ").append(p.getCi()).append("\n")
                            .append("  * Nombres: ").append(p.getNombres()).append("\n")
                            .append("  * Apellidos: ").append(p.getApellidos()).append("\n")
                            .append("  * Dirección: ")
                            .append(p.getDireccion() != null ? p.getDireccion() : "No especificada").append("\n")
                            .append("  * Teléfono: ")
                            .append(p.getTelefono() != null ? p.getTelefono() : "No especificado").append("\n")
                            .append("  * Fecha Nacimiento: ")
                            .append(p.getFechaNacimiento() != null ? p.getFechaNacimiento() : "No especificada")
                            .append("\n")
                            .append("  * Emergencia: ")
                            .append(p.getTelefonoEmergencia() != null ? p.getTelefonoEmergencia() : "No especificado")
                            .append(" (")
                            .append(p.getNombreContactoEmergencia() != null ? p.getNombreContactoEmergencia()
                                    : "Sin nombre")
                            .append(")\n")
                            .append("  * Usuario: ").append(codigo).append("\n")
                            .append("  * Email: ").append(email).append("\n")
                            .append("  * Foto: ").append(foto).append("\n\n");
                }
            }
            sendResponse(fromEmail, "Listado de Pacientes", sb.toString(), base64Images.toArray(new String[0]));
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar pacientes: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {

            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], ContactoEmerg[7], TelfEmerg[8], CORREO[9], PASS[10]
            if (params.size() < 11) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Paciente. Se requieren 11.");
                return;
            }

            String validationMsg = pacienteVal.updateValid(params);
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
            String contactoEmergencia = params.get(7);
            String telefonoEmergencia = params.get(8);
            String correoElectronico = params.get(9);
            String contraseña = params.get(10);

            Paciente pac = pacienteRepository.findById(ci).orElse(null);
            if (pac == null) {
                sendResponse(fromEmail, "Error", "Paciente no encontrado.");
                return;
            }
            pac.setNombres(nombres);
            pac.setApellidos(apellidos);
            pac.setDireccion(direccion);
            pac.setGenero(genero);
            pac.setTelefono(telefono);
            pac.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            pac.setNombreContactoEmergencia(contactoEmergencia);
            pac.setTelefonoEmergencia(telefonoEmergencia);
            pacienteRepository.save(pac);

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

            sendResponse(fromEmail, "Éxito", "Paciente " + nombres + " actualizado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo actualizar paciente: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {
            String ci = params.get(0);
            Paciente pac = pacienteRepository.findById(ci).orElse(null);

            if (pac == null) {
                sendResponse(fromEmail, "Error", "Paciente no encontrado.");
                return;
            }

            Usuario u = usuarioRepository.findByPersona_Ci(ci).orElse(null);

            if (u != null) {
                usuarioRepository.delete(u);
            }
            pacienteRepository.delete(pac);

            sendResponse(fromEmail, "Éxito", "Paciente " + pac.getNombres() + " eliminado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo eliminar paciente: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body, String... base64Image) {
        try {
            smtpService.sendEmail(to, subject, body, base64Image);
        } catch (IOException e) {
            System.err.println("Error SMTP en PacienteService: " + e.getMessage());
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
