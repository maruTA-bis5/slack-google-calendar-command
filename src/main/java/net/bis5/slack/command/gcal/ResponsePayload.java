/*
 * @(#) net.bis5.slack.command.gcal.ResponsePayload
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

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * TODO 型の説明
 * 
 * @author Maruyama Takayuki <bis5.wsys@gmail.com>
 * @since 2016/12/17
 */
@Data
public class ResponsePayload {

	@JsonProperty("response_type")
	private String responseType = "in_channel";
	private String text;

}
