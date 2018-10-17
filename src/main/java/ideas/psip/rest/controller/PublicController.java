package ideas.psip.rest.controller;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestTemplate;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import ideas.psip.config.ServerConfig;

@Controller
public class PublicController {
	private static Configuration configuration;
	@Autowired
	private ServerConfig config;
	@Autowired
	private RestTemplate restTemplate;

	static {
		configuration = new Configuration(Configuration.VERSION_2_3_23);
		configuration.setClassForTemplateLoading(PublicController.class, "/");
	}

	@GetMapping({"/public/psip"})
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<?> getPSIP(HttpServletResponse response, @RequestParam("url") String url) {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setAccept(Collections.singletonList(MediaType.parseMediaType("text/plain")));
		HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);
		String document = this.restTemplate
				.exchange(url, HttpMethod.GET, requestEntity, String.class, new Object[0]).getBody();
		Map<String, String> result = Arrays.stream(document.split("\n"))
				.map(s -> s.split(":"))
				.collect(Collectors.toMap(
					a -> a[0].trim(), 
					a -> a[1].trim()
				));

		for (Entry<String, String> entry : result.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		try {
			Template template = configuration.getTemplate("templates/psip.html");
			StringWriter writer = new StringWriter();
			template.process(result, writer);
			String html = writer.toString();
			return ResponseEntity.ok(html);
		} catch (TemplateNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedTemplateNameException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return ResponseEntity.badRequest().build();
	}

	@GetMapping({"/public/version"})
	public ResponseEntity<?> version() {
		return ResponseEntity
				.ok(new Resource<>(this.config.getVersion() + "." + this.config.getBuildTimestamp(), new Link[0]));
	}

	@GetMapping({"/public/config"})
	public ResponseEntity<?> config() {
		return ResponseEntity.ok(new Resource<>(this.config.getProfiles(), new Link[0]));
	}

	public URL getResource(String name) {
		try {
			return (new ClassPathResource(name)).getURL();
		} catch (IOException var3) {
			return null;
		}
	}
}