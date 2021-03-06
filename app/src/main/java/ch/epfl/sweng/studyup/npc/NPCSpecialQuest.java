package ch.epfl.sweng.studyup.npc;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;

import ch.epfl.sweng.studyup.specialQuest.SpecialQuest;
import ch.epfl.sweng.studyup.specialQuest.SpecialQuestDisplayActivity;

import static ch.epfl.sweng.studyup.utils.Constants.SPECIAL_QUEST_KEY;

public class NPCSpecialQuest extends NPC {
    private SpecialQuest specialQuest;

    public NPCSpecialQuest(SpecialQuest specialQuest, String name, LatLng latLng, int image, int numberOfMessages) {
        super(name, latLng, image, numberOfMessages);
        this.specialQuest = specialQuest;
    }

    @Override
    public void onYesButton(Activity activity) {
        activity.startActivity(new Intent(activity, SpecialQuestDisplayActivity.class).putExtra(SPECIAL_QUEST_KEY, specialQuest));
    }
}
