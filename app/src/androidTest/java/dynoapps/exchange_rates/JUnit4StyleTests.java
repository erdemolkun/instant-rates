package dynoapps.exchange_rates;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertNotNull;

/**
 * Created by erdemmac on 16/12/2016.
 */

@RunWith(JUnit4.class)
public class JUnit4StyleTests {
    @ClassRule
    public static final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Rule
    public ActivityTestRule<SplashActivity> activityRule = new ActivityTestRule<>(SplashActivity.class);

    @Test
    public void testTakeScreenshot() {
        onView(withId(R.id.iv_splash)).check(matches(isDisplayed()));

        Screengrab.screenshot("splash_activity");

        // register next activity that need to be monitored.
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(LandingActivity.class.getName(), null, false);
        //Watch for the timeout
        Activity nextActivity = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
        // next activity is opened and captured.
        assertNotNull(nextActivity);

        Screengrab.screenshot("landing_activity");


        onView(withId(R.id.menu_add_alarm))
                .perform(click());

        Screengrab.screenshot("menu_item_alarm");
    }


    @Test
    public void testTakeMoreScreenshots() {
//        onView(withId(R.id.nav_button)).perform(click());
//
//        Screengrab.screenshot("anotherActivity");
//
//        onView(withId(R.id.show_dialog_button)).perform(click());
//
//        Screengrab.screenshot("anotherActivity-dialog");

//        onView(withText(android.R.string.ok)).perform(click());
    }
}