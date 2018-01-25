package com.example.yule.camera_test2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnTakePhoto;
    Button btnLoadImg;
    ImageView imgTakenPhoto;
    TextView textTargetUri;
    //ImageView targetImage;
    Uri capturedImageUri = null;
    String path = "/sdcard/DCIM/Camera/img.jpg";
    File file = new File(path);

    private Uri filePath;

    private final int PICK_IMAGE_REQUEST = 71;

    private static final int CAM_REQUEST = 1313;
    private static final int LIB_REQUEST = 1300;

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnTakePhoto = (Button) findViewById(R.id.button1);
        imgTakenPhoto = (ImageView) findViewById(R.id.imageview1);
        btnTakePhoto.setOnClickListener(new btnTakePhotoClicker());

        btnLoadImg = (Button)findViewById(R.id.loadimage);
        textTargetUri = (TextView)findViewById(R.id.targeturi);
        //targetImage = (ImageView)findViewById(R.id.targetimage);
        btnLoadImg.setOnClickListener(new btnAccessPhotoClicker());



    }



    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);



        if(resultCode == RESULT_OK && requestCode == CAM_REQUEST){      // TODO access camera
            filePath = data.getData();
            Bitmap imageBitmap;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 3;
                imageBitmap = BitmapFactory.decodeFile(path, options);
                imgTakenPhoto.setImageBitmap(imageBitmap);

                /*note: donno why cant get content uri from file path*/
                //Uri uri = Uri.parse("file://"+path);
                //textTargetUri.setText(uri.toString());
                textTargetUri.setText(capturedImageUri.toString());
                //imgTakenPhoto.setImageURI(uri);
                //imgTakenPhoto.setImageURI(capturedImageUri);

            } catch (Exception e) {
                Toast.makeText(this, "Picture Not taken",
                        Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }


        }else if(resultCode == RESULT_OK && requestCode == LIB_REQUEST){        // TODO access library
            filePath = data.getData();
            Uri targetUri = data.getData();
            textTargetUri.setText(targetUri.toString());
            Bitmap bitmap;
            try {
                uploadImage();
                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                imgTakenPhoto.setImageBitmap(bitmap);
                //imgTakenPhoto.setImageURI(targetUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    class btnTakePhotoClicker implements Button.OnClickListener
    {

        @Override
        public void onClick(View v) {
            capturedImageUri = Uri.fromFile(file);

            Intent cameraintent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            startActivityForResult(cameraintent, CAM_REQUEST);

        }

    }


    class btnAccessPhotoClicker implements Button.OnClickListener
    {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, LIB_REQUEST);
        }

    }








}
