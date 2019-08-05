package com.example.travelmantics;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private static final int PICTURE_RESULT = 42;
    private EditText titleEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private TravelDeal deal;
    private Button upload_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        FirebaseUtil.openFbReference("traveldeals", this);
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        titleEditText = findViewById(R.id.text_title);
        priceEditText = findViewById(R.id.textprice);
        descriptionEditText = findViewById(R.id.text_description);
        upload_btn = findViewById(R.id.upload_image_button);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        titleEditText.setText(deal.getTitle());
        priceEditText.setText(deal.getPrice());
        descriptionEditText.setText(deal.getDescription());

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(intent.createChooser(intent, "Insert Picture"), PICTURE_RESULT);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                saveDeal();
                return true;
            case R.id.action_delete:
                deleteDeal();
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveDeal() {
        deal.setTitle(titleEditText.getText().toString());
        deal.setPrice(priceEditText.getText().toString());
        deal.setDescription(descriptionEditText.getText().toString());
        if(deal.getId() == null){
            databaseReference.push().setValue(deal);
            Toast.makeText(DealActivity.this, "Deal Saved", Toast.LENGTH_SHORT).show();
            sendToMain();
        }else {
            databaseReference.child(deal.getId()).setValue(deal);
            Toast.makeText(DealActivity.this, "Deal Updated", Toast.LENGTH_SHORT).show();
            sendToMain();
        }
    }

    private void deleteDeal(){
        if (deal == null){
            Toast.makeText(DealActivity.this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
            return;
        }
        databaseReference.child(deal.getId()).removeValue();
        Toast.makeText(DealActivity.this, "Please save the deal before deleting", Toast.LENGTH_SHORT).show();
        sendToMain();

    }

    private void sendToMain(){
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.save_menu, menu);
        if(!FirebaseUtil.isAdmin){
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(false);
            disableEditTexts();
        }
        return true;
    }

    private void disableEditTexts(){
        titleEditText.setEnabled(false);
        descriptionEditText.setEnabled(false);
        priceEditText.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICTURE_RESULT && resultCode == RESULT_OK);{
            Uri imageUri = data.getData();
            final StorageReference storageRef = FirebaseUtil.storageReference.child(imageUri.getLastPathSegment());
            UploadTask uploadTask = storageRef.putFile(imageUri);
            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        String error  = task.getException().getMessage();
                        Toast.makeText(DealActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    // Continue with the task to get the download URL
                    return storageRef.getDownloadUrl();
                }

            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        String downloadUri = task.getResult().toString();
                        deal.setImageUrl(downloadUri);
                        Toast.makeText(DealActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String error  = task.getException().getMessage();
                        Toast.makeText(DealActivity.this, error, Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
}
