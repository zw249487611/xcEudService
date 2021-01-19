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
        String jwtString = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiaXRjYXN0In0.BSNqAhG3cbY451c8FhTLsBhfHrV6jIuKyhgoLG9tfNy8CYISBAW9sk2yvDVsO8CKiCg-1ktRYeL88g7_rX-_MWr3k-nvCjwmavgGiicP5naTMXlgKOXxa2PJIKeIL6p_PzT9wC60cb9T6WPn9HSJt5rcHKuZuM2n2c-_Trq-Wz_6GRnfqtqCShev18DjGk-TNHkUYbF59nSdG4ZWecAas1boMvdO552IEnbLmV4QU8HZB7wtshAy0t4TcYXp9GhUjIo03r2PuRiBtobWBgn7ZtrX93r_AkV2JRiGIHwBdrzq8dSJK3q__Zga1mFEuZ0XJqOZDn_xx3N2jTWd4PDklg";
        //校验jwt令牌
        Jwt jwt = JwtHelper.decodeAndVerify(jwtString, new RsaVerifier(publickey));
        //拿到jwt中我们自定义的内容
        String claims = jwt.getClaims();
        System.out.println(claims);

    }
}
