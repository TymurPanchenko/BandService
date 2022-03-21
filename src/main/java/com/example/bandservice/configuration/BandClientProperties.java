package com.example.bandservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "bands")
@Component
public class BandClientProperties {
    private String urlTasks;
    private String urlWeapons;
    private String urlUsers;
    private String urlBands;
}
