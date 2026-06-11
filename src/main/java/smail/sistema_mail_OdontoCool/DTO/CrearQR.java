package smail.sistema_mail_OdontoCool.DTO;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CrearQR {
    @JsonProperty("paymentMethod")
    private int metodoPago;

    @JsonProperty("clientName")
    private String nombreCliente;

    @JsonProperty("documentType")
    private int tipoDocumento;

    @JsonProperty("documentId")
    private String CI;

    @JsonProperty("phoneNumber")
    private String numeroTelefono;

    @JsonProperty("email")
    private String email;

    @JsonProperty("paymentNumber")
    private String idBoletaPago;

    @JsonProperty("amount")
    private double monto; // 0.1 para pruevas

    @JsonProperty("currency")
    private int moneda;

    @JsonProperty("clientCode")
    private String codCliente;

    @JsonProperty("callbackUrl")
    private String CallBack;

    @JsonProperty("orderDetail")
    private List<String> detallesOden = new ArrayList<String>();

    public CrearQR() {
    }

    // Getters y Setters
    public int getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(int metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(int tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getCI() {
        return CI;
    }

    public void setCI(String CI) {
        this.CI = CI;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdBoletaPago() {
        return idBoletaPago;
    }

    public void setIdBoletaPago(String idBoletaPago) {
        this.idBoletaPago = idBoletaPago;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public int getMoneda() {
        return moneda;
    }

    public void setMoneda(int moneda) {
        this.moneda = moneda;
    }

    public String getCodCliente() {
        return codCliente;
    }

    public void setCodCliente(String codCliente) {
        this.codCliente = codCliente;
    }

    public String getCallBack() {
        return CallBack;
    }

    public void setCallBack(String callBack) {
        this.CallBack = callBack;
    }

    public List<String> getDetallesOden() {
        return detallesOden;
    }

    public void setDetallesOden(List<String> detallesOden) {
        this.detallesOden = detallesOden;
    }
}
