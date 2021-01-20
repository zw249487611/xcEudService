package com.xuecheng.auth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.rsa.crypto.KeyStoreKeyFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestJwt {

    //创建jwt令牌
    @Test
    public void testCreateJwt() {
        //1.密钥库的文件，
        String keystore = "xc.keystore";
        //2.密钥库的密码
        String keystore_password = "xuechengkeystore";
        //密钥库的文件路径
        ClassPathResource classPathResource = new ClassPathResource(keystore);
        //3.密钥的别名
        String alias = "xckey";
        //密钥的访问密码
        String key_password = "xuecheng";
        //密钥工厂
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource, keystore_password.toCharArray());
        //密钥对（公钥和私钥）
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(alias, key_password.toCharArray());
        //获取私钥
        RSAPrivateKey aPrivate = (RSAPrivateKey) keyPair.getPrivate();
        //jwt令牌的内容
        Map<String, String> body = new HashMap<>();
        body.put("name", "itcast");
        String bodyString = JSON.toJSONString(body);

        //生成jwt令牌
        Jwt jwt = JwtHelper.encode(bodyString, new RsaSigner(aPrivate));
        //生成jwt令牌编码
        String encoded = jwt.getEncoded();
        System.out.println(encoded);


    }

    //校验jwt令牌
    @Test
    public void testVerify() {
        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA8J3z81609Y3K4rZWfjK84XwgRxMRLq9EgseqfnZkCKcOcjMr/s2KlDAP4fFwFiqmHJNZ8AuIX+pZ5POrxZhSKuUQARefqjmDn99uyFNT8vYpuBSnbprWmcIB2fss0X8vGipwVHBCayXkQ3IlZoV7lj57qMfWqaMbteN5YtL4nKTcjc30pczMGkoXy0lfs4UKl/yK7Awiz8pISJ1vfJYjbReA0rCnqQDvg8LFBoy9HWFOmt8r/SYt1pHHuQSsRx6K5tTAMH1DZ18lNgnmIZUnHQGJIDanLGgVvYnko4bzcZEI6/ohNtmrEEpOysMsDSrdcsc2YpOrWqg01oX7TnlDDwIDAQAB-----END PUBLIC KEY-----";
        //jwt令牌
        String jwtString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJjb21wYW55SWQiOiIxIiwidXNlcnBpYyI6bnVsbCwidXNlcl9uYW1lIjoiaXRjYXN0Iiwic2NvcGUiOlsiYXBwIl0sIm5hbWUiOiJ0ZXN0MDIiLCJ1dHlwZSI6IjEwMTAwMiIsImlkIjoiNDkiLCJleHAiOjE2MTExNjc5MjAsImF1dGhvcml0aWVzIjpbInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYmFzZSIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfZGVsIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9saXN0IiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZV9wbGFuIiwieGNfdGVhY2htYW5hZ2VyX2NvdXJzZSIsImNvdXJzZV9maW5kX2xpc3QiLCJ4Y190ZWFjaG1hbmFnZXIiLCJ4Y190ZWFjaG1hbmFnZXJfY291cnNlX21hcmtldCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfcHVibGlzaCIsInhjX3RlYWNobWFuYWdlcl9jb3Vyc2VfYWRkIl0sImp0aSI6IjJiNjQ3YTM5LWE5NGItNGI0NC04Mjg3LTg4YTMxOTQzNjMzMiIsImNsaWVudF9pZCI6IlhjV2ViQXBwIn0.ZfImN95ZgPA-r9t-pxJIKGess4P_Gup38r5JM84ONXAymmJE5CcOoWbXjGXRyWUdtPasF5PyCeGmTXGdk1ZIFxe86Q2JM-HeD6xX1w9b_t1Y2yfXuTLVELwY8S3eHIimPqj7raAVAl-eiK60USlPjOcikJ_MDzP7gQs0hi43w2jb3ufaDQfoyMb5bpiAJNUqAH1hCiTFm0D9AZHdnVmr0XYFoIWxjD_zm65lXfHd4PpIcNGAsgnsmucnS9zBZB_U1ElgeVvHis5VRIuIAxTn7Do8CGSvaKnKBa_Uq3ZxpAGjVtBAdmbHGpuhHubEqj6Np5OZCspynVxgSvuy0p5UjA";
        //校验jwt令牌
        Jwt jwt = JwtHelper.decodeAndVerify(jwtString, new RsaVerifier(publickey));
        //拿到jwt中我们自定义的内容
        String claims = jwt.getClaims();
        System.out.println(claims);

    }
}
