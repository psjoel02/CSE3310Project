package com.example.cse3310project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cse3310project.ml.ModelUnquant;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ImageUploadActivity extends AppCompatActivity
{
    public static final String PACKAGE_EXTRA = "com.example.cse3310project.PACKAGE_EXTRA";
    public static final String PACKAGE_EXTRA2 = "com.example.cse3310project.PACKAGE_EXTRA2";
    public static final int GALLERY_CODE = 100;
    public static final int CAMERA_CODE = 101;

    Button galleryButton, cameraButton, descriptionButton, home; //take picture button
    public static final int IMAGE_SIZE = 224;

    ImageView picView; //image of picture taken
    TextView animal, breed; //for text of animal and breed
    private ActivityResultLauncher<Intent> cameraLauncher, galleryLauncher;
    private boolean uploadedFirstImage = false, takenFirstImage = false;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);
        setTitle("Gallery Upload");
        picView = findViewById(R.id.myImage);
        galleryButton = findViewById(R.id.uploadButton);
        cameraButton = findViewById(R.id.pictureButton);
        descriptionButton = findViewById(R.id.descriptionButton);
        breed = findViewById(R.id.breed);
        animal = findViewById(R.id.animal);
        home = findViewById(R.id.homeButtonCamera);


        home.setOnClickListener(view -> {
            Intent i = new Intent(this, HomeActivity.class);
            startActivity(i);
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null)
            {
                try {
                    Uri data = result.getData().getData();
                    Bitmap pic = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data); //changes URI to bitmap
                    pic = processImage(pic);

                    picView.setImageBitmap(pic); //shows the image uploaded
                    if (!uploadedFirstImage) {
                        uploadedFirstImage = true;
                        galleryButton.setText("Reupload Picture");
                    }
                    classify(pic); //classifies image uploaded
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                System.out.println("Something went wrong with the result codes");
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null)
            {
                Bundle data = result.getData().getExtras();
                Bitmap pic =  (Bitmap) data.get("data");
                pic = processImage(pic);
                picView.setImageBitmap(pic); //shows the image taken
                if(!takenFirstImage)
                {
                    takenFirstImage = true;
                    cameraButton.setText("Retake Picture");
                }
                classify(pic); //classifies image taken
            }
            else
            {
                System.out.println("Something went wrong with the result codes");
            }
        });

        //fixes an issue with minimum API being too low
        galleryButton.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Intent picIntent = new Intent();
                picIntent.setType("image/*");
                picIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryLauncher.launch(picIntent);
            }
            else
            {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_CODE);
            }
        });

        cameraButton.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(cameraIntent);
            }
            else
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
            }
        });

        Bundle extra = getIntent().getExtras();
        if(extra != null) {
            String launch = extra.getString("launch", "");
            switch(launch) {
                case "camera":
                    cameraButton.performClick();
                    break;
                case "gallery":
                    galleryButton.performClick();
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this.finish();
                return;
            }
        }
        if(requestCode == GALLERY_CODE) {
            galleryButton.performClick();
        } else if(requestCode == CAMERA_CODE) {
            cameraButton.performClick();
        }
    }

    public Bitmap processImage(Bitmap source) {
        int x = 0, y = 0;
        int width = source.getWidth(), height = source.getHeight();
        if(width > height) {
            x = (int) ((((float)width) / 2) - (((float)height)/2));
            width = height;
        } else {
            y = (int) ((((float)height) / 2) - (((float)width)/2));
            height = width;
        }
        Bitmap pic = Bitmap.createBitmap(source, x, y, width, height);
        pic = Bitmap.createScaledBitmap(pic, IMAGE_SIZE, IMAGE_SIZE, false);
        return pic;
    }

    public void classify(Bitmap image)
    {
        try {
            //size from model
            int size = IMAGE_SIZE;
            image = Bitmap.createScaledBitmap(image, size, size, false);
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4* size * size *3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int[] pixelVal = new int[size * size]; //pixels for RGB values; got code from link below
            image.getPixels(pixelVal, 0, image.getWidth(),0,0,image.getWidth(),image.getHeight()); //pixels for RGB values; got code from link below
            int numPixel = 0; //pixels for RGB values; got code from link below
            for(int i = 0; i < size; i++) //RGB values; got this code from https://stackoverflow.com/questions/55777086/converting-bitmap-to-bytebuffer-float-in-tensorflow-lite-android
            {
                for(int j = 0; j < size; j++)
                {
                    int value = pixelVal[numPixel++];
                    byteBuffer.putFloat(((value >> 16) & 0xFF) / 255.f); //formulas for RGB values for bytebuffer; code from link above
                    byteBuffer.putFloat(((value >> 8) & 0xFF) / 255.f);
                    byteBuffer.putFloat((value & 0xFF) / 255.f);
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            int maxBreed = 0;
            float maxConfidencePercent = 0;
            float[] confidence = outputFeature0.getFloatArray();
            for(int i = 0; i < confidence.length; i++) //gets max confidence
            {
                if(confidence[i] > maxConfidencePercent) //if confidence percent at i is greater than store percent, then it is replaced with new percent
                {
                    maxConfidencePercent = confidence[i]; //changes max breed percent
                    maxBreed = i; //index of maximum breed percent
                }
            }
            String[] animals = {"Dog", "Cat", "Parrot"};
            String[] breeds = {"Labrador", "Pitbull", "German Shepard", "Chihuahua", "Golden Retriever", "Bombay", "Sphinx", "Abyssinian", "Macaw", "Cockatoo", "Parakeet"};
            if(maxBreed < 5)
            {
                animal.setText(animals[0]);
            }
            else if(maxBreed >= 5 && maxBreed < 8)
            {
                animal.setText(animals[1]);
            }
            else
            {
                animal.setText(animals[2]);
            }

            breed.setText(breeds[maxBreed]);

            for(int i = 0; i < breeds.length; i++) //prints confidences into logcat
            {
                System.out.println(breeds[i]+"= "+confidence[i]*100);
            }

            // Releases model resources if no longer used.
            model.close();
            String breedIn = breed.getText().toString();
            String typeIn = animal.getText().toString();

            descriptionButton.setOnClickListener(view -> {
                Intent intent = new Intent(this, AnimalInfo.class);
                intent.putExtra(PACKAGE_EXTRA, breedIn);
                intent.putExtra(PACKAGE_EXTRA2, typeIn);
                startActivity(intent);
            });


        } catch (IOException e) {
            System.out.println("Something went wrong with model");
            e.printStackTrace();
        }
    }
}
