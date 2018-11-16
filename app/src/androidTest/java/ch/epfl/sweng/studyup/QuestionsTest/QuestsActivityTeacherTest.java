package ch.epfl.sweng.studyup.QuestionsTest;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.studyup.R;
import ch.epfl.sweng.studyup.firebase.Firestore;
import ch.epfl.sweng.studyup.player.Player;
import ch.epfl.sweng.studyup.questions.Question;
import ch.epfl.sweng.studyup.questions.QuestionParser;
import ch.epfl.sweng.studyup.teacher.QuestsActivityTeacher;
import ch.epfl.sweng.studyup.utils.Constants;
import ch.epfl.sweng.studyup.utils.Utils;
import okhttp3.internal.Util;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sweng.studyup.utils.Constants.*;
import static ch.epfl.sweng.studyup.utils.GlobalAccessVariables.*;
import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class QuestsActivityTeacherTest {
    @Rule
    public final ActivityTestRule<QuestsActivityTeacher> rule =
            new ActivityTestRule<>(QuestsActivityTeacher.class, true, false);
    private final String TAG = QuestsActivityTeacher.class.getSimpleName();
    private Question q;
    private  final String fakeTitle = "fake title";

    @BeforeClass
    public static void enableMock() {
        Player.get().initializeDefaultPlayerData();
        Player.get().setRole(Role.teacher);
    }

    @Before
    public void addQuestionThatWillBeDisplayed() {
        q = new Question(MOCK_UUID, fakeTitle, true, 0, Course.SWENG.name());
        Firestore.get().addQuestion(q);
        Utils.waitAndTag(500, TAG);
        rule.launchActivity(new Intent());
        Utils.waitAndTag(100, TAG);
    }

    @After
    public void deleteQuestions() {
        LiveData<List<Question>> parsedList = QuestionParser.parseQuestionsLiveData(rule.getActivity().getApplicationContext());
        assertNotNull(parsedList);
        parsedList.observe(rule.getActivity(), new Observer<List<Question>>() {
            @Override
            public void onChanged(@Nullable List<Question> questions) {
                if (!questions.isEmpty()) {
                    for(Question q : questions) {
                        Firestore.get().deleteQuestion(q.getQuestionId());
                    }
                }
            }
        });
    }


    //Test must be changed when changing the function called when clicking on a question
    public void listViewRedirectOnCorrectQuestion() {
        final ListView list = rule.getActivity().findViewById(R.id.listViewQuests);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < list.getAdapter().getCount(); ++i) {
                    if (list.getAdapter().getItem(i).toString().equals(fakeTitle))
                        list.performItemClick(list.getAdapter().getView(i, null, list), 0, 0);
                }
            }
        });
        String intentLaunchedTitle = rule.getActivity().getIntent().getStringExtra(FB_QUESTION_TITLE);
        int intentLaunchedAnswer = Integer.parseInt(rule.getActivity().getIntent().getStringExtra(FB_QUESTION_ANSWER));
        boolean intentLaunchedTrueOrFalse = Boolean.parseBoolean(rule.getActivity().getIntent().getStringExtra(FB_QUESTION_TRUEFALSE));

        assert (q.getTitle().equals(intentLaunchedTitle));
        assert (q.getAnswer() == intentLaunchedAnswer);
        assert (q.isTrueFalse() == intentLaunchedTrueOrFalse);
    }

    @Test
    public void shouldHaveAtLeastOneQuestion() {
        final ListView list = rule.getActivity().findViewById(R.id.listViewQuests);
        assert (1 <= list.getAdapter().getCount());
    }

    @Test
    public void canTryDeletingQuestionAndCancel() {
        final List<Integer> idsToDelete = new ArrayList<>();
        final ListView list = rule.getActivity().findViewById(R.id.listViewQuests);
        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < list.getAdapter().getCount(); ++i) {
                    if (list.getAdapter().getItem(i).toString().equals("fake title")) {
                        Log.i("ThatsAReal", list.getAdapter().getItem(i).toString() + " gives " + list.getAdapter().getItem(i).toString().equals("fake title"));
                        idsToDelete.add(i);
                    }
                    Log.i("ThatsAReal", idsToDelete.toString());

                }
            }
        });

        for(Integer i : idsToDelete) {
            /*
                Other workaround, just in case
                onView(allOf(
                    withId(R.id.delete_question),
                    nthChildsDescendant(withId(R.id.listViewQuests), 1)))
                    .perform(click());*/
            onData(anything()).inAdapterView(withId(R.id.listViewQuests))
                    .atPosition(i)
                    .onChildView(withId(R.id.delete_question))
                    .perform(click());
            Log.i("555", "555");
            Utils.waitAndTag(50, TAG);
            onView(withText(R.string.yes_upper)).inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());
            Utils.waitAndTag(600, TAG);
        }

        Firestore.get().loadQuestions(rule.getActivity());
        LiveData<List<Question>> parsedList = QuestionParser.parseQuestionsLiveData(rule.getActivity().getApplicationContext());
        assertNotNull(parsedList);
        parsedList.observe(rule.getActivity(), new Observer<List<Question>>() {
            @Override
            public void onChanged(@Nullable List<Question> questions) {
                if (!questions.isEmpty()) {
                    for(Question q : questions) {
                        assertFalse(q.getTitle().equals("fake title"));
                    }
                }
            }
        });
    }

    // Method not used, can be useful so keep it just in case. Credit: https://stackoverflow.com/questions/32823508/how-can-i-click-on-a-view-in-listview-specific-row-position#
    private static Matcher<View> nthChildsDescendant(final Matcher<View> parentMatcher, final int childPosition) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with " + childPosition + " child view of type parentMatcher");
            }

            @Override
            public boolean matchesSafely(View view) {

                while(view.getParent() != null) {
                    if(parentMatcher.matches(view.getParent())) {
                        return view.equals(((ViewGroup) view.getParent()).getChildAt(childPosition));
                    }
                    view = (View) view.getParent();
                }

                return false;
            }
        };
    }
}