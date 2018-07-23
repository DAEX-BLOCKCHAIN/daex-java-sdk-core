package io.daex.sdk.core.service.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HmacSigner {

    public static String getSignature(HmacOptions hmacOptions){
        String macId = hmacOptions.getMacId();
        String macSecret = hmacOptions.getMacSecret();
        String combinedSecret = macSecret+hmacOptions.getNonce()+hmacOptions.getTimeStamp();
        try {
            String data = getDataToSign(hmacOptions);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(combinedSecret.getBytes("UTF-8"), "HmacSHA256"));
            String signature = new String(Base64.getEncoder().encode(mac.doFinal(data.getBytes("UTF-8"))), "UTF-8");
            return macId+":"+signature;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getDataToSign(HmacOptions hmacOptions){

        String nonce = hmacOptions.getNonce();
        String timestamp = hmacOptions.getTimeStamp();
        String path = hmacOptions.getPath();
        String formParams = hmacOptions.getFormParams();
        String queryParams = hmacOptions.getQuery();

        StringBuilder sb = new StringBuilder();
        sb.append(nonce).append("|").append(timestamp).append("|").append(path);
        if(queryParams!=null){
            sb.append("|").append(queryParams);
        }
        if(formParams!=null){
            sb.append("|").append(formParams);
        }
        return sb.toString();
    }
}
