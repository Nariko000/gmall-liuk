package com.atguigu.gmall.passport;

import com.atguigu.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {

	}

	@Test
	public void testJWT(){
		String key = "atguigu";
		Map<String, Object> map = new HashMap<>();
		map.put("userId", "1001");
		map.put("nickName", "Administrator");
		String salt = "192.168.194.132";
		String token = JwtUtil.encode(key, map, salt);
		System.out.println("token:" + token);
		System.out.println("-------------------------------");
		Map<String, Object> map1 = JwtUtil.decode(token, key, salt);
		System.out.println(map1);
	}

}
