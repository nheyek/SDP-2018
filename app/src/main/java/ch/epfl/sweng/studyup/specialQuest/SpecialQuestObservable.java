package ch.epfl.sweng.studyup.specialQuest;

import android.content.Context;

import ch.epfl.sweng.studyup.utils.Constants;

public interface SpecialQuestObservable {

    void notifySpecialQuestObservers(Constants.SpecialQuestUpdateFlag updateFlag);
}
