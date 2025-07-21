package com.masterello.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.valves.AbstractAccessLogValve;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.CharArrayWriter;

@Configuration
@Slf4j(topic = "ACCESS_LOG")
public class AccessLogConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> accessLogValveCustomizer() {
        return factory -> factory.addContextValves(new AbstractAccessLogValve() {
            {
                setPattern("[ACCESS] %h %t \"%r\" %s %b");
                setConditionUnless("NO_LOG"); // skip if header present
            }
            @Override
            protected void log(CharArrayWriter message) {
                log.info(message.toString());
            }
        });
    }
}

