package com.example.asdfssdad;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Background {
    int x=0,y=0;
    Bitmap background;

    public Background(int screenX, int screenY, Resources resources) {

        background = BitmapFactory.decodeResource(resources,R.drawable.background);
        background = Bitmap.createScaledBitmap(background,screenX,screenY,false);
    }
}
