package org.sleepless.io.tunnels.config;

import org.sleepless.io.tunnels.State;

public class Tunnel {
    
    private int localPort;
    private String hostName;
    private int remotePort;
    
    private transient boolean local;
    private transient Host host;
    private transient State state;
    
    public int getLocalPort() {
        return localPort;
    }
    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }
    
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }
    
    public int getRemotePort() {
        return remotePort;
    }
    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
    
    public boolean isLocal() {
        return local;
    }
    public void setLocal(boolean local) {
        this.local = local;
    }

    public Host getHost() {
        return host;
    }
    public void setHost(Host host) {
        this.host = host;
    }

    public State getState() {
        return state;
    }
    public void setState(State state) {
        this.state = state;
    }

    public String toString(){
        return localPort + ":" + hostName + ":" + remotePort;
    }
    
    public int hashCode(){
        return (host + ":" + toString()).hashCode();
    }
    
    public boolean equals(Object o){
        if(o instanceof Tunnel) {
            Tunnel t = (Tunnel) o;
            return host.equals(t.host) && toString().equals(t.toString());
        }
        return false;
    }
    
}
