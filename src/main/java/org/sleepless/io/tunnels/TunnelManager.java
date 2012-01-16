package org.sleepless.io.tunnels;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sleepless.io.tunnels.config.Host;
import org.sleepless.io.tunnels.config.Tunnel;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class TunnelManager {
    
    private JSch jsch;
    
    private Map<Host, Session> sessions;
    
    public TunnelManager(String knownHostsFile){
        jsch = new JSch();
        sessions = new HashMap<Host, Session>();
        try {
            jsch.setKnownHosts(knownHostsFile);
        } catch(JSchException e) {
            throw new IllegalStateException(e);
        }
    }
    
    public void allDown(){
        for(Map.Entry<Host, Session> e: sessions.entrySet()) {
            Host h = e.getKey();
            if(h.getLocalTunnels() != null) {
                for(Tunnel t: h.getLocalTunnels()) {
                    t.setState(State.DOWN);
                }
            }
            if(h.getRemoteTunnels() != null) {
                for(Tunnel t: h.getRemoteTunnels()) {
                    t.setState(State.DOWN);
                }
            }
            
            e.getValue().disconnect();
        }
        sessions.clear();
    }

    public void tunnelUp(Tunnel t) {
        if(!t.isLocal()) {
            // TODO
            return;
        }
        //System.out.println("Tunnel up: " + t.getHost() + " - " + t);
        t.setState(State.GOING_UP);
        
        Host h = t.getHost();
        Session s = sessions.get(h);
        try{
            
            if(s == null) {
                s = jsch.getSession(h.getUserInfo().getName(), h.getName(), h.getPort() == null ? 22 : h.getPort());
                s.setUserInfo(new MyUserInfo());
                s.setPassword(h.getUserInfo().getPassword());
                if(h.getKeepAliveInterval() != null) {
                    s.setServerAliveInterval(h.getKeepAliveInterval());
                    s.setServerAliveCountMax(Integer.MAX_VALUE);
                }
                s.connect();
                sessions.put(h, s);
                
            }
            s.setPortForwardingL(t.getLocalPort(), t.getHostName(), t.getRemotePort());
            t.setState(State.UP);
            
        } catch(JSchException e) {
            //e.printStackTrace();
            t.setState(State.DOWN);
        } finally {
        
            int c = getRemoteTunnelCount(h) + getLocalTunnelCount(h);
            if(c == 0 && s != null) {
                s.disconnect();
                sessions.remove(h);
            }
        }
        
    }
    
    public void tunnelDown(Tunnel t) {
        if(!t.isLocal()) {
            // TODO
            return;
        }
        //System.out.println("Tunnel down: " + t.getHost() + " - " + t);
        t.setState(State.GOING_DOWN);
        
        Host h = t.getHost();
        Session s = sessions.get(h);
        
        try{
            if(s != null) {
                s.delPortForwardingL(t.getLocalPort());
                t.setState(State.DOWN);
            }
        } catch(JSchException e) {
            //e.printStackTrace();
            t.setState(State.DOWN);
        } finally {
            int c = getRemoteTunnelCount(h) + getLocalTunnelCount(h);
            if(c == 0 && s != null) {
                s.disconnect();
                sessions.remove(h);
            }
        }
    }
    
    public int getRemoteTunnelCount(Host h) {
        int c = 0;
        
        List<Tunnel> rTunnels = h.getRemoteTunnels();
        
        if(rTunnels == null) rTunnels = Collections.emptyList();
        
        for(Tunnel t: rTunnels) {
            if(t.getState() == State.UP) c++;
        }
        
        return c;
    }
    
    public int getLocalTunnelCount(Host h) {
        int c = 0;
        
        List<Tunnel> lTunnels = h.getLocalTunnels();
        
        if(lTunnels == null) lTunnels = Collections.emptyList();
        
        for(Tunnel t: lTunnels) {
            if(t.getState() == State.UP) c++;
        }
        
        return c;
    }
    
}
