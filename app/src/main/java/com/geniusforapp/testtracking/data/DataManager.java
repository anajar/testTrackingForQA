package com.geniusforapp.testtracking.data;

import com.geniusforapp.testtracking.data.db.DbHelper;
import com.geniusforapp.testtracking.data.fb.FireBaseHelper;
import com.geniusforapp.testtracking.data.network.ApiHelper;
import com.geniusforapp.testtracking.data.prefs.AppPrefs;


/**
 * @name TestTracking
 * Copyrights (c) 11/10/17 Created By Ahmad Najar
 **/

public interface DataManager extends AppPrefs, FireBaseHelper, DbHelper, ApiHelper {
}
