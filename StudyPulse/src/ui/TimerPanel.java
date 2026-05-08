package ui;

import dao.TaskDAO;
import model.Task;
import service.PomodoroService;
import service.PomodoroService.Mode;
import util.SessionContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * LAYER: UI — TimerPanel
 * Pomofocus-style timer screen.
 * FIX: Removed all transparent Color() usage that caused white hover flash.
 *      Using setContentAreaFilled(false)/setOpaque(false) instead.
 *      Scroll pane now uses solid backgrounds to prevent white repaints.
 */
public class TimerPanel extends JPanel implements PomodoroService.TimerListener {

    private final PomodoroService service;
    private final MainFrame       mainFrame;
    private final TaskDAO         taskDAO = new TaskDAO();

    private JLabel  lblTimer;
    private JButton btnStart;
    private JButton btnTabWork, btnTabShort, btnTabLong;
    private JLabel  lblPomoNum, lblFocusMsg;
    private Color   modeColor = UI.POMO_RED;

    private JPanel     taskListPanel;
    private JPanel     addTaskForm;
    private boolean    addFormVisible = false;
    private JTextField tfNewTask;
    private JSpinner   spNewEst;
    private JLabel     lblFooterPomos, lblFooterFinish;

    // Keep reference to scroll+viewport for bg syncing
    private JScrollPane mainScroll;
    private JPanel      centerPanel;

    public TimerPanel(PomodoroService svc, MainFrame mf) {
        this.service   = svc;
        this.mainFrame = mf;
        service.addListener(this);
        setBackground(UI.POMO_RED);
        setLayout(new BorderLayout());
        buildUI();
        updateTimerDisplay(service.getSecondsLeft(), service.getMode());
        if (SessionContext.getUser() != null) refreshTasks();
    }

    public void refresh() {
        if (SessionContext.getUser() != null) refreshTasks();
    }

    private void buildUI() {
        centerPanel = new JPanel();
        centerPanel.setBackground(UI.POMO_RED);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(16, 0, 40, 0));

        centerPanel.add(buildTimerCard());
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(buildFocusLabel());
        centerPanel.add(Box.createVerticalStrut(16));
        centerPanel.add(buildTaskSection());

        mainScroll = new JScrollPane(centerPanel);
        mainScroll.setBackground(UI.POMO_RED);
        mainScroll.getViewport().setBackground(UI.POMO_RED);
        mainScroll.setBorder(null);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScroll.getVerticalScrollBar().setUnitIncrement(12);

