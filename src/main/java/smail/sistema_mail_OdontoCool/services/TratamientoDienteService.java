package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smail.sistema_mail_OdontoCool.entities.Diente;
import smail.sistema_mail_OdontoCool.entities.Tratamiento;
import smail.sistema_mail_OdontoCool.entities.TratamientoDiente;
import smail.sistema_mail_OdontoCool.repositories.DienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoDienteRepository;
import smail.sistema_mail_OdontoCool.repositories.TratamientoRepository;

@Service
public class TratamientoDienteService {

    @Autowired
    private SmtpClientService smtpService;

    @Autowired
    private TratamientoDienteRepository tratamientoDienteRepository;

    @Autowired
    private TratamientoRepository tratamientoRepository;
    @Autowired
    private DienteRepository dienteRepository;

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
                sendResponse(fromEmail, "Error", "Acción no permitida para Doctores.");
        }
    }

    private void insert(List<String> params, String fromEmail) {
        try {
            // Parámetros: caraDental[0], observacion[1], fechaRegistro[2], estado[3],
            // tratamientoPlanificado[4], tratamientoId[5], dienteId[6]

            if (params.size() < 7) {
                sendResponse(fromEmail, "Error", "Faltan parámetros para registrar un tratamiento de diente.");
                return;
            }

            String caraDental = params.get(0);
            String observacion = params.get(1);
            LocalDate fechaRegistro = LocalDate.parse(params.get(2));
            String estado = params.get(3);
            String tratamientoPlanificado = params.get(4);
            Long tratamientoId = Long.parseLong(params.get(5));
            Long dienteId = Long.parseLong(params.get(6));

            if (caraDental.trim().isEmpty()
                    || fechaRegistro == null
                    || estado.trim().isEmpty()
                    || tratamientoPlanificado.trim().isEmpty()) {
                sendResponse(fromEmail, "Error", "Faltan parámetros obligatorios para registrar un tratamiento de diente.");
                return;
            }

            Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                    .orElseThrow(() -> new RuntimeException("Tratamiento no encontrado con ID: " + tratamientoId));

            Diente diente = dienteRepository.findById(dienteId)
                    .orElseThrow(() -> new RuntimeException("Diente no encontrado con ID: " + dienteId));

            TratamientoDiente tratamientoDiente = new TratamientoDiente();
            tratamientoDiente.setCaraDental(caraDental);
            tratamientoDiente.setObservacion(observacion);
            tratamientoDiente.setFechaRegistro(fechaRegistro);
            tratamientoDiente.setEstado(estado);
            tratamientoDiente.setTratamientoPlanificado(tratamientoPlanificado);
            tratamientoDiente.setTratamiento(tratamiento);
            tratamientoDiente.setDiente(diente);

            tratamientoDienteRepository.save(tratamientoDiente);

            sendResponse(fromEmail, "Éxito",
                    "Tratamiento de diente registrado correctamente con ID: " + tratamientoDiente.getId());

        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "No se pudo registrar un tratamiento de diente: " + e.getMessage());
        }
    }

    private void list(List<String> params, String fromEmail) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void update(List<String> params, String fromEmail) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void delete(List<String> params, String fromEmail) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendResponse(String to, String subject, String body) {
        try {
            smtpService.sendEmail(to, subject, body);
        } catch (IOException e) {
            System.err.println("Error SMTP: " + e.getMessage());
        }
    }

}
