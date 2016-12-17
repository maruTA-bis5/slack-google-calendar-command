/*
 * @(#) net.bis5.slack.command.gcal.AppConfig
 * Copyright (c) 2016 Maruyama Takayuki <bis5.wsys@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 */
package net.bis5.slack.command.gcal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.jetty.JettyEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;

/**
 * TODO 型の説明
 * 
 * @author Maruyama Takayuki <bis5.wsys@gmail.com>
 * @since 2016/12/17
 */
@Configuration
public class AppConfig {
	@Autowired
	Environment env;

	@Bean
	public EmbeddedServletContainerFactory createServletContainerFactory() {
		JettyEmbeddedServletContainerFactory factory = new JettyEmbeddedServletContainerFactory();
		factory.setPort(Integer.valueOf(env.getProperty("PORT", "8000")));
		return factory;
	}

	public String getTargetCalendarId() {
		return Objects.requireNonNull(env.getProperty("CALENDAR_ID"), "env.CALENDAR_ID");
	}

	public JsonFactory jsonFactory() {
		return JacksonFactory.getDefaultInstance();
	}

	public HttpTransport newTransport() {
		try {
			return GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Bean
	public Credential loadCredential() throws IOException {
		String clientSectetsJson = env.getProperty("GOOGLE_CLIENT_SECRETS");
		InputStream stream;
		if (StringUtils.isEmpty(clientSectetsJson)) {
			stream = getClass().getClassLoader().getResourceAsStream("client_secret.json");
		} else {
			stream = new ByteArrayInputStream(clientSectetsJson.getBytes());
		}
		return GoogleCredential.fromStream(stream).createScoped(Collections.singleton(CalendarScopes.CALENDAR));
	}

	@Bean
	@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
	@Autowired
	public Calendar createCalendarClient(Credential credential) {
		return new Calendar.Builder(newTransport(), jsonFactory(), credential).build();
	}
}
