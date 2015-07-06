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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.test.web.client.ResponseCreator;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;

import retrofit.client.Response;
import retrofit.mime.TypedFile;

@Controller
public class VideoController {
	public static final String DATA_PARAMETER = "data";
	public static final String ID_PARAMETER = "id";
	public static final String VIDEO_SVC_PATH = "/video";	
	public static final String VIDEO_DATA_PATH = VIDEO_SVC_PATH + "/{id}/data";
	private Dictionary<Long, Video> videos = new Hashtable<Long, Video>(); 
	private static final AtomicLong currentId = new AtomicLong(0L);
	
	// Receives GET requests to /video and returns the current
	// list of videos in memory. Spring automatically converts
	// the list of videos to JSON because of the @ResponseBody
	// annotation.
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList(){
		List<Video> list=new ArrayList<Video>();
	    Enumeration<Video> e=((Dictionary<Long, Video>)videos).elements();
	    while (e.hasMoreElements()) list.add(e.nextElement());
	    return list;
	}
	
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.GET)
	public @ResponseBody byte[] getData(@PathVariable(ID_PARAMETER) long id){
		//check if the video id exists
		if(id > currentId.get() || id <= 0){
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
			ByteArrayOutputStream outputStream = getVideoData(video);
			byte[] outputBytes = outputStream.toByteArray();
			//InputStream inputStream = new ByteArrayInputStream(outputBytes);
			return outputBytes;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceNotFoundException();
		}
	}
	
	// Adds a video meta data to the repository 
	@RequestMapping(value=VIDEO_SVC_PATH, method=RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v){
		return save(v);
	}
	
	@RequestMapping(value=VIDEO_DATA_PATH, method=RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(ID_PARAMETER) long id, @RequestParam(DATA_PARAMETER) MultipartFile videoData){
		//check file data
		if(videoData.isEmpty()){
			throw new ResourceNotFoundException();
		}
		//check if the video id exists
		if(id > currentId.get() || id <= 0){
			throw new ResourceNotFoundException();
		}
		
		//get video from list
		Video video = videos.get(id);
		//save data
		Boolean saveResponse = saveVideoBinary(video, videoData);
		
		//check if there was an error saving video data
		if(!saveResponse){
			throw new ResourceNotFoundException();
		}
		
		VideoStatus status = new VideoStatus(VideoStatus.VideoState.READY);
		return status;
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
        entity.setDataUrl(getDataUrl(entity.getId()));
        videos.put(entity.getId(), entity);
        return entity;
    }
	
	private Boolean saveVideoBinary(Video video, MultipartFile videoFile){
		try {
			VideoFileManager videoFileMngr = VideoFileManager.get();
			InputStream is = new ByteArrayInputStream(videoFile.getBytes());
			videoFileMngr.saveVideoData(video, is);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }
	
	private ByteArrayOutputStream getVideoData(Video video){
		
		try {
			VideoFileManager videoFileMngr = VideoFileManager.get();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			videoFileMngr.copyVideoData(video, outputStream);
			return outputStream;
		} catch (IOException e) {
			e.printStackTrace();
			throw new ResourceNotFoundException();
		}
	}

}
