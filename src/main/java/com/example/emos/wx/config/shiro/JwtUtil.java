package com.example.emos.wx.config.shiro;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author leach
 * mmbj_18193320486(微信)
 */
@Component
public class JwtUtil {
    //jwtToken分为
    // head（type,algo）、
    // 载荷payload(有效信息)、
    // signature(algo、signaturemessage)
    @Value("${emos.jwt.secret}")
    private String secret;
    @Value("${emos.jwt.expire}")
    private int expire;

    //创建token
    public String creatToken(int userId) {
        //对其时间进行确定

        DateTime offset = DateUtil.offset(new Date(), DateField.DAY_OF_YEAR, expire);
        //对其密码进行加密确定
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //通过参数生成密钥
        JWTCreator.Builder builder = JWT.create();
        return builder.withExpiresAt(offset).withClaim("userId", userId).sign(algorithm);
    }

    //获取userId
    public int getUserId(String token) {
        DecodedJWT decode = JWT.decode(token);
        return decode.getClaim("userId").asInt();
    }

    //校验token
    public void verifierToken(String token) {
        //对加密后的签名进行验证
        Algorithm algorithm = Algorithm.HMAC256(secret);
        //创建验证对象
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        //验证
        jwtVerifier.verify(token);

    }
}
