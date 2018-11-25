package ch.epfl.sweng.studyup.SettingsTest;

import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.studyup.R;
import ch.epfl.sweng.studyup.player.HomeActivity;
import ch.epfl.sweng.studyup.settings.ChooseColorActivity;
import ch.epfl.sweng.studyup.settings.SettingsActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ChooseColorActivityTest {

    @Rule
    public final ActivityTestRule<ChooseColorActivity> rule =
            new ActivityTestRule<>(ChooseColorActivity.class);

    @Before
    public void initIntent() {
        Intents.init();
    }

    @After
    public void releaseIntent() {
        Intents.release();
    }

    @Test
    public void canSelectThemeRed() {
        onView(ViewMatchers.withId(R.id.setThemeRed)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void canSelectThemeGreen() {
        onView(withId(R.id.setThemeGreen)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void canSelectThemeBlue() {
        onView(withId(R.id.setThemeBlue)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void canSelectThemeBrown() {
        onView(withId(R.id.setThemeBrown)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }

    @Test
    public void canSelectThemeMulti() {
        onView(withId(R.id.setThemeMulti)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }
    
    @Test
    public void canGoBack() {
        onView(withId(R.id.back_button)).perform(click());
        intended(hasComponent(SettingsActivity.class.getName()));
    }
}