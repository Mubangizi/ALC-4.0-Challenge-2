package com.example.travelmantics;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DealActivity extends AppCompatActivity {
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private EditText titleEditText;
    private EditText priceEditText;
    private EditText descriptionEditText;
    private TravelDeal deal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert);
        FirebaseUtil.openFbReference("traveldeals", this);
        firebaseDatabase = FirebaseUtil.firebaseDatabase;
        databaseReference = FirebaseUtil.databaseReference;
        titleEditText = findViewById(R.id.text_title);
        priceEditText = findViewById(R.id.textprice);
        descriptionEditText = findViewById(R.id.text_description);

        Intent intent = getIntent();
        TravelDeal deal = (TravelDeal) intent.getSerializableExtra("Deal");
        if (deal == null){
            deal = new TravelDeal();
        }
        this.deal = deal;
        titleEditText.setText(deal.getTitle());
        priceEditText.setText(deal.getPrice());
        descriptionEditText.setText(deal.getDescription());
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
}
