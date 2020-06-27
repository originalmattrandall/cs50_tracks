package edu.harvard.cs50.pokedex;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class CapturedPokemonPreferences{
    public final String PREFERENCE_NAME = "POKEMON_PREFERENCES";
    public final String CAPTURED_POKEMON = "CAPTURED_POKEMON";

    public void savePokemon(Context context, Set<String> pokemon){
        SharedPreferences sharedPreferences;
        SharedPreferences.Editor editor;

        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putStringSet(CAPTURED_POKEMON, pokemon);

        editor.apply();
    }

    public HashSet<String> getCapturedPokemon(Context context){
        SharedPreferences sharedPreferences;
        Set<String> pokemon = new HashSet<>();

        sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);

        pokemon = sharedPreferences.getStringSet(CAPTURED_POKEMON, new HashSet<String>());

        return new HashSet<>(pokemon);
    }

    public void addPokemon(Context context, String pokemonName){
        Set<String> pokemon = getCapturedPokemon(context);

        if(pokemon == null){
            pokemon = new HashSet<>();
        }

        pokemon.add(pokemonName);

        savePokemon(context, pokemon);
    }

    public void removePokemon(Context context, String pokemonName){
        Set<String> pokemon = getCapturedPokemon(context);

        if(pokemon != null){
            pokemon.remove(pokemonName);
            savePokemon(context, pokemon);
        }
    }
}
