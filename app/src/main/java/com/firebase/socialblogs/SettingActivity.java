 package com.firebase.socialblogs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

 public class SettingActivity extends AppCompatActivity {

    SwitchCompat postSwitch;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    private static final String  TOPIC_POST_NOTIFICATION="POST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Settings");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);


        postSwitch=findViewById(R.id.postSwitch);

        sp=getSharedPreferences("Notification_SP",MODE_PRIVATE);
        final boolean isPostEnabled=sp.getBoolean(""+TOPIC_POST_NOTIFICATION,false);
        if (isPostEnabled){
            postSwitch.setChecked(true);
        }else {
            postSwitch.setChecked(false);
        }

        postSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                editor=sp.edit();
                editor.putBoolean(""+TOPIC_POST_NOTIFICATION,b);
                editor.apply();

                if (b){
                    subscribePostNotification();
                }else {
                    unSubscribePostNotification();

                }
            }
        });


    }

     @Override
     public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
     }

     private void unSubscribePostNotification() {
         FirebaseMessaging.getInstance().unsubscribeFromTopic( ""+TOPIC_POST_NOTIFICATION)
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         String msg="You will not receive post Notification";
                         if (!task.isSuccessful()){
                             msg="UnSubscription Failed";
                         }
                         Toast.makeText(SettingActivity.this, msg, Toast.LENGTH_SHORT).show();

                     }
                 });

     }

     private void subscribePostNotification() {
         FirebaseMessaging.getInstance().subscribeToTopic(""+TOPIC_POST_NOTIFICATION)
                 .addOnCompleteListener(new OnCompleteListener<Void>() {
                     @Override
                     public void onComplete(@NonNull Task<Void> task) {
                         String msg="You will receive post Notification";
                         if (!task.isSuccessful()){
                             msg="Subscription Failed";
                         }
                         Toast.makeText(SettingActivity.this, msg, Toast.LENGTH_SHORT).show();

                     }
                 });
     }
 }