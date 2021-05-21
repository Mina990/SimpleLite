package com.creativetrends.simple.app.utils;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.TimeZone;

public class EUCheck {

    public static boolean isEU(Context context) {
        boolean error = false;


        String TAG = "EU checker";
        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2) {
                simCountry = simCountry.toUpperCase();

                if (EUCountry.contains(simCountry)) {
                    Log.v(TAG, "is EU User (sim)");
                    return true;
                }
            }
        } catch (Exception e) {
            error = true;
        }


        try {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA && tm.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE) {
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2) {
                    networkCountry = networkCountry.toUpperCase();

                    if (EUCountry.contains(networkCountry)) {
                        Log.v(TAG, "is EU User (network)");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            error = true;
        }

        try {
            String tz = TimeZone.getDefault().getID().toLowerCase();
            if (tz.length() < 10) {
                error = true;
            } else if (tz.contains("euro")) {
                Log.v(TAG, "is EU User (time)");
                return true;
            }
        } catch (Exception e) {
            error = true;
        }


        if (error) {
            Log.v(TAG, "is EU User (err)");
            return true;
        }

        return false;
    }



    private enum EUCountry {
        AT, BE, BG, HR, CY, CZ, DK, EE, FI, FR, DE, GR, HU, IE, IT, LV, LT, LU, MT, NL, PL, PT, RO, SK, SI, ES, SE, GB,
        GF, PF, TF,
        EL, UK,
        ME, IS, AL, RS, TR, MK;

        public static boolean contains(String s) {
            for (EUCountry eucountry : values())
                if (eucountry.name().equalsIgnoreCase(s))
                    return true;
            return false;
        }

    }
}
