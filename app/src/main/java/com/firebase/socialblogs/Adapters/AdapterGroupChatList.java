package com.firebase.socialblogs.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.socialblogs.GroupChatActivity;
import com.firebase.socialblogs.Models.ModelGroupChatList;
import com.firebase.socialblogs.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists) {
        this.context = context;
        this.groupChatLists = groupChatLists;
    }


    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_list,parent,false);

        return new HolderGroupChatList(view);
    }


    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {

        ModelGroupChatList model=groupChatLists.get(position);
        final String groupId=model.getGroupId();
        String groupIcon=model.getGroupIcon();
        String groupTitle=model.getGroupTitle();

        holder.nameTV.setText("");
        holder.timeTV.setText("");
        holder.messageTV.setText("");

        loadLastMessage(model,holder);

        holder.groupTitleTV.setText(groupTitle);
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_groupicon_primary).into(holder.groupIconIv);

        }catch (Exception e){
            holder.groupIconIv.setImageResource(R.drawable.ic_groupicon_primary);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId",groupId);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(ModelGroupChatList model, final HolderGroupChatList holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds:snapshot.getChildren()){
                            String messgae=""+ds.child("message").getValue();
                            String timestamp=""+ds.child("timestamp").getValue();
                            String sender=""+ds.child("sender").getValue();
                            String messageType=""+ds.child("type").getValue();

                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(timestamp));
                            String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

                            if (messageType.equals("image")){
                                holder.messageTV.setText("Sent Photo");

                            }else {
                                holder.messageTV.setText(messgae);
                            }
                            holder.timeTV.setText(dateTime);

                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
                            ref.orderByChild("uid").equalTo(sender)
                                    .addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot ds:snapshot.getChildren()){
                                                String name=""+ds.child("name").getValue();
                                                holder.nameTV.setText(name);
                                            }
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
    public int getItemCount() {
        return groupChatLists.size();
    }

    class HolderGroupChatList extends RecyclerView.ViewHolder{

        private ImageView groupIconIv;
        private TextView groupTitleTV,nameTV,messageTV,timeTV;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);
            groupIconIv=itemView.findViewById(R.id.groupIconIv);
            groupTitleTV=itemView.findViewById(R.id.groupTitleTV);
            nameTV=itemView.findViewById(R.id.nameTV);
            messageTV=itemView.findViewById(R.id.messageTV);
            timeTV=itemView.findViewById(R.id.timeTV);
        }
    }
}
