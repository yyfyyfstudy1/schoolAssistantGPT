package comp5216.sydney.edu.au.learn.fragment;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import comp5216.sydney.edu.au.learn.R;
import comp5216.sydney.edu.au.learn.util.FireBaseUtil;
import comp5216.sydney.edu.au.learn.viewAdapter.DateListAdapter;

public class TimeTableFragment extends Fragment{
    private View rootView;
    private RecyclerView dateListRecyclerView;
    private DateListAdapter dateListAdapter;
    private MaterialButton addEventBtn;
    private String userId;
    private  CalendarView calendarView;

    ArrayList<String> todayPlanList;
    private String selectTimestamp;

    private MaterialButton composeTimeTableBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_timetable, container, false);
        Bundle arguments = getArguments();
        if (arguments != null) {
            userId = arguments.getString("userId");
            Log.d(TAG, "onCreateView: "+ userId);
        }
        return rootView;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        dateListRecyclerView = view.findViewById(R.id.dateListRecyclerView);
        addEventBtn = view.findViewById(R.id.addEventBtn);
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        composeTimeTableBtn = view.findViewById(R.id.composeTimeTableBtn);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        dateListRecyclerView.setLayoutManager(layoutManager);

        todayPlanList = new ArrayList<>();

        // create and set the adapter
        dateListAdapter = new DateListAdapter(getContext(),todayPlanList);
        dateListRecyclerView.setAdapter(dateListAdapter);

        // render the calendar time by database
        renderMarkDate();

        addEventBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        composeTimeTableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // dump to general gpt answer fragment
                TimeTableResponseFragment timeTableResponseFragment = new TimeTableResponseFragment();
                Bundle args = new Bundle();
                args.putString("userId", userId);
                timeTableResponseFragment.setArguments(args);
                // change fragment
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fl_container, timeTableResponseFragment);
                // Add FragmentA to the back stack so the user can return to it
                transaction.addToBackStack(null);
                // Allowing State Loss
                transaction.commitAllowingStateLoss();
            }
        });

    }


    private void renderMarkDate(){
        FireBaseUtil.getAllMarkedDate(userId, new FireBaseUtil.FirebaseCallback() {
            @Override
            public void onCallback(List<Long> timestamps) {
                bindCalendarData(timestamps);
            }
        });
    }

    private void bindCalendarData(List<Long> timestamps){
        List<EventDay> events = new ArrayList<>();
        List<Calendar> calendars = new ArrayList<>();

        for (Long timestamp : timestamps) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timestamp);
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.set(year, month, day);
            calendars.add(calendar);
            events.add(new EventDay(calendar, R.drawable.ic_baseline_star_24));
        }


        calendarView.setEvents(events);
        calendarView.setHighlightedDays(calendars);

        calendarView.setOnDayClickListener(new OnDayClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDayClick(EventDay eventDay) {

                // clear the arraylist
                todayPlanList.clear();
                Calendar clickedDayCalendar = eventDay.getCalendar();
                long timestamp = clickedDayCalendar.getTimeInMillis();
                System.out.println(timestamp);

                // set the global select day
                selectTimestamp = String.valueOf(timestamp);

                // get the plan for the selected day
                getTimeSchedule(String.valueOf(timestamp));

            }
        });

    }

    // get the schedule for the select day
    private void getTimeSchedule(String timestamp){
        FireBaseUtil.getTimeScheduleData(userId, timestamp, new FireBaseUtil.FirebaseListCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onCallback(List<String> dataList) {
                if (dataList!=null){
                    todayPlanList.addAll(dataList);
                    // Notify the adapter to refresh the RecyclerView
                }
                dateListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void showInputDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getContext()));
        builder.setTitle("Enter event");

        // Create a layout for the EditText
        TextInputLayout inputLayout = new TextInputLayout(getContext());
        TextInputEditText editText = new TextInputEditText(getContext());
        inputLayout.addView(editText);

        builder.setView(inputLayout);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredValue = editText.getText().toString();
                if (enteredValue.trim().isEmpty()) {
                    // If input is empty, set error on TextInputLayout
                    inputLayout.setError("Input cannot be empty");
                    return; // Important: do not dismiss the dialog
                } else {
                    inputLayout.setError(null); // Clear any previous error
                }

                // Handle the entered value
                Log.d("InputValue", enteredValue);

                todayPlanList.add(enteredValue);

                // update the list to firebase database
                FireBaseUtil.updateOrInsertTimestamp(userId, selectTimestamp, todayPlanList, new FireBaseUtil.FirebaseUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        // Handle success
                        Snackbar.make(rootView, "Add new plan successful", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        renderMarkDate();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle failure
                    }
                });


                dateListAdapter.notifyDataSetChanged();

                dialog.dismiss(); // Close the dialog if input is valid
            }
        });
        builder.setNegativeButton("Cancel", null);

        builder.show();
    }





}
