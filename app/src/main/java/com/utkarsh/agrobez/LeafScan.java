package com.utkarsh.agrobez;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import java.io.IOException;

public class LeafScan extends AppCompatActivity {

    private static final int REQ_CODE = 1,pic_id=2;
    private ImageView cameraIv,galleryIv,prevImageIv;
    private Bitmap bitmap;
    private LottieAnimationView lottieAnimationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_leaf_scan);
        cameraIv = findViewById(R.id.CameraButton);
        galleryIv = findViewById(R.id.GalleryButton);
        lottieAnimationView = findViewById(R.id.uiAnimation);
        prevImageIv = findViewById(R.id.showPreviewImage);

        cameraIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LeafScan.this, "Opening Camera", Toast.LENGTH_SHORT).show();
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
        });
        galleryIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LeafScan.this, "Opening Gallery", Toast.LENGTH_SHORT).show();
                lottieAnimationView.setVisibility(View.GONE);
                openGallery();
            }
        });
    }
    private void openGallery() {
        Intent pickImg = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImg,REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQ_CODE && resultCode==RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            prevImageIv.setImageBitmap(bitmap);
            prevImageIv.setVisibility(View.VISIBLE);
        }
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // Set the image in imageview for display
            prevImageIv.setImageBitmap(photo);
            prevImageIv.setVisibility(View.VISIBLE);
        }
    }
}