package datacollect.ryan.grant.com.datacollect.storage;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPrefHelper {
    private static final String SETTINGS_NAME = "default_settings";
    private static SharedPrefHelper sSharedPrefs;
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private boolean mBulkUpdate = false;

    public static class Key {
        public static final String CURRENT_USER = "user_current";
        public static final String CURRENT_LOCATION = "location_current";
        public static final String IS_USER_SELECTED = "user_is_selected";
        public static final String USERS_HISTORY = "users_history";

    }

    private SharedPrefHelper(Context context) {
        mPref = context.getSharedPreferences(SETTINGS_NAME, Context.MODE_PRIVATE);
    }


    public static SharedPrefHelper getInstance(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new SharedPrefHelper(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    public static SharedPrefHelper getInstance() {
        if (sSharedPrefs != null) {
            return sSharedPrefs;
        }
        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");
    }

    public void put(String key, String val) {
        doEdit();
        mEditor.putString(key, val);
        doCommit();
    }

    public void put(String key, int val) {
        doEdit();
        mEditor.putInt(key, val);
        doCommit();
    }

    public void put(String key, boolean val) {
        doEdit();
        mEditor.putBoolean(key, val);
        doCommit();
    }

    public void put(String key, float val) {
        doEdit();
        mEditor.putFloat(key, val);
        doCommit();
    }

    public void put(String key, double val) {
        doEdit();
        mEditor.putString(key, String.valueOf(val));
        doCommit();
    }

    public void put(String key, long val) {
        doEdit();
        mEditor.putLong(key, val);
        doCommit();
    }

    public void saveUsersHistory(Set<String> history) {
        doEdit();
        mEditor.putStringSet(Key.USERS_HISTORY, history);
        mEditor.commit();
    }

    public Set<String> getUsersHistory() {
        return mPref.getStringSet(Key.USERS_HISTORY, new HashSet<String>());
    }

    public String getString(String key, String defaultValue) {
        return mPref.getString(key, defaultValue);
    }

    public String getString(String key) {
        return mPref.getString(key, null);
    }

    public int getInt(String key) {
        return mPref.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return mPref.getInt(key, defaultValue);
    }

    public long getLong(String key) {
        return mPref.getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return mPref.getLong(key, defaultValue);
    }

    public float getFloat(String key) {
        return mPref.getFloat(key, 0);
    }

    public float getFloat(String key, float defaultValue) {
        return mPref.getFloat(key, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.valueOf(mPref.getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return mPref.getBoolean(key, defaultValue);
    }

    public boolean getBoolean(String key) {
        return mPref.getBoolean(key, false);
    }

    public void remove(String... keys) {
        doEdit();
        for (String key : keys) {
            mEditor.remove(key);
        }
        doCommit();
    }

    public void clear() {
        doEdit();
        mEditor.clear();
        doCommit();
    }

    public void edit() {
        mBulkUpdate = true;
        mEditor = mPref.edit();
    }

    public void commit() {
        mBulkUpdate = false;
        mEditor.commit();
        mEditor = null;
    }

    private void doEdit() {
        if (!mBulkUpdate && mEditor == null) {
            mEditor = mPref.edit();
        }
    }

    private void doCommit() {
        if (!mBulkUpdate && mEditor != null) {
            mEditor.commit();
            mEditor = null;
        }
    }

    public void setCurrentUser(String currentUser) {
        sSharedPrefs.put(Key.CURRENT_USER, currentUser);
    }

    public String getCurrentUser() {
        return sSharedPrefs.getString(Key.CURRENT_USER, "null");
    }

    public void setCurrentLocation(String currentLocation) {
        sSharedPrefs.put(Key.CURRENT_LOCATION, currentLocation);
    }

    public String getCurrentLocation() {
        return sSharedPrefs.getString(Key.CURRENT_LOCATION, "");
    }


}
