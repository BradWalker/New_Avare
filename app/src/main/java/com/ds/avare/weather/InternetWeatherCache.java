/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
    *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
    *
    *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package com.ds.avare.weather;

import com.ds.avare.StorageService;
import com.ds.avare.shapes.MetShape;
import com.ds.avare.utils.Helper;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 
 * @author zkhan
 *
 */
public class InternetWeatherCache {

    /**
     * Task that would draw tiles on bitmap.
     */
    private WeatherTask                mWeatherTask; 
    private Thread                     mWeatherThread;
    private List<AirSigMet>      mAirSig;
    private StorageService             mService;
    private Date                       mDate;

    public InternetWeatherCache() {
        mWeatherTask = null;
        mWeatherThread = null;
        mAirSig = null;
        mService = StorageService.getInstance();
        mDate = null;
    }

    /**
     *
     * @param expiry
     * @return
     */
    public boolean isOld(int expiry) {
        if(mDate == null) {
            return false;
        }
        long diff = Helper.getMillisGMT();
        diff -= mDate.getTime();
        if(diff > expiry * 60 * 1000) {
            return true;
        }
        return false;
    }

    /**
     * 
     */
    public void parse() {
        
        /*
         * Do weather parsing in background. It takes a long time.
         */
        if(mWeatherThread != null) {
            if(mWeatherThread.isAlive()) {
                return;
            }
        }
        mWeatherTask =  new WeatherTask();
        mWeatherThread = new Thread(mWeatherTask);
        mWeatherThread.start();
    }
    
    /**
     * 
     * @return
     */
    public List<AirSigMet> getAirSigMet() {
        return mAirSig;
    }

    private class WeatherTask implements Runnable {

        @Override
        public void run() {
            try {
                
                /*
                 * Create a list of air/sigmets
                 */
                mAirSig = mService.getDBResource().getAirSigMets();

                String filenameManifest = StorageService.getInstance().getPreferences().getServerDataFolder() + File.separator + "weather";
                String dataManifest = Helper.readTimestampFromFile(filenameManifest);
                if(null != dataManifest) {
                    // Find date of TFRs of format 09_03_2015_15:30_UTC, first line in manifest
                    SimpleDateFormat format = new SimpleDateFormat("MM_dd_yyyy_HH:mm", Locale.getDefault());

                    try {
                        mDate = format.parse(dataManifest.replace("_UTC", ""));
                    } catch (Exception e) {
                        return;
                    }
                }

                /*
                 * Convert AIRMET/SIGMETS to shapes compatible coordinates
                 */
                for(int i = 0; i < mAirSig.size(); i++) {
                    AirSigMet asm = mAirSig.get(i);
                    /*
                     * Discard none intensity
                     */
                    if(asm.getSeverity().equals("NONE")) {
                        continue;
                    }

                    StringBuilder b = new StringBuilder();
                    b.append(asm.getReportType());
                    b.append(" ");
                    b.append(asm.getHazard());
                    if(!asm.getSeverity().isEmpty()) {
                        b.append(" ");
                        b.append(asm.getSeverity());
                    }
                    b.append("\n");
                    if(!asm.getMinFt().isEmpty()) {
                        b.append(asm.getMinFt());
                        b.append(" to ");
                    }
                    if(!asm.getMaxFt().isEmpty()) {
                        b.append(asm.getMaxFt());
                        b.append(" ft MSL");
                    }
                    b.append("\n");
                    b.append(asm.getTimeFrom());
                    b.append(" to \n");
                    b.append(asm.getTimeTo());
                    b.append("\n::\n");
                    b.append(asm.getRawText());

                    asm.setShape(new MetShape(b.toString(), mDate));
                    String[] tokens = asm.getPoints().split("[;]");
                    for (String token : tokens) {
                        String[] point = token.split("[:]");
                        double lon = Double.parseDouble(point[0]);
                        double lat = Double.parseDouble(point[1]);
                        if (0 == lat || 0 == lon) {
                            continue;
                        }
                        asm.getShape().add(lon, lat, false);
                    }
                    asm.getShape().makePolygon();
                }
            }
            catch(Exception e) {
            }
        }
    }
}
