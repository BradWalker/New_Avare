/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.ds.avare.R;
import com.ds.avare.StorageService;
import com.ds.avare.utils.Helper;

/**
 * 
 * @author zkhan
 *
 * Draws memory use 
 */
public class GlassView extends View {

    /*
     * Satellite view
     */
    private Paint            mPaint;

    private String           mAgl;
    private String           mErrorStatus;

    private static final int TEXT_COLOR = Color.WHITE;
    private static final int TEXT_COLOR_OPPOSITE = Color.BLACK;

    /**
     *
     */
    private void setup() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTypeface(Helper.getTypeFace(StorageService.getInstance().getApplicationContext()));
        mPaint.setShadowLayer(4, 4, 4, Color.BLACK);
        mPaint.setTextSize(Helper.adjustTextSize(StorageService.getInstance().getApplicationContext(), R.dimen.TextSize));

    }


    /**
     *
     * @param context
     */
    public GlassView(Context context) {
        super(context);
        setup();
    }

    /**
     *
     * @param context
     * @param aset
     */
    public GlassView(Context context, AttributeSet aset) {
        super(context, aset);
        setup();
    }

    /**
     * @param context
     * Default for tools, do not call
     */
    public GlassView(Context context, AttributeSet aset, int arg) {
        super(context, aset, arg);
        setup();
    }

    /* (non-Javadoc)
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    public void onDraw(Canvas canvas) {

        /*
         * Now draw the target cross hair
         */

        drawStatusLines(canvas);

        if(mAgl != null) {
            mPaint.setColor(Color.WHITE);
            canvas.drawText(mAgl, 8, getHeight() - mPaint.getTextSize() - 8, mPaint);
        }
    }

    // Draw the top status lines
    private void drawStatusLines(Canvas canvas) {
        if(StorageService.getInstance().getPreferences().show3DInfoLines()) {
            StorageService.getInstance().getInfoLines().drawCornerTextsDynamic(canvas, mPaint,
                    TEXT_COLOR, TEXT_COLOR_OPPOSITE, 4,
                    getWidth(), getHeight(), mErrorStatus, null);
        }
    }



    public void setAgl(String agl) {
        mAgl = agl;
        invalidate();
    }

    public void setStatus(String status) {
        mErrorStatus = status;
        invalidate();
    }
}
