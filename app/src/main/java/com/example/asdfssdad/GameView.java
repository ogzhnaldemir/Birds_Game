package com.example.asdfssdad;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class GameView extends SurfaceView implements Runnable {

    private Thread thread;
    private boolean isplaying, isGameOver = false;
    private Paint paint;
    private List<Bullet> bullets;
    private Flight flight;
    private int screenX, screenY, score = 0, bestscore;
    public static float screenRatioX, screenRatioY;
    private Background background1, background2;
    private SharedPreferences prefs;
    private Random random;
    private Bird[] birds;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private int sound;
    private GameActivity activity;
    private SoundPool soundPool;
    private DatabaseReference databaseReference;

    public GameView(GameActivity activity, int screenX, int screenY) {
        super(activity);
        this.activity = activity;
        prefs = activity.getSharedPreferences("game", Context.MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("kullanıcılar").child(firebaseUser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String scorestring = snapshot.child("Score").getValue().toString();
                bestscore = Integer.parseInt(scorestring);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .build();

        } else
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        sound = soundPool.load(activity, R.raw.shoot, 1);


        this.screenX = screenX;
        this.screenY = screenY;
        screenRatioX = 1920f / screenX;
        screenRatioY = 1080f / screenY;

        background1 = new Background(screenX, screenY, getResources());
        background2 = new Background(screenX, screenY, getResources());

        flight = new Flight(this, screenY, getResources());

        bullets = new ArrayList<>();

        background2.x = screenX;
        paint = new Paint();
        paint.setTextSize(100);
        paint.setColor(Color.DKGRAY);

        birds = new Bird[4];

        for (int i = 0; i < 4; i++) {

            Bird bird = new Bird(getResources());
            birds[i] = bird;

        }
        random = new Random();
    }

    @Override
    public void run() {
        while (isplaying) {
            update();
            draw();
            sleep();
        }
    }

    public void Restart() {
        isplaying = true;
        run();
    }

    private void update() {
        background1.x -= 10 * screenRatioX;
        background2.x -= 10 * screenRatioX;
        if (background1.x + background1.background.getWidth() < 0) {
            background1.x = screenX;
        }
        if (background2.x + background2.background.getWidth() < 0) {
            background2.x = screenX;
        }
        if (flight.isGoingUp) {
            flight.y -= 30 * screenRatioY;
        } else
            flight.y += 30 * screenRatioY;
        if (flight.y < 0) {
            flight.y = 0;
        }
        if (flight.y >= screenY - flight.height) {
            flight.y = screenY - flight.height;
        }
        List<Bullet> trash = new ArrayList<>();

        for (Bullet bullet : bullets) {
            if (bullet.x > screenX) {
                trash.add(bullet);
            }
            bullet.x += 50 * screenRatioX;

            for (Bird bird : birds) {

                if (Rect.intersects(bird.getCollisionShape(),
                        bullet.getCollisionShape())) {

                    score++;
                    bird.x = -500;
                    bullet.x = screenX + 500;
                    bird.wasShot = true;
                }
            }
        }

        for (Bullet bullet : trash) {
            bullets.remove(bullet);
        }
        for (Bird bird : birds) {

            bird.x -= bird.speed;

            if (bird.x + bird.width < 0) {

                if (!bird.wasShot) {
                    isGameOver = true;
                    return;
                }
                int bound = (int) (30 * screenRatioX);
                bird.speed = random.nextInt(bound);

                if (bird.speed < 10 * screenRatioX)
                    bird.speed = (int) (10 * screenRatioX);

                bird.x = screenX;
                bird.y = random.nextInt(screenY - bird.height);

                bird.wasShot = false;
            }

            if (Rect.intersects(bird.getCollisionShape(), flight.getCollisionShape())) {

                isGameOver = true;
                return;
            }

        }

    }

    private void draw() {
        if (getHolder().getSurface().isValid()) {

            Canvas canvas = getHolder().lockCanvas();
            canvas.drawBitmap(background1.background, background1.x, background1.y, paint);
            canvas.drawBitmap(background2.background, background2.x, background2.y, paint);

            for (Bird bird : birds)
                canvas.drawBitmap(bird.getBird(), bird.x, bird.y, paint);

            canvas.drawText(score + "", screenX / 2f, 164, paint);
            canvas.drawText("Hihg score" + bestscore + "", screenX / 1.58f, 164, paint);

            if (isGameOver) {
                isplaying = false;
                canvas.drawBitmap(flight.getDead(), flight.x, flight.y, paint);
                getHolder().unlockCanvasAndPost(canvas);
                saveIfHighScore();
                waitBeforeExiting();
                return;
            }

            canvas.drawBitmap(flight.getFlight(), flight.x, flight.y, paint);

            for (Bullet bullet : bullets)
                canvas.drawBitmap(bullet.bullet, bullet.x, bullet.y, paint);

            getHolder().unlockCanvasAndPost(canvas);

        }
    }

    private void waitBeforeExiting() {
        try {
            Thread.sleep(3000);
            activity.startActivity(new Intent(activity, GameActivity.class));
            activity.finish();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void saveIfHighScore() {

        if (score > bestscore) {
            bestscore = score;
            HashMap<String, Object> mData = new HashMap<String, Object>();
            mData.put("Score", bestscore);
            databaseReference.updateChildren(mData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {

                }
            });
        }
    }

    private void sleep() {
        try {
            Thread.sleep(17);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {

        isplaying = true;
        thread = new Thread(this);
        thread.start();

    }

    public void pause() {

        try {
            isplaying = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (event.getX() < screenX / 2) {
                    flight.isGoingUp = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                flight.isGoingUp = false;
                if (event.getX() > screenX / 2)
                    flight.toShoot++;
                break;
        }
        return true;
    }


    public void newBullet() {

        if (!prefs.getBoolean("isMute", false))
            soundPool.play(sound, 1, 1, 0, 0, 1);

        Bullet bullet = new Bullet(getResources());
        bullet.x = flight.x + flight.width;
        bullet.y = flight.y + (flight.height / 2);
        bullets.add(bullet);
    }
}
