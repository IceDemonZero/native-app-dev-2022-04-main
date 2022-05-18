package com.silverorange.videoplayer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.StyledPlayerView;

import org.commonmark.node.Node;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import io.noties.markwon.Markwon;

public class MainActivity extends AppCompatActivity {
    private ExoPlayer videoPlayer;
    private StyledPlayerView videoView;
    private TextView description;
    private Markwon markwon;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        description = findViewById(R.id.description);
        description.setMovementMethod(new ScrollingMovementMethod());

        videoPlayer = new ExoPlayer.Builder(this).build();
        videoView = findViewById(R.id.videoView);
        videoView.setPlayer(videoPlayer);

        DataFetcher fetcher = new DataFetcher();
        fetcher.execute();

        markwon = Markwon.create(this);

    }

    private void onBackgroundTaskDataObtained(List<String> results) {
        MediaItem mediaItem = MediaItem.fromUri(results.get(0));
        videoPlayer.addMediaItem(mediaItem);
        videoPlayer.prepare();//videoPlayer.play();

        // parse markdown to commonmark-java Node
        final Node node = markwon.parse(results.get(2));
        // create styled text from parsed Node
        final Spanned markdown = markwon.render(node);
        // use it on a TextView
        markwon.setParsedMarkdown(description, markdown);
        //description.setText(results.get(1) + "\n" + markdown);
    }

    private class DataFetcher extends AsyncTask<Void,Void,List<String>> {
        List<String> data;
        @Override
        protected List<String> doInBackground(Void... voids) {
            JSONObject json = null;
            try {
                json = readJsonFromUrl("http://192.168.2.46:4000/videos");
                System.out.println("ARE YOU READY" + json.toString());
                data = new ArrayList<>();
                data.add((String) json.get("fullURL"));
                data.add((String) json.get("title"));
                data.add((String) json.get("description"));

                //data.add((String) json.get("name"));
                return data;
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        private String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }

        public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
            try (InputStream is = new URL(url).openStream()) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);

                jsonText = jsonText.substring(1, jsonText.length() - 1);
                System.out.println("TO DO NOW: " + jsonText);
                JSONObject json = new JSONObject(jsonText);
                return json;
            }
        }

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            System.out.println("AM I WORK");
            MainActivity.this.onBackgroundTaskDataObtained(data);
        }
    }

   /* public void loadVideos () {
        String data = "";
        try {
            URL url = new URL("http://localhost:4000/videos");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            String line = "";

            int index = 0;
            int character = 'a';
            while ((line = bufferedReader.readLine()) != null) {//line = bufferedReader.readLine();
                data = data + line;
            }
            String splits [] = line.split("fullURL |description |title ");
            JSONObject object = new JSONObject(data);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }*/
}
