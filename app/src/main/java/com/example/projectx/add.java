package com.example.projectx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class add extends AppCompatActivity {

    private Spinner spinnerType, spinnerColor, spinnerSeason;
    private RadioGroup radioFavorite;
    private Button buttonAdd, buttonChooseGallery, buttonTakePhoto;
    private ImageView itemImage;
    private Uri imageUri;


    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // חיבור Spinners
        spinnerType = findViewById(R.id.spinner_type);
        spinnerColor = findViewById(R.id.spinner_color);
        spinnerSeason = findViewById(R.id.spinner_season);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.typeArr, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,
                R.array.colorArr, android.R.layout.simple_spinner_item);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        ArrayAdapter<CharSequence> seasonAdapter = ArrayAdapter.createFromResource(this,
                R.array.seasonArr, android.R.layout.simple_spinner_item);
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeason.setAdapter(seasonAdapter);

        // חיבור RadioGroup וכפתור
        radioFavorite = findViewById(R.id.radio_favorite);
        buttonAdd = findViewById(R.id.button_add);

        // ImageView וכפתורים של תמונה
        itemImage = findViewById(R.id.item_image);
        buttonChooseGallery = findViewById(R.id.button_choose_gallery);
        buttonTakePhoto = findViewById(R.id.button_take_photo);

        // לאתחל Launchers
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                            itemImage.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap photo = (Bitmap) result.getData().getExtras().get("data");
                        itemImage.setImageBitmap(photo);
                    }
                });

        // כפתור גלריה
        buttonChooseGallery.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // כפתור מצלמה
        buttonTakePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });

        // כפתור הוסף פריט
        buttonAdd.setOnClickListener(v -> {
            String type = spinnerType.getSelectedItem().toString();
            String color = spinnerColor.getSelectedItem().toString();
            String season = spinnerSeason.getSelectedItem().toString();

            int selectedId = radioFavorite.getCheckedRadioButtonId();
            boolean isFavorite = selectedId == R.id.radio_yes;

            // ניתן להוסיף כאן שמירת פריט בבסיס נתונים או העברה לעמוד אחר
            String message = "סוג: " + type + "\nצבע: " + color + "\nעונה: " + season + "\nמועדף: " + isFavorite
                    + "\nתמונה: " + (imageUri != null ? imageUri.toString() : "לא הוזנה");
            Toast.makeText(add.this, message, Toast.LENGTH_LONG).show();
        });
    }
}
