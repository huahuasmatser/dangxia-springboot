package com.zyz.dangxia;

import com.zyz.dangxia.mqtt.MqttManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class DangxiaApplication {

	public static void main(String[] args) {
		SpringApplication.run(DangxiaApplication.class, args);
        MqttManager.getInstance().creatConnect();
	}
}
