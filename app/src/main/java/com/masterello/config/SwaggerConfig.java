package com.masterello.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.masterello.com", description = "production"),
                @Server(url = "http://127.0.0.1:8090", description = "local")
        },
        info = @Info(title = "Masterello APIs",
                description = "This lists all the Masterello API Calls. The Calls are OAuth2 secured, so please use your client ID and Secret to test them out.",
                version = "v1.0")
)
@SecurityScheme(name = "security_oauth2", type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(password = @OAuthFlow(tokenUrl = "${masterello.openapi.oAuthFlow.tokenUrl}")))
public class SwaggerConfig {

}
