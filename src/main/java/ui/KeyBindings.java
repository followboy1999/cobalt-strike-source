package ui;

import common.CommonUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;

public class KeyBindings implements KeyEventDispatcher {
    protected Map<String, KeyHandler> bindings = new HashMap<>();

    public void bind(String description, KeyHandler handler) {
        synchronized (this) {
            this.bindings.put(description, handler);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent ev) {
        StringBuilder description = new StringBuilder();
        if (ev.getModifiers() != 0) {
            description.append(KeyBindings.getKeyModifiers(ev));
        }
        description.append(KeyBindings.getKeyText(ev));
        synchronized (this) {
            if (this.bindings.containsKey(description.toString())) {
                ev.consume();
                if (ev.getID() != 401) {
                    return false;
                }
                CommonUtils.runSafe(new ExecuteBinding(description.toString(), this.bindings.get(description.toString())));
                return true;
            }
        }
        return false;
    }

    private static String getKeyModifiers(KeyEvent ev) {
        StringBuilder modifiers = new StringBuilder();
        if (ev.isShiftDown()) {
            modifiers.append("Shift+");
        }
        if (ev.isControlDown()) {
            modifiers.append("Ctrl+");
        }
        if (ev.isAltDown()) {
            modifiers.append("Alt+");
        }
        if (ev.isMetaDown()) {
            modifiers.append("Meta+");
        }
        return modifiers.toString();
    }

    private static String getKeyText(KeyEvent ev) {
        switch (ev.getKeyCode()) {
            case VK_ACCEPT: {
                return "Accept";
            }
            case VK_BACK_QUOTE: {
                return "Back_Quote";
            }
            case VK_BACK_SPACE: {
                return "Backspace";
            }
            case VK_CAPS_LOCK: {
                return "Caps_Lock";
            }
            case VK_CLEAR: {
                return "Clear";
            }
            case VK_CONVERT: {
                return "Convert";
            }
            case VK_DELETE: {
                return "Delete";
            }
            case VK_DOWN: {
                return "Down";
            }
            case VK_END: {
                return "End";
            }
            case VK_ENTER: {
                return "Enter";
            }
            case VK_ESCAPE: {
                return "Escape";
            }
            case VK_F1: {
                return "F1";
            }
            case VK_F2: {
                return "F2";
            }
            case VK_F3: {
                return "F3";
            }
            case VK_F4: {
                return "F4";
            }
            case VK_F5: {
                return "F5";
            }
            case VK_F6: {
                return "F6";
            }
            case VK_F7: {
                return "F7";
            }
            case VK_F8: {
                return "F8";
            }
            case VK_F9: {
                return "F9";
            }
            case VK_F10: {
                return "F10";
            }
            case VK_F11: {
                return "F11";
            }
            case VK_F12: {
                return "F12";
            }
            case VK_FINAL: {
                return "Final";
            }
            case VK_HELP: {
                return "Help";
            }
            case VK_HOME: {
                return "Home";
            }
            case VK_INSERT: {
                return "Insert";
            }
            case VK_LEFT: {
                return "Left";
            }
            case VK_NUM_LOCK: {
                return "Num_Lock";
            }
            case VK_MULTIPLY: {
                return "NumPad_*";
            }
            case VK_PLUS: {
                return "NumPad_+";
            }
            case VK_COMMA: {
                return "NumPad_,";
            }
            case VK_SUBTRACT: {
                return "NumPad_-";
            }
            case VK_PERIOD: {
                return "Period";
            }
            case VK_SLASH: {
                return "NumPad_/";
            }
            case VK_PAGE_DOWN: {
                return "Page_Down";
            }
            case VK_PAGE_UP: {
                return "Page_Up";
            }
            case VK_PAUSE: {
                return "Pause";
            }
            case VK_PRINTSCREEN: {
                return "Print_Screen";
            }
            case VK_QUOTE: {
                return "Quote";
            }
            case VK_RIGHT: {
                return "Right";
            }
            case VK_SCROLL_LOCK: {
                return "Scroll_Lock";
            }
            case VK_SPACE: {
                return "Space";
            }
            case VK_TAB: {
                return "Tab";
            }
            case VK_UP: {
                return "Up";
            }
        }
        return KeyEvent.getKeyText(ev.getKeyCode());
    }

    private static class ExecuteBinding implements Runnable {
        protected String binding;
        protected KeyHandler handler;

        public ExecuteBinding(String b, KeyHandler h) {
            this.binding = b;
            this.handler = h;
        }

        @Override
        public void run() {
            this.handler.key_pressed(this.binding);
        }
    }

}

