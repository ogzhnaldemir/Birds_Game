package com.example.asdfssdad;

import static com.example.asdfssdad.GameView.screenRatioX;
import static com.example.asdfssdad.GameView.screenRatioY;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

public class Bullet {

    int x,y, width, height;
    Bitmap bullet;

    Bullet (Resources resources){

        bullet = BitmapFactory.decodeResource(resources,R.drawable.bullet);

        int width = bullet.getWidth();
        int height = bullet.getHeight();

        width /= 4;
        height/= 4;

        width = (int) (width * screenRatioX);
        height = (int) (height * screenRatioY);

        bullet = Bitmap.createScaledBitmap(bullet, width,height,false);

    }
    Rect getCollisionShape () {
        return new Rect(x, y, x + width, y + height);
    }

}
