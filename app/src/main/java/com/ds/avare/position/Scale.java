/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.position;

/**
 * 
 * @author zkhan
 * Keeps scale factor and scale correction at this particular point on the map
 */
public class Scale {

    private double mScaleFactor;
    private int mMacro;

    private double mMaxScale;
    private double mMinScale;

    /**
     * Scale for pictures
     */
    public Scale(double max) {
        mMaxScale = max;
        mMinScale = 0.03125;
        mScaleFactor = 1;
        mMacro = 1;
    }

    public Scale(double max, double min) {
        mMaxScale = max;
        mMinScale = min;
        mScaleFactor = 1;
        mMacro = 1;
    }

    /**
     * Scale for charts
     */
    public Scale() {
        mMaxScale = 2;
        mMinScale = 0.03125;
        mScaleFactor = 1;
        mMacro = 1;
    }
    
    // Determine a stepping factor based upon the macro and scale
    // Used by dynamic distance rings and edge markers
    public double getStep() {
    	double step;
        int macro = getMacroFactor();
        float scaleRaw = getScaleFactor();
        if(macro <= 1 && scaleRaw > 1) {  
            step = 2.5;        
        } 
        else if(macro <= 1 && scaleRaw <= 1) {  
            step = 5;
        } 
        else if (macro <= 2) {
            step = 10;
        } 
        else if (macro <= 4) {
            step = 20;
        } 
        else if (macro <= 8) {
            step = 40;
        }  
        else {
            step = 80;
        } 
        return step;
    }
    
    /**
     * 
     * @param factor
     */
    public void setScaleFactor(float factor) {
        mScaleFactor = (double)factor;
    }
    
    /**
     * This one is for plates drawing only
     * @return
     */
    public float getScaleFactor() {
        double s;
        if(mScaleFactor > mMaxScale) {
            s = mMaxScale;
        }
        else if(mScaleFactor < mMinScale) {
            s = mMinScale;
        }
        else {
            s = mScaleFactor;
        }
        return((float)s);
    }

    /**
     * 
     * @return
     */
    public int getMacroFactor() {
        return mMacro;
    }

    public void setMacroFactor() {
        if(mScaleFactor >= 0.5) {
            mMacro = 1;
        }
        else if(mScaleFactor >= 0.25) {
            mMacro = 2;
        }
        else if(mScaleFactor >= 0.125) {
            mMacro = 4;
        }
        else if(mScaleFactor >= 0.0625) {
            mMacro = 8;
        }
        else {
            mMacro = 16;
        }
    }

    public int getNewMacroFactor() {
        if(mScaleFactor >= 0.5) {
            return(1);
        }
        else if(mScaleFactor >= 0.25) {
            return(2);
        }
        else if(mScaleFactor >= 0.125) {
            return(4);
        }
        else if(mScaleFactor >= 0.0625) {
            return(8);
        }
        return(16);
    }

    /**
     * 
     * @return
     */
    public int downSample() {
        if(mScaleFactor >= 0.5) {
            return 0;
        }
        else if(mScaleFactor >= 0.25) {
            return 1;
        }
        else if(mScaleFactor >= 0.125) {
            return 2;
        }
        else if(mScaleFactor >= 0.0625) {
            return 3;
        }
        return 4;
    }

    /**
     * 
     */
    public void zoomOut() {
       mScaleFactor = mMinScale;
    }
}
