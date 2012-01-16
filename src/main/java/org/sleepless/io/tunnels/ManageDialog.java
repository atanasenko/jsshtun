package org.sleepless.io.tunnels;

import javax.swing.JDialog;

public class ManageDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private Tunnels t;
    
    public ManageDialog(Tunnels t) {
        this.t = t;
        
        setTitle("Manage hosts");
        
        setVisible(true);
    }
    
}
