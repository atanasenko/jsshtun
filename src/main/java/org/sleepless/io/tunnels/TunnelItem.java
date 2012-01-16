package org.sleepless.io.tunnels;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.sleepless.io.tunnels.config.Tunnel;

public class TunnelItem extends CheckboxMenuItem implements ItemListener {

    private static final long serialVersionUID = -5879292575621156720L;

    private Tunnels tunnels;
    private Tunnel tunnel;
    
    public TunnelItem(Tunnels ts, Tunnel t, String prefix){
        tunnels = ts;
        tunnel = t;
        setLabel((prefix == null ? "" : prefix) + (t.isLocal() ? "L" : "R") + " " + t);
        
        addItemListener(this);
    }
    
    public Tunnel getTunnel(){
        return tunnel;
    }

    public void itemStateChanged(ItemEvent ie) {
        if(ie.getStateChange() == ItemEvent.SELECTED) {
            tunnels.tunnelUp(this);
        } else {
            tunnels.tunnelDown(this);
        }
    }
    
}
