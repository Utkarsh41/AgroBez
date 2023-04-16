package com.utkarsh.agrobez;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.utkarsh.agrobez.ml.DiseaseDetection;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class LeafScan extends AppCompatActivity {

    private static final int REQ_CODE = 1,pic_id=2;
    private ImageView cameraIv,prevImageIv;
    private Bitmap bitmap;
    private LottieAnimationView lottieAnimationView;
    int imageSize=224;
    TextView plantNameTv,plantConfidenceTv,DiseaseStatusTv,diseaseKey,tvplant,tvhealth,searchPlantDetails;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaf_scan);
        cameraIv = findViewById(R.id.CameraButton);
        lottieAnimationView = findViewById(R.id.uiAnimation);
        prevImageIv = findViewById(R.id.showPreviewImage);

        plantNameTv=findViewById(R.id.plantName);
        plantConfidenceTv = findViewById(R.id.plantConfidence);
        tvplant=findViewById(R.id.plantIs);
        tvhealth= findViewById(R.id.pCTv);
        diseaseKey=findViewById(R.id.plantDiseaseU);
        DiseaseStatusTv=findViewById(R.id.plantDiseaseValue);
        searchPlantDetails=findViewById(R.id.searchDetails);

        tvplant.setVisibility(View.GONE);
        tvhealth.setVisibility(View.GONE);
        plantConfidenceTv.setVisibility(View.GONE);
        plantNameTv.setVisibility(View.GONE);
        diseaseKey.setVisibility(View.GONE);
        DiseaseStatusTv.setVisibility(View.GONE);
        searchPlantDetails.setVisibility(View.GONE);

        cameraIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LeafScan.this, "Opening Camera", Toast.LENGTH_SHORT).show();
                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Start the activity with camera_intent, and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id ) {
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            int dimension = Math.min(photo.getWidth(),photo.getHeight());
            photo = ThumbnailUtils.extractThumbnail(photo,dimension,dimension);
            // Set the image in imageview for display
            prevImageIv.setImageBitmap(photo);

            photo = Bitmap.createScaledBitmap(photo,imageSize,imageSize,false);

            classifyImage(photo);
        }

    }

    private void classifyImage(Bitmap image) {

        try {
            DiseaseDetection model = DiseaseDetection.newInstance(getApplicationContext());

            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int [] intValue = new int[imageSize*imageSize];
            image.getPixels(intValue,0,image.getWidth(),0,0,image.getWidth(),image.getHeight());


//             iterate over pixels and extract R, G,B values, add to bytebuffer
            int pixel = 0;
            for (int i = 0; i< imageSize ; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValue[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }
            inputFeature0.loadBuffer (byteBuffer) ;
//          run model interface and gets result
            DiseaseDetection.Outputs outputs = model.process(inputFeature0) ;
            TensorBuffer outputFeatures0 = outputs.getOutputFeature0AsTensorBuffer();

            float [] confidence = outputFeatures0.getFloatArray();

//                    Finding index of class with the biggest confidence
            int maxPos = 0;
            float maxConfidence=0.0f;
            for(int i=0;i<confidence.length;i++){
                if(confidence[i]>maxConfidence){
                    maxConfidence=confidence[i];
                    maxPos=i;
                }
            }
            maxConfidence=Float.parseFloat(String.format(Locale.getDefault(), "%.4f", maxConfidence));
            maxConfidence*=100;
            String chkPos=Integer.toString(maxPos);
            Log.i("Max Position",chkPos);
            String convertF = Float.toString(maxConfidence);
            String chkCOnfid=convertF;
            Log.i("Max Position",chkCOnfid);

            tvplant.setVisibility(View.VISIBLE);
            tvhealth.setVisibility(View.VISIBLE);
            plantConfidenceTv.setVisibility(View.VISIBLE);
            plantNameTv.setVisibility(View.VISIBLE);
            diseaseKey.setVisibility(View.VISIBLE);
            DiseaseStatusTv.setVisibility(View.VISIBLE);
            searchPlantDetails.setVisibility(View.VISIBLE);


//            String[] classes = {"Maize Common Rust","Maize Gray Leaf Spot","Maize Healthy","Maize Northern Leaf Blight","Potato Early Blight" , "Potato Late Blight" , "Potato Healthy","Tomato Bacterial Spot","Tomato Early Blight","Tomato Healthy","Tomato Late Blight","Tomato Leaf Mold","Tomato Mosaic Virus","Tomato Septoria Leaf Spot","Tomato Spider Mites","Tomato Target Spot","Tomato Yellow Leaf Curl Virus","Wheat Stripe Rust","Wheat Healthy","Wheat Septoria"};

            String[] classes ={"Tomato Bacterial Spot","Tomato Early Blight","Tomato late blight","Tomato Healthy","Tomato Leaf Mold","Tomato Mosaic Virus","Tomato Septoria Leaf Spot"};
            String str = classes[maxPos];
            int index=0;
            while(str.charAt(index)!=' '){
                index++;
            }
            String pName = str.substring(0,index);
            String pDisease = str.substring(index+1,str.length());


            plantNameTv.setText(pName);
            String plantConfi = Float.toString(maxConfidence)+"%";
            plantConfidenceTv.setText(plantConfi);
            DiseaseStatusTv.setText(pDisease);
            index=0;

            if(pDisease.equals("Healthy")){
                searchPlantDetails.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/search?q="+DiseaseStatusTv.getText()+" tomato leaf")));
                    }
                });
            }
            else{

            searchPlantDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/search?q="+DiseaseStatusTv.getText())));
                }
            });
            }

            model.close();


//            inputFeature0.loadBuffer(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
