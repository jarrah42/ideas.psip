package ideas.psip.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties
@ConfigurationProperties
public class ServerConfig {
	private static final String ENABLE_TEST_ACCOUNT = "enableTestAccount";
	private static final String VERSION = "version";
	private static final String BUILD_TIMESTAMP = "buildTimestamp";
	
	private Map<String, String> properties = new HashMap<>();

	public Map<String, String> getProfiles() {
		return properties;
	}
	
	public void setProfiles(Map<String, String> profiles) {
		this.properties = profiles;
		for (String key : new HashSet<>(profiles.keySet())) {
			if (key.contains("password") || key.contains("username")) {
				profiles.remove(key);
			}
		}
	}
   
	public boolean hasTestAccount() {
		return properties.containsKey(ENABLE_TEST_ACCOUNT) ? Boolean.parseBoolean(properties.get(ENABLE_TEST_ACCOUNT)) : false;
	}
	public String getVersion() {
		return properties.containsKey(VERSION) ? properties.get(VERSION) : "";
	}
	public String getBuildTimestamp() {
		return properties.containsKey(BUILD_TIMESTAMP) ? properties.get(BUILD_TIMESTAMP) : "";
	}
}
