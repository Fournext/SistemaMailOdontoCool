package smail.sistema_mail_OdontoCool.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RespuestaQR {

    @JsonProperty("error")
    private int error;

    @JsonProperty("status")
    private int status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("values")
    private Values values;

    public static class Values {
        @JsonProperty("transactionId")
        private String transactionId;

        @JsonProperty("paymentMethodTransactionId")
        private String paymentMethodTransactionId;

        @JsonProperty("status")
        private int status;

        @JsonProperty("expirationDate")
        private String expirationDate;

        @JsonProperty("qrBase64")
        private String qrBase64;

        @JsonProperty("checkoutUrl")
        private String checkoutUrl;

        @JsonProperty("deepLink")
        private String deepLink;

        @JsonProperty("qrContentUrl")
        private String qrContentUrl;

        @JsonProperty("universalUrl")
        private String universalUrl;

        // Getters y Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getPaymentMethodTransactionId() {
            return paymentMethodTransactionId;
        }

        public void setPaymentMethodTransactionId(String paymentMethodTransactionId) {
            this.paymentMethodTransactionId = paymentMethodTransactionId;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getExpirationDate() {
            return expirationDate;
        }

        public void setExpirationDate(String expirationDate) {
            this.expirationDate = expirationDate;
        }

        public String getQrBase64() {
            return qrBase64;
        }

        public void setQrBase64(String qrBase64) {
            this.qrBase64 = qrBase64;
        }

        public String getCheckoutUrl() {
            return checkoutUrl;
        }

        public void setCheckoutUrl(String checkoutUrl) {
            this.checkoutUrl = checkoutUrl;
        }

        public String getDeepLink() {
            return deepLink;
        }

        public void setDeepLink(String deepLink) {
            this.deepLink = deepLink;
        }

        public String getQrContentUrl() {
            return qrContentUrl;
        }

        public void setQrContentUrl(String qrContentUrl) {
            this.qrContentUrl = qrContentUrl;
        }

        public String getUniversalUrl() {
            return universalUrl;
        }

        public void setUniversalUrl(String universalUrl) {
            this.universalUrl = universalUrl;
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
