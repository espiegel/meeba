package com.meeba.google;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

/**
 * Created by Eidan on 12/6/13.
 */
@ReportsCrashes(
        formKey = "", // will not be used
        mailTo = "meeba-dev@googlegroups.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text
)
public class Acra extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // The following line triggers the initialization of ACRA
        ACRA.init(this);
    }
}
