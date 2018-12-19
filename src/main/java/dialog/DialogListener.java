package dialog;

import java.awt.event.ActionEvent;
import java.util.HashMap;

public interface DialogListener {
    void dialogAction(ActionEvent event, HashMap<String, Object> options);
}

