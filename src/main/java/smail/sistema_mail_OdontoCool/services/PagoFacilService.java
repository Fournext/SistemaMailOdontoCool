package smail.sistema_mail_OdontoCool.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import smail.sistema_mail_OdontoCool.DTO.CrearQR;
import smail.sistema_mail_OdontoCool.DTO.RespuestaQR;
import tools.jackson.databind.ObjectMapper;
import smail.sistema_mail_OdontoCool.DTO.RespuestaEstadoQR;

@Service
public class PagoFacilService {
    @Value("${pago.facil.access-token}")
    private String accessToken;

    @Value("${pago.facil.url}")
    private String baseUrl;

    private final WebClient webClient;

    public PagoFacilService(WebClient webClient) {
        this.webClient = webClient;
    }

    public RespuestaQR generarQR(CrearQR request) {
        try {
            String url = baseUrl;
            if (url == null || url.trim().isEmpty()) {
                url = "https://api.pagofacil.com.bo";
            }

            ObjectMapper mapper = new ObjectMapper();
            String jsonEnviado = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(request);
            System.out.println("JSON Enviado a PagoFacil Service: \n" + jsonEnviado);

            return webClient.post()
                    .uri(url + "/generate-qr")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(RespuestaQR.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error al consumir API Pago Facil: " + e.getMessage());
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                String responseBody = ((org.springframework.web.reactive.function.client.WebClientResponseException) e)
                        .getResponseBodyAsString();
                System.err.println("Response Body del Error: " + responseBody);
            }
            e.printStackTrace();
            RespuestaQR errorResponse = new RespuestaQR();
            errorResponse.setError(1);
            errorResponse.setMessage("Error al conectar con Pago Fácil: " + e.getMessage());
            return errorResponse;
        }
    }

    public RespuestaEstadoQR consultarEstadoTransaccion(String transaccion_id) {
        try {
            String url = baseUrl;
            if (url == null || url.trim().isEmpty()) {
                url = "https://api.pagofacil.com.bo";
            }
            
            java.util.Map<String, Object> bodyMap = new java.util.HashMap<>();
            bodyMap.put("pagofacilTransactionId", Long.valueOf(transaccion_id));

            return webClient.post()
                    .uri(url + "/query-transaction")
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(bodyMap)
                    .retrieve()
                    .bodyToMono(RespuestaEstadoQR.class)
                    .block();
        } catch (Exception e) {
            System.err.println("Error al consultar el estado del QR: " + e.getMessage());
            if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                String responseBody = ((org.springframework.web.reactive.function.client.WebClientResponseException) e)
                        .getResponseBodyAsString();
                System.err.println("Response Body del Error: " + responseBody);
            }
            e.printStackTrace();
            RespuestaEstadoQR errorResponse = new RespuestaEstadoQR();
            errorResponse.setError(1);
            errorResponse.setMessage("Error al conectar con Pago Fácil: " + e.getMessage());
            return errorResponse;
        }
    }
}
