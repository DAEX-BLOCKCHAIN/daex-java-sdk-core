package io.daex.sdk.core.enums;

import io.daex.sdk.core.client.DaexClientConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by qingyun.yu on 2018/9/17.
 */
public enum ApiType {
    TAPI(DaexClientConfig.getInstance().getTApiRSAId(), DaexClientConfig.getInstance().getTApiRSAPrivateKey()),
    MAPI(DaexClientConfig.getInstance().getMApiRSAId(), DaexClientConfig.getInstance().getMApiRSAPrivateKey()),
    TCAPI(DaexClientConfig.getInstance().getTcApiRSAId(), DaexClientConfig.getInstance().getTcApiRSAPrivateKey());

    private String apiId;
    private String apiPrivateKey;
    public static final Map<String, ApiType> apiTypeMap = new HashMap<>();

    static {
        apiTypeMap.put("/api/service/transfer", TAPI);
        apiTypeMap.put("/api/service/putApply", TAPI);
        apiTypeMap.put("/api/service/walletAddress", TAPI);
        apiTypeMap.put("/api/service/putApplyConfirm", TCAPI);
        apiTypeMap.put("/api/service/getBalance", MAPI);
        apiTypeMap.put("/api/service/getTransaction", MAPI);
        apiTypeMap.put("/api/service/getTransactionList", MAPI);
    }

    ApiType(String apiId, String apiPrivateKey) {
        this.apiId = apiId;
        this.apiPrivateKey = apiPrivateKey;
    }

    public String getApiId() {
        return this.apiId;
    }

    public String getApiPrivateKey() {
        return this.apiPrivateKey;
    }
}
