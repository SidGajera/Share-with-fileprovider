package com.example.shareimagedemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView imageView, pickImageBtn;
    FloatingActionButton clear, share;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1001;

    int CAMERA_PIC_REQUEST = 100;
    int GALLERY_PIC_REQUEST = 101;
    Uri uri;
    Context context;
    Bitmap photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        imageView = findViewById(R.id.imageView);
        pickImageBtn = findViewById(R.id.pickImageBtn);
        clear = findViewById(R.id.clear);
        share = findViewById(R.id.share);

        imageView.setVisibility(View.GONE);
        clear.setVisibility(View.GONE);
        share.setVisibility(View.GONE);

        clear.setOnClickListener(v -> {
            imageView.setImageResource(0);
            pickImageBtn.setVisibility(View.VISIBLE);
            share.setVisibility(View.GONE);
            clear.setVisibility(View.GONE);
        });

        pickImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermissions()) {
                    selectImage();
                } else {
                }
            }
        });

//        checkPermissions();
    }

    private boolean checkPermissions() {

        int READ_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int WRITE_EXTERNAL_STORAGE = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int CAMERA = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int CALL_PHONE = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        int ACCESS_FINE_LOCATION = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int CONTACTS = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (READ_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (WRITE_EXTERNAL_STORAGE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (CAMERA != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (CALL_PHONE != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (ACCESS_FINE_LOCATION != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (CONTACTS != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void displayNeverAskAgainDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("If you reject permission,you can not use this service\n" +
                "\nPlease turn on permissions at [Setting] > [Permission]");
        builder.setCancelable(false);
        builder.setPositiveButton("Permit Manually", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Permission granted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    displayNeverAskAgainDialog();
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void selectImage() {

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.sample_dialog, null);

        LinearLayout cameraBtn = view.findViewById(R.id.camera);
        LinearLayout galleryBtn = view.findViewById(R.id.gallery);
        TextView cancle = view.findViewById(R.id.cancle);

        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setCancelable(false)
                .setView(view).create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        cameraBtn.setOnClickListener(v -> {
            Random r = new Random();
            int i1 = r.nextInt(80 - 65) + 65;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), i1 + "_temp.jpg");
            Uri uri = FileProvider.getUriForFile(context, getPackageName() + ".provider", file);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
            Log.e("TAG", "Clickimage: " + intent);
            dialog.dismiss();
        });

        galleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, GALLERY_PIC_REQUEST);
            dialog.dismiss();
        });

        cancle.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_PIC_REQUEST && resultCode == Activity.RESULT_OK) {

            uri = data.getData();
            Log.e("TAG ", "onActivityResult CAMERA_PIC_REQUEST " + uri);

            photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);

            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), photo);

            // CALL THIS METHOD TO GET THE ACTUAL PATH
            File finalFile = new File(getRealPathFromURI(tempUri));

            uri = FileProvider.getUriForFile(context, getPackageName() + ".provider", finalFile);
            Log.e("TAG ", "onActivityResult CAMERA_PIC_REQUEST finalFile" + finalFile);

            processAndSetImage();
        } else if (requestCode == GALLERY_PIC_REQUEST && resultCode == Activity.RESULT_OK) {
            uri = data.getData();
            Log.e("TAG ", "onActivityResult GALLERY_PIC_REQUEST " + uri);
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(uri, filePath, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePath[0]);
            String picturePath = c.getString(columnIndex);
            c.close();
            photo = (BitmapFactory.decodeFile(picturePath));
            imageView.setImageBitmap(photo);
            processAndSetImage();
        }

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(intent, "Send image"));
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContentResolver() != null) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    private void processAndSetImage() {
        pickImageBtn.setVisibility(View.GONE);
        share.setVisibility(View.VISIBLE);
        clear.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
    }
}