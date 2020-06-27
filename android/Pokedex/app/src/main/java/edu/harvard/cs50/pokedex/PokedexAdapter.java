package edu.harvard.cs50.pokedex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PokedexAdapter extends RecyclerView.Adapter<PokedexAdapter.PokedexViewHolder> implements Filterable {

    private List<Pokemon> pokemon = new ArrayList<>();
    private List<Pokemon> filtered = new ArrayList<>();
    private Set<String> pokemonSet = new HashSet<>();
    private RequestQueue requestQueue;

    public static class PokedexViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout containerView;
        public TextView textView;

        PokedexViewHolder(View view) {
            super(view);

            containerView = view.findViewById(R.id.pokedex_row);
            textView = view.findViewById(R.id.pokedex_row_text_view);

            containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CapturedPokemonPreferences preferences = new CapturedPokemonPreferences();
                    HashSet<String> capturedPokemon = preferences.getCapturedPokemon(v.getContext());

                    Pokemon current = (Pokemon) containerView.getTag();
                    Intent intent = new Intent(v.getContext(), PokemonActivity.class);
                    intent.putExtra("url", current.getUrl());
                    intent.putExtra("isCaught", capturedPokemon.contains(current.getName()));

                    v.getContext().startActivity(intent);
                }
            });
        }
    }

    public class PokemonFilter extends Filter{
        @Override
        protected FilterResults performFiltering(final CharSequence constraint) {

            FilterResults results = new FilterResults();

            List<Pokemon> searchResults = new ArrayList<>();
            if(constraint.length() != 0) {
                for (Pokemon currentPokemon : pokemon) {
                    String name = constraint.toString().substring(0, 1).toUpperCase() + constraint.toString().substring(1);
                    if (currentPokemon.getName().contains(name)) {
                        searchResults.add(currentPokemon);
                    }
                }

                results.values = searchResults;
                results.count = searchResults.size();
            } else {
                results.values = pokemon;
                results.count = pokemon.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filtered = (List<Pokemon>) results.values;
            notifyDataSetChanged();
        }
    }

    PokedexAdapter(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        loadPokemon(context);
        filtered = pokemon;
    }

    public void loadPokemon(final Context context) {
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String name = result.getString("name");
                        String formattedName = name.substring(0, 1).toUpperCase() + name.substring(1);
                        pokemon.add(new Pokemon(
                                formattedName,
                                result.getString("url")
                        ));

                    }

                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("cs50", "Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon list error", error);
            }
        });

        requestQueue.add(request);
    }

    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    @NonNull
    @Override
    public PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokedexViewHolder holder, int position) {
        Pokemon current = filtered.get(position);
        holder.textView.setText(current.getName());
        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        if (filtered != null){
            return filtered.size();
        } else {
            return 0;
        }
    }
}
