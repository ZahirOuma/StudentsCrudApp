package ma.ensa.projetws;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AddEtudiant extends AppCompatActivity {

    private static final int CODE_IMAGE_PICK = 999;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 123;

    private EditText nom;
    private EditText prenom;
    private Spinner ville;
    private RadioButton m;
    private RadioButton f;
    private Button add;
    private ImageButton imgButton;
    private ImageView profileImage;
    private Bitmap selectedBitmap;

    private RequestQueue requestQueue;
    String insertUrl = "http://10.0.2.2/volley2/volley/sourcefiles/ws/createEtudiant.php";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_etudiant);

        // Initialize views
        nom = findViewById(R.id.nom);
        prenom = findViewById(R.id.prenom);
        ville = findViewById(R.id.ville);
        profileImage = findViewById(R.id.profile_image);
        add = findViewById(R.id.add);
        m = findViewById(R.id.m);
        f = findViewById(R.id.f);
        imgButton = findViewById(R.id.add_photo);

        requestQueue = Volley.newRequestQueue(this);

        imgButton.setOnClickListener(view -> checkPermissionAndSelectImage());

        add.setOnClickListener(view -> {
            if (validateInputs()) {
                uploadImage();
                Intent intent = new Intent(AddEtudiant.this, StudentListActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Pour éviter que l'utilisateur puisse revenir en arrière à l'activité actuelle
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(AddEtudiant.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check permission and select image
    private void checkPermissionAndSelectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            selectImage();
        }
    }


    // Launch intent to pick image
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CODE_IMAGE_PICK);
    }


    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(AddEtudiant.this, "Permission denied to access external storage", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



    // Handle the result of image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODE_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                selectedBitmap = BitmapFactory.decodeStream(inputStream);

                // Use Glide to load the image into the ImageView with circular crop
                Glide.with(this)
                        .load(imageUri)
                        .circleCrop()
                        .into(profileImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Convert image bitmap to Base64 string
    private String imageToString(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    // Upload image along with student data
    private void uploadImage() {
        StringRequest request = new StringRequest(Request.Method.POST, insertUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(AddEtudiant.this, "Student added successfully!", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(AddEtudiant.this, "Error adding student: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                String sexe = m.isChecked() ? "homme" : "femme";
                HashMap<String, String> params = new HashMap<>();
                params.put("nom", nom.getText().toString());
                params.put("prenom", prenom.getText().toString());
                params.put("ville", ville.getSelectedItem().toString());
                params.put("sexe", sexe);
                if (selectedBitmap != null) {
                    params.put("image", imageToString(selectedBitmap));
                }
                return params;
            }
        };

        requestQueue.add(request);
    }

    private boolean validateInputs() {
        return !nom.getText().toString().isEmpty() &&
                !prenom.getText().toString().isEmpty() &&
                ville.getSelectedItem() != null;
    }
}