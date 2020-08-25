package com.firebase.socialblogs.Adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.socialblogs.Models.ModelGroupChat;
import com.firebase.socialblogs.Notification.Data;
import com.firebase.socialblogs.R;
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

public class AdapterGroupChat extends RecyclerView.Adapter<AdapterGroupChat.HolderGroupChat> {
    private static final int MSG_TYPE_left=0;
    private static final int MSG_TYPE_RIGHT=1;

    private Context context;
    private ArrayList<ModelGroupChat> modelGroupChatslist;
    private FirebaseAuth firebaseAuth;

    public AdapterGroupChat(Context context, ArrayList<ModelGroupChat> modelGroupChatslist) {
        this.context = context;
        this.modelGroupChatslist = modelGroupChatslist;
        firebaseAuth=FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public HolderGroupChat onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType==MSG_TYPE_left){
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_left,parent,false);
            return new HolderGroupChat(view);
        }
        else {
            View view= LayoutInflater.from(context).inflate(R.layout.row_groupchat_right,parent,false);
            return new HolderGroupChat(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChat holder, int position) {
        ModelGroupChat model=modelGroupChatslist.get(position);
        String message=model.getMessage();
        String senderUid=model.getSender();
        String timestamp=model.getTimestamp();
        String messageType=model.getType();


        Calendar cal=Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(Long.parseLong(timestamp));
        String dateTime= DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();

        if (messageType.equals("text")){
            holder.messageIV.setVisibility(View.GONE);
            holder.messageTV.setVisibility(View.VISIBLE);
            holder.messageTV.setText(message);


        }
        else {
            holder.messageIV.setVisibility(View.VISIBLE);
            holder.messageTV.setVisibility(View.GONE);

            try {
                Picasso.get().load(message).placeholder(R.drawable.ic_image_black).into(holder.messageIV);
            }catch (Exception e){
                holder.messageIV.setImageResource(R.drawable.ic_image_black);
            }
        }
        holder.timeTV.setText(dateTime);
        holder.messageTV.setText(message);

        setUserName(model,holder);

    }

    private void setUserName(ModelGroupChat model, final HolderGroupChat holder) {
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(model.getSender())
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

    @Override
    public int getItemCount() {
        return modelGroupChatslist.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (modelGroupChatslist.get(position).getSender().equals(firebaseAuth.getUid())){
            return MSG_TYPE_RIGHT;
        }else {
            return MSG_TYPE_left;
        }
    }

    class HolderGroupChat extends RecyclerView.ViewHolder {

        private TextView nameTV,messageTV,timeTV;
        private ImageView messageIV;

        public HolderGroupChat(@NonNull View itemView) {
            super(itemView);

            nameTV=itemView.findViewById(R.id.nameTV);
            messageTV=itemView.findViewById(R.id.messageTV);
            timeTV=itemView.findViewById(R.id.timeTV);
            messageIV=itemView.findViewById(R.id.messageIV);
        }
    }
}
