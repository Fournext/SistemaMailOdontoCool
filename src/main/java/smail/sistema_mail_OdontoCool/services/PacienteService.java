package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smail.sistema_mail_OdontoCool.entities.Paciente;
import smail.sistema_mail_OdontoCool.entities.Usuario;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

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
            Paciente pac = new Paciente();
            pac.setCi(params.get(0));
            pac.setNombres(params.get(1));
            pac.setApellidos(params.get(2));
            pac.setDireccion(params.get(3));
            pac.setGenero(params.get(4));
            pac.setTelefono(params.get(5));
            pac.setFechaNacimiento(LocalDate.parse(params.get(6)));

            pac.setNombreContactoEmergencia(params.get(7));
            pac.setTelefonoEmergencia(params.get(8));
            pacienteRepository.save(pac);

            String firstLetter = (pac.getApellidos() != null && !pac.getApellidos().trim().isEmpty())
                    ? pac.getApellidos().trim().substring(0, 1).toUpperCase()
                    : "";

            String fotoUrl = cloudinaryServices.subirImagen(imagenesBase64.get(0));

            Usuario u = new Usuario();

            u.setCodigoUsuario(firstLetter + pac.getCi());
            u.setCorreoElectronico(params.get(9));
            u.setContraseña(passwordService.hashPassword(params.get(10)));
            u.setFotoUrl(fotoUrl);
            u.setEstado("ACTIVO");
            u.setPersona(pac);
            usuarioRepository.save(u);

            sendResponse(fromEmail, "Éxito", "Paciente " + pac.getNombres() + " registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar paciente: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Pacientes.");
                }

            }
            sendResponse(fromEmail, "Listado de Pacientes", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar pacientes: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Paciente> lista = pacienteRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Pacientes:\n\n");
        for (Paciente p : lista) {
            sb.append(String.format("- [%s] %s %s %s %s %s (Emergencia: %s %s)\n",
                    p.getCi(),
                    p.getNombres(),
                    p.getApellidos(),
                    p.getDireccion(),
                    p.getTelefono(),
                    p.getFechaNacimiento(),
                    p.getTelefonoEmergencia(),
                    p.getNombreContactoEmergencia()));
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

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en PacienteService: " + e.getMessage());
        }
    }
}
