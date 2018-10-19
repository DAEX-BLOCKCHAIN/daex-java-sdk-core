package io.daex.sdk.core.service.security;

/**
 * Created by qingyun.yu on 2018/8/20.
 */
public class RSAOptions {
    private String rsaId;
    private String privateKey;
    private String timeStamp;
    private String nonce;
    private String method;
    private String path;
    private String query;
    private String formParams;
    public static class Builder {

        private String rsaId;
        private String privateKey;
        private String timeStamp;
        private String nonce;
        private String method;
        private String path;
        private String query;
        private String formParams;

        public RSAOptions build() {
            return new RSAOptions(this);
        }

        public Builder rsaId(String rsaId) {
            this.rsaId = rsaId;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder timeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }
        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }
        public Builder method(String method) {
            this.method = method;
            return this;
        }
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        public Builder query(String query) {
            this.query = query;
            return this;
        }
        public Builder formParams(String formParams) {
            this.formParams = formParams;
            return this;
        }
    }

    private RSAOptions(Builder builder) {
        this.rsaId = builder.rsaId;
        this.privateKey = builder.privateKey;
        this.timeStamp = builder.timeStamp;
        this.nonce = builder.nonce;
        this.method = builder.method;
        this.path = builder.path;
        this.query = builder.query;
        this.formParams = builder.formParams;
    }


    public String getRsaId() {
        return rsaId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getNonce() {
        return nonce;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public String getFormParams() {
        return formParams;
    }
}
