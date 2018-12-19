package aggressor.bridges;

import common.CommonUtils;
import cortana.Cortana;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Stack;

public class ToolBarBridge implements Function,
        Loadable {
    protected JToolBar toolbar;

    public ToolBarBridge(JToolBar toolbar) {
        this.toolbar = toolbar;
    }

    @Override
    public void scriptLoaded(ScriptInstance si) {
        Cortana.put(si, "&image", this);
        Cortana.put(si, "&image_internal", this);
        Cortana.put(si, "&toolbar", this);
        Cortana.put(si, "&toolbar_separator", this);
    }

    @Override
    public void scriptUnloaded(ScriptInstance si) {
    }

    @Override
    public Scalar evaluate(String name, ScriptInstance script, Stack args) {
        if (name.equals("&image")) {
            String file = BridgeUtilities.getString(args, "");
            return SleepUtils.getScalar(new ImageIcon(file));
        }
        if (name.equals("&image_internal")) {
            try {
                String file = BridgeUtilities.getString(args, "");
                BufferedImage image = ImageIO.read(CommonUtils.resource(file));
                return SleepUtils.getScalar(new ImageIcon(image));
            } catch (IOException ioex) {
                throw new RuntimeException(ioex);
            }
        }
        if (name.equals("&toolbar")) {
            Icon i = (Icon) BridgeUtilities.getObject(args);
            final String t = (String) BridgeUtilities.getObject(args);
            final SleepClosure f = BridgeUtilities.getFunction(args, script);
            JButton temp = new JButton(i);
            temp.setToolTipText(t);
            temp.addActionListener(ev -> {
                Stack<Scalar> args1 = new Stack<>();
                args1.push(SleepUtils.getScalar(t));
                SleepUtils.runCode(f, "toolbar", null, args1);
            });
            this.toolbar.add(temp);
        } else if (name.equals("&toolbar_separator")) {
            this.toolbar.addSeparator();
        }
        return SleepUtils.getEmptyScalar();
    }

}

