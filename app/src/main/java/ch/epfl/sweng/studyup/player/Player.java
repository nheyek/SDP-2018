package ch.epfl.sweng.studyup.player;

import android.app.Activity;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.epfl.sweng.studyup.firebase.Firestore;
import ch.epfl.sweng.studyup.items.Items;
import ch.epfl.sweng.studyup.npc.NPC;
import ch.epfl.sweng.studyup.specialQuest.SpecialQuest;
import ch.epfl.sweng.studyup.specialQuest.SpecialQuestObservable;
import ch.epfl.sweng.studyup.specialQuest.SpecialQuestObserver;
import ch.epfl.sweng.studyup.specialQuest.SpecialQuestType;
import ch.epfl.sweng.studyup.utils.Constants;

import static ch.epfl.sweng.studyup.utils.Constants.CURRENCY_PER_LEVEL;
import static ch.epfl.sweng.studyup.utils.Constants.Course;
import static ch.epfl.sweng.studyup.utils.Constants.FB_ANSWERED_QUESTIONS;
import static ch.epfl.sweng.studyup.utils.Constants.FB_COURSES_ENROLLED;
import static ch.epfl.sweng.studyup.utils.Constants.FB_COURSES_TEACHED;
import static ch.epfl.sweng.studyup.utils.Constants.FB_CURRENCY;
import static ch.epfl.sweng.studyup.utils.Constants.FB_ITEMS;
import static ch.epfl.sweng.studyup.utils.Constants.FB_KNOWN_NPCS;
import static ch.epfl.sweng.studyup.utils.Constants.FB_UNLOCKED_THEME;
import static ch.epfl.sweng.studyup.utils.Constants.FB_LEVEL;
import static ch.epfl.sweng.studyup.utils.Constants.FB_QUESTION_CLICKEDINSTANT;
import static ch.epfl.sweng.studyup.utils.Constants.FB_SPECIALQUESTS;
import static ch.epfl.sweng.studyup.utils.Constants.FB_USERNAME;
import static ch.epfl.sweng.studyup.utils.Constants.FB_USERS;
import static ch.epfl.sweng.studyup.utils.Constants.FB_XP;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_CURRENCY;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_FIRSTNAME;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_LASTNAME;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_LEVEL;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_SCIPER;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_USERNAME;
import static ch.epfl.sweng.studyup.utils.Constants.INITIAL_XP;
import static ch.epfl.sweng.studyup.utils.Constants.Role;
import static ch.epfl.sweng.studyup.utils.Constants.SUPER_USERS;
import static ch.epfl.sweng.studyup.utils.Constants.XP_TO_LEVEL_UP;
import static ch.epfl.sweng.studyup.utils.Rooms.ROOMS_LOCATIONS;
import static ch.epfl.sweng.studyup.utils.Utils.getCourseListFromStringList;
import static ch.epfl.sweng.studyup.utils.Utils.getItemsFromString;
import static ch.epfl.sweng.studyup.utils.Utils.getOrDefault;
import static ch.epfl.sweng.studyup.utils.Utils.getSpecialQuestListFromMapList;

/**
 * Player
 * <p>
 * Used to store the Player's state and informations.
 */
public class Player implements SpecialQuestObservable {

    private static final String TAG = Player.class.getSimpleName();

    private static Player instance = null;

    // Basic biographical data
    private String sciperNum;
    private String firstName;
    private String lastName;

    private String username;
    private Role role = null;

    // Game-related data
    private int experience;
    private int level;
    private int currency;
    private Map<String, List<String>> answeredQuestions;
    private List<Items> items;
    private Map<String, Long> clickedInstants;

    private List<Course> coursesEnrolled;
    private List<Course> coursesTeached;
    private List<WeekViewEvent> scheduleStudent;
    private Set<String> knownNPCs;
    private List<SpecialQuest> specialQuests;
    private Set<String> unlockedThemes;

