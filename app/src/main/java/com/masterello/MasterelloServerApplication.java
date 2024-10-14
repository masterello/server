package com.masterello;

import com.masterello.file.configuration.FileProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableConfigurationProperties(value = FileProperties.class)
@ConfigurationPropertiesScan
public class MasterelloServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MasterelloServerApplication.class, args);
	}

}
