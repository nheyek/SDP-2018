package ch.epfl.sweng.studyup;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.studyup.map.MapActivity;
import ch.epfl.sweng.studyup.utils.Utils;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class MapsActivityTest {
    private final double LAT = 35.323;
    private final double LONG = 56.43;
    private LatLng latlng;
    private MapActivity mActivity;

    @Rule public final ActivityTestRule<MapActivity> mActivityRule = new ActivityTestRule<>(MapActivity.class);

    @Before
    public void init() {
        latlng = new LatLng(LAT, LONG);
        mActivity = mActivityRule.getActivity();
    }

    @Test
    public void locationRequestSetsUpCorrectly() {
        assertEquals(Utils.LOCATION_REQ_INTERVAL, mActivity.getIntervals());
        assertEquals(Utils.LOCATION_REQ_FASTEST_INTERVAL, mActivity.getFastedIntervals());
        assertEquals(LocationRequest.PRIORITY_HIGH_ACCURACY, mActivity.getPriority());
    }

    @Test
    public void onLocationUpdateChangesUtilsPos() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActivity.onLocationUpdate(latlng);
                assertEquals(LAT, Utils.position.latitude, 0.0);
                assertEquals(LONG, Utils.position.longitude, 0.0);
            }
        });
    }

}
