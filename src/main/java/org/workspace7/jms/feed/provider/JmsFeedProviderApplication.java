package org.workspace7.jms.feed.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
public class JmsFeedProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(JmsFeedProviderApplication.class, args);
	}
}
