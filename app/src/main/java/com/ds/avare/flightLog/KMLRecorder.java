/*-
 * SPDX-License-Identifier: BSD-2-Clause
 *
 * Copyright (c) 2013, Apps4Av Inc. (apps4av.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice unmodified, this list of conditions, and the following
 *    disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.ds.avare.flightLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

import android.annotation.SuppressLint;

import com.ds.avare.StorageService;
import com.ds.avare.gps.GpsParams;
import com.ds.avare.shapes.CrumbsShape;
import com.ds.avare.shapes.Shape;
import com.ds.avare.utils.Helper;

/**
 * Class to record GPS Position information to a file formatted in KML suitable
 * for consumption by GoogleEarth.
 * 
 * @author Ron
 *
 */
public class KMLRecorder {
	private BufferedWriter  mTracksFile;			// File handle to use for writing the data
    private final LinkedList<GpsParams> mPositionHistory; // Stored GPS points
	private URI 			mFileURI;				// The URI of the file created for these datapoints
	private GpsParams		mLastFix;				// the last time we wrote a position
	private final CrumbsShape     mShape;
	private final String          mFolder;
	
	/**
	 * Statics that all class instances share
	 */
    public static final String KMLFILENAMEFORMAT = "yyyy-MM-dd_HH-mm-ss";
    public static final String KMLFILENAMEEXTENTION = ".KML";
    public static final String KMLFILEPREFIX  = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +
			"	<Document>\n" +
			"		<name>Flight Data by Avare</name>\n" +
			"		<Style id=\"AircraftFlight\">\n" +
			"			<LineStyle>\n" +
			"				<color>ffff00ff</color>\n" +
			"				<width>4</width>\n" +
			"			</LineStyle>\n" +
			"			<PolyStyle>\n" +
			"				<color>7fcccccc</color>\n" +
			"			</PolyStyle>\n" +
		    "		</Style>\n" + 
			"		<Style id=\"dot\">\n" + 
			"			<IconStyle>\n" +
			"				<color>FDFFFFFF</color>\n" +
			"				<scale>0.5</scale>\n" +
			"				<Icon>\n" +
			"					<href>root://icons/palette-4.png</href>\n" +
			"					<x>32</x>\n" +
			"					<y>128</y>\n" +
			"					<w>32</w>\n" +
			"					<h>32</h>\n" +
			"				</Icon>\n" +
			"			</IconStyle>\n" +
			"		</Style>\n";

    
    public static final String KMLCOORDINATESHEADER =
			"		<Placemark>\n" +
			"			<name>Avare Flight Path</name>\n" +
			"			<visibility>1</visibility>\n" +
			"			<description>3-D Flight Position Data</description>\n" +
			"			<styleUrl>#AircraftFlight</styleUrl>\n" +
			"			<LineString>\n" +
			"				<extrude>1</extrude>\n" +
			"				<altitudeMode>absolute</altitudeMode>\n" +
			"				<coordinates>\n";

    public static final String KMLCOORDINATESTRAILER =
            "				</coordinates>\n" +
    		"			</LineString>\n" +
    		"		</Placemark>\n";

    public static final String KMLTRACKPOINT =
		    "		<Placemark>\n" +
		    "           <name>%d</name>\n" +
		    "			<description><![CDATA[\n" +
		    "				Time: %s\n" + 
		    "				Altitude: %f\n" +
		    "				Bearing: %f\n" +
		    "				Speed: %f\n" +
		    "				Long: %f\n" +
		    "				Lat: %f]]>\n" +
		    "			</description>\n" +
		    "			<styleUrl>#dot</styleUrl>\n" +
		    "			<Point>\n" +
		    "				<altitudeMode>absolute</altitudeMode>\n" +
		    "				<coordinates>%f,%f,%f</coordinates>\n" +
		    "			</Point>\n" +
		    "		</Placemark>\n";

    public static final String KMLFILESUFFIX = 
    		"	</Document>\n" +
    		"</kml>\n";

    /**
     * Default constructor. Allocate the list that holds our collection
     * of gps points
     */
    public KMLRecorder() {
		mPositionHistory = new LinkedList<>();
		mShape = new CrumbsShape();
		mLastFix = new GpsParams(null);
		mFolder = StorageService.getInstance().getPreferences().getUserDataFolder() + File.separator + "tracks";
    }
    
    /** 
     * Are we actively recording data to a file ? 
     * @return true if we are, false otherwise
     */
    public boolean isRecording() {
    	return mTracksFile != null;
    }
    
