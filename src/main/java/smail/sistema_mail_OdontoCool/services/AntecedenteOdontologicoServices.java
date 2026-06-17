package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import smail.sistema_mail_OdontoCool.entities.AntecedenteOdontologico;
import smail.sistema_mail_OdontoCool.entities.DetalleAntecedenteOdontologico;
import smail.sistema_mail_OdontoCool.entities.HistorialClinico;
import smail.sistema_mail_OdontoCool.repositories.AntecedenteOdontologicoRepository;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.validations.AntecedenteOdontologicoVal;

@Service
public class AntecedenteOdontologicoServices {

    @Autowired
    private AntecedenteOdontologicoRepository antecedenteOdontologicoRepository;

    @Autowired
    private AntecedenteOdontologicoVal antecedenteOdontologicoVal;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Transactional
    public void handle(String actions, List<String> params, String fromEmail) {
        switch (actions) {
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
                sendResponse(fromEmail, "Error", "Acción DEL no implementada aún para Antecedente Odontológico.");
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Antecedente Odontológico.");
                break;
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parametros: ObservacionGeneral[0], DetalleAntecedentes[1], CodHistorial[2]
            if (params.size() < 3) {
                sendResponse(fromEmail, "Error",
                        "Error al registrar Antecedente Odontológico: Parámetros insuficientes.");
                return;
            }

            String validationMsg = antecedenteOdontologicoVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            AntecedenteOdontologico antecedente = new AntecedenteOdontologico();
            antecedente.setObservacionGeneral(params.get(0));

            HistorialClinico historialClinico = historialClinicoRepository.findByCodigoHistorial(params.get(2));
            if (historialClinico == null) {
                sendResponse(fromEmail, "Error", "El código de historial no existe.");
                return;
            }
            antecedente.setHistorialClinico(historialClinico);

            List<DetalleAntecedenteOdontologico> detalles = new ArrayList<>();
            String detailsRaw = params.get(1);
            if (detailsRaw != null && !detailsRaw.trim().isEmpty()) {
                String[] detailsArray = detailsRaw.split("\\s*\\|\\s*");
                for (String detailStr : detailsArray) {
                    if (detailStr.trim().isEmpty()) {
                        continue;
                    }
                    String[] fields = detailStr.split("\\s*;\\s*", -1);
                    DetalleAntecedenteOdontologico detalle = new DetalleAntecedenteOdontologico();
                    detalle.setNombreTratamiento(fields[0].trim());
                    detalle.setDescripcion(fields[1].trim());
                    detalle.setFechaTratamiento(LocalDate.parse(fields[2].trim()));
                    detalle.setLugarTratamiento(fields[3].trim());
                    detalle.setObservacion(fields[4].trim());
                    detalle.setAntecedenteOdontologico(antecedente);
                    detalles.add(detalle);
                }
            }

            antecedente.setDetallesAntecedenteOdontologico(detalles);
            antecedenteOdontologicoRepository.save(antecedente);

            sendResponse(fromEmail, "Éxito", "Antecedente Odontológico registrado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar Antecedente Odontológico: " + e.getMessage());
        }
    }

    @Transactional
    private void list(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor o Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR")
                    || usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Antecedente Odontológico.");
                        return;
                }
            }
            sendResponse(fromEmail, "Listado de Antecedentes Odontológicos", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar Antecedentes Odontológicos: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<AntecedenteOdontologico> lista = antecedenteOdontologicoRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Antecedentes Odontológicos:\n\n");
        for (AntecedenteOdontologico a : lista) {
            sb.append(String.format("- [%s] Obs: %s (Historial: %s)\n",
                    a.getIdAntecedente(),
                    a.getObservacionGeneral(),
                    a.getHistorialClinico() != null ? a.getHistorialClinico().getCodigoHistorial() : "N/A"));
            List<DetalleAntecedenteOdontologico> detalles = a.getDetallesAntecedenteOdontologico();
            if (detalles != null && !detalles.isEmpty()) {
                sb.append("  Detalles:\n");
                for (DetalleAntecedenteOdontologico d : detalles) {
                    sb.append(String.format("    * [%s] Tratamiento: %s, Desc: %s, Fecha: %s, Lugar: %s, Obs: %s\n",
                            d.getIdDetalleAntecendente(),
                            d.getNombreTratamiento(),
                            d.getDescripcion(),
                            d.getFechaTratamiento(),
                            d.getLugarTratamiento(),
                            d.getObservacion()));
                }
            }
            sb.append("\n");
        }
        return sb;
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            // Verificar si es Doctor
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "DOCTOR");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parametros: idAntecedente[0], ObservacionGeneral[1], DetalleAntecedentes[2],
            // CodHistorial[3]
            if (params.size() < 4) {
                sendResponse(fromEmail, "Error",
                        "Error al modificar Antecedente Odontológico: Parámetros insuficientes.");
                return;
            }

            String validationMsg = antecedenteOdontologicoVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long id = Long.parseLong(params.get(0));
            AntecedenteOdontologico antecedente = antecedenteOdontologicoRepository.findById(id).orElse(null);
            if (antecedente == null) {
                sendResponse(fromEmail, "Error", "Antecedente Odontológico no encontrado con ID: " + id);
                return;
            }

            if (!params.get(1).isEmpty()) {
                antecedente.setObservacionGeneral(params.get(1));
            }

            if (!params.get(3).isEmpty()) {
                HistorialClinico historialClinico = historialClinicoRepository.findByCodigoHistorial(params.get(3));
                if (historialClinico == null) {
                    sendResponse(fromEmail, "Error", "El código de historial no existe.");
                    return;
                }
                antecedente.setHistorialClinico(historialClinico);
            }

            if (!params.get(2).isEmpty()) {
                antecedente.getDetallesAntecedenteOdontologico().clear();

                List<DetalleAntecedenteOdontologico> detalles = new ArrayList<>();
                String detailsRaw = params.get(2);
                String[] detailsArray = detailsRaw.split("\\s*\\|\\s*");
                for (String detailStr : detailsArray) {
                    if (detailStr.trim().isEmpty()) {
                        continue;
                    }
                    String[] fields = detailStr.split("\\s*;\\s*", -1);
                    DetalleAntecedenteOdontologico detalle = new DetalleAntecedenteOdontologico();
                    detalle.setNombreTratamiento(fields[0].trim());
                    detalle.setDescripcion(fields[1].trim());
                    detalle.setFechaTratamiento(LocalDate.parse(fields[2].trim()));
                    detalle.setLugarTratamiento(fields[3].trim());
                    detalle.setObservacion(fields[4].trim());
                    detalle.setAntecedenteOdontologico(antecedente);
                    detalles.add(detalle);
                }
                antecedente.getDetallesAntecedenteOdontologico().addAll(detalles);
            }

            antecedenteOdontologicoRepository.save(antecedente);
            sendResponse(fromEmail, "Éxito", "Antecedente Odontológico modificado correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al modificar Antecedente Odontológico: " + e.getMessage());
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
