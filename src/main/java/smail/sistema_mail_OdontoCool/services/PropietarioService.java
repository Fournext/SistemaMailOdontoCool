package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Propietario;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.PropietarioRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PropietarioService {

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CloudinaryServices cloudinaryServices;
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
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Propietarios.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail, List<String> imagenesBase64) {
        try {
            // Parámetros: CI[0], Nombres[1], Apellidos[2], Dir[3], Gen[4], Telf[5],
            // FNac[6], Porcentaje[7], EMAIL[8], PASSWORD[9]
            if (params.size() < 10) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Propietario. Se requieren 10.");
                return;
            }

            Propietario prop = new Propietario();
            prop.setCi(params.get(0));
            prop.setNombres(params.get(1));
            prop.setApellidos(params.get(2));
            prop.setDireccion(params.get(3));
            prop.setGenero(params.get(4));
            prop.setTelefono(params.get(5));
            prop.setFechaNacimiento(LocalDate.parse(params.get(6)));

            prop.setPorcentajeParticipacion(new BigDecimal(params.get(7)));
            propietarioRepository.save(prop);

            String firstLetter = (prop.getApellidos() != null && !prop.getApellidos().trim().isEmpty())
                    ? prop.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";
            String fotoUrl = cloudinaryServices.subirImagen(imagenesBase64.get(0));

            Usuario u = new Usuario();
            u.setCodigoUsuario(firstLetter + prop.getCi());
            u.setCorreoElectronico(params.get(8));
            u.setContraseña(passwordService.hashPassword(params.get(9)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(prop);
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + prop.getNombres() + " registrado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar propietario: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Propietarios.");
                }

            }
            sendResponse(fromEmail, "Listado de Propietarios", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar propietarios: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Propietario> lista = propietarioRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Propietarios:\n\n");
        for (Propietario p : lista) {
            sb.append(String.format("- [%s] %s %s %s (Participación: %s%%)\n",
                    p.getCi(),
                    p.getNombres(),
                    p.getApellidos(),
                    p.getFechaRegistro(),
                    p.getPorcentajeParticipacion()));
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
            String porcentaje = params.get(7);
            String correoElectronico = params.get(8);
            String contraseña = params.get(9);

            Propietario prop = propietarioRepository.findById(ci).orElse(null);
            if (prop == null) {
                sendResponse(fromEmail, "Error", "Propietario no encontrado.");
                return;
            }
            prop.setNombres(nombres);
            prop.setApellidos(apellidos);
            prop.setDireccion(direccion);
            prop.setGenero(genero);
            prop.setTelefono(telefono);
            prop.setFechaNacimiento(LocalDate.parse(fechaNacimiento));
            prop.setPorcentajeParticipacion(new BigDecimal(porcentaje));
            propietarioRepository.save(prop);

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

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + nombres + " actualizado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo actualizar propietario: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {
            String ci = params.get(0);
            Propietario prop = propietarioRepository.findById(ci).orElse(null);

            if (prop == null) {
                sendResponse(fromEmail, "Error", "Propietario no encontrado.");
                return;
            }

            Usuario u = usuarioRepository.findByPersona_Ci(ci).orElse(null);

            if (u != null) {
                usuarioRepository.delete(u);
            }
            propietarioRepository.delete(prop);

            sendResponse(fromEmail, "Éxito", "Propietario(a) " + prop.getNombres() + " eliminado(a) correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo eliminar propietario: " + e.getMessage());
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
