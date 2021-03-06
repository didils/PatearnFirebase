package com.example.patearn.patearnfirebase.Tabs;

import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.patearn.patearnfirebase.AttorDTO;
import com.example.patearn.patearnfirebase.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

/**
 * Created by #1 patearn on 2017-09-04.
 */

public class Tab2Attor extends Fragment {

    private static final int GALLERY_CODE = 10;
    private Button button;
    private Button button2;
    private ImageView imageView;
    private EditText attorName;
    private EditText description;
    private EditText univMajor;
    private EditText gradUniv;
    private EditText advTags;
    private String imagePath;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private FirebaseDatabase database;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2attor, container, false);
        button = (Button) rootView.findViewById(R.id.button);
        button2 = (Button) rootView.findViewById(R.id.button2);
        imageView = (ImageView) rootView.findViewById(R.id.imageView);
        attorName = (EditText) rootView.findViewById(R.id.attorName);
        description = (EditText) rootView.findViewById(R.id.description);
        univMajor = (EditText) rootView.findViewById(R.id.univMajor);
        gradUniv = (EditText) rootView.findViewById(R.id.gradUniv);
        advTags = (EditText) rootView.findViewById(R.id.advTags);
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upload(imagePath);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
                startActivityForResult(intent, GALLERY_CODE);
            }
        });

        return rootView;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GALLERY_CODE) {

            imagePath = getPath(data.getData());
            File f = new File(imagePath);
            imageView.setImageURI(Uri.fromFile(f));

        }

    }
    public String getPath(Uri uri){
        String [] proj = { MediaStore.Images.Media.DATA };
        CursorLoader cursorLoader = new CursorLoader(getContext(), uri, proj, null, null, null);

        Cursor cursor = cursorLoader.loadInBackground ();
        int index = cursor.getColumnIndexOrThrow (MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        return cursor.getString(index);
    }

    private void upload(String uri) {
        StorageReference storageRef = storage.getReference();

        final Uri file = Uri.fromFile(new File(uri));
        StorageReference riversRef = storageRef.child("attorney/"+file.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests")
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                AttorDTO attorDTO = new AttorDTO();
                attorDTO.imageUrl = downloadUrl.toString();
                attorDTO.attorName = attorName.getText().toString();
                attorDTO.description = description.getText().toString();
                attorDTO.gradUniv = gradUniv.getText().toString();
                attorDTO.univMajor = univMajor.getText().toString();
                attorDTO.advTags = advTags.getText().toString();
                attorDTO.uid = mAuth.getCurrentUser().getUid();
                attorDTO.userId = mAuth.getCurrentUser().getEmail();
                attorDTO.imageName = file.getLastPathSegment();

                database.getReference().child("attorney").push().setValue(attorDTO)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getActivity(), "업로드 완료", Toast.LENGTH_SHORT).show();
                                imageView.setImageResource(R.color.colorAccent);
                                attorName.setText("");
                                description.setText("");
                                gradUniv.setText("");
                                univMajor.setText("");
                                advTags.setText("");
                            }
                        });
            }
        });
    }



}

