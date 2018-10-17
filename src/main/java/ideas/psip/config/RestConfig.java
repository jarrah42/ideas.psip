package ideas.psip.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestConfig {
	@Bean
	public HttpMessageConverters customConverters() {
		HttpMessageConverter<?> formConverter = new FormHttpMessageConverter();
		ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
		byteArrayHttpMessageConverter.setSupportedMediaTypes(this.getSupportedMediaTypes());
		return new HttpMessageConverters(new HttpMessageConverter[]{formConverter, byteArrayHttpMessageConverter});
	}

	private List<MediaType> getSupportedMediaTypes() {
		List<MediaType> list = new ArrayList<>();
		list.add(MediaType.IMAGE_JPEG);
		list.add(MediaType.IMAGE_PNG);
		list.add(MediaType.IMAGE_GIF);
		list.add(MediaType.APPLICATION_OCTET_STREAM);
		return list;
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
}