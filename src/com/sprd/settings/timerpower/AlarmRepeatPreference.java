package com.sprd.settings.timerpower;

import android.content.Context;
import androidx.preference.MultiSelectListPreference;
import android.util.AttributeSet;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class AlarmRepeatPreference extends MultiSelectListPreference {

    // Initial value that can be set with the values saved in the database.
    private Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);

    private String[] mWeekdays = new DateFormatSymbols().getWeekdays();
    private String[] mEntryValues = new String[] {
            mWeekdays[Calendar.MONDAY],
            mWeekdays[Calendar.TUESDAY],
            mWeekdays[Calendar.WEDNESDAY],
            mWeekdays[Calendar.THURSDAY],
            mWeekdays[Calendar.FRIDAY],
            mWeekdays[Calendar.SATURDAY],
            mWeekdays[Calendar.SUNDAY],
    };

    public AlarmRepeatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEntries(mEntryValues);
        setEntryValues(mEntryValues);
    }

    public void setDaysOfWeek(Alarm.DaysOfWeek dow) {
        mDaysOfWeek.set(dow);
        setSummary(dow.toString(getContext(), true));
        setSelectedItems(mDaysOfWeek.getBooleanArray());
    }

    public Alarm.DaysOfWeek getDaysOfWeek(Set<String> values) {
        Alarm.DaysOfWeek dow = new Alarm.DaysOfWeek(0);
        final int entryCount = mEntryValues.length;
        boolean[] result = new boolean[entryCount];

        for (int i = 0; i < entryCount; i++) {
            result[i] = values.contains(mEntryValues[i].toString());
            dow.set(i, result[i]);
        }
        return dow;
    }

    private void setSelectedItems(boolean[] items) {
        Set<String> values = new HashSet<>();
        for (int index = 0; index < items.length; index++) {
            if (items[index]) {
                values.add(mEntryValues[index]);
            }
        }
        setValues(values);
    }
}
