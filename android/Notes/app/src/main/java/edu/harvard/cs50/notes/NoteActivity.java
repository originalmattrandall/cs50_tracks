package edu.harvard.cs50.notes;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class NoteActivity extends AppCompatActivity {
    private EditText editText;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Intent intent = getIntent();
        id = intent.getIntExtra("id", 0);
        editText = findViewById(R.id.note_edit_text);
        editText.setText(intent.getStringExtra("content"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        MainActivity.database.noteDao().save(editText.getText().toString(), id);
    }

    public void deleteNote(View view){
        MainActivity.database.noteDao().delete(id);
        finish();
    }
}
