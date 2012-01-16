package org.sleepless.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropChain {
    
    private String[] chain;
    private int idx;
    
    private static Pattern P = Pattern.compile("\\[[^\\]]*\\]|[^\\.]+");
    
    public PropChain(String prop) {
        Matcher m = P.matcher(prop);
        
        List<String> l = new ArrayList<String>();
        while(m.find()) {
            l.add(m.group());
        }
        
        chain = l.toArray(new String[l.size()]);
        idx = -1;
    }
    
    public PropChain(PropChain c, String prop) {
        
        List<String> l = new ArrayList<String>();
        if(c != null) {
            for(String s: c.chain) {
                l.add(s);
            }
        }
        l.add(prop);
        
        chain = l.toArray(new String[l.size()]);
        idx = -1;
    }
    
    private static String encode(String s){
        if(s.contains(".")) return String.format("[%s]", s);
        return s;
    }
    
    public String get(){
        return chain[idx];
    }
    
    public String head(){
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        for(int i = 0; i < idx; i++) {
            if(f) f = !f;
            else sb.append(".");
            sb.append(encode(chain[i]));
        }
        
        return sb.toString();
    }
    
    public String addr(){
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        for(int i = 0; i <= idx; i++) {
            if(f) f = !f;
            else sb.append(".");
            sb.append(encode(chain[i]));
        }
        
        return sb.toString();
    }
    
    public String tail(){
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        for(int i = idx + 1; i < chain.length; i++) {
            if(f) f = !f;
            else sb.append(".");
            sb.append(encode(chain[i]));
        }
        
        return sb.toString();
    }
    
    public String whole(){
        StringBuilder sb = new StringBuilder();
        boolean f = true;
        for(String s: chain) {
            if(f) f = !f;
            else sb.append(".");
            sb.append(encode(s));
        }
        
        return sb.toString();        
    }
    
    public void next(){
        if(!hasNext()) throw new IllegalStateException("No next");
        idx++;
    }
    
    public void prev(){
        if(!hasPrev()) throw new IllegalStateException("No prev");
        idx--;
    }
    
    public boolean hasNext(){
        return idx < chain.length - 1;
    }
    
    public boolean hasPrev(){
        return idx > 0;
    }
    
    public String toString(){
        return whole();
    }

}
