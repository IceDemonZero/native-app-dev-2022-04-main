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

public class MainActivity extends AppCompatActivity {

    // UI related global variables
    private ExoPlayer videoPlayer;
    private StyledPlayerView videoView;
    private TextView description;
    private Markwon markwon;

    // Other
    private List<Video> videos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The textview which holds the description.
        description = findViewById(R.id.description);
        description.setMovementMethod(new ScrollingMovementMethod());

        // The video player and the view its associated with
        videoPlayer = new ExoPlayer.Builder(this).build();
        videoView = findViewById(R.id.videoView);
        videoView.setPlayer(videoPlayer);

        // This inner class retrieves the
        DataFetcher fetcher = new DataFetcher();
        fetcher.execute();

        markwon = Markwon.create(this);

        final ImageButton play = findViewById(R.id.play);
        final ImageButton pause = findViewById(R.id.pause);
        final ImageButton previous = findViewById(R.id.previous);
        final ImageButton next = findViewById(R.id.next);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoPlayer.play();
                pause.setVisibility(View.VISIBLE);
                play.setVisibility(View.INVISIBLE);
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
                MediaItem mediaItem = MediaItem.fromUri(videos.get(1).getUrl());
                videoPlayer.setMediaItem(mediaItem);
                videoPlayer.prepare();
                videoPlayer.pause();

                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.INVISIBLE);

                String text  = videos.get(1).getTitle() + "\n\n" + videos.get(1).getName() + "\n\n"
                        + videos.get(1).getDescription();
                // parse markdown to commonmark-java Node
                final Node node = markwon.parse(text);
                // create styled text from parsed Node
                final Spanned markdown = markwon.render(node);
                // use it on a TextView
                markwon.setParsedMarkdown(description, markdown);
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaItem mediaItem = MediaItem.fromUri(videos.get(0).getUrl());
                videoPlayer.setMediaItem(mediaItem);
                videoPlayer.prepare();//

                play.setVisibility(View.VISIBLE);
                pause.setVisibility(View.INVISIBLE);

                String text  = videos.get(0).getTitle() + "\n\n" + videos.get(0).getName() + "\n\n"
                        + videos.get(0).getDescription();
                // parse markdown to commonmark-java Node
                final Node node = markwon.parse(text);
                // create styled text from parsed Node
                final Spanned markdown = markwon.render(node);
                // use it on a TextView
                markwon.setParsedMarkdown(description, markdown);
            }
        });
    }

    private void onBackgroundTaskDataObtained(List<Video> results) {
        videos = results;
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

    }

    private class DataFetcher extends AsyncTask<Void,Void,List<Video>> {
        private List<Video> data;
        @Override
        protected List<Video> doInBackground(Void... voids) {
            try {
                JSONObject [] json = readJsonFromUrl("http://192.168.2.46:4000/videos");
                data = new ArrayList<>();

                for (JSONObject jsonObject : json) {
                    JSONObject author = jsonObject.getJSONObject("author");
                    Video video = new Video(jsonObject.getString("fullURL"),
                            jsonObject.getString("title"),
                            author.getString("name"),
                            jsonObject.getString("publishedAt"),
                            jsonObject.getString("description"));
                    try {
                        if (data.size() > 1) {
                            for (int i = 0; i < data.size(); i++) {
                                if (video.isDateAfter(data.get(i).getDate())) {
                                    data.add(video);
                                    break;
                                } else if (i == data.size() - 1)
                                    data.add(video);
                            }
                        } else
                            data.add(video);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return data;
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
            MainActivity.this.onBackgroundTaskDataObtained(data);
        }
    }
}
