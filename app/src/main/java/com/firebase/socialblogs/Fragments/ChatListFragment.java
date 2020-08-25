package com.firebase.socialblogs.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import com.firebase.socialblogs.Adapters.AdapterChatlist;
import com.firebase.socialblogs.GroupCreateActivity;
import com.firebase.socialblogs.MainActivity;
import com.firebase.socialblogs.Models.ModelChat;
import com.firebase.socialblogs.Models.ModelChatlist;
import com.firebase.socialblogs.Models.ModelUsers;
import com.firebase.socialblogs.R;
import com.firebase.socialblogs.SettingActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class ChatListFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    RecyclerView recyclerView;
    List<ModelChatlist> chatlistsList;
    List<ModelUsers> usersList;
    DatabaseReference reference;
    FirebaseUser currentuser;
    AdapterChatlist adapterChatlist;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
       View view= inflater.inflate(R.layout.fragment_chat_list, container, false);

        recyclerView=view.findViewById(R.id.recyclerView);

       firebaseAuth=FirebaseAuth.getInstance();
       currentuser=firebaseAuth.getCurrentUser();

       chatlistsList=new ArrayList<>();

       reference= FirebaseDatabase.getInstance().getReference("Chatlist").child(currentuser.getUid());
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               chatlistsList.clear();
               for (DataSnapshot ds:snapshot.getChildren()){
                   ModelChatlist chatlist=ds.getValue(ModelChatlist.class);
                   chatlistsList.add(chatlist);
               }

               losdChats();

           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

       return view;
    }

    private void losdChats() {
        usersList=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelUsers users=ds.getValue(ModelUsers.class);

                    for (ModelChatlist chatlist:chatlistsList){
                        if (users.getUid()!=null && users.getUid().equals(chatlist.getId())){
                            usersList.add(users);
                            break;
                        }
                    }
                    adapterChatlist=new AdapterChatlist(getContext(),usersList);
                    recyclerView.setAdapter(adapterChatlist);

                    for (int i=0;i<usersList.size();i++){
                        lastMessage(usersList.get(i).getUid());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void lastMessage(final String userId) {
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String thelastMessage="default";
                for (DataSnapshot ds:snapshot.getChildren()){
                    ModelChat chat=ds.getValue(ModelChat.class);
                    if (chat==null){
                        continue;
                    }
                    String sender= chat.getSender();
                    String receiver= chat.getSender();

                    if (sender==null||receiver==null){
                        continue;
                    }
                    if (chat.getReceiver().equals(currentuser.getUid()) &&
                            chat.getSender().equals(userId)||
                            chat.getReceiver().equals(userId) &&
                                    chat.getSender().equals(currentuser.getUid())){

                        if (chat.getType().equals("image"))
                        {
                            thelastMessage="Sent a photo..";
                        }

                        else {
                            thelastMessage=chat.getMessage();
                        }
                    }

                }
                adapterChatlist.setLastMessageMap(userId,thelastMessage);
                adapterChatlist.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkUserStatus(){

        FirebaseUser user=firebaseAuth.getCurrentUser();

        if (user!=null)
        {

//            mProfile.setText(user.getEmail());

        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_add_post).setVisible(false);
        menu.findItem(R.id.action_add_participant).setVisible(false);
        menu.findItem(R.id.action_groupinfo).setVisible(false);

        super.onCreateOptionsMenu(menu,inflater);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if (id==R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(id==R.id.action_settings){
            startActivity(new Intent(getContext(), SettingActivity.class));
        }
        else if(id==R.id.action_create_group){
            startActivity(new Intent(getContext(), GroupCreateActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

}