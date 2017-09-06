package com.example.patearn.patearnfirebase;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by #1 patearn on 2017-09-06.
 */

public class Splashscreen extends Activity {
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }
    /** Called when the activity is first created. */
    Thread splashTread;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
        StartAnimations();
    }
    private void StartAnimations() {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.alpha);
        anim.reset();
        FrameLayout l=(FrameLayout) findViewById(R.id.lin_lay);
        l.clearAnimation();
        l.startAnimation(anim);

//        anim = AnimationUtils.loadAnimation(this, R.anim.translate);
//        anim.reset();
//        ImageView iv = (ImageView) findViewById(R.id.splash);
//        iv.clearAnimation();
//        iv.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate2);
        anim.reset();
        ImageView iv2 = (ImageView) findViewById(R.id.splash2);
        iv2.clearAnimation();
        iv2.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate3);
        anim.reset();
        ImageView iv3 = (ImageView) findViewById(R.id.splash3);
        iv3.clearAnimation();
        iv3.startAnimation(anim);

        anim = AnimationUtils.loadAnimation(this, R.anim.translate_slow);
        anim.reset();
        ImageView iv4 = (ImageView) findViewById(R.id.splash4);
        iv4.clearAnimation();
        iv4.startAnimation(anim);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 2100) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(Splashscreen.this,
                            MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    Splashscreen.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    Splashscreen.this.finish();
                }

            }
        };
        splashTread.start();

    }

}