package com.masterello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableJpaAuditing
@ConfigurationPropertiesScan
public class MasterelloServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MasterelloServerApplication.class, args);
	}

}
