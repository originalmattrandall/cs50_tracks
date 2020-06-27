package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class PokemonActivity extends AppCompatActivity {
    private ImageView imageView;
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView description;
    private Button isCaughtButton;

    private String url;
    private RequestQueue requestQueue;
    private boolean isCaught = false;

    private CapturedPokemonPreferences preferences = new CapturedPokemonPreferences();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");

        imageView = findViewById(R.id.pokemon_picture);
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        description = findViewById(R.id.pokemon_description);
        isCaughtButton = findViewById(R.id.is_caught);

        if(getIntent().getBooleanExtra("isCaught", false)){
            isCaughtButton.setText("Release");
            isCaught = true;
        }

        load();
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String name = response.getString("name");
                    String formattedName = name.substring(0, 1).toUpperCase() + name.toString().substring(1);
                    nameTextView.setText(formattedName);
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    String spriteUrl = response.getJSONObject("sprites").getString("front_default");
                    new DownloadSpriteTask().execute(spriteUrl);

                    String descriptionUrl = response.getJSONObject("species").getString("url");
                    LoadDescription(descriptionUrl);

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }

    public void toggleCatch(View view){
        String state = "";
        isCaught = !isCaught;
        String pokemonName = nameTextView.getText().toString();

        if(isCaught){
            state = "Release";
            if(!pokemonName.equals("")) {
                preferences.addPokemon(view.getContext(), pokemonName);
            }
        } else {
            state = "Catch";
            if(!pokemonName.equals("")) {
                preferences.removePokemon(view.getContext(), pokemonName);
            }
        }

        isCaughtButton.setText(state);
    }

    private void LoadDescription(String descriptionUrl){
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, descriptionUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray languages = response.getJSONArray("flavor_text_entries");
                    String englishDesc = languages.getJSONObject(0).getString("flavor_text");

                    description.setText(englishDesc);

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            try{
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }catch(IOException ex){
                Log.e("cs50", "Error downloading sprite");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap){
            imageView.setImageBitmap(bitmap);
        }
    }
}
