package com.silverorange.videoplayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import org.commonmark.node.Image;
import org.commonmark.node.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import io.noties.markwon.Markwon;

/**
 * Handles the UI and functionality for the video player
 */
public class MainActivity extends AppCompatActivity {

    // UI related global variables
    private ExoPlayer videoPlayer;
    private StyledPlayerView videoView;
    private TextView description;
    private Markwon markwon;

    // Other
    private List<Video> videos; // Holds the videos. Probably should have used a linkedlist but I'm only now realizing it.
    private static boolean isLoaded; //
    private static int index; // The index of the current video.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isLoaded = false; // No video loaded on startup

        // The textview which holds the description.
        description = findViewById(R.id.description);
        description.setMovementMethod(new ScrollingMovementMethod());

        // The video player and the view its associated with
        videoPlayer = new ExoPlayer.Builder(this).build();
        videoView = findViewById(R.id.videoView);
        videoView.setPlayer(videoPlayer);
        videoView.setUseController(false); // Disables the natural video controllers.

        // This inner class retrieves the videos from the server
        DataFetcher fetcher = new DataFetcher();
        fetcher.execute();

        // This instance will be used for the markdown
        markwon = Markwon.create(this);

        // Instances for the buttons
        final ImageButton play = findViewById(R.id.play);
        final ImageButton pause = findViewById(R.id.pause);
        final ImageButton previous = findViewById(R.id.previous);
        final ImageButton next = findViewById(R.id.next);

        // Click listeners for the buttons are all below
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLoaded) { // only play when the video has been loaded properly
                    videoPlayer.play();
                    pause.setVisibility(View.VISIBLE);
                    play.setVisibility(View.INVISIBLE);
                }
            }
        });

        pause.setVisibility(View.INVISIBLE);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPlayer.pause();
                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.INVISIBLE);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index != videos.size() - 1) {
                    index++;
                    isLoaded = false;

                    MediaItem mediaItem = MediaItem.fromUri(videos.get(index).getUrl());
                    videoPlayer.setMediaItem(mediaItem);
                    videoPlayer.prepare();
                    videoPlayer.pause();

                    play.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.INVISIBLE);

                    String text = videos.get(index).getTitle() + "\n\n" + videos.get(index).getName()
                            + "\n\n" + videos.get(index).getDescription();
                    // parse markdown to commonmark-java Node
                    final Node node = markwon.parse(text);
                    // create styled text from parsed Node
                    final Spanned markdown = markwon.render(node);
                    // use it on a TextView
                    markwon.setParsedMarkdown(description, markdown);
                    isLoaded = true;
                }
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (index != 0) {
                    index--;
                    isLoaded = false;

                    MediaItem mediaItem = MediaItem.fromUri(videos.get(index).getUrl());
                    videoPlayer.setMediaItem(mediaItem);
                    videoPlayer.prepare();
                    videoPlayer.setPlayWhenReady(false);

                    play.setVisibility(View.VISIBLE);
                    pause.setVisibility(View.INVISIBLE);

                    //The below string combines all the text for the description
                    String text = videos.get(index).getTitle() + "\n\n" + videos.get(index).getName()
                            + "\n\n" + videos.get(index).getDescription();
                    // parse markdown to commonmark-java Node
                    final Node node = markwon.parse(text);
                    // create styled text from parsed Node
                    final Spanned markdown = markwon.render(node);
                    // use it on a TextView
                    markwon.setParsedMarkdown(description, markdown);
                    isLoaded = true;
                }
            }
        });
    }

    /**
     * Once the videos have been loaded in the background. Insert the first
     * into the video player.
     * @param results
     */
    private void onBackgroundTaskDataObtained(List<Video> results) {
        videos = results; // store videos into the video list

        // Create a video from the url
        MediaItem mediaItem = MediaItem.fromUri(results.get(0).getUrl());
        videoPlayer.addMediaItem(mediaItem);
        videoPlayer.prepare();//

        String text  = results.get(0).getTitle() + "\n\n" + results.get(0).getName() + "\n\n"
                + results.get(0).getDescription();
        // parse markdown to commonmark-java Node
        final Node node = markwon.parse(text);
        // create styled text from parsed Node
        final Spanned markdown = markwon.render(node);
        // use it on a TextView
        markwon.setParsedMarkdown(description, markdown);

        isLoaded = true; // video loaded
        index = 0;
    }

    /**
     * The class responsible for fetching data from the server
     */
    private class DataFetcher extends AsyncTask<Void,Void,List<Video>> {
        private List<Video> videosRetrieved; // Videos currently stored
        @Override
        protected List<Video> doInBackground(Void... voids) {
            try {
                // Gets the json object(s) from the server. Currently using my
                // ipv4 address but you'll want to change that.
                JSONObject [] json = readJsonFromUrl("http://192.168.2.46:4000/videos");
                videosRetrieved = new ArrayList<>();

                for (JSONObject jsonObject : json) {
                    JSONObject author = jsonObject.getJSONObject("author");
                    // Created a video object with the data from the Json object
                    Video video = new Video(jsonObject.getString("fullURL"),
                            jsonObject.getString("title"),
                            author.getString("name"),
                            jsonObject.getString("publishedAt"),
                            jsonObject.getString("description"));

                    // This algorithm below sorts the new video into its correct spot.
                    // Went with a newest to oldest sort. I found that the second video in the
                    // sample was not working so that's my reason why.
                    try {
                        if (videosRetrieved.size() > 0) {
                            for (int i = 0; i < videosRetrieved.size(); i++) {
                                if (video.isDateAfter(videosRetrieved.get(i).getDate())) {
                                    Video temporary = videosRetrieved.get(i);
                                    videosRetrieved.set(i, video);
                                    videosRetrieved.add(temporary);
                                    break;
                                }else if (i == videosRetrieved.size() - 1) {
                                    videosRetrieved.add(video);
                                    break;
                                }
                            }
                        } else
                            videosRetrieved.add(video);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return videosRetrieved;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1)
                sb.append((char) cp);
            return sb.toString();
        }

        /**
         * Creates a list of the Jsonobjects from the server
         * @param url
         * @return Json objects for each video
         * @throws IOException
         * @throws JSONException
         */
        private JSONObject[] readJsonFromUrl(String url) throws IOException, JSONException {
            try (InputStream is = new URL(url).openStream()) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                jsonText = jsonText.substring(1, jsonText.length() - 1);

                String videos [] = jsonText.split( "\\},");
                JSONObject jsons [] = new JSONObject[videos.length];

                for (int i = 0; i < videos.length; i++) {
                    if (i != videos.length - 1) videos[i] = videos[i] +  "}";
                    jsons[i] = new JSONObject(videos[i]);
                }
                return jsons;
            }
        }

        @Override
        protected void onPostExecute(List<Video> result) {
            super.onPostExecute(result);
            // Send back a list of all the videos that could be retrieved from the server
            MainActivity.this.onBackgroundTaskDataObtained(videosRetrieved);
        }
    }
}
