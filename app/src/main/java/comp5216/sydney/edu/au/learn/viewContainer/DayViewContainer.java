package comp5216.sydney.edu.au.learn.viewContainer;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.kizitonwose.calendar.view.ViewContainer;

import comp5216.sydney.edu.au.learn.R;

public class DayViewContainer extends ViewContainer {
    public TextView textView;
    public DayViewContainer(@NonNull View view) {
        super(view);
        textView = view.findViewById(R.id.calendarDayText);
    }
}
