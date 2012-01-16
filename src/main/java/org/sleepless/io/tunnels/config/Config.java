package org.sleepless.io.tunnels.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import org.sleepless.util.Configurer;
import org.sleepless.util.TypeCoercer;

public class Config {
    
    private List<Host> hosts;
    private String knownHosts;
    
    public List<Host> getHosts() {
        return hosts;
    }
    public void setHosts(List<Host> hosts) {
        this.hosts = hosts;
    }

    public String getKnownHosts() {
        return knownHosts;
    }
    public void setKnownHosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }

    public void read(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        Configurer c = new Configurer(this, props);
        TypeCoercer.addCoercion(String.class, Tunnel.class, new TypeCoercer.TypeCoercion<String, Tunnel>(){

            public Tunnel coerce(String k) {
                Tunnel t = new Tunnel();
                
                String[] s = k.split(":");
                t.setLocalPort(Integer.parseInt(s[0]));
                t.setHostName(s[1]);
                t.setRemotePort(Integer.parseInt(s[2]));
                
                return t;
            }
            
        });
        c.configure();
        
    }
    
    public void write(OutputStream out) throws IOException {
        Properties props = new Properties();
        
        Configurer c = new Configurer(this, props);
        c.addInline(Tunnel.class);
        c.deconfigure();
        
        props.store(out, null);
    }
    
}
