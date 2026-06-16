package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Secretaria;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.entities.Rol;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.repositories.RolRepository;
import smail.sistema_mail_OdontoCool.validations.SecretariaVal;

@Service
public class SecretariaService {

    @Autowired
    private SecretariaRepository secretariaRepository;

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
    private SecretariaVal secretariaVal;

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
                sendResponse(fromEmail, "Error", "Acción no soportada para Secretarias.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], FContrat[7], [EMAIL_ADDRESS][8], [PASSWORD][9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Secretaria. Se requieren 10.");
                return;
            }

            String validationMsg = secretariaVal.insertValid(params);
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

            // Validar si ya existe esta secretaria
            Secretaria s = secretariaRepository.findById(ci).orElse(null);
            if (s != null) {
                sendResponse(fromEmail, "Error", "Ya existe una secretaria registrada con el CI: " + ci);
                return;
            }

            s = new Secretaria();
            s.setPersona(persona);
            s.setFechaContratacion(LocalDate.parse(params.get(7).replace('/', '-')));
            secretariaRepository.save(s);

            String firstLetter = (s.getApellidos() != null && !s.getApellidos().trim().isEmpty())
                    ? s.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";

            String fotoUrl = (imagenesBase64 != null && !imagenesBase64.isEmpty())
                    ? cloudinaryServices.subirImagen(imagenesBase64.get(0))
                    : "null";

            Usuario u = new Usuario();
            u.setCodigoUsuario(firstLetter + s.getCi() + "SEC");
            u.setCorreoElectronico(params.get(8));
            u.setContraseña(passwordService.hashPassword(params.get(9)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(persona);
            rolRepository.findByNombreIgnoreCase("SECRETARIA").ifPresent(u::setRol);
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Secretaria " + s.getNombres() + " registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar secretaria: " + e.getMessage());
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
            List<Secretaria> lista;

            if ("*".equals(query)) {
                lista = secretariaRepository.findAll();
                sb.append("Lista de Secretarias:\n\n");
            } else {
                lista = secretariaRepository.searchByNameOrCi(query);
                sb.append("Resultados de búsqueda de Secretarias para '").append(query).append("':\n\n");
            }

            java.util.List<String> base64Images = new java.util.ArrayList<>();
            if (lista.isEmpty()) {
                sb.append("No se encontraron secretarias.\n");
            } else {
                for (Secretaria s : lista) {
                    String codigo = "Sin código";
                    String email = "Sin correo";
                    String foto = "Sin foto";
                    Usuario u = usuarioRepository.findByPersonaCiAndSuffix(s.getCi(), "SEC").orElse(null);
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
                    sb.append("- Secretaria:\n")
                      .append("  * CI: ").append(s.getCi()).append("\n")
                      .append("  * Nombre: ").append(s.getNombres()).append(" ").append(s.getApellidos()).append("\n")
                      .append("  * Dirección: ").append(s.getDireccion() != null ? s.getDireccion() : "No especificada").append("\n")
                      .append("  * Género: ").append(s.getGenero() != null ? s.getGenero() : "No especificado").append("\n")
                      .append("  * Teléfono: ").append(s.getTelefono() != null ? s.getTelefono() : "No especificado").append("\n")
                      .append("  * Fecha Nacimiento: ").append(s.getFechaNacimiento() != null ? s.getFechaNacimiento() : "No especificada").append("\n")
                      .append("  * Fecha Contratación: ").append(s.getFechaContratacion() != null ? s.getFechaContratacion() : "No especificada").append("\n")
                      .append("  * Usuario: ").append(codigo).append("\n")
                      .append("  * Email: ").append(email).append("\n")
                      .append("  * Foto: ").append(foto).append("\n\n");
                }
            }
            sendResponse(fromEmail, "Listado de Secretarias", sb.toString(), base64Images.toArray(new String[0]));
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar secretarias: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {

            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], FContrat[7], [EMAIL_ADDRESS][8], [PASSWORD][9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Secretaria. Se requieren 10.");
                return;
            }

            String validationMsg = secretariaVal.updateValid(params);
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
            String fechaContratacion = params.get(7);
            String correoElectronico = params.get(8);
            String contraseña = params.get(9);

            Secretaria s = secretariaRepository.findById(ci).orElse(null);
            if (s == null) {
                sendResponse(fromEmail, "Error", "Secretaria no encontrada.");
                return;
            }
            s.setNombres(nombres);
            s.setApellidos(apellidos);
            s.setDireccion(direccion);
            s.setGenero(genero);
            s.setTelefono(telefono);
            s.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            s.setFechaContratacion(LocalDate.parse(fechaContratacion));
            secretariaRepository.save(s);

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

            sendResponse(fromEmail, "Éxito", "Secretaria " + nombres + " actualizada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo actualizar secretaria: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {
            String ci = params.get(0);
            Secretaria s = secretariaRepository.findById(ci).orElse(null);

            if (s == null) {
                sendResponse(fromEmail, "Error", "Secretaria no encontrada.");
                return;
            }

            Usuario u = usuarioRepository.findByPersona_Ci(ci).orElse(null);

            if (u != null) {
                usuarioRepository.delete(u);
            }
            secretariaRepository.delete(s);

            sendResponse(fromEmail, "Éxito", "Secretaria " + s.getNombres() + " eliminada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo eliminar secretaria: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body, String... base64Image) {
        try {
            smtpService.sendEmail(to, subject, body, base64Image);
        } catch (IOException e) {
            System.err.println("Error SMTP en SecretariaService: " + e.getMessage());
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
