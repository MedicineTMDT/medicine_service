package com.ryo.identity.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @NonFinal
    @Value("${cloudinary.name}")
    String CLOUDINARY_CLOUD_NAME;

    @NonFinal
    @Value("${cloudinary.apiKey}")
    String CLOUDINARY_API_KEY;

    @NonFinal
    @Value("${cloudinary.password}")
    String CLOUDINARY_API_SECRET;


    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", CLOUDINARY_CLOUD_NAME,
                "api_key", CLOUDINARY_API_KEY,
                "api_secret", CLOUDINARY_API_SECRET,
                "secure", true
        ));
    }
}
