package com.example.gym_server.util;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class JwtUtil {


    // 密钥
    private final String SECRET_KEY =
            "gymserverjwtsecretkey123456789012345678901234567890";

    // token有效时间
    private final long EXPIRATION_TIME =
            1000 * 60 * 60 * 24;



    // 生成token
    public String generateToken(String username){


        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis()
                                + EXPIRATION_TIME)
                )
                .signWith(
                        SignatureAlgorithm.HS256,
                        SECRET_KEY
                )
                .compact();

    }



    // 解析token获取用户名
    public String getUsername(String token){


        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }



    // 判断token是否有效
    public boolean validateToken(String token){


        try {

            Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token);


            return true;


        }catch (Exception e){

            return false;

        }

    }

}