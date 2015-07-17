/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class VideoServiceController {

	private static final String VIDEO_PATH = "/video";
	private static final String DATA_PATH = "/video/{id}/data";

	private AtomicLong currentId = new AtomicLong();

	private Map<Long, Video> videos = new ConcurrentHashMap<>();

	private VideoFileManager manager;

	public VideoServiceController() throws IOException {
		manager = VideoFileManager.get();
	}

	@RequestMapping(value = VIDEO_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideos() {
		return videos.values();
	}

	@RequestMapping(value = VIDEO_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		long id = currentId.incrementAndGet();

		v.setId(id);
		v.setDataUrl(getDataUrl(id));

		videos.put(v.getId(), v);

		return v;
	}

	@RequestMapping(value = DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable("id") long id,
			@RequestParam("data") MultipartFile videoData, HttpServletResponse response) throws IOException {

		VideoStatus result = null;

		if (videos.containsKey(id)) {
			if (videoData != null) {
				manager.saveVideoData(videos.get(id), videoData.getInputStream());
				result = new VideoStatus(VideoState.READY);
			} else {
				response.sendError(400, "No video data given.");
			}
		} else {
			response.sendError(404, "No video with id=" + id + " exists.");
		}

		return result;
	}

	@RequestMapping(value = DATA_PATH, method = RequestMethod.GET)
	public void getData(@PathVariable("id") long id, HttpServletResponse response) throws IOException {
		if (videos.containsKey(id)) {
			Video v = videos.get(id);
			manager.copyVideoData(v, response.getOutputStream());
		} else {
			response.sendError(404, "No video with id=" + id + " exists.");
		}
	}

	private static String getDataUrl(long videoId) {
		return getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
	}

	private static String getUrlBaseForLocalServer() {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
				.getRequest();
		String base = "http://" + request.getServerName()
				+ ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
		return base;
	}

}
