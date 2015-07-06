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
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.magnum.dataup.model.Video;

@Controller
public class VideoController {
	
	//private List<Video> videos = new CopyOnWriteArrayList<Video>();
	private Dictionary<Long, Video> videos = new Hashtable<Long, Video>(); 
	private long nextVideoId = 1;
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	// Receives GET requests to /video and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	@RequestMapping(value="/video", method=RequestMethod.GET)
	public @ResponseBody List<Video> getVideoList(){
		List<Video> list=new ArrayList<Video>();
	    Enumeration<Video> e=((Dictionary<Long, Video>)videos).elements();
	    while (e.hasMoreElements()) list.add(e.nextElement());
	    return list;
	}
	
	@RequestMapping(value="/video/{id}/data", method=RequestMethod.GET)
	public void getVideo(@PathVariable("id") long id, HttpServletResponse response){
		//check if the video id exists
		if(id >= nextVideoId){
			throw new ResourceNotFoundException();
		}
		
		//get video from list
		Video video = videos.get(id);
		try {
			//check if video binary file exists
			VideoFileManager videoMngr = VideoFileManager.get();
			
			if(!videoMngr.hasVideoData(video)){
				throw new ResourceNotFoundException();
			}
			
			//return binary video content		
			videoMngr.copyVideoData(video, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceNotFoundException();
		}
	}
	
	// Adds a video meta data to the repository 
	@RequestMapping(value="/video", method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		return save(video);
	}
	
	
	private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }
	
	private String getUrlBaseForLocalServer() {
        HttpServletRequest request = 
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base = 
           "http://"+request.getServerName() 
           + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
     }
	
	private Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }
	
	private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }
}
