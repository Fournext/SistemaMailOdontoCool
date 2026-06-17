package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.AsignacionPermiso;
import smail.sistema_mail_OdontoCool.entities.Modulo;
import smail.sistema_mail_OdontoCool.entities.Rol;
import smail.sistema_mail_OdontoCool.repositories.AsignacionPermisoRepository;
import smail.sistema_mail_OdontoCool.repositories.ModuloRepository;
import smail.sistema_mail_OdontoCool.repositories.RolRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class AsignacionPermisoService {

    @Autowired
    private AsignacionPermisoRepository asignacionPermisoRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Transactional(readOnly = true)
    public List<AsignacionPermiso> findAll() {
        return asignacionPermisoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<AsignacionPermiso> findById(Long idPermiso) {
        return asignacionPermisoRepository.findById(idPermiso);
    }

    @Transactional(readOnly = true)
    public List<AsignacionPermiso> findByRol(Long idRol) {
        return asignacionPermisoRepository.findByRol_IdRol(idRol);
    }

    @Transactional
    public AsignacionPermiso save(AsignacionPermiso asignacionPermiso) {
        return asignacionPermisoRepository.save(asignacionPermiso);
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
                sendResponse(fromEmail, "Error", "Acción no soportada para Asignación de Permisos.");
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
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error",
                        "Faltan parámetros para Permiso. Se requieren nombre, idRol e idModulo.");
                return;
            }

            String nombre = params.get(0).trim();
            String rolParam = params.get(1).trim();
            String moduloParam = params.get(2).trim();
            LocalDate fechaFinalizacion = null;
            if (params.size() > 3 && !params.get(3).trim().isEmpty()) {
                fechaFinalizacion = LocalDate.parse(params.get(3).trim().replace('/', '-'));
            }

            Rol rol = null;
            try {
                Long idRol = Long.parseLong(rolParam);
                rol = rolRepository.findById(idRol).orElse(null);
            } catch (NumberFormatException e) {
                rol = rolRepository.findByNombreIgnoreCase(rolParam).orElse(null);
            }

            if (rol == null) {
                sendResponse(fromEmail, "Error", "Rol no encontrado: " + rolParam);
                return;
            }

            Modulo modulo = null;
            try {
                Long idModulo = Long.parseLong(moduloParam);
                modulo = moduloRepository.findById(idModulo).orElse(null);
            } catch (NumberFormatException e) {
                modulo = moduloRepository.findByNombreIgnoreCase(moduloParam).orElse(null);
            }

            if (modulo == null) {
                sendResponse(fromEmail, "Error", "Módulo no encontrado: " + moduloParam);
                return;
            }

            final Rol finalRol = rol;
            final Modulo finalModulo = modulo;
            boolean existe = asignacionPermisoRepository.findAll().stream()
                    .anyMatch(p -> p.getRol().getIdRol().equals(finalRol.getIdRol())
                            && p.getModulo().getIdModulo().equals(finalModulo.getIdModulo()));
            if (existe) {
                sendResponse(fromEmail, "Error", "Ya existe un permiso asignado al rol " + rol.getNombre()
                        + " para el módulo " + modulo.getNombre());
                return;
            }

            AsignacionPermiso permiso = new AsignacionPermiso();
            permiso.setNombre(nombre);
            permiso.setRol(rol);
            permiso.setModulo(modulo);
            permiso.setFechaFinalizacion(fechaFinalizacion);
            asignacionPermisoRepository.save(permiso);

            sendResponse(fromEmail, "Éxito",
                    "Permiso '" + nombre + "' asignado correctamente con ID: " + permiso.getIdPermiso());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar permiso: " + e.getMessage());
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

            List<AsignacionPermiso> lista = asignacionPermisoRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Asignaciones de Permisos:\n\n");
            for (AsignacionPermiso p : lista) {
                sb.append(String.format("- [%d] Permiso: %s | Rol: [%d] %s | Módulo: [%d] %s | Expiración: %s\n",
                        p.getIdPermiso(), p.getNombre(), p.getRol().getIdRol(), p.getRol().getNombre(),
                        p.getModulo().getIdModulo(), p.getModulo().getNombre(),
                        p.getFechaFinalizacion() != null ? p.getFechaFinalizacion() : "Ninguna"));
            }
            sendResponse(fromEmail, "Listado de Permisos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar permisos: " + e.getMessage());
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
            if (params.size() < 4) {
                sendResponse(fromEmail, "Error",
                        "Faltan parámetros para actualizar Permiso. Se requieren idPermiso, nombre, idRol e idModulo.");
                return;
            }

            Long idPermiso = Long.parseLong(params.get(0).trim());
            String nombre = params.get(1).trim();
            String rolParam = params.get(2).trim();
            String moduloParam = params.get(3).trim();
            LocalDate fechaFinalizacion = null;
            if (params.size() > 4 && !params.get(4).trim().isEmpty()) {
                fechaFinalizacion = LocalDate.parse(params.get(4).trim().replace('/', '-'));
            }

            AsignacionPermiso permiso = asignacionPermisoRepository.findById(idPermiso).orElse(null);
            if (permiso == null) {
                sendResponse(fromEmail, "Error", "Permiso no encontrado con ID: " + idPermiso);
                return;
            }

            Rol rol = null;
            try {
                Long idRol = Long.parseLong(rolParam);
                rol = rolRepository.findById(idRol).orElse(null);
            } catch (NumberFormatException e) {
                rol = rolRepository.findByNombreIgnoreCase(rolParam).orElse(null);
            }

            if (rol == null) {
                sendResponse(fromEmail, "Error", "Rol no encontrado: " + rolParam);
                return;
            }

            Modulo modulo = null;
            try {
                Long idModulo = Long.parseLong(moduloParam);
                modulo = moduloRepository.findById(idModulo).orElse(null);
            } catch (NumberFormatException e) {
                modulo = moduloRepository.findByNombreIgnoreCase(moduloParam).orElse(null);
            }

            if (modulo == null) {
                sendResponse(fromEmail, "Error", "Módulo no encontrado: " + moduloParam);
                return;
            }

            final Rol finalRol = rol;
            final Modulo finalModulo = modulo;
            boolean existe = asignacionPermisoRepository.findAll().stream()
                    .anyMatch(p -> !p.getIdPermiso().equals(idPermiso)
                            && p.getRol().getIdRol().equals(finalRol.getIdRol())
                            && p.getModulo().getIdModulo().equals(finalModulo.getIdModulo()));
            if (existe) {
                sendResponse(fromEmail, "Error", "Ya existe otra asignación de permiso activa al rol " + rol.getNombre()
                        + " para el módulo " + modulo.getNombre());
                return;
            }

            permiso.setNombre(nombre);
            permiso.setRol(rol);
            permiso.setModulo(modulo);
            permiso.setFechaFinalizacion(fechaFinalizacion);
            asignacionPermisoRepository.save(permiso);

            sendResponse(fromEmail, "Éxito", "Permiso con ID " + idPermiso + " actualizado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar permiso: " + e.getMessage());
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
                sendResponse(fromEmail, "Error", "Falta parámetro idPermiso para eliminar.");
                return;
            }

            Long idPermiso = Long.parseLong(params.get(0).trim());
            AsignacionPermiso permiso = asignacionPermisoRepository.findById(idPermiso).orElse(null);
            if (permiso == null) {
                sendResponse(fromEmail, "Error", "Permiso no encontrado con ID: " + idPermiso);
                return;
            }

            asignacionPermisoRepository.delete(permiso);
            sendResponse(fromEmail, "Éxito", "Permiso " + permiso.getNombre() + " eliminado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar permiso: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en AsignacionPermisoService: " + e.getMessage());
        }
    }
}
