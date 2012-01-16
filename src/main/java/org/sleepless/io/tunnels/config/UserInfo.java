package org.sleepless.io.tunnels.config;

public class UserInfo {

    private String name;
    private String password;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String toString(){
        return name + "/" + password;
    }
    
}
