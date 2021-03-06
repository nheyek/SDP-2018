package ch.epfl.sweng.studyup;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import ch.epfl.sweng.studyup.specialQuest.SpecialQuest;
import ch.epfl.sweng.studyup.specialQuest.SpecialQuestType;
import ch.epfl.sweng.studyup.utils.GlobalAccessVariables;

import static org.junit.Assert.assertFalse;


@RunWith(JUnit4.class)
public class SpecialQuestTest {

    @BeforeClass
    public static void init() {
        GlobalAccessVariables.MOCK_ENABLED = true;
    }

    @AfterClass
    public static void clean(){
        GlobalAccessVariables.MOCK_ENABLED = false;
    }

    @Test
    public void initTest() {
        SpecialQuest specialQuest = new SpecialQuest(SpecialQuestType.THREE_QUESTIONS);

        assert(specialQuest.getSpecialQuestType().equals(SpecialQuestType.THREE_QUESTIONS));
        assert(specialQuest.getReward() != null);
        assert(specialQuest.getProgress() == 0);
        assert(specialQuest.getCompletionCount() == 0);
    }

    @Test
    public void completionUpdateTest() {
        SpecialQuest specialQuest = new SpecialQuest(SpecialQuestType.FIVE_QUESTIONS);

        specialQuest.setCompletionCount(5);
        assert(specialQuest.getCompletionCount() == 5);
        assert(specialQuest.getProgress() == 1);
    }

    @Test
    public void testEqualityOverloadValid() {
        SpecialQuest specialQuestA = new SpecialQuest(SpecialQuestType.CONSISTENT_USE);
        SpecialQuest specialQuestB = new SpecialQuest(SpecialQuestType.CONSISTENT_USE);
        assert(specialQuestA.equals(specialQuestB));
    }

    @Test
    public void testEqualityOverloadInvalid() {
        SpecialQuest specialQuestA = new SpecialQuest(SpecialQuestType.LEVEL_UP_BONUS);
        SpecialQuest specialQuestB = new SpecialQuest(SpecialQuestType.CREATIVE_USERNAME);
        assertFalse(specialQuestA.equals(specialQuestB));
    }
}
