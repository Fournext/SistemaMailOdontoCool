package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import smail.sistema_mail_OdontoCool.entities.AsignacionTurnoSecretaria;

import smail.sistema_mail_OdontoCool.entities.Secretaria;
import smail.sistema_mail_OdontoCool.entities.Turno;

import smail.sistema_mail_OdontoCool.repositories.AsignacionTurnoSecretariaRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;
import smail.sistema_mail_OdontoCool.repositories.TurnoRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;

@Service
public class AsignacionTurnoSecretariaService {

    @Autowired
    private SmtpClientService smtpService;
    @Autowired
    private TurnoRepository TurnoRepository;
    @Autowired
    private AsignacionTurnoSecretariaRepository AsignacionTurnoSecretariaRepository;
    @Autowired
    private SecretariaRepository SecretariaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

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
            default:
                sendResponse(fromEmail, "Error", "Acción no permitida para asignación de turnos a secretarias.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es sercretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Validar parámetros
            if (params.size() < 5) {
                sendResponse(fromEmail, "Error", "Parámetros insuficientes para asignar turno a secretaria.");
                return;
            }

            String diasSemana = params.get(0);
            String fechaInicioStr = params.get(1);
            String fechaFinStr = params.get(2);
            String estado = params.get(3);
            String secretariaCi = params.get(4);
            Long turnoId = Long.parseLong(params.get(5));
            AsignacionTurnoSecretaria atd = new AsignacionTurnoSecretaria();
            Secretaria secretaria = SecretariaRepository.findById(secretariaCi).orElse(null);
            Turno turno = TurnoRepository.findById(turnoId).orElse(null);

            atd.setDiasSemana(diasSemana);
            atd.setFechaInicio(LocalDate.parse(fechaInicioStr));
            atd.setFechaFin(LocalDate.parse(fechaFinStr));
            atd.setEstado(estado);
            atd.setSecretaria(secretaria);
            atd.setTurno(turno);
            AsignacionTurnoSecretariaRepository.save(atd);

            sendResponse(fromEmail, "Éxito",
                    "Turno asignado a " + atd.getSecretaria().getNombres() + " correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo asignar turno a secretaria: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido.");
                }
            }
            sendResponse(fromEmail, "Listado de Asignaciones de Turnos a secretarias", sb.toString());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "No se pudo listar asignaciones de turnos a secretarias: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<AsignacionTurnoSecretaria> atds = AsignacionTurnoSecretariaRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Asignaciones de Turnos a secretarias:\n\n");
        for (AsignacionTurnoSecretaria atd : atds) {
            sb.append(String.format(
                    "- [%s] Nombre: %s - Turno: %s -HoraInicio: %s -HoraFin: %s (Días: %s, Estado: %s)\n",
                    atd.getId(),
                    atd.getSecretaria().getNombres() + " " + atd.getSecretaria().getApellidos(),
                    atd.getTurno().getNombre(),
                    atd.getTurno().getHoraInicio(),
                    atd.getTurno().getHoraFin(),
                    atd.getDiasSemana(),
                    atd.getEstado()));
        }
        return sb;
    }

    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es sercretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            if (params.size() < 7) {
                sendResponse(fromEmail, "Error",
                        "Parámetros insuficientes para modificar asignación de turno a secretaria.");
                return;
            }

            Long id = Long.parseLong(params.get(0));
            String diasSemana = params.get(1);
            String fechaInicioStr = params.get(2);
            String fechaFinStr = params.get(3);
            String estado = params.get(4);
            String secretariaCi = params.get(5);
            Long turnoId = null;
            if (!params.get(6).trim().isEmpty()) {
                turnoId = Long.parseLong(params.get(6));
            }

            AsignacionTurnoSecretaria atd = AsignacionTurnoSecretariaRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Asignación de turno a secretaria no encontrada"));
            Secretaria secretaria = null;
            if (!secretariaCi.trim().isEmpty()) {
                secretaria = SecretariaRepository.findById(secretariaCi)
                        .orElseThrow(() -> new RuntimeException("Secretaria no encontrada"));
            }

            Turno turno = null;
            if (turnoId != null) {
                turno = TurnoRepository.findById(turnoId)
                        .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
            }
            if (!diasSemana.trim().isEmpty()) {
                atd.setDiasSemana(diasSemana);
            }
            if (!fechaInicioStr.trim().isEmpty()) {
                atd.setFechaInicio(LocalDate.parse(fechaInicioStr));
            }
            if (!fechaFinStr.trim().isEmpty()) {
                atd.setFechaFin(LocalDate.parse(fechaFinStr));
            }
            if (!estado.trim().isEmpty()) {
                atd.setEstado(estado);
            }
            if (secretaria != null) {
                atd.setSecretaria(secretaria);
            }
            if (turno != null) {
                atd.setTurno(turno);
            }
            AsignacionTurnoSecretariaRepository.save(atd);
            sendResponse(fromEmail, "Asignación de Turno a Secretaria Actualizada",
                    "La asignación de turno a secretaria ha sido actualizada correctamente.");

        } catch (Exception e) {
            sendResponse(fromEmail, "Error",
                    "No se pudo modificar asignación de turno a secretaria: " + e.getMessage());
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