    private Player() {
        sciperNum = INITIAL_SCIPER;
        firstName = INITIAL_FIRSTNAME;
        lastName = INITIAL_LASTNAME;
        experience = INITIAL_XP;
        currency = INITIAL_CURRENCY;
        level = INITIAL_LEVEL;
        username = INITIAL_USERNAME;
        answeredQuestions = new HashMap<>();
        items = new ArrayList<>();
        specialQuests = new ArrayList<>();
        // By default every player has a "three questions" special quest
        specialQuests.add(new SpecialQuest(SpecialQuestType.THREE_QUESTIONS));
        coursesEnrolled = new ArrayList<>();
        coursesTeached = new ArrayList<>();
        coursesEnrolled.add(Course.SWENG);
        scheduleStudent = new ArrayList<>();
        clickedInstants = new HashMap<>();
        knownNPCs = new HashSet<>();
        unlockedThemes = new HashSet<>();
    }

    public static Player get() {
        if (instance == null) {
            instance = new Player();
        }
        return instance;
    }

    /**
     * User for testing purposes.
     * Clear data from current Player instance.
     */
    public void resetPlayer() {
        experience = INITIAL_XP;
        currency = INITIAL_CURRENCY;
        level = INITIAL_LEVEL;
        username = INITIAL_USERNAME;
        answeredQuestions = new HashMap<>();
        items = new ArrayList<>();
        coursesEnrolled = new ArrayList<>();
        coursesEnrolled.add(Course.SWENG);
        coursesTeached = new ArrayList<>();
        scheduleStudent = new ArrayList<>();
        clickedInstants = new HashMap<>();
        knownNPCs = new HashSet<>();
        unlockedThemes = new HashSet<>();
    }

    /**
     * Populates the player class with persisted data.
     * This method is called from FireStore.loadPlayerData(), which is called
     * in AuthenticationActivity.
     */
    @SuppressWarnings("unchecked")
    public void updateLocalDataFromRemote(Map<String, Object> remotePlayerData) {

        if (remotePlayerData.isEmpty()) {
            Log.e(TAG,"Unable to retrieve player data from Firebase.");
            return;
        }

        username = getOrDefault(remotePlayerData, FB_USERNAME, INITIAL_USERNAME).toString();
        experience = Integer.parseInt(getOrDefault(remotePlayerData, FB_XP, INITIAL_XP).toString());
        currency = Integer.parseInt(getOrDefault(remotePlayerData, FB_CURRENCY, INITIAL_CURRENCY).toString());
        level = Integer.parseInt(getOrDefault(remotePlayerData, FB_LEVEL, INITIAL_LEVEL).toString());
        items = getItemsFromString((List<String>) getOrDefault(remotePlayerData, FB_ITEMS, new ArrayList<String>()));

        List<SpecialQuest> remoteSpecialQuests =
                getSpecialQuestListFromMapList((List<Map<String, String>>) remotePlayerData.get(FB_SPECIALQUESTS));
        if (remoteSpecialQuests != null) {
            /*
            If special quests data in firebase, populate Player specialQuests with this data.
            Otherwise, leave Player specialQuests as is (the default).
             */
            this.specialQuests = remoteSpecialQuests;
        }

        List<String> defaultCourseListEnrolled = new ArrayList<>();
        defaultCourseListEnrolled.add(Course.SWENG.name());
        coursesEnrolled = getCourseListFromStringList((List<String>) getOrDefault(remotePlayerData, FB_COURSES_ENROLLED, defaultCourseListEnrolled));

        List<String> defaultCourseListTeached = new ArrayList<>();
        coursesTeached = getCourseListFromStringList((List<String>) getOrDefault(remotePlayerData, FB_COURSES_TEACHED, defaultCourseListTeached));

        answeredQuestions = (Map<String, List<String>>) getOrDefault(remotePlayerData, FB_ANSWERED_QUESTIONS, new HashMap<String, List<String>>());
        clickedInstants = (Map<String, Long>) getOrDefault(remotePlayerData, FB_QUESTION_CLICKEDINSTANT, new HashMap<String, Long>());
        knownNPCs = new HashSet<>((List<String>) getOrDefault(remotePlayerData, FB_KNOWN_NPCS, new ArrayList<>()));
        unlockedThemes = new HashSet<>((List<String>) getOrDefault(remotePlayerData, FB_UNLOCKED_THEME, new ArrayList<>()));
        Firestore.get().getCoursesSchedule(null, Role.student);

        Log.d(TAG, "Loaded courses: \n");
        Log.d(TAG, "Enrolled: "+coursesEnrolled.toString()+"\n");
        Log.d(TAG, "Teached: "+coursesTeached.toString()+"\n");
    }

