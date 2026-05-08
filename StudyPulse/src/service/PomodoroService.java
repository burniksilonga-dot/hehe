package service;

import dao.StudySessionDAO;
import dao.TaskDAO;
import model.StudySession;
import util.SessionContext;

import javax.swing.Timer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * LAYER: Service
 * PURPOSE: The core Pomodoro timer engine. Contains ALL timer logic.
 *
 * HOW IT WORKS:
 *   1. Uses javax.swing.Timer — fires an ActionEvent every 1000ms (1 second)
 *      on the Event Dispatch Thread (EDT) so the UI can be updated safely.
 *   2. Keeps track of secondsLeft, current mode, and pomodoroCount.
 *   3. When a session completes, it auto-transitions to the next mode.
 *   4. Notifies all registered TimerListeners on every tick and on completion.
 *   5. Logs each session start/end to the database via StudySessionDAO.
 *
 * OBSERVER PATTERN:
 *   Panels register themselves as TimerListeners. When the timer ticks,
 *   all listeners receive onTick() and update their display. This decouples
 *   the timer logic from the UI.
 */
public class PomodoroService {

    public enum Mode { WORK, SHORT_BREAK, LONG_BREAK }

    /** Listener interface — implemented by TimerPanel and DashboardPanel */
    public interface TimerListener {
        void onTick(int secondsLeft, Mode mode);
        void onSessionComplete(Mode completed, Mode next);
    }

    private final StudySessionDAO sessionDAO = new StudySessionDAO();
    private final TaskDAO         taskDAO    = new TaskDAO();
    private final List<TimerListener> listeners = new ArrayList<>();

    private Timer  swingTimer;
    private Mode   currentMode   = Mode.WORK;
    private int    secondsLeft;
    private boolean running      = false;
    private int    pomoDone      = 0;
    private int    currentSessionId = -1;
    private LocalDateTime sessionStart;
    private Integer activeTaskId = null;

    // Settings (read from user prefs)
    private int workSec, shortSec, longSec, longInterval;

    public PomodoroService() { reloadSettings(); }

    /** Called after settings are changed so timer uses new durations */
    public void reloadSettings() {
        var u = SessionContext.getUser();
        workSec      = (u != null ? u.getWorkMinutes() : 25) * 60;
        shortSec     = (u != null ? u.getShortBreak()  : 5)  * 60;
        longSec      = (u != null ? u.getLongBreak()   : 15) * 60;
        longInterval = (u != null ? u.getLongInterval() : 4);
        secondsLeft  = durationFor(currentMode);
    }

    public void addListener(TimerListener l) { listeners.add(l); }

    /** START — begins ticking and logs session start to DB */
    public void start() {
        if (running) return;
        running = true;
        sessionStart = LocalDateTime.now();

        StudySession s = new StudySession();
        s.setUserId(SessionContext.getUser().getId());
        s.setSessionDate(LocalDate.now());
        s.setStartTime(sessionStart);
        s.setSessionType(currentMode.name());
        s.setTaskId(activeTaskId);
        currentSessionId = sessionDAO.startSession(s);

        swingTimer = new Timer(1000, e -> tick());
        swingTimer.start();
    }

    /** PAUSE — stops ticking, does NOT save to DB (session continues) */
    public void pause() {
        if (!running) return;
        running = false;
        if (swingTimer != null) swingTimer.stop();
    }

    /** RESET — resets timer to current mode's full duration */
    public void reset() {
        if (swingTimer != null) swingTimer.stop();
        running = false;
        secondsLeft = durationFor(currentMode);
        notifyTick();
    }

    /** SWITCH MODE — manually jump to a different mode */
    public void switchMode(Mode mode) {
        if (swingTimer != null) swingTimer.stop();
        running = false;
        currentMode = mode;
        secondsLeft = durationFor(mode);
        notifyTick();
    }

    private void tick() {
        secondsLeft--;
        notifyTick();
        if (secondsLeft <= 0) onSessionEnd();
    }

    private void onSessionEnd() {
        swingTimer.stop();
        running = false;

        // Save completed session to DB
        if (currentSessionId > 0) {
            sessionDAO.completeSession(currentSessionId, LocalDateTime.now(), durationFor(currentMode));
            currentSessionId = -1;
        }

        // If work session ended and a task is linked, increment its done count
        if (currentMode == Mode.WORK && activeTaskId != null) {
            taskDAO.incrementDone(activeTaskId);
        }

        Mode completed = currentMode;

        // Auto-advance mode
        if (currentMode == Mode.WORK) {
            pomoDone++;
            currentMode = (pomoDone % longInterval == 0) ? Mode.LONG_BREAK : Mode.SHORT_BREAK;
        } else {
            currentMode = Mode.WORK;
        }

        secondsLeft = durationFor(currentMode);
        Mode next = currentMode;

        listeners.forEach(l -> l.onSessionComplete(completed, next));
        notifyTick();
    }

    private void notifyTick() {
        int sl = secondsLeft;
        Mode m = currentMode;
        listeners.forEach(l -> l.onTick(sl, m));
    }

    private int durationFor(Mode m) {
        return switch (m) {
            case WORK        -> workSec;
            case SHORT_BREAK -> shortSec;
            case LONG_BREAK  -> longSec;
        };
    }

    public boolean isRunning()      { return running; }
    public Mode    getMode()        { return currentMode; }
    public int     getSecondsLeft() { return secondsLeft; }
    public int     getPomoDone()    { return pomoDone; }
    public void    setActiveTask(Integer id) { activeTaskId = id; }
    public Integer getActiveTask()  { return activeTaskId; }
}
