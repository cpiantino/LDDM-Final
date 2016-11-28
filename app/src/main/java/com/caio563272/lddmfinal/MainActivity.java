package com.caio563272.lddmfinal;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 600;

    private LocationManager senLocationManager;
    private Location location;
    private double latitude = 0.0;
    private double longitude = 0.0;
    String currentDateTimeString = "";

    Uri imageFile;
    ImageView imageView;
    Bitmap help1;
    ThumbnailUtils thumbnail;
    String mCurrentPhotoPath;

    private DatabaseHelper dbHelper;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        LocationManager senLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            Location location = senLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
        }

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Create a new DatabaseHelper
        dbHelper = new DatabaseHelper(this);
        clearAll();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    startBoardCapture();
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;
    boolean captureComplete = false;
    Uri imageUri;

    public void startBoardCapture(View view) {
        startBoardCapture();
    }

    @TargetApi(24)
    private void startBoardCapture() {

        launch_camera();

        LocationManager senLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = senLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
    }

    public void launch_camera() {
        // the intent is my camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //getting uri of the file
        imageFile = Uri.fromFile(getFile());

        //Setting the file Uri to my photo
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageFile);

        if(intent.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    //this method will create and return the path to the image file
    @TargetApi(24)
    private File getFile() {
        File folder = Environment.getExternalStoragePublicDirectory("/From_camera/imagens");// the file path

        //if it doesn't exist the folder will be created
        if(!folder.exists())
        {folder.mkdir();}

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+ timeStamp + "_";
        File image_file = null;

        try {
            image_file = File.createTempFile(imageFileName,".jpg",folder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCurrentPhotoPath = image_file.getAbsolutePath();
        return image_file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode == Activity.RESULT_OK) {
                try {
                    help1 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageFile);
                    imageView.setImageBitmap( thumbnail.extractThumbnail(help1, help1.getWidth(), help1.getHeight()));

                    captureComplete = true;
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void insertData(View view) {
        insertData("", latitude, longitude, currentDateTimeString);
    }

    private void insertData(String photo, double latitude, double longitude, String date) {

        if (captureComplete) {

            ContentValues values = new ContentValues();

            values.put(DatabaseHelper.PHOTO, photo);
            values.put(DatabaseHelper.LATITUDE, latitude);
            values.put(DatabaseHelper.LONGITUDE, longitude);
            values.put(DatabaseHelper.DATE, date);
            dbHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_NAME, null, values);
            values.clear();
            captureComplete = false;
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Não há quadro!");
            alertDialog.setMessage("Tire uma foto do quadro primeiro para poder salvar :)");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private void clearAll() {
        dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_NAME, null, null);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.caio563272.lddmfinal/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.caio563272.lddmfinal/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