    // Getters
    public String getSciperNum() { return this.sciperNum; }
    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public Role getRole() { return this.role; }
    public String getUserName() { return this.username; }
    public int getExperience() { return this.experience; }
    public int getLevel() { return this.level; }
    public Set<String> getUnlockedThemes () {
        return Collections.unmodifiableSet(new HashSet<>(unlockedThemes));
    }
    public int getCurrency() { return this.currency; }
    public Set<String> getKnownNPCs() {
        return Collections.unmodifiableSet(new HashSet<>(knownNPCs));
    }
    public List<Items> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
    public List<String> getItemNames() {
        List<String> itemStringsList = new ArrayList<>();
        for (Items currItem : items) {
            itemStringsList.add(currItem.name());
        }
        return itemStringsList;
    }

    public double getLevelProgress() {
        return (experience % XP_TO_LEVEL_UP) * 1.0 / XP_TO_LEVEL_UP;
    }

    public List<Course> getCoursesEnrolled() {
        return new ArrayList<>(coursesEnrolled);
    }
    public List<Course> getCoursesTeached() {
        return new ArrayList<>(coursesTeached);
    }
    public List<WeekViewEvent> getScheduleStudent() {
        return scheduleStudent;
    }
    public Map<String, List<String>> getAnsweredQuestion() { return Collections.unmodifiableMap(new HashMap<>(answeredQuestions)); }

    public List<SpecialQuest> getSpecialQuests() { return specialQuests; }

    public boolean isSuperUser() {
        return SUPER_USERS.contains(getSciperNum());
    }
    public Map<String, Long> getClickedInstants() {return Collections.unmodifiableMap(clickedInstants); }

