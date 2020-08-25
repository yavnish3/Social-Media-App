package com.firebase.socialblogs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.socialblogs.Adapters.AdapterParticipantAdd;
import com.firebase.socialblogs.Models.ModelUsers;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class GroupInfoActivity extends AppCompatActivity {

    private String groupId;
    private String mygroupRole="";
    private ActionBar actionBar;

    private FirebaseAuth firebaseAuth;

    private ImageView groupIconIv;
    private TextView descriptionTV,createdByTv,editGroupTV,addParticipantTV,leaveGroupTV,participantsTV;
    private RecyclerView participantsRV;

    private ArrayList<ModelUsers> userList;
    private AdapterParticipantAdd adapterParticipantAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        groupId=getIntent().getStringExtra("groupId");

        groupIconIv=findViewById(R.id.groupIconIv);
        descriptionTV=findViewById(R.id.descriptionTV);
        createdByTv=findViewById(R.id.createdByTv);
        editGroupTV=findViewById(R.id.editGroupTV);
        addParticipantTV=findViewById(R.id.addParticipantTV);
        leaveGroupTV=findViewById(R.id.leaveGroupTV);
        participantsTV=findViewById(R.id.participantsTV);
        participantsRV=findViewById(R.id.participantsRV);

        actionBar=getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        firebaseAuth=FirebaseAuth.getInstance();

        loadGroupInfo();
        loadGroupRole();

        addParticipantTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupParticipantAddActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        editGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GroupInfoActivity.this,GroupEditActivity.class);
                intent.putExtra("groupId",groupId);
                startActivity(intent);
            }
        });

        leaveGroupTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dialogTitle="";
                String dialogDescription="";
                String dialogPostiveButton="";
                if (mygroupRole.equals("creator")){
                    dialogTitle="Delete Group";
                    dialogDescription="Are you sure want to delete group permanently?";
                    dialogPostiveButton="DELETE";
                }
                else {
                    dialogTitle="Leave Group";
                    dialogDescription="Are you sure want to delete leave permanently?";
                    dialogPostiveButton="LEAVE";
                }

                AlertDialog.Builder builder=new AlertDialog.Builder(GroupInfoActivity.this);
                builder.setTitle(dialogTitle)
                .setMessage(dialogDescription)
                        .setPositiveButton(dialogPostiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (mygroupRole.equals("creator")){
                                    deleteGroup();
                                }else {
                                    leaveGroup();
                                }

                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

    }

    private void leaveGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").child(firebaseAuth.getUid())
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Group Left Successfully......", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteGroup() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(GroupInfoActivity.this, "Group Successfully Deleted....", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(GroupInfoActivity.this,DashboardActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(GroupInfoActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadGroupInfo() {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.orderByChild("groupId").equalTo(groupId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String groupId=""+ds.child("groupId").getValue();
                            String groupTitle=""+ds.child("groupTitle").getValue();
                            String groupDescription=""+ds.child("groupDescription").getValue();
                            String groupIcon=""+ds.child("groupIcon").getValue();
                            String createdBy=""+ds.child("createdBy").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();

                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            actionBar.setTitle(groupTitle);
                            descriptionTV.setText(groupDescription);

                            try {
                                Picasso.get().load(groupIcon).placeholder(R.drawable.ic_groupicon_primary).into(groupIconIv);
                            }catch (Exception e){
                                groupIconIv.setImageResource(R.drawable.ic_groupicon_primary);
                            }

                            loadCreatorInfo(dateTime,createdBy);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadCreatorInfo(final String dateTime, final String createdBy) {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(createdBy).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds:snapshot.getChildren()){
                    String name=""+ds.child("name").getValue();
                    createdByTv.setText("Created By "+name+" on "+dateTime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadGroupRole() {
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants").orderByChild("uid")
                .equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            mygroupRole=""+ds.child("role").getValue();
                            actionBar.setSubtitle(firebaseAuth.getCurrentUser().getEmail()+"("+mygroupRole+")");

                            if (mygroupRole.equals("participant")){
                                editGroupTV.setVisibility(View.GONE);
                                addParticipantTV.setVisibility(View.GONE);
                                leaveGroupTV.setText("Leave Group");
                            }
                            else  if (mygroupRole.equals("admin")){
                                editGroupTV.setVisibility(View.GONE);
                                addParticipantTV.setVisibility(View.VISIBLE);
                                leaveGroupTV.setText("Leave Group");
                            }
                            else if (mygroupRole.equals("creator")){
                                editGroupTV.setVisibility(View.VISIBLE);
                                addParticipantTV.setVisibility(View.VISIBLE);
                                leaveGroupTV.setText("Delete Group");
                            }

                        }

                        loadParticipants();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadParticipants() {
        userList=new ArrayList<>();
        DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(groupId).child("Participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userList.clear();
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String uid=""+ds.child("uid").getValue();

                            DatabaseReference ref=FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(uid)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds:snapshot.getChildren()){
                                                ModelUsers model=ds.getValue(ModelUsers.class);

                                                userList.add(model);
                                            }
                                            adapterParticipantAdd=new AdapterParticipantAdd(GroupInfoActivity.this,userList,groupId,mygroupRole);
                                            participantsTV.setText("Participants ("+userList.size()+")");
                                            participantsRV.setAdapter(adapterParticipantAdd);
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public boolean onNavigateUp() {
        onBackPressed();
        return super.onNavigateUp();
    }
}