package smail.sistema_mail_OdontoCool.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import smail.sistema_mail_OdontoCool.entities.CuotaBoleta;
import smail.sistema_mail_OdontoCool.entities.CuotaMulta;
import smail.sistema_mail_OdontoCool.entities.BoletaPago;
import smail.sistema_mail_OdontoCool.repositories.CuotaBoletaRepository;
import smail.sistema_mail_OdontoCool.repositories.CuotaMultaRepository;
import smail.sistema_mail_OdontoCool.repositories.UsuarioRepository;
import smail.sistema_mail_OdontoCool.repositories.BoletaPagoRepository;

@Service
public class CuotaMultaService {

    @Autowired
    private CuotaMultaRepository cuotaMultaRepository;

    @Autowired
    private BoletaPagoRepository boletaPagoRepository;

    @Autowired
    private CuotaBoletaRepository cuotaBoletaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private SmtpClientService smtpService;

    public void handle(String actions, List<String> params, String fromEmail) {
        switch (actions) {
            case "ASG":
                asignarCuotaMulta(params, fromEmail);
                break;
            case "DEL":
                delete(params, fromEmail);
                break;
            default:
                sendResponse(fromEmail, "Error", "Acción no soportada para Cuota Multa.");
                break;
        }
    }

    private void asignarCuotaMulta(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parametreos:ID_BOletaPago[0], Nro Cuota[1], Monto[2], Motivo[3]
            if (params.size() < 4) {
                sendResponse(fromEmail, "Error", "Error al asignar cuota multa: Faltan parámetros.");
                return;
            }

            // Validaciones

            // String validationMsg = cuotaMultaVal.asignarValid(params);
            // if (!validationMsg.isEmpty()) {
            // sendResponse(fromEmail, "Error en la Validación", validationMsg);
            // return;
            // }

            Long idBoletaPago = Long.parseLong(params.get(0));
            Integer nroCuota = Integer.parseInt(params.get(1));
            Double monto = Double.parseDouble(params.get(2));
            String motivo = params.get(3);

            BoletaPago boletaPago = boletaPagoRepository.findById(idBoletaPago).orElse(null);
            if (boletaPago == null) {
                sendResponse(fromEmail, "Error", "Error al asignar cuota multa: No se encontro la Boleta de Pago.");
                return;
            }

            CuotaBoleta cuotaBoleta = cuotaBoletaRepository.findByNumeroCuotaAndBoletaPago(nroCuota, boletaPago);
            if (cuotaBoleta == null) {
                sendResponse(fromEmail, "Error", "Error al asignar cuota multa: No existe la cuota Nro: " + nroCuota
                        + " para la boleta de pago Nro: " + boletaPago.getIdBoleta());
                return;
            }

            CuotaMulta cuotaMulta = new CuotaMulta();
            cuotaMulta.setMontoMulta(monto);
            cuotaMulta.setMotivo(motivo);
            cuotaMulta.setEstado("PENDIENTE");

            cuotaMultaRepository.save(cuotaMulta);

            cuotaBoleta.setCuotaMulta(cuotaMulta);
            cuotaBoleta.setEstado("MULTADA");
            cuotaBoleta.setObservacion("Se le ha asignado una multa a la cuota Nro: " + nroCuota
                    + " para la boleta de pago Nro: " + boletaPago.getIdBoleta());
            cuotaBoletaRepository.save(cuotaBoleta);

            sendResponse(fromEmail, "Cuota Multa", "Cuota multa asignada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al asignar cuota multa: " + e.getMessage());
        }
    }

    private void delete(List<String> params, String fromEmail) {
        try {
            // Verificar si es Secretaria
            boolean exists = usuarioRepository.existsByCorreoElectronicoAndRolNombre(fromEmail, "SECRETARIA");
            if (!exists) {
                sendResponse(fromEmail, "Error", "No tiene permisos para realizar esta operacion");
                return;
            }
            // Parametros:Id_BoletaPago[0], Nro_Cuota[1]
            if (params.size() < 2) {
                sendResponse(fromEmail, "Error", "Error al eliminar una cuota multa: Faltan parámetros.");
                return;
            }

            // Validaciones
            // String validationMsg = cuotaMultaVal.deleteValid(params);
            // if (!validationMsg.isEmpty()) {
            // sendResponse(fromEmail, "Error en la Validación", validationMsg);
            // return;
            // }

            Long idBoletaPago = Long.parseLong(params.get(0));
            Integer nroCuota = Integer.parseInt(params.get(1));

            BoletaPago boletaPago = boletaPagoRepository.findById(idBoletaPago).orElse(null);
            if (boletaPago == null) {
                sendResponse(fromEmail, "Error",
                        "Error al eliminar una cuota multa: No se encontro la boleta de pago.");
                return;
            }

            CuotaBoleta cuotaBoleta = cuotaBoletaRepository.findByNumeroCuotaAndBoletaPago(nroCuota, boletaPago);
            if (cuotaBoleta == null) {
                sendResponse(fromEmail, "Error",
                        "Error al eliminar una cuota multa: No existe la cuota Nro: " + nroCuota
                                + " para la boleta de pago Nro: " + boletaPago.getIdBoleta());
                return;
            }

            CuotaMulta cuotaMulta = cuotaBoleta.getCuotaMulta();
            if (cuotaMulta == null) {
                sendResponse(fromEmail, "Error", "Error al eliminar una cuota multa: No existe la multa.");
                return;
            }

            cuotaMultaRepository.delete(cuotaMulta);

            cuotaBoleta.setCuotaMulta(null);
            cuotaBoleta.setEstado("PENDIENTE");
            cuotaBoleta.setObservacion("Se ha eliminado la multa de la cuota Nro: " + nroCuota
                    + " para la boleta de pago Nro: " + boletaPago.getIdBoleta());
            cuotaBoletaRepository.save(cuotaBoleta);

            sendResponse(fromEmail, "Cuota Multa", "Cuota multa eliminada correctamente.");
        } catch (Exception e) {
            sendResponse(fromEmail, "Error", "Error al eliminar una cuota multa: " + e.getMessage());
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
