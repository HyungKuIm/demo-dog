package com.oraclejava.demodog.image;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * images 폴더의 파일을 /images/** 경로로 제공한다.
 */
@Configuration
public class ImageWebConfig implements WebMvcConfigurer {

	private final ImageStorage imageStorage;

	public ImageWebConfig(ImageStorage imageStorage) {
		this.imageStorage = imageStorage;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(ImageStorage.URL_PREFIX + "**")
				.addResourceLocations(imageStorage.getDirectory().toUri().toString());
	}

}
