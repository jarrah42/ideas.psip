package ideas.psip.rest.controller;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.io.StreamDocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import cz.vutbr.web.css.MediaSpec;
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

	@GetMapping(
			  value = "/public/psip",
			  produces = MediaType.IMAGE_PNG_VALUE
			)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<InputStreamResource> getPSIP(@RequestParam("url") String url) {
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
			
			Dimension size = new Dimension(1200, 600);
			
			DocumentSource src = new StreamDocumentSource(new ByteArrayInputStream(html.getBytes()), null, "text/html");
			DOMSource parser = new DefaultDOMSource(src);
			Document doc = parser.parse();
			
			MediaSpec media = new MediaSpec("screen");
			//specify some media feature values
			media.setDimensions(size.width, size.height); //set the visible area size in pixels
			media.setDeviceDimensions(size.width, size.height); //set the display size in pixels
			
			DOMAnalyzer da = new DOMAnalyzer(doc, src.getURL());
			da.setMediaSpec(media);
//			da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
			da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the standard style sheet
//			da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the additional style sheet
//	        da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT); //render form fields using css
	        da.getStyleSheets(); //load the author style sheets
	        
	        BrowserCanvas contentCanvas = new BrowserCanvas(da.getRoot(), da, src.getURL());
	        contentCanvas.setAutoMediaUpdate(false); //we have a correct media specification, do not update
	        contentCanvas.getConfig().setClipViewport(false);
	        contentCanvas.getConfig().setLoadImages(true);
	        contentCanvas.getConfig().setLoadBackgroundImages(true);

	        contentCanvas.createLayout(size);
	        
	        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
	        ImageIO.write(contentCanvas.getImage(), "png", tmp);
	        tmp.close();

	        src.close();
	        
	        ByteArrayInputStream b = new ByteArrayInputStream(tmp.toByteArray());
	        
	        
	        return ResponseEntity.ok()
	        		.contentLength(tmp.size())
	                .contentType(MediaType.parseMediaType("image/png"))
	                .body(new InputStreamResource(b));
	        
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
		} catch (SAXException e) {
			// TODO Auto-generated catch block
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