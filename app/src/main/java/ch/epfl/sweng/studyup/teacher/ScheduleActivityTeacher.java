package ch.epfl.sweng.studyup.teacher;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ch.epfl.sweng.studyup.R;
import ch.epfl.sweng.studyup.firebase.Firestore;
import ch.epfl.sweng.studyup.player.Player;
import ch.epfl.sweng.studyup.utils.Constants;
import ch.epfl.sweng.studyup.utils.Rooms;
import ch.epfl.sweng.studyup.utils.Utils;
import ch.epfl.sweng.studyup.utils.navigation.NavigationTeacher;

import static ch.epfl.sweng.studyup.utils.Constants.MONTH_OF_SCHEDULE;
import static ch.epfl.sweng.studyup.utils.Constants.WEEK_OF_MONTH_SCHEDULE;
import static ch.epfl.sweng.studyup.utils.Constants.YEAR_OF_SCHEDULE;
import static ch.epfl.sweng.studyup.utils.GlobalAccessVariables.MOCK_ENABLED;
import static ch.epfl.sweng.studyup.utils.Utils.tooRecentAPI;

public class ScheduleActivityTeacher extends NavigationTeacher {
    private List<WeekViewEvent> weekViewEvents;
    private WeekView weekView;
    private int id = 0;
    private String courseName;
    public static final String COURSE_NAME_INTENT_SCHEDULE = "CourseName";

    private final MonthLoader.MonthChangeListener monthChangeListener = new MonthLoader.MonthChangeListener() {
        @Override
        public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
            if(newMonth == MONTH_OF_SCHEDULE + 1 && newYear == YEAR_OF_SCHEDULE) {
                return weekViewEvents;
            }else{
                return new ArrayList<>();
            }
        }
    };

    private final WeekView.EventClickListener eventClickListener = new WeekView.EventClickListener() {
        @Override
        public void onEventClick(WeekViewEvent event, RectF eventRect) {
            Log.d("ScheduleActivityTeacher", "Clicked on event with id " + event.getId());
            weekViewEvents.remove(event);
            weekView.notifyDatasetChanged();
        }
    };


    private final WeekView.EmptyViewClickListener emptyViewClickListener = new WeekView.EmptyViewClickListener() {
        @Override
        public void onEmptyViewClicked(Calendar time) {
            Log.d("ScheduleTeacherLog", "Clicked empty event");
            Log.d("ScheduleTeacherLog", "time = " + time.toString());
            Log.d("ScheduleTeacherLog", "Day of week = " + time.get(Calendar.DAY_OF_WEEK));
            Log.d("ScheduleTeacherLog", "Hour = " + time.get(Calendar.HOUR_OF_DAY));
            int dayOfWeek = time.get(Calendar.DAY_OF_WEEK);
            int hour = time.get(Calendar.HOUR_OF_DAY);

            Calendar eventStart = Calendar.getInstance();
            eventStart.set(Calendar.YEAR, YEAR_OF_SCHEDULE);
            eventStart.set(Calendar.MONTH, MONTH_OF_SCHEDULE);
            eventStart.set(Calendar.WEEK_OF_MONTH, WEEK_OF_MONTH_SCHEDULE);
            eventStart.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            eventStart.set(Calendar.HOUR_OF_DAY, hour);
            eventStart.set(Calendar.MINUTE, 0);

            Calendar eventEnd = Calendar.getInstance();
            eventEnd.setTimeInMillis(eventStart.getTimeInMillis() + 59 * 60000);

            launchRoomSelectionDialog(new WeekViewEvent(id % 100 + 100 * Constants.Course.valueOf(courseName).ordinal(), courseName, "", eventStart, eventEnd));
        }
    };

    private void launchRoomSelectionDialog(final WeekViewEvent e) {
        final AlertDialog.Builder roomChoiceDialog = new AlertDialog.Builder(this);
        roomChoiceDialog.setTitle(R.string.room_alert_title);
        final String[] roomsAsArray = MOCK_ENABLED ?
                new String[]{"CE_1_1"}
                :
                (new ArrayList<>(Rooms.ROOMS_LOCATIONS.keySet())).toArray(new String[0]);

        roomChoiceDialog.setItems(roomsAsArray , new DialogInterface.OnClickListener() {
            @SuppressWarnings("HardCodedStringLiteral")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                e.setLocation(roomsAsArray[which]);
                weekViewEvents.add(e);
                id += 1;
                weekView.notifyDatasetChanged();
            }
        });
        roomChoiceDialog.setNegativeButton(R.string.cancel, null);
        roomChoiceDialog.create().show();
    }

    private final WeekView.EventLongPressListener eventLongPressListener = new WeekView.EventLongPressListener() {
        @Override
        public void onEventLongPress(WeekViewEvent event, RectF eventRect) {}};

    private final DateTimeInterpreter dateTimeInterpreter = new DateTimeInterpreter() {
        @Override
        public String interpretDate(Calendar date) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
                return sdf.format(date.getTime()).toUpperCase();
            } catch (Exception e) {
                e.printStackTrace(); return "";
            }
        }

        @Override
        public String interpretTime(int hour, int minutes) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, 0);

            try {
                SimpleDateFormat sdf = DateFormat.is24HourFormat(getApplicationContext()) ? new SimpleDateFormat("HH:mm", Locale.getDefault()) : new SimpleDateFormat("hh a", Locale.getDefault());
                return sdf.format(calendar.getTime());
            } catch (Exception e) {
                e.printStackTrace(); return "";
            }
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        weekViewEvents = new ArrayList<>();

        if(tooRecentAPI()) {
            setContentView(R.layout.activity_schedule_teacher_api_higher_than_27);
        } else {
            setContentView(R.layout.activity_schedule_teacher);
            weekView = findViewById(R.id.weekView);
            if(MOCK_ENABLED){
                weekView.setNumberOfVisibleDays(1);
            }
            Utils.setupWeekView(weekView, eventLongPressListener, dateTimeInterpreter, monthChangeListener, eventClickListener, emptyViewClickListener);
        }

        courseName = getIntent().getStringExtra(COURSE_NAME_INTENT_SCHEDULE);
        ((TextView) findViewById(R.id.course_text_schedule_teacher)).setText(courseName);

        Firestore.get().getCoursesSchedule(this, Player.get().getRole());
    }



    public void updateSchedule(List<WeekViewEvent> events){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) return;
        List<WeekViewEvent> localEvents = new ArrayList<>(events);

        // Filter of the events, to keep only the wanted course
        for(WeekViewEvent e : events) {
            if(!e.getName().startsWith(courseName)) localEvents.remove(e);
        }

        weekViewEvents.clear();
        weekViewEvents.addAll(localEvents);
        id += localEvents.size();
        weekView.notifyDatasetChanged();
    }

    public List<WeekViewEvent> getWeekViewEvents(){
        return new ArrayList<>(weekViewEvents);
    }

    public void onSaveButtonClick(View view){
        Firestore.get().setCourseEvents(Constants.Course.valueOf(courseName), getWeekViewEvents());
        Toast.makeText(this, R.string.schedule_updated_confirmation, Toast.LENGTH_SHORT).show();
    }

    public void onBackButtonScheduleTeacher(View v) {
        finish();
    }
}
