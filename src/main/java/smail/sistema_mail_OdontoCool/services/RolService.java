package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Rol;
import smail.sistema_mail_OdontoCool.repositories.RolRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<Rol> findAll() {
        return rolRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Rol> findById(Long idRol) {
        return rolRepository.findById(idRol);
    }

    @Transactional(readOnly = true)
    public Optional<Rol> findByNombre(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre);
    }

    @Transactional
    public Rol save(Rol rol) {
        return rolRepository.save(rol);
    }

    public void handle(String action, List<String> params, String fromEmail) {
        switch (action) {
            case "INS":
                insert(params, fromEmail);
                break;
            case "LIS":
                list(params, fromEmail);
                break;
            case "MOD":
                update(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Roles.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() < 2) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Rol. Se requieren al menos nombre y estado.");
                return;
            }

            String nombre = params.get(0).trim().toUpperCase();
            String estado = params.get(1).trim().toUpperCase();
            String descripcion = params.size() > 2 ? params.get(2).trim() : "";

            if (rolRepository.findByNombreIgnoreCase(nombre).isPresent()) {
                sendResponse(fromEmail, "Error", "Ya existe un rol registrado con el nombre: " + nombre);
                return;
            }

            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setEstado(estado);
            rol.setDescripcion(descripcion);
            rolRepository.save(rol);

            sendResponse(fromEmail, "Éxito", "Rol " + nombre + " registrado correctamente con ID: " + rol.getIdRol());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar rol: " + e.getMessage());
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
            if (params.isEmpty() || !params.get(0).equals("*")) {
                sendResponse(fromEmail, "Error", "Acción de listado incorrecta. Use '*' para listar todos.");
                return;
            }

            List<Rol> lista = rolRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Roles:\n\n");
            for (Rol r : lista) {
                sb.append(String.format("- [%d] %s (Estado: %s) - %s\n",
                        r.getIdRol(), r.getNombre(), r.getEstado(), r.getDescripcion()));
            }
            sendResponse(fromEmail, "Listado de Roles", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar roles: " + e.getMessage());
        }
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error",
                        "Faltan parámetros para actualizar Rol. Se requieren idRol, nombre y estado.");
                return;
            }

            Long idRol = Long.parseLong(params.get(0).trim());
            String nombre = params.get(1).trim().toUpperCase();
            String estado = params.get(2).trim().toUpperCase();
            String descripcion = params.size() > 3 ? params.get(3).trim() : "";

            Rol rol = rolRepository.findById(idRol).orElse(null);
            if (rol == null) {
                sendResponse(fromEmail, "Error", "Rol no encontrado con ID: " + idRol);
                return;
            }

            Optional<Rol> existente = rolRepository.findByNombreIgnoreCase(nombre);
            if (existente.isPresent() && !existente.get().getIdRol().equals(idRol)) {
                sendResponse(fromEmail, "Error", "Ya existe otro rol registrado con el nombre: " + nombre);
                return;
            }

            rol.setNombre(nombre);
            rol.setEstado(estado);
            rol.setDescripcion(descripcion);
            rolRepository.save(rol);

            sendResponse(fromEmail, "Éxito", "Rol con ID " + idRol + " actualizado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar rol: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es propietario
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "PROPIETARIO");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Falta parámetro idRol para eliminar.");
                return;
            }

            Long idRol = Long.parseLong(params.get(0).trim());
            Rol rol = rolRepository.findById(idRol).orElse(null);
            if (rol == null) {
                sendResponse(fromEmail, "Error", "Rol no encontrado con ID: " + idRol);
                return;
            }

            rolRepository.delete(rol);
            sendResponse(fromEmail, "Éxito", "Rol " + rol.getNombre() + " eliminado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar rol: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en RolService: " + e.getMessage());
        }
    }
}
