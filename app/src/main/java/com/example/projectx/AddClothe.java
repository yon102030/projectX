package com.example.projectx;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
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
    private LinearLayout layoutFavorite; // LinearLayout במקום RadioGroup
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
    }

    private void initViews() {
        spinnerType = findViewById(R.id.spinner_type);
        spinnerColor = findViewById(R.id.spinner_color);
        spinnerSeason = findViewById(R.id.spinner_season);

        layoutFavorite = findViewById(R.id.layout_favorite); // LinearLayout

        itemImage = findViewById(R.id.item_image);

        btnGallery = findViewById(R.id.button_choose_gallery);
        btnCamera = findViewById(R.id.button_take_photo);
        btnAdd = findViewById(R.id.button_add);
    }

    private void addClothe() {

        String type = spinnerType.getSelectedItem().toString();
        String color = spinnerColor.getSelectedItem().toString();
        String season = spinnerSeason.getSelectedItem().toString();

        // בדיקה ידנית של הרדיו בתוך ה-LinearLayout
        boolean isFavorite = false;
        for (int i = 0; i < layoutFavorite.getChildCount(); i++) {
            if (layoutFavorite.getChildAt(i) instanceof RadioButton) {
                RadioButton rb = (RadioButton) layoutFavorite.getChildAt(i);
                if (rb.isChecked()) {
                    // כאן אנחנו מגדירים לפי הטקסט
                    isFavorite = rb.getText().toString().equals("גבר"); // או "מועדף" בהתאם למה שאתה רוצה
                    break;
                }
            }
        }

        if (itemImage.getDrawable() == null) {
            Toast.makeText(this, "נא לבחור תמונה", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageUrl = ImageUtil.convertTo64Base(itemImage);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String clotheId = databaseService.generateClotheId();

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

    void imageChooser() {
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
