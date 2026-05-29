package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import smail.sistema_mail_OdontoCool.entities.Cita;
import smail.sistema_mail_OdontoCool.repositories.CitaRepository;
import smail.sistema_mail_OdontoCool.validations.CitaVal;
import smail.sistema_mail_OdontoCool.repositories.HistorialClinicoRepository;
import smail.sistema_mail_OdontoCool.repositories.PacienteRepository;
import smail.sistema_mail_OdontoCool.repositories.SecretariaRepository;

@Service
public class CitaServices {
    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private SecretariaRepository secretariaRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private CitaVal citaVal;

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
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Citas.");
        }
    }

    @Transactional
    private void insert(List<String> params, String fromEmail) {
        try {
            // Parametros: FechaCita[0], HoraInicio[1], HoraFin[2], Mofivo[3],
            // Observacion[4], CI_Secretaria[5], CI_Paciente[6], CodigoHistorial[7]
            if (params.size() < 8) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Cita. Se requieren 8.");
                return;
            }

            String validationMsg = citaVal.insertValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Cita cita = new Cita();
            cita.setFechaCita(LocalDate.parse(params.get(0)));
            cita.setHoraInicio(LocalTime.parse(params.get(1)));
            cita.setHoraFin(LocalTime.parse(params.get(2)));
            cita.setMotivo(params.get(3));
            cita.setObservacion(params.get(4));
            cita.setSecretaria(secretariaRepository.findById(params.get(5)).orElse(null));
            cita.setPaciente(pacienteRepository.findById(params.get(6)).orElse(null));
            cita.setHistorialClinico(historialClinicoRepository.findById(params.get(7)).orElse(null));

            citaRepository.save(cita);
            sendResponse(fromEmail, "Éxito", "Cita registrada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al registrar cita: " + e.getMessage());
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
                        sendResponse(fromEmail, "Error", "Listado no permitido para Citas.");
                }

            }
            sendResponse(fromEmail, "Listado de Citas", sb.toString());
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al listar citas: " + e.getMessage());
        }
    }

    private StringBuilder listAll() {
        List<Cita> lista = citaRepository.findAll();
        StringBuilder sb = new StringBuilder("Lista de Citas:\n\n");
        for (Cita c : lista) {
            sb.append(String.format("- [%s] %s %s %s %s (Secretaria: %s) (Paciente: %s) (Historial: %s)\n",
                    c.getIdCita(),
                    c.getFechaCita(),
                    c.getHoraInicio(),
                    c.getHoraFin(),
                    c.getMotivo(),
                    c.getSecretaria().getCi(),
                    c.getPaciente().getCi(),
                    c.getHistorialClinico().getCodigoHistorial()));
        }
        return sb;
    }

    @Transactional
    private void update(List<String> params, String fromEmail) {
        try {
            if (params.size() < 9) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Cita. Se requieren 9.");
                return;
            }

            String validationMsg = citaVal.updateValid(params);
            if (!validationMsg.isEmpty()) {
                sendResponse(fromEmail, "Error en la Validación", validationMsg);
                return;
            }

            Long idCita = Long.parseLong(params.get(0));
            Cita cita = citaRepository.findById(idCita).orElse(null);
            if (cita == null) {
                sendResponse(fromEmail, "Error", "Cita no encontrada.");
                return;
            }

            if (!params.get(1).isEmpty()) {
                cita.setFechaCita(LocalDate.parse(params.get(1).replace('/', '-')));
            }
            if (!params.get(2).isEmpty()) {
                cita.setHoraInicio(LocalTime.parse(params.get(2)));
            }
            if (!params.get(3).isEmpty()) {
                cita.setHoraFin(LocalTime.parse(params.get(3)));
            }
            if (!params.get(4).isEmpty()) {
                cita.setMotivo(params.get(4));
            }
            if (!params.get(5).isEmpty()) {
                cita.setObservacion(params.get(5));
            }
            if (!params.get(6).isEmpty()) {
                cita.setSecretaria(secretariaRepository.findById(params.get(6)).orElse(null));
            }
            if (!params.get(7).isEmpty()) {
                cita.setPaciente(pacienteRepository.findById(params.get(7)).orElse(null));
            }
            if (!params.get(8).isEmpty()) {
                cita.setHistorialClinico(historialClinicoRepository.findById(params.get(8)).orElse(null));
            }

            citaRepository.save(cita);
            sendResponse(fromEmail, "Éxito", "Cita modificada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al modificar cita: " + e.getMessage());
        }
    }

    @Transactional
    private void delete(List<String> params, String fromEmail) {
        try {
            if (params.size() != 1) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para Cita. Se requieren 1.");
                return;
            }
            Long idCita = Long.parseLong(params.get(0));
            Cita cita = citaRepository.findById(idCita).orElse(null);
            if (cita == null) {
                sendResponse(fromEmail, "Error", "Cita no encontrada.");
                return;
            }
            citaRepository.delete(cita);
            sendResponse(fromEmail, "Éxito", "Cita eliminada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar cita: " + e.getMessage());
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
