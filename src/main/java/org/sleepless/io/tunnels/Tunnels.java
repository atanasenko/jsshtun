package org.sleepless.io.tunnels;

import java.awt.AWTException;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.swing.SwingWorker;

import org.sleepless.io.tunnels.config.Config;
import org.sleepless.io.tunnels.config.Host;
import org.sleepless.io.tunnels.config.Tunnel;

public class Tunnels {
    
    private File configFile;
    private Config config;
    private TrayIcon trayIcon;
    private TunnelManager tManager;
    private ActionListener mAction;
    
    public Tunnels(File configFile) {
        this.configFile = configFile;
        config = new Config();
    }
    
    public void start(){
        
        tManager = new TunnelManager(new File(".known_hosts").getAbsolutePath());
        mAction = new ManageActionListener(this);

        readConfig();
        
        if(!SystemTray.isSupported()) {
            throw new IllegalStateException("SystemTray not supported");
        }
        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        InputStream in = this.getClass().getResourceAsStream("tray.gif");
        byte[] buf = new byte[1024];
        int l;
        try{
            while((l = in.read(buf)) != -1) {
                bout.write(buf, 0, l);
            }
        } catch(IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try{ in.close(); } catch(IOException e){}
        }

        Image img = Toolkit.getDefaultToolkit().createImage(bout.toByteArray());
        trayIcon = new TrayIcon(img);
        try{
            SystemTray st = SystemTray.getSystemTray();
            st.add(trayIcon);
        } catch(AWTException e) {
            throw new IllegalStateException(e);
        }
        
        //trayIcon.addActionListener(mAction);

        update();
    }
    
    public void update() {
        updateTooltip();
        updateMenu();
    }
    
    public void readConfig(){
        try {
            if(configFile.exists()) {
                InputStream fin = new FileInputStream(configFile);
                try{ 
                    config.read(fin);
                } finally {
                    fin.close();
                }
            }
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }

        // configure hosts/tunnels
        if(config.getHosts() != null) {
            for(Host h: config.getHosts()) {
                if(h.getLocalTunnels() != null) {
                    for(Tunnel t: h.getLocalTunnels()) {
                        t.setLocal(true);
                        t.setHost(h);
                        t.setState(State.DOWN);
                    }
                }

                if(h.getRemoteTunnels() != null) {
                    for(Tunnel t: h.getRemoteTunnels()) {
                        t.setLocal(false);
                        t.setHost(h);
                        t.setState(State.DOWN);
                    }
                }
            }
        }
    }
    
    private void editConfig(){
        try{
            Desktop.getDesktop().edit(configFile);
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }
    
    private void updateMenu(){
        PopupMenu pm = new PopupMenu();
        
        if(config.getHosts() != null) {
            for(Host h: config.getHosts()) {
                
                List<Tunnel> lTunnels = h.getLocalTunnels();
                List<Tunnel> rTunnels = h.getRemoteTunnels();
                
                if(lTunnels == null) lTunnels = Collections.emptyList();
                if(rTunnels == null) rTunnels = Collections.emptyList();
                
                int c = lTunnels.size() + rTunnels.size();
                
                if(c == 0) {
                    continue;
                    /*
                } else if(c == 1) {
                    
                    boolean local = lTunnels.size() > 0;
                    
                    Tunnel t = (local ? lTunnels : rTunnels).get(0);
                    
                    String label = 
                        h.getUserInfo().getName() + "@" + 
                        h.getName() + 
                        (h.getPort() == null || h.getPort() == 22 ? "" : ":" + h.getPort()) + 
                        " - ";
                    
                    TunnelItem cmi = new TunnelItem(this, t, label);
                    pm.add(cmi);
                    */
                } else {
                    
                    Menu m = new Menu(
                            h.getUserInfo().getName() + "@" + 
                            h.getName() + 
                            (h.getPort() == null || h.getPort() == 22 ? "" : ":" + h.getPort()));
                    
                    pm.add(m);
                    
                    for(Tunnel t: lTunnels) {
                        TunnelItem ti = new TunnelItem(this, t, null);
                        m.add(ti);
                    }
                    if(lTunnels.size() > 0 && rTunnels.size() > 0) {
                        m.add(new MenuItem("-"));
                    }
                    for(Tunnel t: rTunnels) {
                        TunnelItem ti = new TunnelItem(this, t, null);
                        m.add(ti);
                    }
                    
                }
                
            }
        }
            
        pm.add(new MenuItem("-"));
        
        MenuItem mManage = new MenuItem("Manage...");
        mManage.addActionListener(mAction);
        mManage.setEnabled(false);
        pm.add(mManage);
        
        MenuItem mEdit = new MenuItem("Edit config...");
        mEdit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                editConfig();
            }
        });
        pm.add(mEdit);

        MenuItem mReload = new MenuItem("Reload config");
        mReload.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent arg0) {
                
                tManager.allDown();
                trayIcon.displayMessage(State.DOWN.toString(), "Config reloaded, tunnels down", MessageType.INFO);
                config = new Config();
                readConfig();
                update();
            }
        });
        pm.add(mReload);

        pm.add(new MenuItem("-"));
        
        MenuItem mExit = new MenuItem("Exit");
        mExit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });
        pm.add(mExit);

        trayIcon.setPopupMenu(pm);
    }
    
    private void updateTooltip(){
        
        int lc = 0;
        int rc = 0;
        
        if(config.getHosts() != null) {
            for(Host h: config.getHosts()) {
                lc += tManager.getLocalTunnelCount(h);
                rc += tManager.getRemoteTunnelCount(h);
            }
        }
        
        trayIcon.setToolTip("L: " + lc + ", R: " + rc);
    }
    
    public void tunnelUp(TunnelItem ti) {
        ti.setEnabled(false);
        new TunnelWorker(ti, true).execute();
    }
    
    public void tunnelDown(TunnelItem ti) {
        ti.setEnabled(false);
        new TunnelWorker(ti, false).execute();
    }
    
    private class TunnelWorker extends SwingWorker<Object, Object> {
        
        private TunnelItem ti;
        private boolean up;
        
        public TunnelWorker(TunnelItem ti, boolean up) {
            this.ti = ti;
            this.up = up;
        }

        @Override
        protected Object doInBackground() throws Exception {
            
            if(up) {
                tManager.tunnelUp(ti.getTunnel());
            } else {
                tManager.tunnelDown(ti.getTunnel());
            }
            //JOptionPane.showMessageDialog(null, "Cannot create tunnel", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        @Override
        protected void done() {
            ti.setEnabled(true);
            boolean up = ti.getTunnel().getState() == State.UP;
            ti.setState(up);
            
            updateTooltip();
            
            if(this.up == up) {
                trayIcon.displayMessage(ti.getTunnel().getState().toString(), getTunnelDesc(ti.getTunnel()), MessageType.INFO);
            } else {
                trayIcon.displayMessage(ti.getTunnel().getState().toString(), getTunnelDesc(ti.getTunnel()), MessageType.ERROR);
            }
            
        }
        
    }
    
    public static String getTunnelDesc(Tunnel t){
        Host h = t.getHost();
        
        return 
            h.getUserInfo().getName() + "@" + 
            h.getName() + 
            (h.getPort() == null || h.getPort() == 22 ? "" : ":" + h.getPort()) + "\n" + t;
    }
}
