package ch.epfl.sweng.studyup.questions;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import ch.epfl.sweng.studyup.R;

import static ch.epfl.sweng.studyup.utils.GlobalAccessVariables.MOST_RECENT_ACTIVITY;

@Database(entities = {Question.class}, version = 1, exportSchema = false)
public abstract class QuestionDatabase extends RoomDatabase {

    public abstract QuestionDAO questionDAO();

    private static final String databaseName = MOST_RECENT_ACTIVITY.getString(R.string.title_questiondb);
    private static QuestionDatabase instance = null;

    public static QuestionDatabase get(Context c){
        if (instance == null){
            instance = Room.databaseBuilder(c, QuestionDatabase.class, databaseName).build();
            return instance;
        }else{
            return instance;
        }
    }
}
