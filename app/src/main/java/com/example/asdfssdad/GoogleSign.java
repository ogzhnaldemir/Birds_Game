package com.example.asdfssdad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.asdfssdad.databinding.ActivityGoogleSignBinding;
import com.example.asdfssdad.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class GoogleSign extends AppCompatActivity {

    private ActivityGoogleSignBinding activityMainBinding;

    private static final int RC_SIGN_IN = 100;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "GOOGLE_SIGN_IN_TAG";
    private DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_google_sign);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        activityMainBinding = ActivityGoogleSignBinding.inflate(getLayoutInflater());

        setContentView(activityMainBinding.getRoot());

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions);

        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference= FirebaseDatabase.getInstance().getReferenceFromUrl("https://mobil-proje-91d39-default-rtdb.firebaseio.com/");

        activityMainBinding.googleSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"onClick: begin Google SignIn");
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent,RC_SIGN_IN);
            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Log.d(TAG,"onActivyt : Google result");
            Task<GoogleSignInAccount> accountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = accountTask.getResult(ApiException.class);
                firebaseAuthWithGoogleAccount(account);
            }catch (Exception e){
                Log.d(TAG,"onActivtyresult: "+e.getMessage());
            }
        }
    }
    private void firebaseAuthWithGoogleAccount(GoogleSignInAccount account) {
        Log.d(TAG,"firebaseauthwithgoogle :begin firebase");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"onsucces");
                        HashMap <String, Object> mData = new HashMap<String, Object>();
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        String uId = firebaseUser.getUid();
                        String email = firebaseUser.getEmail();

                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            mData=new HashMap<>();
                            mData.put("Score",0);
                            mData.put("Id",uId);
                            mData.put("email",email);

                            databaseReference.child("kullanıcılar").child(firebaseUser.getUid())
                                    .setValue(mData)
                                    .addOnCompleteListener(GoogleSign.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(GoogleSign.this,"başarılı",Toast.LENGTH_SHORT).show();
                                            }else {
                                                Toast.makeText(GoogleSign.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                            Toast.makeText(GoogleSign.this,"accautn created",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(GoogleSign.this,"accautn exeute",Toast.LENGTH_SHORT).show();
                        }

                        Intent intent = new Intent(GoogleSign.this,MainActivity2.class);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"onFailure: Login failed"+e.getMessage());
            }
        });
    }
    public void signOut() {
        googleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }
}