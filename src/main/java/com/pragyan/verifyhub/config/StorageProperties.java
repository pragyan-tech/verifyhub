package com.pragyan.verifyhub.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "app.storage")
@Validated
@Getter
@Setter
public class StorageProperties {

    @NotBlank
    private String basePath;

    @Positive
    private long maxFileSizeBytes;

    @NotEmpty
    private List<String> allowedMimeTypes;
}