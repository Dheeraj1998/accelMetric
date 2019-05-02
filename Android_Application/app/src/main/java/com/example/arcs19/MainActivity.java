package com.example.arcs19;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyroscopeSensor, rotationSensor;
    private float value0, value1, value2;
    private boolean calibrate = false;
    private RelativeLayout currentLayout;

    // Setting up a sample location value
    private double latitude = 12.970151, longitude = 79.155673;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLayout = findViewById(R.id.main_layout);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("rash_drive").getRef();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Double value = Double.parseDouble(dataSnapshot.child("percent").getValue().toString());

                if (value < 0.3) {
                    currentLayout.setBackgroundResource(R.drawable.gradient_red);
                } else {
                    currentLayout.setBackgroundResource(R.drawable.gradient_green);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor_type = event.sensor;

        if (calibrate) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("car_data");

            String timestamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
            if (sensor_type.getType() == Sensor.TYPE_ACCELEROMETER) {
                myRef.child(timestamp).child("accel_x").setValue(event.values[0]);
                myRef.child(timestamp).child("accel_y").setValue(event.values[1]);
                myRef.child(timestamp).child("accel_z").setValue(event.values[2]);
                myRef.child(timestamp).child("latitude").setValue(latitude);
                myRef.child(timestamp).child("longitude").setValue(longitude);
            }

            if (sensor_type.getType() == Sensor.TYPE_GYROSCOPE) {
                value0 = event.values[0];
                value1 = event.values[1];
                value2 = event.values[2];

                myRef.child(timestamp).child("gyro_x").setValue(event.values[0]);
                myRef.child(timestamp).child("gyro_y").setValue(event.values[1]);
                myRef.child(timestamp).child("gyro_z").setValue(event.values[2]);
            }

            if (sensor_type.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                myRef.child(timestamp).child("rot_x").setValue(event.values[0]);
                myRef.child(timestamp).child("rot_y").setValue(event.values[1]);
                myRef.child(timestamp).child("rot_z").setValue(event.values[2]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void calibrateSensor(View view) {
        calibrate = !calibrate;

        Button sensorControl = findViewById(R.id.sensorBtn);

        if (sensorControl.getText().equals("CALIBRATE")) {
            sensorControl.setText("STOP");
        } else {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("rash_drive");
            myRef.child("percent").setValue("1");

            sensorControl.setText("CALIBRATE");
        }
    }
}
