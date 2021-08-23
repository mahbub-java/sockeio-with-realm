package com.example.socketio.Utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StaticAccess {

    // show multiple item on spinner
    public static void showMultipleItemSpinner(Context context, Spinner spinner, Set<String> list) {

        if (spinner != null && list.size() > 0) {
            Object[] array = list.toArray();
            ArrayAdapter<String> spinnerArrayAdapter = null;
            spinnerArrayAdapter = new ArrayAdapter<String>
                    (context, android.R.layout.simple_spinner_item,
                            Arrays.copyOf(array, array.length, String[].class));

            spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                    .simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);
        }
    }

    //some date operation

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
