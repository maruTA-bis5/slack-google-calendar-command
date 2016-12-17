/*
 * @(#) net.bis5.slack.command.gcal.SlashCommandApi
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

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import lombok.extern.slf4j.Slf4j;
import net.bis5.slack.command.gcal.RequestPayload.EventRequest;

/**
 * TODO 型の説明
 * 
 * @author Maruyama Takayuki <bis5.wsys@gmail.com>
 * @since 2016/12/17
 */
@RestController
@SpringBootApplication
@Slf4j
public class SlashCommandApi {
	public static void main(String[] args) {
		SpringApplication.run(SlashCommandApi.class, args);
	}

	@Autowired
	Calendar client;
	@Autowired
	AppConfig config;

	@RequestMapping(path = "/debug", method = RequestMethod.POST)
	public ResponseEntity<String> execute(String body) {
		return ResponseEntity.ok(body);
	}

	@RequestMapping(path = "/execute", method = RequestMethod.POST)
	public ResponseEntity<String> execute(@ModelAttribute RequestPayload payload) {
		log.info("Request: " + payload.toString());

		EventRequest event = payload.createEvent();
		log.info("Parsed Request: " + event.toString());

		try {
			Event result = client.events().insert(config.getTargetCalendarId(), createEvent(event)).execute();
			log.info("Event Create Result: " + result.toString());

			ResponsePayload response = new ResponsePayload();
			Date date = toDate(event.getDate().atStartOfDay());
			Date start = toDate(LocalDateTime.of(event.getDate(), event.getFrom()));
			Date end = toDate(LocalDateTime.of(event.getDate(), event.getTo()));
			String user = payload.getUser_name();
			String title = event.getTitle();
			response.setText(String.format("%sが予定を追加しました。\n・日付: %tY/%tm/%td\n・開始: %tH:%tM\n・終了: %tH:%tM\n・件名: %s", //
					user, date, date, date, start, start, end, end, title));

			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				ObjectMapper mapper = new ObjectMapper();
				String responseBody = mapper.writeValueAsString(response);
				log.info("Response Payload: " + responseBody);
				Request.Post(payload.getResponse_url()).bodyString(responseBody, ContentType.APPLICATION_JSON)
						.execute();
			}

			return ResponseEntity.ok(null);
		} catch (IOException ex) {
			log.error(ex.toString());
			return ResponseEntity.ok(ex.getMessage()); // 全然OKじゃないけど、ユーザにレス返すので
		}
	}

	private Date toDate(LocalDateTime dateTime) {
		return Date.from(dateTime.toInstant(ZoneOffset.ofHours(+9)));
	}

	private Event createEvent(EventRequest request) {
		Event event = new Event();
		event.setSummary(request.getTitle());
		event.setLocation(request.getPlace());
		EventDateTime start = toDateTime(request.getDate(), request.getFrom());
		EventDateTime end = toDateTime(request.getDate(), request.getTo());
		event.setStart(start);
		event.setEnd(end);

		return event;
	}

	private EventDateTime toDateTime(LocalDate date, LocalTime time) {
		if (time != null) {
			DateTime dateTime = new DateTime(Date.from(LocalDateTime.of(date, time).toInstant(ZoneOffset.ofHours(+9))));
			return new EventDateTime().setDateTime(dateTime);
		} else {
			DateTime dateTime = new DateTime(true, Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC)).getTime(),
					9);
			return new EventDateTime().setDate(dateTime);
		}
	}

}
