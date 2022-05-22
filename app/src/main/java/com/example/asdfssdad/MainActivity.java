package com.example.asdfssdad;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Handler handler;
    private Animation topanim,bottomanim;
    private ImageView imageView;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        topanim= AnimationUtils.loadAnimation(this,R.anim.top_anim);
        bottomanim= AnimationUtils.loadAnimation(this,R.anim.bottom_anim);

        imageView=findViewById(R.id.imageView);
        textView=findViewById(R.id.text);

        imageView.setAnimation(topanim);
        textView.setAnimation(bottomanim);


        handler=new Handler();
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainActivity.this,GoogleSign.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable,4000);

    }
}