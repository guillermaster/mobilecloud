


package org.magnum.dataup;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import retrofit.client.Response;
import retrofit.http.Streaming;

@Controller
public class VideoController {

    private Collection<Video> videoList;
    private Long counter;
    private VideoFileManager objVideoFileManager;
    private static final int DEFAULT_BUFFERSIZE = 20480;

    @PostConstruct
    public void init() throws IOException
    {
        videoList = new CopyOnWriteArrayList<Video>();
        objVideoFileManager = VideoFileManager.get();
        counter = 1L;
    }


    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    @ResponseBody
    public Collection<Video> getVideoList(HttpServletResponse response)
    {
        response.setContentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_OK);
        return videoList;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    @ResponseBody
    public Video addVideo(@RequestBody Video v)
    {
        v.setId(counter);
        v.setDataUrl(getVideoUrl(counter));
        counter++;
        videoList.add(v);
        return v;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
    @ResponseBody
    public VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) Long id,
                                    @RequestPart(VideoSvcApi.DATA_PARAMETER) MultipartFile data,
                                    HttpServletResponse response) {

        VideoStatus videoStatus = new VideoStatus(VideoStatus.VideoState.PROCESSING);

        try
        {
            Video video = getVideoById(id);
            if (video != null)
            {
                saveVideo(video, data);
                videoStatus = new VideoStatus(VideoStatus.VideoState.READY);
            }
            else
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return videoStatus;
    }

    @Streaming
    @RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
    public HttpServletResponse getData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
                                       HttpServletResponse response) {

        Video video = getVideoById(id);
        if (video == null)
        {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else
        {
            if (response.getContentType() == null) {
                response.setContentType("video/mp4");
            }
            try
            {
                objVideoFileManager.copyVideoData(video, response.getOutputStream());
            }
            catch (Exception e)
            {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        }
        return response;
    }


    private Video getVideoById(Long id) {
        for (Video v : videoList) {
            if (v.getId() == id) return v;
        }
        return null;
    }

    private String getVideoUrl(Long id) {
        return getUrlBaseForLocalServer() + String.format("/video/%s/data", id.toString());
    }

    public void saveVideo(Video v, MultipartFile videoData) throws IOException
    {
        objVideoFileManager.saveVideoData(v, videoData.getInputStream());
    }

    private String getUrlBaseForLocalServer()
    {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "http://" + request.getServerName()
                + ((request.getServerPort() != 80) ? ":"
                + request.getServerPort() : "");
    }
}