/*
 * *
 *  * Created by Jorell Rutledge on 5/23/19 10:45 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 5/23/19 10:45 AM
 *
 */

package com.creativetrends.simple.app.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class SimpleBarService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new SimpleBarViewFactory(getApplicationContext());
    }
}
