package com.meeba.google.util;

import com.meeba.google.objects.Event;

import org.joda.time.DateTime;

import java.util.Comparator;

/**
 * Created by Eidan on 1/8/14.
 */
public class EventComparator implements Comparator<Event> {
    @Override
    public int compare(Event event1, Event event2) {
        DateTime dt1 = Utils.parseDate(event1.getFormmatedWhen());
        DateTime dt2 = Utils.parseDate(event2.getFormmatedWhen());
        if (dt1 == null || dt2 == null) {
            return 0;
        }

        if (dt1.isBefore(dt2))
            return 1;

        else if (dt1.isAfter(dt2))
            return -1;

        else
            return 0;
    }
}
