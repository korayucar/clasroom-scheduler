/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kondi.android.classroomscheduler;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.SparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneData {
    private static final String TAG = "TimeZoneData";
    private static final boolean DEBUG = false;
    private static final int OFFSET_ARRAY_OFFSET = 20;

    ArrayList<TimeZoneInfo> mTimeZones;
    LinkedHashMap<String, ArrayList<Integer>> mTimeZonesByCountry;
    HashSet<String> mTimeZoneNames = new HashSet<String>();

    private long mTimeMillis;
    private HashMap<String, String> mCountryCodeToNameMap = new HashMap<String, String>();

    public String mDefaultTimeZoneId;
    public static boolean is24HourFormat;
    private TimeZoneInfo mDefaultTimeZoneInfo;
    private String mAlternateDefaultTimeZoneId;
    private String mDefaultTimeZoneCountry;

    public TimeZoneData(Context context, String defaultTimeZoneId, long timeMillis) {
        is24HourFormat = TimeZoneInfo.is24HourFormat = DateFormat.is24HourFormat(context);
        mDefaultTimeZoneId = mAlternateDefaultTimeZoneId = defaultTimeZoneId;
        long now = System.currentTimeMillis();

        if (timeMillis == 0) {
            mTimeMillis = now;
        } else {
            mTimeMillis = timeMillis;
        }
        loadTzs(context);
        Log.i(TAG, "Time to load time zones (ms): " + (System.currentTimeMillis() - now));

        // now = System.currentTimeMillis();
        // printTz();
        // Log.i(TAG, "Time to print time zones (ms): " +
        // (System.currentTimeMillis() - now));
    }

    public void setTime(long timeMillis) {
        mTimeMillis = timeMillis;
    }

    public TimeZoneInfo get(int position) {
        return mTimeZones.get(position);
    }

    public int size() {
        return mTimeZones.size();
    }

    public int getDefaultTimeZoneIndex() {
        return mTimeZones.indexOf(mDefaultTimeZoneInfo);
    }

    // TODO speed this up
    public int findIndexByTimeZoneIdSlow(String timeZoneId) {
        int idx = 0;
        for (TimeZoneInfo tzi : mTimeZones) {
            if (timeZoneId.equals(tzi.mTzId)) {
                return idx;
            }
            idx++;
        }
        return -1;
    }

    void loadTzs(Context context) {
        mTimeZones = new ArrayList<TimeZoneInfo>();
        HashSet<String> processedTimeZones = loadTzsInZoneTab(context);
        String[] tzIds = TimeZone.getAvailableIDs();

        if (DEBUG) {
            Log.e(TAG, "Available time zones: " + tzIds.length);
        }

        for (String tzId : tzIds) {
            if (processedTimeZones.contains(tzId)) {
                continue;
            }

            final TimeZone tz = TimeZone.getTimeZone(tzId);
            if (tz == null) {
                Log.e(TAG, "Timezone not found: " + tzId);
                continue;
            }

            TimeZoneInfo tzInfo = new TimeZoneInfo(tz, null);

            if (getIdenticalTimeZoneInTheCountry(tzInfo) == -1) {
                if (DEBUG) {
                    Log.e(TAG, "# Adding time zone from getAvailId: " + tzInfo.toString());
                }
                mTimeZones.add(tzInfo);
            } else {
                if (DEBUG) {
                    Log.e(TAG,
                            "# Dropping identical time zone from getAvailId: " + tzInfo.toString());
                }
                continue;
            }
            //
            // TODO check for dups
            // checkForNameDups(tz, tzInfo.mCountry, false /* dls */,
            // TimeZone.SHORT, groupIdx, !found);
            // checkForNameDups(tz, tzInfo.mCountry, false /* dls */,
            // TimeZone.LONG, groupIdx, !found);
            // if (tz.useDaylightTime()) {
            // checkForNameDups(tz, tzInfo.mCountry, true /* dls */,
            // TimeZone.SHORT, groupIdx,
            // !found);
            // checkForNameDups(tz, tzInfo.mCountry, true /* dls */,
            // TimeZone.LONG, groupIdx,
            // !found);
            // }
        }

        // Don't change the order of mTimeZones after this sort
        Collections.sort(mTimeZones);

        mTimeZonesByCountry = new LinkedHashMap<String, ArrayList<Integer>>();
        mTimeZonesByOffsets = new SparseArray<ArrayList<Integer>>(mHasTimeZonesInHrOffset.length);

        Date date = new Date(mTimeMillis);
        Locale defaultLocal = Locale.getDefault();

        int idx = 0;
        for (TimeZoneInfo tz : mTimeZones) {
            tz.mDisplayName = tz.mTz.getDisplayName(tz.mTz.inDaylightTime(date),
                    TimeZone.LONG, defaultLocal);

            // /////////////////////
            // Grouping tz's by country for search by country
            ArrayList<Integer> group = mTimeZonesByCountry.get(tz.mCountry);
            if (group == null) {
                group = new ArrayList<Integer>();
                mTimeZonesByCountry.put(tz.mCountry, group);
            }

            group.add(idx);

            // /////////////////////
            // Grouping tz's by GMT offsets
            indexByOffsets(idx, tz);

            // Skip all the GMT+xx:xx style display names from search
            if (!tz.mDisplayName.endsWith(":00")) {
                mTimeZoneNames.add(tz.mDisplayName);
            } else if (DEBUG) {
                Log.e(TAG, "# Hiding from pretty name search: " +
                        tz.mDisplayName);
            }

            idx++;
        }
    }

    private boolean[] mHasTimeZonesInHrOffset = new boolean[40];
    SparseArray<ArrayList<Integer>> mTimeZonesByOffsets;

    public boolean hasTimeZonesInHrOffset(int offsetHr) {
        int index = OFFSET_ARRAY_OFFSET + offsetHr;
        if (index >= mHasTimeZonesInHrOffset.length || index < 0) {
            return false;
        }
        return mHasTimeZonesInHrOffset[index];
    }

    private void indexByOffsets(int idx, TimeZoneInfo tzi) {
        int offsetMillis = tzi.getNowOffsetMillis();
        int index = OFFSET_ARRAY_OFFSET + (int) (offsetMillis / DateUtils.HOUR_IN_MILLIS);
        mHasTimeZonesInHrOffset[index] = true;

        ArrayList<Integer> group = mTimeZonesByOffsets.get(index);
        if (group == null) {
            group = new ArrayList<Integer>();
            mTimeZonesByOffsets.put(index, group);
        }
        group.add(idx);
    }

    public ArrayList<Integer> getTimeZonesByOffset(int offsetHr) {
        int index = OFFSET_ARRAY_OFFSET + offsetHr;
        if (index >= mHasTimeZonesInHrOffset.length || index < 0) {
            return null;
        }
        return mTimeZonesByOffsets.get(index);
    }

    private HashSet<String> loadTzsInZoneTab(Context context) {
        HashSet<String> processedTimeZones = new HashSet<String>();
        AssetManager am = context.getAssets();
        InputStream is = null;

        /*
         * The 'backward' file contain mappings between new and old time zone
         * ids. We will explicitly ignore the old ones.
         */
        try {
            is = am.open("backward");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                // Skip comment lines
                if (!line.startsWith("#") && line.length() > 0) {
                    // 0: "Link"
                    // 1: New tz id
                    // Last: Old tz id
                    String[] fields = line.split("\t+");
                    String newTzId = fields[1];
                    String oldTzId = fields[fields.length - 1];

                    final TimeZone tz = TimeZone.getTimeZone(newTzId);
                    if (tz == null) {
                        Log.e(TAG, "Timezone not found: " + newTzId);
                        continue;
                    }

                    processedTimeZones.add(oldTzId);

                    if (DEBUG) {
                        Log.e(TAG, "# Dropping identical time zone from backward: " + oldTzId);
                    }

                    // Remember the cooler/newer time zone id
                    if (mDefaultTimeZoneId != null && mDefaultTimeZoneId.equals(oldTzId)) {
                        mAlternateDefaultTimeZoneId = newTzId;
                    }
                }
            }
        } catch (IOException ex) {
            Log.e(TAG, "Failed to read 'backward' file.");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) {
            }
        }

        /*
         * zone.tab contains a list of time zones and country code. They are
         * "sorted first by country, then an order within the country that (1)
         * makes some geographical sense, and (2) puts the most populous zones
         * first, where that does not contradict (1)."
         */
        try {
            String lang = Locale.getDefault().getLanguage();
            is = am.open("zone.tab");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#")) { // Skip comment lines
                    // 0: country code
                    // 1: coordinates
                    // 2: time zone id
                    // 3: comments
                    final String[] fields = line.split("\t");
                    final String timeZoneId = fields[2];
                    final String countryCode = fields[0];
                    final TimeZone tz = TimeZone.getTimeZone(timeZoneId);
                    if (tz == null) {
                        Log.e(TAG, "Timezone not found: " + timeZoneId);
                        continue;
                    }

                    // Remember the mapping between the country code and display
                    // name
                    String country = mCountryCodeToNameMap.get(fields[0]);
                    if (country == null) {
                        country = new Locale(lang, countryCode)
                                .getDisplayCountry(Locale.getDefault());
                        mCountryCodeToNameMap.put(countryCode, country);
                    }

                    // TODO Don't like this here but need to get the country of
                    // the default tz.

                    // Find the country of the default tz
                    if (mDefaultTimeZoneId != null && mDefaultTimeZoneCountry == null
                            && timeZoneId.equals(mAlternateDefaultTimeZoneId)) {
                        mDefaultTimeZoneCountry = country;
                        TimeZone defaultTz = TimeZone.getTimeZone(mDefaultTimeZoneId);
                        if (defaultTz != null) {
                            mDefaultTimeZoneInfo = new TimeZoneInfo(defaultTz, country);

                            int tzToOverride = getIdenticalTimeZoneInTheCountry(mDefaultTimeZoneInfo);
                            if (tzToOverride == -1) {
                                if (DEBUG) {
                                    Log.e(TAG, "Adding default time zone: "
                                            + mDefaultTimeZoneInfo.toString());
                                }
                                mTimeZones.add(mDefaultTimeZoneInfo);
                            } else {
                                mTimeZones.add(tzToOverride, mDefaultTimeZoneInfo);
                                if (DEBUG) {
                                    TimeZoneInfo tzInfoToOverride = mTimeZones.get(tzToOverride);
                                    String tzIdToOverride = tzInfoToOverride.mTzId;
                                    Log.e(TAG, "Replaced by default tz: "
                                            + tzInfoToOverride.toString());
                                    Log.e(TAG, "Adding default time zone: "
                                            + mDefaultTimeZoneInfo.toString());
                                }
                            }
                        }
                    }

                    // Add to the list of time zones if the time zone is unique
                    // in the given country.
                    TimeZoneInfo timeZoneInfo = new TimeZoneInfo(tz, country);
                    int identicalTzIdx = getIdenticalTimeZoneInTheCountry(timeZoneInfo);
                    if (identicalTzIdx == -1) {
                        if (DEBUG) {
                            Log.e(TAG, "# Adding time zone: " + timeZoneId + " ## " +
                                    tz.getDisplayName());
                        }
                        mTimeZones.add(timeZoneInfo);
                    } else {
                        if (DEBUG) {
                            Log.e(TAG, "# Dropping identical time zone: " + timeZoneId + " ## " +
                                    tz.getDisplayName());
                        }
                    }
                    processedTimeZones.add(timeZoneId);
                }
            }

        } catch (IOException ex) {
            Log.e(TAG, "Failed to read 'zone.tab'.");
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ignored) {
            }
        }

        return processedTimeZones;
    }

    private int getIdenticalTimeZoneInTheCountry(TimeZoneInfo timeZoneInfo) {
        int idx = 0;
        for (TimeZoneInfo tzi : mTimeZones) {
            if (tzi.hasSameRules(timeZoneInfo)) {
                if (tzi.mCountry == null) {
                    if (timeZoneInfo.mCountry == null) {
                        return idx;
                    }
                } else if (tzi.mCountry.equals(timeZoneInfo.mCountry)) {
                    return idx;
                }
            }
            ++idx;
        }
        return -1;
    }

    private void printTz() {
        for (TimeZoneInfo tz : mTimeZones) {
            Log.e(TAG, "" + tz.toString());
        }

        Log.e(TAG, "Total number of tz's = " + mTimeZones.size());
    }

    // void checkForNameDups(TimeZone tz, String country, boolean dls, int
    // style, int idx,
    // boolean print) {
    // if (country == null) {
    // return;
    // }
    // String displayName = tz.getDisplayName(dls, style);
    //
    // if (print) {
    // Log.e(TAG, "" + idx + " " + tz.getID() + " " + country + " ## " +
    // displayName);
    // }
    //
    // if (tz.useDaylightTime()) {
    // if (displayName.matches("GMT[+-][0-9][0-9]:[0-9][0-9]")) {
    // return;
    // }
    //
    // if (displayName.length() == 3 && displayName.charAt(2) == 'T' &&
    // (displayName.charAt(1) == 'S' || displayName.charAt(1) == 'D')) {
    // displayName = "" + displayName.charAt(0) + 'T';
    // } else {
    // displayName = displayName.replace(" Daylight ",
    // " ").replace(" Standard ", " ");
    // }
    // }
    //
    // String tzNameWithCountry = country + " ## " + displayName;
    // Integer groupId = mCountryPlusTzName2Tzs.get(tzNameWithCountry);
    // if (groupId == null) {
    // mCountryPlusTzName2Tzs.put(tzNameWithCountry, idx);
    // } else if (groupId != idx) {
    // Log.e(TAG, "Yikes: " + tzNameWithCountry + " matches " + groupId +
    // " and " + idx);
    // }
    // }

}
