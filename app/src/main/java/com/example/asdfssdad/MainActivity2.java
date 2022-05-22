package com.example.asdfssdad;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.asdfssdad.databinding.ActivityMain2Binding;
import com.example.asdfssdad.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity2 extends AppCompatActivity {
    private boolean isMute;
    private ImageView imageView;
    public GameView gw;

    private ActivityMain2Binding activityMain2Binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private TextView play;
    private ToggleButton toggleButton;
    private Button btnout,btnabout,btnlogout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main2);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        //imageView=findViewById(R.id.menu);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        checkUser();
        toggleButton=findViewById(R.id.toggleButton);
        btnout=findViewById(R.id.btnexit);
        btnabout=findViewById(R.id.aboutus);
        btnlogout=findViewById(R.id.btnlogout);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    btnabout.setVisibility(View.VISIBLE);
                    btnabout.setAlpha(0f);
                    btnabout.setTranslationY(-20);
                    btnabout.animate().alpha(1f).translationYBy(20).setDuration(750);

                    btnout.setVisibility(View.VISIBLE);
                    btnout.setAlpha(0f);
                    btnout.setTranslationY(-20);
                    btnout.animate().alpha(1f).translationYBy(20).setDuration(750);

                    btnlogout.setVisibility(View.VISIBLE);
                    btnlogout.setAlpha(0f);
                    btnlogout.setTranslationY(-20);
                    btnlogout.animate().alpha(1f).translationYBy(20).setDuration(750);

                }
                else {
                    btnabout.setVisibility(View.INVISIBLE);
                    btnout.setVisibility(View.INVISIBLE);
                    btnlogout.setVisibility(View.INVISIBLE);
                }
            }
        });
        btnabout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity2.this,About_us.class));
            }
        });
        btnout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
            }
        });


        //play = findViewById(R.id.play);
        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity2.this,GameActivity.class));

            }
        });

        //TextView highScoreTxt = findViewById(R.id.highscoretxt);

        final SharedPreferences prefs = getSharedPreferences("game", MODE_PRIVATE);

       // highScoreTxt.setText("HighScore: " + prefs.getInt("highscore", 0));

        isMute = prefs.getBoolean("isMute", false);

        final ImageView volumeCtrl = findViewById(R.id.volumectrl);

        if (isMute)
            volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_off_24);
        else
            volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_up_24);

        volumeCtrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                isMute = !isMute;
                if (isMute)
                    volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_off_24);
                else
                    volumeCtrl.setImageResource(R.drawable.ic_baseline_volume_up_24);

                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isMute", isMute);
                editor.apply();

            }
        });

    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null){
            startActivity(new Intent(MainActivity2.this,GoogleSign.class));
            finish();
        }else {
            String email = firebaseUser.getEmail();
            Toast.makeText(this,"hesap girişi başarılı: "+email,Toast.LENGTH_SHORT).show();
        }
    }
    public void openWinDialog(int score) {
        Dialog dialog = new Dialog(MainActivity2.this);
        dialog.setContentView(R.layout.highscore);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView txt =findViewById(R.id.txtadyas);
        txt.setText("High Score: "+score);
        ImageView imgclose = dialog.findViewById(R.id.imgclose);
        imgclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        Button btnClose = dialog.findViewById(R.id.btnclose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }



}