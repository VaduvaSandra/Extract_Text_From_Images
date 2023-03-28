package com.example.mlkit;

import static android.Manifest.permission.CAMERA;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScannerActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    //crearea variabilelor pentru image view, text view si cele 2 butoane
    private ImageView captureIV;
    private TextView resultTv;
    private Button snapbtn,detectbtn;
    //variabila pentru imaginea bitmap
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initializam variabilele
        setContentView(R.layout.activity_scanner);
        captureIV = findViewById(R.id.idIVCaptureImage);
        resultTv = findViewById(R.id.idTVDetectedText);
        snapbtn = findViewById(R.id.idBtnSnap);
        detectbtn = findViewById(R.id.idBtnDetect);

        //adaug on click listener pentru butonul detect
        detectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //apelam o metoda pentru a detecta textul
                detectText();

            }
        });
         snapbtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view){
                 if(checkPermissions()){
                     //apelam o metoda pentru a captura imaginea
                     captureImage();
                 }
                 else
                 {
                     requestPermissions();
                 }

             }
         });
    }

    private void requestPermissions() {
        int PERMISSION_CODE = 200;
        ActivityCompat.requestPermissions(this,new String[]{CAMERA},PERMISSION_CODE);

    }

    private boolean checkPermissions(){
        int camerPermision = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return camerPermision == PackageManager.PERMISSION_GRANTED;
    }


    private void captureImage(){
        // în metoda afișăm scopul de a ne capta imaginea.
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // pe linia de mai jos numim o activitate de pornire
        // pentru metoda rezultat pentru a obține imaginea capturată.
        if(takePicture.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePicture,REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length>0){
            boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if(cameraPermission){
                Toast.makeText(this, "Permisiune Acceptata", Toast.LENGTH_SHORT).show();
                captureImage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // apelam la metoda rezultatului activității.
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            // pe linia de mai jos primim
            // date din pachetele noastre.
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");

            // linia de mai jos este pentru a seta
            // imagine bitmap la imaginea noastră.
            captureIV.setImageBitmap(imageBitmap);

        }
    }
    //cream metoda detectText, care va detecta textul din imagine
    private void detectText(){
        InputImage image = InputImage.fromBitmap(imageBitmap,0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        Task<Text> result = recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            //cream o metoda in care stocam textul din imagine
            public void onSuccess(@NonNull Text text) {
                StringBuilder result = new StringBuilder();
                //extragem datele
                for(Text.TextBlock block: text.getTextBlocks()){
                    String blockText = block.getText();
                    Point[] blockCornerPoint = block.getCornerPoints();
                    Rect blockFrame = block.getBoundingBox();
                    for(Text.Line line : block.getLines()){
                        String lineTExt = line.getText();
                        Point[] lineCornerPoint = line.getCornerPoints();
                        Rect lineRect = line.getBoundingBox();
                        for(Text.Element element: line.getElements()){
                            String elementText = element.getText();
                            result.append(elementText);
                        }
                        resultTv.setText(blockText);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ScannerActivity.this, "Nu s-a gasit text in imagine"+e.getMessage(), Toast.LENGTH_LONG).show();

            }
        });
    }
}

