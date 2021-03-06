package com.example.patearn.patearnfirebase;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

public class BoardActivity2 extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<AttorDTO> attorDTOs = new ArrayList<>();
    private List<String> uidLists = new ArrayList<>();
    private FirebaseDatabase database;
    private BoardRecyclerViewAdapter boardRecyclerViewAdapter;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board2);
        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        boardRecyclerViewAdapter = new BoardRecyclerViewAdapter();
        recyclerView.setAdapter(boardRecyclerViewAdapter);

        database.getReference().child("attorney").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                attorDTOs.clear();
                uidLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    AttorDTO attorDTO = snapshot.getValue(AttorDTO.class);
                    String uidkey = snapshot.getKey();                          //Firebase Database의 키 값
                    attorDTOs.add(attorDTO);
                    uidLists.add(uidkey);
                }
                boardRecyclerViewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    class BoardRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_board, parent, false);

            return new CustomViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((CustomViewHolder)holder).textView.setText(attorDTOs.get(position).attorName);
            ((CustomViewHolder)holder).textView2.setText(attorDTOs.get(position).description);
            Glide.with(holder.itemView.getContext()).load(attorDTOs.get(position).imageUrl).into(((CustomViewHolder)holder).imageView);
            ((CustomViewHolder)holder).starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onStarClicked(database.getReference().child("attorney").child(uidLists.get(position)));
                }
            });
            if (attorDTOs.get(position).stars.containsKey(mAuth.getCurrentUser().getUid())) {
                ((CustomViewHolder)holder).starButton.setImageResource(R.drawable.ic_favorite_black_48dp);
            } else {
                ((CustomViewHolder)holder).starButton.setImageResource(R.drawable.ic_favorite_border_black_48dp);
            }

            ((CustomViewHolder)holder).deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete_content(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return attorDTOs.size();
        }

        private void onStarClicked(DatabaseReference postRef) {
            postRef.runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    AttorDTO attorDTO = mutableData.getValue(AttorDTO.class);
                    if (attorDTO == null) {
                        return Transaction.success(mutableData);
                    }

                    if (attorDTO.stars.containsKey(mAuth.getCurrentUser().getUid())) {
                        // Unstar the post and remove self from stars
                        attorDTO.starCount = attorDTO.starCount - 1;
                        attorDTO.stars.remove(mAuth.getCurrentUser().getUid());
                    } else {
                        // Star the post and add self to stars
                        attorDTO.starCount = attorDTO.starCount + 1;
                        attorDTO.stars.put(mAuth.getCurrentUser().getUid(), true);
                    }

                    // Set value and report transaction success
                    mutableData.setValue(attorDTO);
                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                    // Transaction completed
                    //Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                }
            });

        }

        private void delete_content(final int position) {

            storage.getReference().child("attorney").child(attorDTOs.get(position).imageName).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    //storage의 이미지 파일을 먼저 삭제 성공한 경우, database의 관련 텍스트 데이터를 함께 삭제
                    database.getReference().child("attorney").child(uidLists.get(position)).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(BoardActivity2.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(BoardActivity2.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                }
            });




        }

        private class CustomViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView textView;
            TextView textView2;
            ImageView starButton;
            ImageView deleteButton;

            public CustomViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.item_imageView);
                textView = view.findViewById(R.id.item_textView);
                textView2 = view.findViewById(R.id.item_textView2);
                starButton = view.findViewById(R.id.item_starButton_imageView);
                deleteButton = view.findViewById(R.id.item_deleteButton);
            }
        }
    }
}
