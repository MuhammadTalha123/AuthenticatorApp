package com.example.authenticatorapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    TextView fullName,email,phone,verifyMsg,balance;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    Button resendCode, buttonDeposit,buttonCredit;
    EditText editTextAmount;
    String userId;
    DocumentReference documentReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phone = findViewById(R.id.profilePhone);
        fullName = findViewById(R.id.profileName);
        email = findViewById(R.id.profileEmail);
        verifyMsg = findViewById(R.id.verifyMsg);
        editTextAmount = findViewById(R.id.editTextAmount);
        balance = findViewById(R.id.balance);
        resendCode = findViewById(R.id.resendCode);
        buttonDeposit = findViewById(R.id.buttonDeposit);
        buttonCredit = findViewById(R.id.buttonCredit);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        userId = fAuth.getCurrentUser().getUid();
        final FirebaseUser user = fAuth.getCurrentUser();


        if (!user.isEmailVerified()) {
            resendCode.setVisibility(View.VISIBLE);
            resendCode.setVisibility(View.VISIBLE);

            resendCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Verification Email Has Been Sent.", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("tag", "onFailure: Email not sent " + e.getMessage());
                        }
                    });
                }
            });

        }


        documentReference = fStore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                phone.setText(documentSnapshot.getString("phone"));
                fullName.setText(documentSnapshot.getString("fName"));
                email.setText(documentSnapshot.getString("email"));
                balance.setText(documentSnapshot.getString("balance"));
            }
        });

    }

    private void getOldBalance(final String type) {
        final String enterAmount = editTextAmount.getText().toString();
        if (enterAmount.isEmpty()){
            Toast.makeText(this, "Please Enter Your Amount", Toast.LENGTH_SHORT).show();
        } else {
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Log.i("TAG", "DocumentSnapshot data: " + document.getData());
                            int myOldBalance = Integer.parseInt(document.getString("balance"));
                            if (type == "+") {
                                updateBalanceFirebase(myOldBalance);
                            } else {
                                updateMinusBalance(myOldBalance);
                            }
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }
    }


    private void updateBalanceFirebase(int oldBalance) {
        final String enterAmount = editTextAmount.getText().toString();
        int newAmount = Integer.parseInt(enterAmount);
        int result = oldBalance + newAmount;
        String resultString = Integer.toString(result);
        documentReference.update("balance", resultString);
        balance.setText(resultString);
    }

    private void updateMinusBalance(int oldBalance) {
        final String enterAmount = editTextAmount.getText().toString();
        int newAmount = Integer.parseInt(enterAmount);
        int result = oldBalance - newAmount;
        String resultString = Integer.toString(result);
        documentReference.update("balance", resultString);
        balance.setText(resultString);
    }



        public void depositMoney (View view) {
            getOldBalance("+");
        }



    public void yourValue (View view) {
       getOldBalance("-");
    }




    public void logout (View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(),Login.class));
        finish();
    }
}