        add(mainScroll, BorderLayout.CENTER);
    }

    private JPanel buildTimerCard() {
        // Custom panel with painted dark overlay — no opaque transparent Color()
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 35));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 24, 24));
        card.setAlignmentX(CENTER_ALIGNMENT);
        card.setMaximumSize(new Dimension(480, 270));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        tabs.setOpaque(false);
        btnTabWork  = modeTabBtn("Pomodoro",    true);
        btnTabShort = modeTabBtn("Short Break", false);
        btnTabLong  = modeTabBtn("Long Break",  false);
        btnTabWork.addActionListener(e  -> switchMode(Mode.WORK,        btnTabWork));
        btnTabShort.addActionListener(e -> switchMode(Mode.SHORT_BREAK, btnTabShort));
        btnTabLong.addActionListener(e  -> switchMode(Mode.LONG_BREAK,  btnTabLong));
        tabs.add(btnTabWork); tabs.add(btnTabShort); tabs.add(btnTabLong);

        lblTimer = new JLabel("25:00", SwingConstants.CENTER);
        lblTimer.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 88));
        lblTimer.setForeground(Color.WHITE);
        lblTimer.setAlignmentX(CENTER_ALIGNMENT);

        btnStart = new JButton("START");
        btnStart.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 20));
        btnStart.setForeground(UI.POMO_RED);
        btnStart.setBackground(Color.WHITE);
        btnStart.setOpaque(true);
        btnStart.setContentAreaFilled(true);
        btnStart.setBorderPainted(false);
        btnStart.setFocusPainted(false);
        btnStart.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnStart.setPreferredSize(new Dimension(220, 52));
        btnStart.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnStart.setBackground(new Color(240,240,240)); }
            public void mouseExited(MouseEvent e)  { btnStart.setBackground(Color.WHITE); }
        });
        btnStart.addActionListener(e -> toggleTimer());

        JPanel tw = new JPanel(new FlowLayout()); tw.setOpaque(false); tw.add(tabs);
        JPanel lw = new JPanel(new FlowLayout()); lw.setOpaque(false); lw.add(lblTimer);
        JPanel bw = new JPanel(new FlowLayout()); bw.setOpaque(false); bw.add(btnStart);

        card.add(tw);
        card.add(Box.createVerticalStrut(8));
        card.add(lw);
        card.add(Box.createVerticalStrut(12));
        card.add(bw);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrap.setOpaque(false); wrap.add(card);
        return wrap;
    }

    private JPanel buildFocusLabel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        lblPomoNum  = new JLabel("#1", SwingConstants.CENTER);
        lblPomoNum.setFont(new Font("Arial Rounded MT Bold", Font.BOLD,  13));
        lblPomoNum.setForeground(new Color(255, 200, 200));
        lblFocusMsg = new JLabel("Time to focus!", SwingConstants.CENTER);
        lblFocusMsg.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        lblFocusMsg.setForeground(new Color(255, 220, 220));
        JPanel col = new JPanel();
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
        col.setOpaque(false);
        lblPomoNum.setAlignmentX(CENTER_ALIGNMENT);
        lblFocusMsg.setAlignmentX(CENTER_ALIGNMENT);
        col.add(lblPomoNum); col.add(lblFocusMsg);
        p.add(col);
        return p;
    }

    private JPanel buildTaskSection() {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setMaximumSize(new Dimension(480, Integer.MAX_VALUE));
        section.setAlignmentX(CENTER_ALIGNMENT);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 255, 255, 80)));
        JLabel tasksLbl = new JLabel("Tasks");
        tasksLbl.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
        tasksLbl.setForeground(Color.WHITE);
        header.add(tasksLbl, BorderLayout.WEST);
        header.setMaximumSize(new Dimension(480, 30));

        taskListPanel = new JPanel();
        taskListPanel.setOpaque(false);
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));

        addTaskForm = buildAddTaskForm();

        // Add task button — solid bg instead of transparent
        JButton btnAdd = new JButton("＋ Add Task");
        btnAdd.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 13));
        btnAdd.setForeground(new Color(255, 230, 230));
        btnAdd.setBackground(new Color(175, 45, 35));
        btnAdd.setOpaque(true);
        btnAdd.setContentAreaFilled(true);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setAlignmentX(CENTER_ALIGNMENT);
        btnAdd.setMaximumSize(new Dimension(480, 46));
        btnAdd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnAdd.setBackground(new Color(155, 35, 28)); }
            public void mouseExited(MouseEvent e)  { btnAdd.setBackground(new Color(175, 45, 35)); }
        });
        btnAdd.addActionListener(e -> toggleAddForm());

        JPanel footer = buildFooter();

        section.add(header);
        section.add(Box.createVerticalStrut(10));
        section.add(taskListPanel);
        section.add(Box.createVerticalStrut(6));
        section.add(addTaskForm);
        section.add(Box.createVerticalStrut(4));
        section.add(btnAdd);
        section.add(Box.createVerticalStrut(14));
        section.add(footer);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrap.setOpaque(false); wrap.add(section);
        return wrap;
    }

    private JPanel buildAddTaskForm() {
        JPanel p = UI.card(new GridBagLayout());
        p.setVisible(false);
        p.setMaximumSize(new Dimension(480, 160));
        p.setAlignmentX(CENTER_ALIGNMENT);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.weightx=1.0;
        gc.gridx=0; gc.insets=new Insets(4,0,4,0);

        tfNewTask = UI.textField(20);
        tfNewTask.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 15));
        tfNewTask.setToolTipText("What are you working on?");

        JPanel estRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        estRow.setBackground(Color.WHITE);
        JLabel estLbl = new JLabel("Est. Pomodoros");
        estLbl.setFont(UI.F_SMALL); estLbl.setForeground(UI.TEXT_GRAY);
        spNewEst = new JSpinner(new SpinnerNumberModel(1,1,20,1));
        spNewEst.setPreferredSize(new Dimension(70,28));
        estRow.add(estLbl); estRow.add(spNewEst);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));
        btns.setBackground(Color.WHITE);
        JButton cancel = new JButton("Cancel");
        cancel.setFont(UI.F_BODY); cancel.setFocusPainted(false);
        cancel.addActionListener(e -> { addTaskForm.setVisible(false); addFormVisible=false; });
        JButton save = UI.filledBtn("Save", UI.POMO_RED);
        save.addActionListener(e -> doAddTask());
        btns.add(cancel); btns.add(save);

        gc.gridy=0; p.add(tfNewTask, gc);
        gc.gridy=1; p.add(estRow,    gc);
        gc.gridy=2; p.add(btns,      gc);
        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(160, 45, 36));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0, new Color(255,255,255,60)),
            BorderFactory.createEmptyBorder(10,14,10,14)
        ));
        p.setMaximumSize(new Dimension(480,44));
        lblFooterPomos  = new JLabel("Pomos: 0 / 0");
        lblFooterPomos.setFont(UI.F_BODY);
        lblFooterPomos.setForeground(Color.WHITE);
        lblFooterFinish = new JLabel("Finish At: --:--");
        lblFooterFinish.setFont(new Font("Arial Rounded MT Bold",Font.BOLD,13));
        lblFooterFinish.setForeground(Color.WHITE);
        p.add(lblFooterPomos,  BorderLayout.WEST);
        p.add(lblFooterFinish, BorderLayout.EAST);
        return p;
    }

    private void refreshTasks() {
        taskListPanel.removeAll();
        List<Task> tasks = taskDAO.getByUser(SessionContext.getUser().getId());
        int totalEst=0, totalDone=0;
        for (Task t : tasks) {
            totalEst  += t.getEstPomos();
            totalDone += t.getDonePomos();
            taskListPanel.add(buildTaskCard(t));
            taskListPanel.add(Box.createVerticalStrut(6));
        }
        updateFooterPomos(totalDone, totalEst, tasks);
        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    private JPanel buildTaskCard(Task t) {
        boolean isActive = t.getId() == (service.getActiveTask()!=null ? service.getActiveTask() : -1);
        JPanel card = new JPanel(new BorderLayout(10,0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            isActive
                ? BorderFactory.createMatteBorder(0,4,0,0,modeColor)
                : BorderFactory.createMatteBorder(0,4,0,0,new Color(220,220,220)),
            BorderFactory.createEmptyBorder(10,12,10,12)
        ));
        card.setMaximumSize(new Dimension(480,58));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){ service.setActiveTask(t.getId()); refreshTasks(); }
            public void mouseEntered(MouseEvent e){ card.setBackground(new Color(252,248,248)); card.repaint(); }
            public void mouseExited(MouseEvent e) { card.setBackground(Color.WHITE); card.repaint(); }
        });

        JPanel circle = buildCheckCircle(t);

        JPanel nameCol = new JPanel();
        nameCol.setOpaque(false);
        nameCol.setLayout(new BoxLayout(nameCol, BoxLayout.Y_AXIS));
        JLabel nameL = new JLabel(t.isCompleted()
            ? "<html><strike>"+t.getName()+"</strike></html>" : t.getName());
        nameL.setFont(new Font("Arial Rounded MT Bold",Font.BOLD,13));
        nameL.setForeground(t.isCompleted() ? UI.TEXT_GRAY : UI.TEXT_DARK);
        nameCol.add(nameL);
        if (!t.getSubject().isEmpty()) {
            JLabel subjL = new JLabel(t.getSubject());
            subjL.setFont(UI.F_SMALL); subjL.setForeground(UI.TEXT_GRAY);
            nameCol.add(subjL);
        }

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,6,0));
        right.setOpaque(false);
        JLabel count = new JLabel(t.getDonePomos()+"/"+t.getEstPomos());
        count.setFont(UI.F_SMALL); count.setForeground(UI.TEXT_GRAY);
        JButton menu = new JButton("⋮");
        menu.setFont(UI.F_BODY); menu.setForeground(UI.TEXT_GRAY);
        menu.setBackground(Color.WHITE); menu.setOpaque(true);
        menu.setContentAreaFilled(true); menu.setBorderPainted(false); menu.setFocusPainted(false);
        menu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        menu.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ menu.setBackground(new Color(240,240,240)); }
            public void mouseExited(MouseEvent e) { menu.setBackground(Color.WHITE); }
        });
        menu.addActionListener(e -> showTaskMenu(menu, t));
        right.add(count); right.add(menu);

        card.add(circle,  BorderLayout.WEST);
        card.add(nameCol, BorderLayout.CENTER);
        card.add(right,   BorderLayout.EAST);
        return card;
    }

    private JPanel buildCheckCircle(Task t) {
        JPanel circle = new JPanel(){
            @Override protected void paintComponent(Graphics g){
                super.paintComponent(g);
                Graphics2D g2=(Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                if(t.isCompleted()){
                    g2.setColor(modeColor); g2.fillOval(2,2,20,20);
                    g2.setColor(Color.WHITE); g2.setStroke(new BasicStroke(2));
                    g2.drawLine(6,12,10,16); g2.drawLine(10,16,18,8);
                } else {
                    g2.setColor(Color.LIGHT_GRAY); g2.setStroke(new BasicStroke(2));
                    g2.drawOval(2,2,20,20);
                }
            }
        };
        circle.setBackground(Color.WHITE);
        circle.setPreferredSize(new Dimension(26,26));
        circle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        circle.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                e.consume();
                taskDAO.setCompleted(t.getId(),!t.isCompleted());
                refreshTasks();
            }
        });
        return circle;
    }

    private void showTaskMenu(JButton anchor, Task t){
        JPopupMenu menu=new JPopupMenu();
        JMenuItem edit=new JMenuItem("Edit Estimate");
        JMenuItem del =new JMenuItem("Delete Task");
        edit.addActionListener(e->{
            String v=JOptionPane.showInputDialog(this,"New estimate (pomodoros):",t.getEstPomos());
            if(v!=null&&!v.isEmpty()){try{taskDAO.updateEst(t.getId(),Integer.parseInt(v));refreshTasks();}catch(NumberFormatException ignored){}}
        });
        del.addActionListener(e->{taskDAO.delete(t.getId());refreshTasks();});
        menu.add(edit); menu.add(del);
        menu.show(anchor,0,anchor.getHeight());
    }

    private void toggleTimer(){
        if(service.isRunning()){ service.pause(); btnStart.setText("START"); }
        else                   { service.start(); btnStart.setText("PAUSE"); }
    }

    private void syncScrollBg(Color bg){
        setBackground(bg);
        centerPanel.setBackground(bg);
        mainScroll.setBackground(bg);
        mainScroll.getViewport().setBackground(bg);
        repaint();
    }

    private void switchMode(Mode mode, JButton activeBtn){
        service.switchMode(mode);
        btnStart.setText("START");
        // Reset all tabs to transparent (no fill)
        for (JButton b : new JButton[]{btnTabWork, btnTabShort, btnTabLong}) {
            b.setContentAreaFilled(false);
            b.setOpaque(false);
            b.setForeground(new Color(255, 220, 220));
            b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        // Active tab: solid dark pill — NO alpha Color()
        activeBtn.setContentAreaFilled(true);
        activeBtn.setOpaque(true);
        activeBtn.setBackground(new Color(140, 30, 22));  // solid, no transparency
        activeBtn.setForeground(Color.WHITE);
        activeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        modeColor = switch(mode){
            case WORK -> {
                Color c = UI.POMO_RED;
                mainFrame.setThemeColor(c);
                btnStart.setForeground(c);
                syncScrollBg(c);
                yield c;
            }
            case SHORT_BREAK -> {
                Color c = UI.SHORT_GRN;
                mainFrame.setThemeColor(c);
                btnStart.setForeground(c);
                syncScrollBg(c);
                // Update active tab bg to match green theme
                activeBtn.setBackground(new Color(38, 100, 72));
                yield c;
            }
            case LONG_BREAK -> {
                Color c = UI.LONG_BLUE;
                mainFrame.setThemeColor(c);
                btnStart.setForeground(c);
                syncScrollBg(c);
                // Update active tab bg to match blue theme
                activeBtn.setBackground(new Color(36, 72, 120));
                yield c;
            }
        };
    }

    private void toggleAddForm(){
        addFormVisible=!addFormVisible;
        addTaskForm.setVisible(addFormVisible);
        if(addFormVisible) tfNewTask.requestFocus();
        revalidate();
    }

    private void doAddTask(){
        String name=tfNewTask.getText().trim();
        if(name.isEmpty()){ UI.error(this,"Task name is required."); return; }
        Task t=new Task(SessionContext.getUser().getId(),name,"", (int)spNewEst.getValue());
        taskDAO.add(t);
        tfNewTask.setText(""); spNewEst.setValue(1);
        addTaskForm.setVisible(false); addFormVisible=false;
        refreshTasks();
    }

    private void updateTimerDisplay(int sec, Mode mode){
        lblTimer.setText(UI.pad(sec/60)+":"+UI.pad(sec%60));
        lblPomoNum.setText("#"+(service.getPomoDone()+1));
        lblFocusMsg.setText(switch(mode){
            case WORK->"Time to focus!"; case SHORT_BREAK->"Short break!"; case LONG_BREAK->"Long break!";
        });
    }

    private void updateFooterPomos(int done, int total, List<Task> tasks){
        lblFooterPomos.setText("Pomos: "+service.getPomoDone()+" / "+total);
        int remaining=(total-done)*SessionContext.getUser().getWorkMinutes();
        java.time.LocalTime fin=java.time.LocalTime.now().plusMinutes(remaining);
        String ampm=fin.getHour()>=12?"PM":"AM";
        int h=fin.getHour()%12; if(h==0) h=12;
        lblFooterFinish.setText("Finish At: "+ampm+" "+h+":"+UI.pad(fin.getMinute())
            +" ("+String.format("%.1f",remaining/60.0)+"h)");
    }

    @Override public void onTick(int secondsLeft, Mode mode){ updateTimerDisplay(secondsLeft,mode); }

    @Override public void onSessionComplete(Mode completed, Mode next){
        btnStart.setText("START");
        if(SessionContext.getUser().isSoundEnabled()) Toolkit.getDefaultToolkit().beep();
        String msg=switch(completed){
            case WORK->"Work session done! Time for a break!";
            case SHORT_BREAK->"Break over! Back to work!";
            case LONG_BREAK->"Long break done! Let's go!";
        };
        JOptionPane.showMessageDialog(this,msg,"Session Complete",JOptionPane.INFORMATION_MESSAGE);
        switchMode(next,switch(next){case WORK->btnTabWork; case SHORT_BREAK->btnTabShort; case LONG_BREAK->btnTabLong;});
        refreshTasks();
    }

    private JButton modeTabBtn(String text, boolean active){
        JButton b = new JButton(text);
        b.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        b.setForeground(active ? Color.WHITE : new Color(255, 210, 210));
        // KEY FIX: never use alpha Color() on an opaque button — causes white flash.
        // Instead use a solid darker shade of the current background.
        if (active) {
            b.setBackground(new Color(140, 30, 22));   // solid dark red pill
            b.setOpaque(true);
            b.setContentAreaFilled(true);
        } else {
            b.setContentAreaFilled(false);
            b.setOpaque(false);
        }
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(5, 14, 5, 14));
        // Hover: only for INACTIVE tabs — solid color, no alpha
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!b.isContentAreaFilled()) {
                    b.setContentAreaFilled(true);
                    b.setOpaque(true);
                    b.setBackground(new Color(130, 28, 20));
                }
            }
            public void mouseExited(MouseEvent e) {
                // Only clear if this tab is not the active one
                // (active tabs stay filled — switchMode handles them)
                boolean isActive = b.getFont().isBold() && b.isOpaque()
                    && b.getBackground().equals(new Color(140, 30, 22));
                if (!isActive) {
                    b.setContentAreaFilled(false);
                    b.setOpaque(false);
                }
            }
        });
        return b;
    }
}