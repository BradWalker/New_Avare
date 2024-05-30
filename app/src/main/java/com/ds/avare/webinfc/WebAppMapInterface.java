/*
Copyright (c) 2012, Apps4Av Inc. (apps4av.com) 
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ds.avare.webinfc;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.ds.avare.StorageService;
import com.ds.avare.storage.Preferences;
import com.ds.avare.touch.LongTouchDestination;
import com.ds.avare.utils.GenericCallback;
import com.ds.avare.utils.Helper;
import com.ds.avare.utils.WeatherHelper;
import com.ds.avare.utils.WindsAloftHelper;
import com.ds.avare.weather.Airep;

/**
 * 
 * @author zkhan
 * This class feeds the WebView with data
 */
public class WebAppMapInterface {
    private WebView mWebView;
    private GenericCallback mCallback;
    private Preferences mPref;

    private static final int MSG_SET_DATA = 1;
    private static final int MSG_ACTION = 2;

    /**
     * Instantiate the interface and set the context
     */
    public WebAppMapInterface(WebView v, GenericCallback cb) {
        mWebView = v;
        mPref = StorageService.getInstance().getPreferences();
        mCallback = cb;
    }

    /**
     * Do something on a button press
     */
    @JavascriptInterface
    public void doAction(String action) {
        Message m = mHandler.obtainMessage();
        m.obj = action;
        m.what = MSG_ACTION;
        mHandler.sendMessage(m);
    }


    public void setData(LongTouchDestination data) {
        Message m = mHandler.obtainMessage();
        m.obj = data;
        m.what = MSG_SET_DATA;
        mHandler.sendMessage(m);
    }



    /**
     * This leak warning is not an issue if we do not post delayed messages, which is true here.
     * Must use handler for functions called from JS, but for uniformity, call all JS from this handler
     */
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (MSG_SET_DATA == msg.what) {


                LongTouchDestination data = (LongTouchDestination)msg.obj;
                String taf = "";
                if(data.getTaf() != null) {
                    String split[] = data.getTaf().getRawText().split(data.getTaf().getStationId(), 2);
                    // Do not color code airport name
                    if(split.length == 2) {
                        taf = "<hr>" + WeatherHelper.addColor("TAF ", "yellow") + data.getTaf().getStationId() + " " + WeatherHelper.formatVisibilityHTML(WeatherHelper.formatTafHTML(WeatherHelper.formatWindsHTML(WeatherHelper.formatWeatherHTML(split[1], mPref.isWeatherTranslated()), mPref.isWeatherTranslated()), mPref.isWeatherTranslated()));
                    }
                }

                String metar = "";
                if(data.getMetar() != null) {
                    metar = WeatherHelper.formatMetarHTML(data.getMetar().getRawText(), mPref.isWeatherTranslated());
                    metar = "<hr>" + WeatherHelper.addColor("METAR ", "yellow") + WeatherHelper.addColorWithStroke(metar, WeatherHelper.metarColorString(data.getMetar().getFlightCategory()));
                }

                String airep = "";
                if(data.getAirep() != null) {
                    for(Airep a : data.getAirep()) {
                        String p = WeatherHelper.formatPirepHTML(a.getRawText(), mPref.isWeatherTranslated());
                        airep += p + "<br><br>";
                    }
                    if(!airep.equals("")) {
                        airep = "<hr><b>" + WeatherHelper.addColor("PIREP", "yellow") + "</b><br>" + airep;
                    }
                }

                String sua = "";
                if(data.getSua() != null) {
                    sua = "<hr><b>" + WeatherHelper.addColor("Special Use Airspace", "yellow") + "</b><br>";
                    sua += data.getSua().replace("\n", "<br>");
                }

                String tfr = "";
                if(data.getTfr() != null) {
                    if(!data.getTfr().equals("")) {
                        tfr = "<hr><b>" + WeatherHelper.addColor("TFR", "yellow") + "</b><br>";
                        tfr += data.getTfr().replace("\n", "<br>");
                    }
                }


                String layer = mPref.useAdsbWeather() ?
                        "<hr><b>" + WeatherHelper.addColor("Weather/SUA Source", "yellow") + "</b>ADS-B<br>" :
                        "<hr><b>" + WeatherHelper.addColor("Weather/SUA Source", "yellow") + "</b>Internet<br>";
                if(data.getLayer() != null) {
                    if(!data.getLayer().equals("")) {
                        layer += "<b>" + WeatherHelper.addColor("Weather Layer Time", "yellow") + "</b> ";
                        layer += data.getLayer();
                    }
                }

                String mets = "";
                if(data.getMets() != null) {
                    if(!data.getMets().equals("")) {
                        mets = "<hr><b>" + WeatherHelper.addColor("SIG/AIRMETs", "yellow") + "</b><br>";
                        mets += data.getMets().replace("\n", "<br>");
                    }
                }

                String performance = "";
                if(data.getPerformance() != null) {
                    if(!data.getPerformance().equals("")) {
                        performance = "<hr><b>" + WeatherHelper.addColor("Performance", "yellow") + "</b> ";
                        performance += data.getPerformance().replace("\n", "<br>");
                    }
                }

                String winds = "";
                if(data.getWa() != null) {
                    winds = "<hr><b>" + WeatherHelper.addColor("Winds/Temp. Aloft", "yellow") + "</b> ";
                    winds += WindsAloftHelper.formatWindsHTML(data.getWa(), mPref.getWindsAloftCeiling());
                }

                if (data.getNavaids() != null) {
                    data.setInfo(data.getInfo() + "<br>" + data.getNavaids());
                }

                if(data.getInfo() == null) {
                    data.setInfo("");
                }
                else {
                    data.setInfo("<b>" + WeatherHelper.addColor("Position", "yellow") + "</b>" + data.getInfo());
                }

                // type from map or from search
                String type = data.hasMoreButtons() ? "more" : "";
                String func = "javascript:setData('" +
                        type + "','" +
                        Helper.formatJsArgs(data.getAirport()) + "','" +
                        Helper.formatJsArgs(data.getInfo()) + "','" +
                        Helper.formatJsArgs(metar) + "','" +
                        Helper.formatJsArgs(taf) + "','" +
                        Helper.formatJsArgs(airep) + "','" +
                        Helper.formatJsArgs(tfr) + "','" +
                        Helper.formatJsArgs(sua) + "','" +
                        Helper.formatJsArgs(mets) + "','" +
                        Helper.formatJsArgs(performance) + "','" +
                        Helper.formatJsArgs(winds) + "','" +
                        Helper.formatJsArgs(layer) +
                        "')";



                mWebView.loadUrl(func);
            }
            else if (MSG_ACTION == msg.what) {
                mCallback.callback((String)msg.obj, null);
            }
        }
    };
}
