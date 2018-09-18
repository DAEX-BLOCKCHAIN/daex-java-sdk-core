package io.daex.sdk.core.service.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

/**
 * Created by qingyun.yu on 2018/8/20.
 */
public class RSASigner {

    public static String getSignature(RSAOptions rsaOptions){
        String rsaId = rsaOptions.getRsaId();
        String privateKey = rsaOptions.getPrivateKey();
        try {
            String data = getDataToSign(rsaOptions);
            PrivateKey pk = RSASigner.getPrivateKey(privateKey);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(pk);
            signature.update(data.getBytes());
            String signatureDate = new String(Base64.getEncoder().encode(signature.sign()), "UTF-8");
            return rsaId+":"+signatureDate;
        } catch (Exception e) {
            return null;
        }
    }

    private static String getDataToSign(RSAOptions rsaOptions){

        String nonce = rsaOptions.getNonce();
        String timestamp = rsaOptions.getTimeStamp();
        String path = rsaOptions.getPath();
        String formParams = rsaOptions.getFormParams();
        String queryParams = rsaOptions.getQuery();

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

    private static PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(privateKey.getBytes());
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }
}
