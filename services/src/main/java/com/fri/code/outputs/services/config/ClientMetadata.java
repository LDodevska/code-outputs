package com.fri.code.outputs.services.config;

import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

import javax.enterprise.context.ApplicationScoped;

@ConfigBundle("app-config")
@ApplicationScoped
public class ClientMetadata {

    @ConfigValue(value = "jdoodle.client-id", watch = true)
    private String clientId;

    @ConfigValue(value = "jdoodle.secret-key", watch = true)
    private String clientSecret;

    @ConfigValue(value = "external-services.enabled", watch = true)
    private boolean externalServicesEnabled;


    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public boolean isExternalServicesEnabled() {
        return externalServicesEnabled;
    }

    public void setExternalServicesEnabled(boolean externalServicesEnabled) {
        this.externalServicesEnabled = externalServicesEnabled;
    }
}
