package aggressor;

import common.CommonUtils;
import cortana.Cortana;
import dialog.DialogUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.stream.Collectors;

public class MultiFrame extends JFrame implements KeyEventDispatcher {
    protected JToolBar toolbar;
    protected JPanel content;
    protected CardLayout cards;
    protected final LinkedList<ClientInstance> buttons;
    protected AggressorClient active;

    public Collection getOtherScriptEngines(AggressorClient bar) {
        Collection foo = this.getScriptEngines();
        foo.remove(bar.getScriptEngine());
        return foo;
    }

    public Collection getScriptEngines() {
        synchronized (this.buttons) {
            LinkedList<Cortana> r = this.buttons.stream().map(temp -> temp.app.getScriptEngine()).collect(Collectors.toCollection(LinkedList::new));
            return r;
        }
    }

    public Map getClients() {
        synchronized (this.buttons) {
            HashMap<String, AggressorClient> r = new HashMap<>();
            for (ClientInstance temp : this.buttons) {
                r.put(temp.button.getText(), temp.app);
            }
            return r;
        }
    }

    public void setTitle(AggressorClient app, String title) {
        if (this.active == app) {
            this.setTitle(title);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        if (this.active != null) {
            return this.active.getBindings().dispatchKeyEvent(ev);
        }
        return false;
    }

    public void closeConnect() {

        synchronized (this.buttons) {
            if (this.buttons.size() == 0) {
                System.exit(0);
            }
        }
    }

    public void quit() {
        CommonUtils.Guard();

        synchronized (this.buttons) {
            ClientInstance temp;
            this.content.remove(this.active);
            Iterator i = this.buttons.iterator();
            while (i.hasNext()) {
                temp = (ClientInstance) i.next();
                if (temp.app != this.active) continue;
                this.toolbar.remove(temp.button);
                i.remove();
                this.toolbar.validate();
                this.toolbar.repaint();
                break;
            }
            if (this.buttons.size() == 0) {
                System.exit(0);
            } else if (this.buttons.size() == 1) {
                this.getContentPane().remove(this.toolbar);
                this.validate();
            }
            temp = i.hasNext() ? (ClientInstance) i.next() : this.buttons.getFirst();
            this.set(temp.button);
        }
        System.gc();
    }

    public MultiFrame() {
        super("");
        this.getContentPane().setLayout(new BorderLayout());
        this.toolbar = new JToolBar();
        this.content = new JPanel();
        this.cards = new CardLayout();
        this.content.setLayout(this.cards);
        this.getContentPane().add(this.content, "Center");
        this.buttons = new LinkedList<>();
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setExtendedState(6);
        this.setIconImage(DialogUtils.getImage("resources/armitage-icon.gif"));
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(this);
    }

    protected void set(JToggleButton button) {
        CommonUtils.Guard();

        synchronized (this.buttons) {
            for (ClientInstance temp : this.buttons) {
                if (temp.button.getText().equals(button.getText())) {
                    temp.button.setSelected(true);
                    this.active = temp.app;
                    this.setTitle(this.active.getTitle());
                    continue;
                }
                temp.button.setSelected(false);
            }
            this.cards.show(this.content, button.getText());
            this.active.touch();
        }
    }

    public boolean checkCollision(String name) {

        synchronized (this.buttons) {
            return this.buttons.stream().anyMatch(temp -> name.equals(temp.button.getText()));
        }
    }

    public void addButton(String title, final AggressorClient component) {
        CommonUtils.Guard();
        if (this.checkCollision(title)) {
            this.addButton(title + " (2)", component);
            return;
        }

        synchronized (this.buttons) {
            final ClientInstance a = new ClientInstance();
            a.button = new JToggleButton(title);
            a.button.setToolTipText(title);
            a.app = component;
            a.button.addActionListener(ev -> MultiFrame.this.set((JToggleButton) ev.getSource()));
            a.button.addMouseListener(new MouseAdapter() {

                public void check(MouseEvent ev) {
                    if (ev.isPopupTrigger()) {
                        final JToggleButton source = a.button;
                        JPopupMenu popup = new JPopupMenu();
                        JMenuItem rename = new JMenuItem("Rename");
                        rename.addActionListener(ev12 -> {
                            String name = JOptionPane.showInputDialog("Rename to?", source.getText());
                            if (name != null) {
                                MultiFrame.this.content.remove(component);
                                MultiFrame.this.content.add(component, name);
                                source.setText(name);
                                MultiFrame.this.set(source);
                            }
                        });
                        popup.add(rename);
                        JMenuItem kill = new JMenuItem("Disconnect");
                        kill.addActionListener(ev1 -> a.app.kill());
                        popup.add(kill);
                        popup.show((JComponent) ev.getSource(), ev.getX(), ev.getY());
                        ev.consume();
                    }
                }

                @Override
                public void mouseClicked(MouseEvent ev) {
                    this.check(ev);
                }

                @Override
                public void mousePressed(MouseEvent ev) {
                    this.check(ev);
                }

                @Override
                public void mouseReleased(MouseEvent ev) {
                    this.check(ev);
                }

            });
            this.toolbar.add(a.button);
            this.content.add(component, title);
            this.buttons.add(a);
            this.set(a.button);
            if (this.buttons.size() == 1) {
                this.setVisible(true);
            } else if (this.buttons.size() == 2) {
                this.getContentPane().add(this.toolbar, "South");
            }
            this.validate();
        }
    }

    private static class ClientInstance {
        public AggressorClient app;
        public JToggleButton button;
        public boolean serviced = false;

        private ClientInstance() {
        }
    }

}

