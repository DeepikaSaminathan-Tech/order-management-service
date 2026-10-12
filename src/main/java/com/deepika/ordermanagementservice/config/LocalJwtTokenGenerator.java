
package com.deepika.ordermanagementservice.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LocalJwtTokenGenerator {

    public static void main(String[] args) throws Exception {
        String secret = "local-only-test-secret-key-123456789012";

        long now = System.currentTimeMillis() / 1000;

        String header = """
                {"alg":"HS256","typ":"JWT"}
                """.trim();

        String payload = """
                {"sub":"customer1","iat":%d,"exp":%d,"realm_access":{"roles":["ADMIN"]}}
                """.formatted(now, now + 3600).trim();

        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        String unsignedToken =
                encoder.encodeToString(header.getBytes(StandardCharsets.UTF_8))
                        + "."
                        + encoder.encodeToString(payload.getBytes(StandardCharsets.UTF_8));

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

        String signature = encoder.encodeToString(
                mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));

        System.out.println(unsignedToken + "." + signature);
    }
}
