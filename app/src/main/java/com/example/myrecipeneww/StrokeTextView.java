package com.example.myrecipeneww;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class StrokeTextView extends AppCompatTextView {

    private Paint strokePaint = new Paint();

    public StrokeTextView(Context context) {
        super(context);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Salin properti paint dari textView
        strokePaint.set(getPaint());
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(8f); // ketebalan garis luar
        strokePaint.setColor(Color.WHITE); // warna outline
        strokePaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int originalTextColor = getCurrentTextColor();

        // Gambar outline
        setTextColor(strokePaint.getColor());
        getPaint().setStyle(Paint.Style.STROKE);
        getPaint().setStrokeWidth(8f);
        super.onDraw(canvas);

        // Gambar isi teks
        setTextColor(originalTextColor);
        getPaint().setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
    }
}
