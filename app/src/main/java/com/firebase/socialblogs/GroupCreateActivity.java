package com.firebase.socialblogs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class GroupCreateActivity extends AppCompatActivity {

    private ActionBar actionBar;
    private FirebaseAuth firebaseAuth;
    ImageView groupIconIv;
    EditText groupTitleET,groupDescriptionEt;
    FloatingActionButton createGroupBtn;

    private static final int CAMERA_REQUEST_CODE=100;
    private static final int STORAGE_REQUEST_CODE=200;

    private static final int IMAGE_PICK_CAMERA_CODE=300;
    private static final int IMAGE_PICK_GALLERY_CODE=400;

    String[] cameraPermission;
    String[] storagePermission;

    private Uri image_uri=null;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_create);

        actionBar=getSupportActionBar();
        actionBar.setTitle("Create Group");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        groupIconIv=findViewById(R.id.groupIconIv);
        groupTitleET=findViewById(R.id.groupTitleET);
        groupDescriptionEt=findViewById(R.id.groupDescriptionEt);
        createGroupBtn=findViewById(R.id.createGroupBtn);

        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        firebaseAuth=FirebaseAuth.getInstance();
        checkUser();

        groupIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();

            }
        });

        createGroupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCreatingGroup();
            }
        });
    }

    private void startCreatingGroup() {
         progressDialog=new ProgressDialog(this);
         progressDialog.setMessage("Creating Group");

         final String groupTitle=groupTitleET.getText().toString().trim();
         final String groupDescription=groupDescriptionEt.getText().toString().trim();
         
         if (TextUtils.isEmpty(groupTitle)){
             Toast.makeText(this, "Please enter group title.....", Toast.LENGTH_SHORT).show();
             return;
         }
//         else if (TextUtils.isEmpty(groupDescription)){
//             Toast.makeText(this, "Please enter group description.....", Toast.LENGTH_SHORT).show();
//             return;
//         }
        final String timestamp=""+System.currentTimeMillis();

        progressDialog.show();

         if (image_uri==null){
             createGroup(
                     ""+timestamp,
                     ""+groupTitle,
                     ""+groupDescription,
                     ""
             );
         }else {

             String fileNameAndPath="Group_Imgs"+timestamp;
             StorageReference storageReference= FirebaseStorage.getInstance().getReference(fileNameAndPath);
             storageReference.putFile(image_uri)
                     .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                         @Override
                         public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                             Task<Uri> p_UriTask=taskSnapshot.getStorage().getDownloadUrl();
                             while (!p_UriTask.isSuccessful());

                             Uri p_downloadUri=p_UriTask.getResult();
                             if (p_UriTask.isSuccessful()){
                                 createGroup(
                                         ""+timestamp,
                                         ""+groupTitle,
                                         ""+groupDescription,
                                         ""+p_downloadUri
                                 );
                             }

                         }
                     })
                     .addOnFailureListener(new OnFailureListener() {
                         @Override
                         public void onFailure(@NonNull Exception e) {
                             progressDialog.dismiss();
                             Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                         }
                     });

         }
    }
    private void createGroup(final String timestamp, String groupTitle, String groupDescription, String groupIcon) {
        final HashMap<String,String> hashMap=new HashMap<>();
        hashMap.put("groupId",""+timestamp);
        hashMap.put("groupTitle",""+groupTitle);
        hashMap.put("groupDescription",""+groupDescription);
        hashMap.put("groupIcon",""+groupIcon);
        hashMap.put("timestamp",""+timestamp);
        hashMap.put("createdBy",""+firebaseAuth.getUid());

        final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(timestamp).setValue(hashMap)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                   HashMap<String,String> hashMap1=new HashMap<>();
                   hashMap1.put("uid",firebaseAuth.getUid());
                   hashMap1.put("role","creator");
                   hashMap1.put("timestamp",timestamp);

                   DatabaseReference ref1=FirebaseDatabase.getInstance().getReference("Groups");
                   ref1.child(timestamp).child("Participants").child(firebaseAuth.getUid())
                           .setValue(hashMap1)
                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   progressDialog.dismiss();
                                   Toast.makeText(GroupCreateActivity.this, "Group Created.....", Toast.LENGTH_SHORT).show();

                               }
                           })
                           .addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   progressDialog.dismiss();
                                   Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                               }
                           });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(GroupCreateActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
    }

    private void showImagePicDialog() {
        String option[]={"Camera","Gallery"};

        AlertDialog.Builder  builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image From");
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (i==0){
                    if (!checkCameraPermission()){
                        requestCameraPermission();
                    }else {
                        pickFromCamera();
                    }

                }
                if (i==1){
                    if (!checkStoragePermission()){
                        requestStoragePermission();
                    }else {
                        pickFromGallery();
                    }

                }

            }
        });
        builder.create().show();
    }

    private boolean checkCameraPermission(){

        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);

        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }
    private boolean checkStoragePermission() {
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickFromCamera() {
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description");

        image_uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }
    private void pickFromGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private void checkUser() {
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if (user!=null){
            actionBar.setSubtitle(user.getEmail());
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
            {
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;

                    if (cameraAccepted&&writeStorageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Please Enable Camera & Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }else {

                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean writeStorageAccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;

                    if (writeStorageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, "Please Enable  Storage Permission", Toast.LENGTH_SHORT).show();
                    }
                }else {

                }

            }
            break;
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE){

                image_uri=data.getData();
                groupIconIv.setImageURI(image_uri);
            }
            else if (requestCode==IMAGE_PICK_CAMERA_CODE){

                groupIconIv.setImageURI(image_uri);


            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}