    // Setters
    public void setSciperNum(String sciperNum) {
        this.sciperNum = sciperNum;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setRole(Role role) {
        this.role = role;
    }
    public void setUserName(String newUsername) {
        username = newUsername;
        Firestore.get().updateRemotePlayerDataFromLocal();
        notifySpecialQuestObservers(Constants.SpecialQuestUpdateFlag.SET_USERNAME);
    }

    // Method suppose that we can only gain experience.
    private void updateLevel(Activity activity) {
        int newLevel = experience / XP_TO_LEVEL_UP + 1;

        if (newLevel - level > 0) {
            addCurrency((newLevel - level) * CURRENCY_PER_LEVEL, activity);
            level = newLevel;
            notifySpecialQuestObservers(Constants.SpecialQuestUpdateFlag.LEVEL_UP);
        }

        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    public void addExperience(int xp, Activity activity) {
        experience += xp;
        updateLevel(activity);

        if (activity instanceof HomeActivity) {
            ((HomeActivity) activity).updateXpAndLvlDisplay();
            ((HomeActivity) activity).updateCurrDisplay();
            Log.i("Check", "Activity is " + activity.toString() + " " + ((HomeActivity) activity).getLocalClassName());
        }

        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    public void addCurrency(int curr, Activity activity) {
        currency += curr;

        if (activity instanceof HomeActivity) {
            ((HomeActivity) activity).updateCurrDisplay();
        }

        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    public void addItem(Items item) {
        if (items.add(item)) {
            Firestore.get().updateRemotePlayerDataFromLocal();
        }
    }
    public void addKnownNPC(NPC newNPC) {
        String NPCName = newNPC.getName();
        knownNPCs.add(NPCName);
        addItemOrThemeToFB(true);

    }

    public void addTheme(String name) {
        unlockedThemes.add(name);
        addItemOrThemeToFB(false);
    }

    private void addItemOrThemeToFB(final boolean isNPCList) {
        final DocumentReference userRef = Firestore.get().getDb().document(FB_USERS + "/" + sciperNum);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> userData = documentSnapshot.getData();
                userData = userData == null ? new HashMap<String, Object>() : userData;
                if(isNPCList) {
                    userData.put(FB_KNOWN_NPCS, new ArrayList<>(knownNPCs));
                } else {
                    userData.put(FB_UNLOCKED_THEME, new ArrayList<>(unlockedThemes));
                }
                userRef.set(userData);
            }
        });
    }

    public void consumeItem(Items item)  {
        items.remove(item);
        item.consume();
        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    /**
     * Set the given courses as this player's course (depending on the role). When setting some
     * teacher's courses, it will upload the courses' data on the server accordingly (overriding
     * the schedule of the course if someone else was teaching that course).
     *
     * @param courses The courses the player attends/teaches
     */
    public void setCourses(List<Course> courses) {
        if(isStudent()) {
            this.coursesEnrolled = new ArrayList<>(courses);
        } else {
            List<Course> oldCourses = getCoursesTeached();
            coursesTeached = new ArrayList<>(courses);
            oldCourses.removeAll(courses);
            for(Course c : courses) {
                Firestore.get().addPlayerToTeachingStaff(c, sciperNum);
            }
            for(Course c : oldCourses) {
                Firestore.get().removePlayerFromTeachingStaff(c, sciperNum);
            }
        }

        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    public void setScheduleStudent(List<WeekViewEvent> scheduleStudent) {
        this.scheduleStudent = scheduleStudent;
    }

    public String getCurrentCourseLocation() {
        Calendar currTime = Calendar.getInstance();
        currTime.set(Calendar.YEAR, Constants.YEAR_OF_SCHEDULE);
        currTime.set(Calendar.MONTH, Constants.MONTH_OF_SCHEDULE);
        currTime.set(Calendar.WEEK_OF_MONTH, Constants.WEEK_OF_MONTH_SCHEDULE);
        Log.d(TAG, "getCurrentCourseLocation: " + currTime.toString());
        List<String> playersCourses = Constants.Course.getNamesFromCourses(Player.get().getCoursesEnrolled());

        for(WeekViewEvent event : Player.get().getScheduleStudent()) {
            if(playersCourses.contains(event.getName()) &&
                    ROOMS_LOCATIONS.containsKey(event.getLocation()) &&
                    currTime.after(event.getStartTime()) &&
                    currTime.before(event.getEndTime())) {
                Log.d(TAG, "getCurrentCourseLocation: " + event.getLocation());
                return event.getLocation();
            }
        }
        return null;
    }

    /**
     * Add the questionID to answered questions field in Firebase, mapped with the value of the answer.
     */
    public void addAnsweredQuestion(String questionID, boolean isAnswerGood, int ansNb) {
        if(this.answeredQuestions.get(questionID) == null) {
            String isAnswerGoodStr = isAnswerGood ? "true" : "false";
            this.answeredQuestions.put(questionID, Arrays.asList(isAnswerGoodStr, Integer.toString(ansNb)));
            Firestore.get().updateRemotePlayerDataFromLocal();
        }
    }

    public void addClickedInstant(String questionID, Long instant) {
        clickedInstants.put(questionID, instant);
        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    public void addSpecialQuest(SpecialQuest specialQuest) {
        this.specialQuests.add(specialQuest);
        Firestore.get().updateRemotePlayerDataFromLocal();
    }

    public boolean isDefault() throws NumberFormatException {

        /*
        If player has not yet been loaded,
        these values will be the default ones.
        This is used in AuthenticationActivity.
         */
        return this.firstName.equals(INITIAL_FIRSTNAME) &&
            this.lastName.equals(INITIAL_LASTNAME) &&
            this.sciperNum.equals(INITIAL_SCIPER);
    }

    public boolean isTeacher() {
        return getRole() == Role.teacher;
    }

    public boolean isStudent() {
        return getRole() == Role.student;
    }

    @Override
    public void notifySpecialQuestObservers(Constants.SpecialQuestUpdateFlag updateFlag) {
        for (SpecialQuestObserver specialQuest : specialQuests) {
            specialQuest.update(updateFlag);
        }
    }
}