package com.example.bandservice.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
//@ConfigurationProperties(prefix = "bands")
public class BandClientProperties {
    private String urlTasks;
    private String urlWeapons;
    private String urlUsers;
}
