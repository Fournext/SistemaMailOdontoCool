package smail.sistema_mail_OdontoCool.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RespuestaEstadoQR {

    @JsonProperty("error")
    private int error;

    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("values")
    private Values values;

    public static class Values {
        @JsonProperty("pagofacilTransactionId")
        private Long pagofacilTransactionId;

        @JsonProperty("companyTransactionId")
        private String idBoletaPagoEmpresa;

        @JsonProperty("clientCompanyCode")
        private String codClienteEmpresa;

        @JsonProperty("amount")
        private String monto;

        @JsonProperty("currencyId")
        private int currencyId;

        @JsonProperty("currencyCode")
        private String CodMoneda;

        @JsonProperty("paymentStatus")
        private int estadoTransaccion;

        @JsonProperty("paymentStatusDescription")
        private String descrpEstadoTransaccion;

        @JsonProperty("paymentMethodId")
        private int idMetodoPago;

        @JsonProperty("paymentMethodDetail")
        private String descpMetodoPago;

        @JsonProperty("requestDate")
        private String fechaSolicitud;

        @JsonProperty("requestTime")
        private String horaSolicitud;

        @JsonProperty("paymentDate")
        private String paymentDate;

        @JsonProperty("paymentTime")
        private String paymentTime;

        @JsonProperty("payerName")
        private String payerName;

        @JsonProperty("payerDocument")
        private String payerDocument;

        @JsonProperty("payerAccount")
        private String payerAccount;

        @JsonProperty("payerBank")
        private String payerBank;

        @JsonProperty("payerBankAcronym")
        private String payerBankAcronym;

        // Getters y Setters
        public Long getPagofacilTransactionId() {
            return pagofacilTransactionId;
        }

        public void setPagofacilTransactionId(Long pagofacilTransactionId) {
            this.pagofacilTransactionId = pagofacilTransactionId;
        }

        public String getIdBoletaPagoEmpresa() {
            return idBoletaPagoEmpresa;
        }

        public void setIdBoletaPagoEmpresa(String idBoletaPagoEmpresa) {
            this.idBoletaPagoEmpresa = idBoletaPagoEmpresa;
        }

        public String getCodClienteEmpresa() {
            return codClienteEmpresa;
        }

        public void setCodClienteEmpresa(String codClienteEmpresa) {
            this.codClienteEmpresa = codClienteEmpresa;
        }

        public String getMonto() {
            return monto;
        }

        public void setMonto(String monto) {
            this.monto = monto;
        }

        public int getCurrencyId() {
            return currencyId;
        }

        public void setCurrencyId(int currencyId) {
            this.currencyId = currencyId;
        }

        public String getCodMoneda() {
            return CodMoneda;
        }

        public void setCodMoneda(String currencyCode) {
            this.CodMoneda = currencyCode;
        }

        public int getEstadoTransaccion() {
            return estadoTransaccion;
        }

        public void setEstadoTransaccion(int paymentStatus) {
            this.estadoTransaccion = paymentStatus;
        }

        public String getDescrpEstadoTransaccion() {
            return descrpEstadoTransaccion;
        }

        public void setDescrpEstadoTransaccion(String paymentStatusDescription) {
            this.descrpEstadoTransaccion = paymentStatusDescription;
        }

        public int getIdMetodoPago() {
            return idMetodoPago;
        }

        public void setIdMetodoPago(int paymentMethodId) {
            this.idMetodoPago = paymentMethodId;
        }

        public String getDescpMetodoPago() {
            return descpMetodoPago;
        }

        public void setDescpMetodoPago(String paymentMethodDetail) {
            this.descpMetodoPago = paymentMethodDetail;
        }

        public String getFechaSolicitud() {
            return fechaSolicitud;
        }

        public void setFechaSolicitud(String requestDate) {
            this.fechaSolicitud = requestDate;
        }

        public String getHoraSolicitud() {
            return horaSolicitud;
        }

        public void setHoraSolicitud(String requestTime) {
            this.horaSolicitud = requestTime;
        }

        public String getPaymentDate() {
            return paymentDate;
        }

        public void setPaymentDate(String paymentDate) {
            this.paymentDate = paymentDate;
        }

        public String getPaymentTime() {
            return paymentTime;
        }

        public void setPaymentTime(String paymentTime) {
            this.paymentTime = paymentTime;
        }

        public String getPayerName() {
            return payerName;
        }

        public void setPayerName(String payerName) {
            this.payerName = payerName;
        }

        public String getPayerDocument() {
            return payerDocument;
        }

        public void setPayerDocument(String payerDocument) {
            this.payerDocument = payerDocument;
        }

        public String getPayerAccount() {
            return payerAccount;
        }

        public void setPayerAccount(String payerAccount) {
            this.payerAccount = payerAccount;
        }

        public String getPayerBank() {
            return payerBank;
        }

        public void setPayerBank(String payerBank) {
            this.payerBank = payerBank;
        }

        public String getPayerBankAcronym() {
            return payerBankAcronym;
        }

        public void setPayerBankAcronym(String payerBankAcronym) {
            this.payerBankAcronym = payerBankAcronym;
        }
    }

    // Getters y Setters
    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Values getValues() {
        return values;
    }

    public void setValues(Values values) {
        this.values = values;
    }
}
