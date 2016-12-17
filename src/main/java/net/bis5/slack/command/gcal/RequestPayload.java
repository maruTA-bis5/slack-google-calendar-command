/*
 * @(#) net.bis5.slack.command.gcal.RequestPayload
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import lombok.Data;
import lombok.Value;

/**
 * TODO 型の説明
 * 
 * @author Maruyama Takayuki <bis5.wsys@gmail.com>
 * @since 2016/12/17
 */
@Data
public class RequestPayload {

	private String token;
	private String team_id;
	private String team_domain;
	private String channel_id;
	private String channel_name;
	private String user_id;
	private String user_name;
	private String command;
	private String text;
	private String response_url;

	// /calendar (yyyy/mm/dd) all (title) [place]
	// /calendar (yyyy/mm/dd) (hh:mm) (hh:mm) (title) [place]

	public EventRequest createEvent() {
		Queue<String> queue = new LinkedList<>(Arrays.asList(text.split(" ")));
		queue.remove("");

		String dateStr = queue.poll();
		LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		String next = queue.poll();
		if (Objects.equals(next, "all")) {
			String title = queue.poll();

			String place = queue.poll();

			return new EventRequest(date, null, null, title, place);
		} else {
			LocalTime from = toTime(next);

			String toStr = queue.poll();
			LocalTime to = toTime(toStr);

			String title = queue.poll();
			String place = queue.poll();

			return new EventRequest(date, from, to, title, place);
		}
	}

	private LocalTime toTime(String input) {
		String[] arr = input.split(":");
		return LocalTime.of(Integer.valueOf(arr[0]), Integer.valueOf(arr[1]));
	}

	@Value
	public static class EventRequest {
		private LocalDate date;
		private LocalTime from;
		private LocalTime to;
		private String title;
		private String place;

		public boolean isAllDay() {
			return from == null && to == null;
		}
	}
}
