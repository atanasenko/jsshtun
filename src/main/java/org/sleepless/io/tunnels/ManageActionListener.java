package org.sleepless.io.tunnels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ManageActionListener implements ActionListener {
    
    private Tunnels t;
    
    public ManageActionListener(Tunnels t){
        this.t = t;
    }

    public void actionPerformed(ActionEvent ae) {
        new ManageDialog(t);
    }

}
