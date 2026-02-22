package com.example.projectx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projectx.model.Clothe;
import com.example.projectx.services.DatabaseService;
import com.example.projectx.util.ImageUtil;
import com.google.firebase.auth.FirebaseAuth;

public class AddClothe extends AppCompatActivity {

    private Spinner spinnerType, spinnerColor, spinnerSeason;
    private RadioGroup radioGenderGroup;
    private ImageView itemImage;
    private Button btnGallery, btnCamera, btnAdd;

    private DatabaseService databaseService;
    private ActivityResultLauncher<Intent> cameraLauncher;

    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addclothe);

        initViews();

        ImageUtil.requestPermission(this);
        databaseService = DatabaseService.getInstance();

        // מצלמה
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = (Bitmap) result.getData().getExtras().get("data");
                        itemImage.setImageBitmap(bitmap);
                    }
                });

        btnGallery.setOnClickListener(v -> imageChooser());
        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        });
        btnAdd.setOnClickListener(v -> addClothe());

        // מלא את ה-spinner של העונות עם "All" בתחילת הרשימה
        String[] seasons = {"All", "קיץ", "אביב", "סתיו", "חורף"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, seasons
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeason.setAdapter(adapter);
    }

    private void initViews() {
        spinnerType = findViewById(R.id.spinner_type);
        spinnerColor = findViewById(R.id.spinner_color);
        spinnerSeason = findViewById(R.id.spinner_season);

        radioGenderGroup = findViewById(R.id.radio_gender_group);

        itemImage = findViewById(R.id.item_image);

        btnGallery = findViewById(R.id.button_choose_gallery);
        btnCamera = findViewById(R.id.button_take_photo);
        btnAdd = findViewById(R.id.button_add);
    }

    private void addClothe() {
        String type = spinnerType.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();
        String season = spinnerSeason.getSelectedItem().toString();

        int selectedId = radioGenderGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "נא לבחור גבר או אישה", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isFavorite = selectedId == R.id.radio_male;

        if (itemImage.getDrawable() == null) {
            Toast.makeText(this, "נא לבחור תמונה", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUrl = ImageUtil.convertTo64Base(itemImage);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String clotheId = databaseService.generateClotheId();

        // יצירת אובייקט Clothe
        Clothe clothe = new Clothe(
                clotheId,
                userId,
                type,
                color,
                imageUrl,
                season,
                isFavorite
        );

        databaseService.createNewClothe(clothe, new DatabaseService.DatabaseCallback<Void>() {
            @Override
            public void onCompleted(Void object) {
                Toast.makeText(AddClothe.this, "הפריט נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailed(Exception e) {
                Toast.makeText(AddClothe.this, "שגיאה בהוספת הפריט", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    itemImage.setImageURI(selectedImageUri);
                }
            }
        }
    }
}