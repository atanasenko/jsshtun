package org.sleepless.io.tunnels.config;

import java.util.List;

public class Host {
    
    private String name;
    private Integer port;
    private UserInfo userInfo;
    private Integer keepAliveInterval;
    private List<Tunnel> localTunnels;
    private List<Tunnel> remoteTunnels;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }
    
    public UserInfo getUserInfo() {
        return userInfo;
    }
    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
    
    public Integer getKeepAliveInterval() {
        return keepAliveInterval;
    }
    public void setKeepAliveInterval(Integer keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }
    
    public List<Tunnel> getLocalTunnels() {
        return localTunnels;
    }
    public void setLocalTunnels(List<Tunnel> localTunnels) {
        this.localTunnels = localTunnels;
    }

    public List<Tunnel> getRemoteTunnels() {
        return remoteTunnels;
    }
    public void setRemoteTunnels(List<Tunnel> remoteTunnels) {
        this.remoteTunnels = remoteTunnels;
    }
    
    public String toString(){
        return name + ":" + port + "[" + userInfo + "]";
    }
    
    public int hashCode(){
        return toString().hashCode();
    }
    
    public boolean equals(Object o){
        if(o instanceof Host) {
            Host h = (Host) o;
            return toString().equals(h.toString());
        }
        return false;
    }
}
