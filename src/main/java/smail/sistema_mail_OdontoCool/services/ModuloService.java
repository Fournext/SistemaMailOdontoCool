package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.Modulo;
import smail.sistema_mail_OdontoCool.repositories.ModuloRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class ModuloService {

    @Autowired
    private ModuloRepository moduloRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Transactional(readOnly = true)
    public List<Modulo> findAll() {
        return moduloRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Modulo> findById(Long idModulo) {
        return moduloRepository.findById(idModulo);
    }

    @Transactional(readOnly = true)
    public Optional<Modulo> findByNombre(String nombre) {
        return moduloRepository.findByNombreIgnoreCase(nombre);
    }

    @Transactional
    public Modulo save(Modulo modulo) {
        return moduloRepository.save(modulo);
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
                sendResponse(fromEmail, "Error", "Acción no soportada para Módulos.");
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
            if (params.isEmpty()) {
                sendResponse(fromEmail, "Error", "Falta parámetro 'nombre' para registrar Módulo.");
                return;
            }

            String nombre = params.get(0).trim();

            if (moduloRepository.findByNombreIgnoreCase(nombre).isPresent()) {
                sendResponse(fromEmail, "Error", "Ya existe un módulo registrado con el nombre: " + nombre);
                return;
            }

            Modulo modulo = new Modulo();
            modulo.setNombre(nombre);
            moduloRepository.save(modulo);

            sendResponse(fromEmail, "Éxito",
                    "Módulo " + nombre + " registrado correctamente con ID: " + modulo.getIdModulo());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar módulo: " + e.getMessage());
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

            List<Modulo> lista = moduloRepository.findAll();
            StringBuilder sb = new StringBuilder("Lista de Módulos:\n\n");
            for (Modulo m : lista) {
                sb.append(String.format("- [%d] %s (Creado el: %s)\n",
                        m.getIdModulo(), m.getNombre(), m.getFechaCreacion()));
            }
            sendResponse(fromEmail, "Listado de Módulos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar módulos: " + e.getMessage());
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
            if (params.size() < 2) {
                sendResponse(fromEmail, "Error",
                        "Faltan parámetros para actualizar Módulo. Se requieren idModulo y nombre.");
                return;
            }

            Long idModulo = Long.parseLong(params.get(0).trim());
            String nombre = params.get(1).trim();

            Modulo modulo = moduloRepository.findById(idModulo).orElse(null);
            if (modulo == null) {
                sendResponse(fromEmail, "Error", "Módulo no encontrado con ID: " + idModulo);
                return;
            }

            Optional<Modulo> existente = moduloRepository.findByNombreIgnoreCase(nombre);
            if (existente.isPresent() && !existente.get().getIdModulo().equals(idModulo)) {
                sendResponse(fromEmail, "Error", "Ya existe otro módulo registrado con el nombre: " + nombre);
                return;
            }

            modulo.setNombre(nombre);
            moduloRepository.save(modulo);

            sendResponse(fromEmail, "Éxito", "Módulo con ID " + idModulo + " actualizado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al actualizar módulo: " + e.getMessage());
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
                sendResponse(fromEmail, "Error", "Falta parámetro idModulo para eliminar.");
                return;
            }

            Long idModulo = Long.parseLong(params.get(0).trim());
            Modulo modulo = moduloRepository.findById(idModulo).orElse(null);
            if (modulo == null) {
                sendResponse(fromEmail, "Error", "Módulo no encontrado con ID: " + idModulo);
                return;
            }

            moduloRepository.delete(modulo);
            sendResponse(fromEmail, "Éxito", "Módulo " + modulo.getNombre() + " eliminado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar módulo: " + e.getMessage());
        }
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP en ModuloService: " + e.getMessage());
        }
    }
}
