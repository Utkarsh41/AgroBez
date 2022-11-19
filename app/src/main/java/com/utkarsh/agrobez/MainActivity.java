package com.utkarsh.agrobez;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_CODE = 1,REQUEST_INTERNET_CODE=2;
    FirebaseDatabase firebaseDatabase;

    // creating a variable for our
    // Database Reference for Firebase.
    DatabaseReference databaseReference;
    private ProgressDialog progressDialog;

    // variable for Text view.
    private TextView temperatureTv,moistureTv,humidityTv,resultTv;
    private Button leafButton;
    String [] healthTrack = {"Checking Plant health","Plant is healthy","Plant is unhealthy"};
    private SwitchMaterial pumpSwitchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);


        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA
            },REQUEST_CAMERA_CODE);
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.INTERNET
            },REQUEST_INTERNET_CODE);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("AgroBez Notification","AgroBez Notification",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }



        firebaseDatabase = FirebaseDatabase.getInstance();

        // below line is used to get 
        // reference for our database.
        databaseReference = firebaseDatabase.getReference();



        // initializing our object class variable.
        temperatureTv = findViewById(R.id.temperatureValue);
        moistureTv= findViewById(R.id.moistureValue);
        humidityTv = findViewById(R.id.humidityValue);
        leafButton = findViewById(R.id.calculate_Button);
        resultTv = findViewById(R.id.show_result_tv);
        pumpSwitchButton = findViewById(R.id.pumpSwitch);
//        mTextView.setTextColor(Color.parseColor("#bdbdbd"));


        resultTv.setText(healthTrack[0]);
        resultTv.setTextColor(Color.parseColor("#63EF0A"));

        leafButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,LeafScan.class);
                startActivity(intent);
            }
        });


        // calling method 
        // for getting data.
        getdata();

    }

    public void onBackPressed() {
//        super.onBackPressed();
        new AlertDialog.Builder(this)
                .setTitle("Exit application")
                .setMessage("Do you really want to exit ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
    }

    private void getdata() {

        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        //  Initializing vibration service
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // this method is call to get the realtime
                // updates in the data.
                // this method is called when the data is
                // changed in our Firebase console.
                // below line is for getting the data from
                // snapshot of our database.
                Float TemperatureValueInit = snapshot.child("test/temperature").getValue(Float.class);
                Float MoistureValueInit = snapshot.child("test/moisture").getValue(Float.class);
                Float HumidityValueInit = snapshot.child("test/humidity").getValue(Float.class);

                String TemperatureValue = Float.toString(TemperatureValueInit)+"°C";
                String MoistureValue = Float.toString( MoistureValueInit);
                String HumidityValue = Float.toString(HumidityValueInit);

                if( (TemperatureValueInit >=17.0 && TemperatureValueInit<=28.0) && (HumidityValueInit >= 45.0 && HumidityValueInit<=65.0)){
                    resultTv.setText(healthTrack[1]);
                    resultTv.setTextColor(Color.parseColor("#63EF0A"));

                }
                else {
                    Toast.makeText(MainActivity.this, "Plant is Unhealthy", Toast.LENGTH_LONG).show();
                    Toast.makeText(MainActivity.this, "Plant is Unhealthy", Toast.LENGTH_LONG).show();

                    resultTv.setText(healthTrack[2]);
                    resultTv.setTextColor(Color.parseColor("#FF0000"));
                    final VibrationEffect vibrationEffect1;

                    NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this,"AgroBez Notification");
                    builder.setContentTitle("AgroBez");
                    builder.setContentText("Plant is Unhealthy");
                    builder.setSmallIcon(R.drawable.planting);
                    builder.setAutoCancel(true);

                    NotificationManagerCompat managerCompat = NotificationManagerCompat.from(MainActivity.this);
                    managerCompat.notify(41,builder.build());

                    // this is the only type of the vibration which requires system version Oreo (API 26)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        // this effect creates the vibration of default amplitude for 1000ms(1 sec)
                        vibrationEffect1 = VibrationEffect.createOneShot(2000, VibrationEffect.DEFAULT_AMPLITUDE);
                        // it is safe to cancel other vibrations currently taking place
                        vibrator.cancel();
                        vibrator.vibrate(vibrationEffect1);
                    }
                }

                // after getting the value we are setting
                // our value to our text view in below line.
                temperatureTv.setText(TemperatureValue);
                moistureTv.setText(MoistureValue);
                humidityTv.setText(HumidityValue);

                progressDialog.dismiss();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // calling on cancelled method when we receive
                // any error or we are not able to get the data.
                Toast.makeText(MainActivity.this, "Fail to get data.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}