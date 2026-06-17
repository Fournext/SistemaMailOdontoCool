package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Persona;
import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.PersonaRepository;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.repositories.RolRepository;
import smail.sistema_mail_OdontoCool.validations.PropietarioVal;

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
    private PropietarioVal propietarioVal;

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
                sendResponse(fromEmail, "Error", "Acción no soportada para Propietarios.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }

            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], Porcentaje[7], EMAIL[8], PASSWORD[9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Propietario. Se requieren 10.");
                return;
            }

            String validationMsg = propietarioVal.insertValid(params);
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

            Propietario prop = new Propietario();
            prop.setPersona(persona);
            prop.setPorcentajeParticipacion(new BigDecimal(params.get(7)));
            propietarioRepository.save(prop);

            String firstLetter = (prop.getApellidos() != null && !prop.getApellidos().trim().isEmpty())
                    ? prop.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";
            String fotoUrl = (imagenesBase64 != null && !imagenesBase64.isEmpty())
                    ? cloudinaryServices.subirImagen(imagenesBase64.get(0))
                    : "null";

            Usuario u = new Usuario();
            u.setCodigoUsuario(firstLetter + prop.getCi() + "PRP");
            u.setCorreoElectronico(params.get(8));
            u.setContraseña(passwordService.hashPassword(params.get(9)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(persona);
            rolRepository.findByNombreIgnoreCase("PROPIETARIO").ifPresent(u::setRol);
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + prop.getNombres() + " registrado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar propietario: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        try {
            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }

            if (params.size() == 0 || params.get(0).trim().isEmpty()) {
                sendResponse(fromEmail, "Error",
                        "Falta especificar tipo de listado o término de búsqueda. Verifique el formato de comandos en la ayuda (HELP).");
                return;
            }

            String query = params.get(0).trim();
            StringBuilder sb = new StringBuilder();
            List<Propietario> lista;

            if ("*".equals(query)) {
                lista = propietarioRepository.findAll();
                sb.append("Lista de Propietarios:\n\n");
            } else {
                lista = propietarioRepository.searchByNameOrCi(query);
                sb.append("Resultados de búsqueda de Propietarios para '").append(query).append("':\n\n");
            }

            java.util.List<String> base64Images = new java.util.ArrayList<>();
            if (lista.isEmpty()) {
                sb.append("No se encontraron propietarios.\n");
            } else {
                for (Propietario p : lista) {
                    String codigo = "Sin código";
                    String email = "Sin correo";
                    String foto = "Sin foto";
                    Usuario u = usuarioRepository.findByPersonaCiAndSuffix(p.getCi(), "PRP").orElse(null);
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
                    sb.append("- Propietario:\n")
                            .append("  * CI: ").append(p.getCi()).append("\n")
                            .append("  * Nombre: ").append(p.getNombres()).append(" ").append(p.getApellidos())
                            .append("\n")
                            .append("  * Dirección: ")
                            .append(p.getDireccion() != null ? p.getDireccion() : "No especificada").append("\n")
                            .append("  * Género: ").append(p.getGenero() != null ? p.getGenero() : "No especificado")
                            .append("\n")
                            .append("  * Teléfono: ")
                            .append(p.getTelefono() != null ? p.getTelefono() : "No especificado").append("\n")
                            .append("  * Fecha Nacimiento: ")
                            .append(p.getFechaNacimiento() != null ? p.getFechaNacimiento() : "No especificada")
                            .append("\n")
                            .append("  * Fecha Registro: ")
                            .append(p.getFechaRegistro() != null ? p.getFechaRegistro() : "No especificada")
                            .append("\n")
                            .append("  * Participación: ")
                            .append(p.getPorcentajeParticipacion() != null ? p.getPorcentajeParticipacion() : "0.00")
                            .append("%\n")
                            .append("  * Usuario: ").append(codigo).append("\n")
                            .append("  * Email: ").append(email).append("\n")
                            .append("  * Foto: ").append(foto).append("\n\n");
                }
            }
            sendResponse(fromEmail, "Listado de Propietarios", sb.toString(), base64Images.toArray(new String[0]));
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar propietarios: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {

            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }

            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], Porcentaje[7], EMAIL[8], PASSWORD[9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Propietario. Se requieren 10.");
                return;
            }

            String ci = params.size() > 0 ? params.get(0) : "";
            if (ci.trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "El CI del propietario es requerido para actualizar.");
                return;
            }

            String validationMsg = propietarioVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error", validationMsg);
                return;
            }

            String nombres = params.get(1);
            String apellidos = params.get(2);
            String direccion = params.get(3);
            String genero = params.get(4);
            String telefono = params.get(5);
            String fechaNacimiento = params.get(6);
            String porcentaje = params.get(7);
            String correoElectronico = params.get(8);
            String contraseña = params.get(9);

            Propietario prop = propietarioRepository.findById(ci).orElse(null);
            if (nombres != null && !nombres.trim().isEmpty()) {
                prop.setNombres(nombres);
            }
            if (apellidos != null && !apellidos.trim().isEmpty()) {
                prop.setApellidos(apellidos);
            }
            if (direccion != null && !direccion.trim().isEmpty()) {
                prop.setDireccion(direccion);
            }
            if (genero != null && !genero.trim().isEmpty()) {
                prop.setGenero(genero);
            }
            if (telefono != null && !telefono.trim().isEmpty()) {
                prop.setTelefono(telefono);
            }
            if (fechaNacimiento != null && !fechaNacimiento.trim().isEmpty()) {
                prop.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            }
            if (porcentaje != null && !porcentaje.trim().isEmpty()) {
                prop.setPorcentajeParticipacion(new BigDecimal(porcentaje));
            }
            propietarioRepository.save(prop);

            Usuario u = usuarioRepository.findByPersonaCiAndSuffix(ci, "PRP").orElse(null);
            if (u == null) {
                sendResponse(fromEmail, "Error", "Usuario no encontrado.");
                return;
            }
            if (correoElectronico != null && !correoElectronico.trim().isEmpty()) {
                u.setCorreoElectronico(correoElectronico);
            }
            if (contraseña != null && !contraseña.trim().isEmpty()) {
                u.setContraseña(passwordService.hashPassword(contraseña));
            }
            if (imagenesBase64 != null && !imagenesBase64.isEmpty()) {
                String fotoUrl = cloudinaryServices.subirImagen(imagenesBase64.get(0));
                u.setFotoUrl(fotoUrl);
            }
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + prop.getNombres() + " actualizado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo actualizar propietario: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }

            String ci = params.get(0);
            Propietario prop = propietarioRepository.findById(ci).orElse(null);

            if (prop == null) {
                sendResponse(fromEmail, "Error", "Propietario no encontrado.");
                return;
            }

            Usuario u = usuarioRepository.findByPersonaCiAndSuffix(ci, "PRP").orElse(null);

            if (u != null) {
                usuarioRepository.delete(u);
            }
            propietarioRepository.delete(prop);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + prop.getNombres() + " eliminado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo eliminar propietario: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body, String... base64Image) {
        try {
            smtpService.sendEmail(to, subject, body, base64Image);
        } catch (IOException e) {
            System.err.println("Error SMTP en PropietarioService: " + e.getMessage());
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