    /**
     * Stop saving datapoints to the file and the historical list
     * @return A URI of the file just closed
     */
	public URI stop(){
		mShape.clearShape();
		if(mTracksFile != null) {
    		// File operations can cause exceptions and we need to account for that
    		try {
				mTracksFile.write(KMLCOORDINATESTRAILER);	// Close off the coordinates section
				// Write out each track point of this flight as its own entry. This
				// saves out more detail than just lat/long of the point.
				for(int idx = 0, max = mPositionHistory.size(); idx < max; idx++) {
					GpsParams gpsParams = mPositionHistory.get(idx);
					String trackPoint = String.format(Locale.getDefault(), KMLTRACKPOINT,
							idx + 1,
							new Date(gpsParams.getTime()),
							gpsParams.getAltitude(),
							gpsParams.getBearing(),
							gpsParams.getSpeed(),
							gpsParams.getLongitude(),
							gpsParams.getLatitude(),
							gpsParams.getLongitude(),
							gpsParams.getLatitude(),
							gpsParams.getAltitude() * .3048 /* meter per feet */
							);
					mTracksFile.write(trackPoint);
    			}
    			
    			// Close off the overall KML file now
				mTracksFile.write(KMLFILESUFFIX);	// The last of the file data
				mTracksFile.flush();
    			mTracksFile.close();				// close the file
    		} catch (IOException ignore) {

			}

    		// Clear out our control objects
    		mTracksFile = null;	// No track file anymore
    		return mFileURI;	// return with the URI of the file we just closed
    	}
    	return null;
    }
    
    /**
     * Begin recording position points to a file and our memory linked list
     */
    @SuppressLint("SimpleDateFormat")
	public void start() {
        mShape.clearShape();

		// Build the file name based upon the current date/time
		String fileName = new SimpleDateFormat(KMLFILENAMEFORMAT).format(Calendar.getInstance().getTime()) + KMLFILENAMEEXTENTION;
    	File mFile = new File(mFolder, fileName);
    	
    	// File handling can throw some exceptions
    	try {
    		
    		// Ensure the full path to the file area exists
    		File mDirPath = new File(mFolder, "");
    		if(!mDirPath.exists()) {
    			mDirPath.mkdirs();
    		}
    		
    		// If the file does not exist, then create it. 
        	if(!mFile.exists()){
        		mFile.createNewFile();	// Create the new file
        	}

        	// Save off the URI of this file we just created. This value
        	// is returned at the stop() method.
        	mFileURI = mFile.toURI();
        	
        	// Create a new writer, then a buffered writer for this file
        	FileWriter fileWriter = new FileWriter(mFile);
			mTracksFile = new BufferedWriter(fileWriter, 8192);

    		// Write out the opening file prefix
			// mTracksFile.write(KMLFILEPREFIX);			// Overall file prelude
			mTracksFile.write(KMLCOORDINATESHEADER);	// Open coordinates data
			mPositionHistory.clear();
    	} catch (Exception ignore) { }
    }
    
    /**
     * This object requires notification of when the position changes. This is
     * done by the caller sending the information periodically via the GpsParams
     * object to this method. If it is greater than our stall speed,  if we have a file
     * open to write it to.
     * @param gpsParams Current location information
     */
    public void setGpsParams(GpsParams gpsParams) {
		if (mTracksFile == null) {
    		// File closed means nothing to do
    		return;
    	}

    	// Start recording faster than 3 knots
		if ((gpsParams.getSpeed() < 3)) {
			// Not going fast enough yet to record
			return;
		}
		
		// Set if we are to record this position
		//
		boolean bRecordPoint = false;
		
		// If the speed has changed more than 5 knots
		if (Math.abs(gpsParams.getSpeed() - mLastFix.getSpeed()) > 5) {
			bRecordPoint = true;
		}

		// If the altitude is 100' or greater different
		if((Math.abs(gpsParams.getAltitude() - mLastFix.getAltitude())) > 100) {
			bRecordPoint = true;
		}
		
		// If the bearing is 15 degrees or more different - that's 24 samples per 360 turn
		if(Helper.angularDifference(gpsParams.getBearing(), mLastFix.getBearing()) > 15) {
    		bRecordPoint = true;
    	}

    	// If the time of the last point and now is greater than 30 seconds
		if (((gpsParams.getTime() - mLastFix.getTime()) > 30 * 1000)) {
			bRecordPoint = true;
		}

		// After all those tests, if nothing says to record, then get out of here
		if (!bRecordPoint) {
			return;
		}

		// Write out the position. Convert the altitude from feet to meters for the KML file
		try {
			mTracksFile.write ("\t\t\t\t\t" + gpsParams.getLongitude() + "," + 
											  gpsParams.getLatitude() + "," + 
											 (gpsParams.getAltitude() * .3048) + "\n");

			// Add this position to our linked list for possible display
			// on the charts
			mPositionHistory.add(GpsParams.copy(gpsParams));
			mShape.updateShape(gpsParams);

			// The data was saved correctly, update our last known position
			mLastFix = gpsParams;

		} catch (Exception ignore) { }
	}
    
    /**
     * 
     * @return The shape of our tracks to draw
     */
    public Shape getShape() {
       return mShape; 
    }
}
