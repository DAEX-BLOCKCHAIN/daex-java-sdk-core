package io.daex.sdk.core.service.security;

import java.util.Map;

public class HmacOptions {

    private String macId;
    private String macSecret;
    private String timeStamp;
    private String nonce;
    private String method;
    private String path;
    private String query;
    private String formParams;

    public static class Builder {

        private String macId;
        private String macSecret;
        private String timeStamp;
        private String nonce;
        private String method;
        private String path;
        private String query;
        private String formParams;

        public HmacOptions build() {
            return new HmacOptions(this);
        }

        public HmacOptions.Builder macId(String macId) {
            this.macId = macId;
            return this;
        }

        public HmacOptions.Builder macSecret(String macSecret) {
            this.macSecret = macSecret;
            return this;
        }

        public HmacOptions.Builder timeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }
        public HmacOptions.Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }
        public HmacOptions.Builder method(String method) {
            this.method = method;
            return this;
        }
        public HmacOptions.Builder path(String path) {
            this.path = path;
            return this;
        }
        public HmacOptions.Builder query(String query) {
            this.query = query;
            return this;
        }
        public HmacOptions.Builder formParams(String formParams) {
            this.formParams = formParams;
            return this;
        }
    }

    private HmacOptions(HmacOptions.Builder builder) {
        this.macId = builder.macId;
        this.macSecret = builder.macSecret;
        this.timeStamp = builder.timeStamp;
        this.nonce = builder.nonce;
        this.method = builder.method;
        this.path = builder.path;
        this.query = builder.query;
        this.formParams = builder.formParams;
    }

    public String getMacId() {
        return macId;
    }

    public String getMacSecret() {
        return macSecret;
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
