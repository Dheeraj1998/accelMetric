package com.example.arcs19;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometerSensor, gyroscopeSensor, rotationSensor;
    private float df_value0 = 0, df_value1 = 0, df_value2 = 0, value0, value1, value2, diff0, diff1, diff2;
    private boolean calibrate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        sensorManager.registerListener(MainActivity.this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
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
//            Log.i("custom", "accel " + event.values[0] + " " + event.values[1] + " " + event.values[2]);
            }

            if (sensor_type.getType() == Sensor.TYPE_GYROSCOPE) {
                value0 = event.values[0];
                value1 = event.values[1];
                value2 = event.values[2];

                diff0 = value0 - df_value0;
                diff1 = value1 - df_value1;
                diff2 = value2 - df_value2;

                if (diff2 < -0.3) {
                    Log.i("custom", "right side");
                    myRef.child(timestamp).child("steering").setValue("right_side");

                } else if (diff2 > 0.3) {
                    Log.i("custom", "left side");
                    myRef.child(timestamp).child("steering").setValue("left_side");
                } else {
                    Log.i("custom", "neutral side");
                    myRef.child(timestamp).child("steering").setValue("neutral_side");
                }

            }

            if (sensor_type.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                myRef.child(timestamp).child("gyro_x").setValue(event.values[0]);
                myRef.child(timestamp).child("gyro_y").setValue(event.values[1]);
                myRef.child(timestamp).child("gyro_z").setValue(event.values[2]);
//            Log.i("custom", "rotation " + event.values[0] + " " + event.values[1] + " " + event.values[2] + " " + event.values[3]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void calibrateSensor(View view) {
        df_value0 = value0;
        df_value1 = value1;
        df_value2 = value2;

        calibrate = true;
    }

    public void stopSensor(View view) {
        calibrate = false;
    }
}
