package org.sleepless.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;


public class Configurer {
    
    private static final Map<Class<?>, Class<?>> DEFAULT_CONCRETES = new HashMap<Class<?>, Class<?>>();
    static {
        
        DEFAULT_CONCRETES.put(Collection.class, ArrayList.class);
        DEFAULT_CONCRETES.put(List.class, ArrayList.class);
        DEFAULT_CONCRETES.put(Set.class, LinkedHashSet.class);
        DEFAULT_CONCRETES.put(Map.class, LinkedHashMap.class);
        
    }

    private static final Set<Class<?>> DEFAULT_INLINES = new HashSet<Class<?>>();
    static {
        
        DEFAULT_INLINES.add(Number.class);
        DEFAULT_INLINES.add(CharSequence.class);
        DEFAULT_INLINES.add(Boolean.class);
        
    }

    private Map<String, Object> addrs;
    private Properties props;
    
    private Map<String, Type> typeInfo;
    
    private Map<Class<?>, Class<?>> concretes;
    private Set<Class<?>> inlines;
    
    private Set<Object> deconfigured;
    
    public Configurer(Object obj, Properties props){
        this.props = props;
        addrs = new HashMap<String, Object>();
        typeInfo = new HashMap<String, Type>();
        
        concretes = new HashMap<Class<?>, Class<?>>();
        concretes.putAll(DEFAULT_CONCRETES);
        
        inlines = new HashSet<Class<?>>();
        inlines.addAll(DEFAULT_INLINES);
        
        deconfigured = new HashSet<Object>();

        addrs.put("", obj);
    }
    
    
    public <I> void setConcreteClass(Class<I> iface, Class<? extends I> cl){
        if(cl.isInterface() || (cl.getModifiers() & Modifier.ABSTRACT) != 0) {
            throw new IllegalArgumentException("Must specify concrete class");
        }
        
        boolean cons = false;
        try{
            if(cl.getConstructor(new Class[0]) != null) {
                cons = true;
            }
        } catch(Exception e){}
        if(!cons) throw new IllegalArgumentException("Concrete class must have a no-arg constructor");
        
        concretes.put(iface, cl);
    }
    
    public void addInline(Class<?> cl) {
        inlines.add(cl);
    }
    
    public void configure(){
        
        for(Entry<Object, Object> e: props.entrySet()){
            String key = e.getKey().toString();
            Object value = e.getValue();
            
            setProperties(new PropChain(key), value);
        }
        
    }
    
    public void deconfigure(){
        deconfigure(null, addrs.get(""));
    }
    
    private void deconfigure(PropChain c, Object parent) {

        if(deconfigured.contains(parent)) {
            // TODO listener
            return;
        }
        
        Class<?> cl = parent.getClass();
        
        boolean inline = cl.isPrimitive();
        if(!inline) {
            for(Class<?> ci: inlines) {
                if(ci.isAssignableFrom(cl)) {
                    inline = true;
                    
                }
            }
        }
        System.out.println("*** inline: " + cl + ", " + inline);
        
        if(inline) {
            
            props.setProperty(c.toString(), parent.toString());
            return;
        } 
    

        
        deconfigured.add(parent);
        
        if(parent instanceof Map) {
            
            deconfigureMap(c, (Map<?,?>)parent);
        
        } else if(parent instanceof Collection) {
            
            deconfigureCollection(c, (Collection<?>) parent);
        
        } else {
        
            for(Field f: MemberAccessor.getFields(parent.getClass())){
                
                if(Modifier.isTransient(f.getModifiers())){
                    continue;
                }
                
                String name = f.getName();
                //Class<?> cl = f.getType();
                
                Object obj = null;
                try {
                    obj = f.get(parent);
                } catch(Exception e) {}
                
                if(obj != null) {
                    
                    PropChain pc = new PropChain(c, name);
                    
                    if((obj instanceof Map) || (obj instanceof Collection)){
                        typeInfo.put(c == null ? "" : c.toString(), f.getGenericType());
                    }
                    
                    deconfigure(pc, obj);
                    
                }
                
            }
            
        }
        
    }
    
    private void deconfigureCollection(PropChain c, Collection<?> parent) {
        int i = 0;
        for(Object o: parent) {
            deconfigure(new PropChain(c, String.valueOf(i++)), o);
        }
    }


    private void deconfigureMap(PropChain c, Map<?, ?> parent) {
        for(Map.Entry<?, ?> e: parent.entrySet()) {
            deconfigure(new PropChain(c, e.getKey().toString()), e.getValue());
        }
    }


    private void setProperties(PropChain c, Object value){
        
        while(c.hasNext()) {
            c.next();
            
            String prop = c.get();
            String paddr = c.head();
            Object parent = addrs.get(paddr);
            //System.out.println(" -- " + prop + ", " + paddr + ", p: " + parent);
            
            // property on parent object this object resides in
            // next

            if(parent != null) {
                
                if(c.hasNext()) {
                    // properties of this object would be set, if there are any next
                    // address of this object
                    String addr = c.addr();
                    
                    // this object
                    Object o = addrs.get(addr);
                    
                    if(o == null) {
                        // create this object based on its location within parent object
                        o = createObject(paddr, addr, prop);

                        if(o != null) {
                            // position it within parent
                            setObject(paddr, prop, o);
                        }
                    }
                
                } else {
                    
                    // position the value
                    //System.out.println(" -- Setting " + prop + " to " + value);
                    setObject(paddr, prop, value);
                }
            }
        }
        
    }
    
    private Object createObject(String parentAddr, String addr, String property) {
        
        //System.out.println(" ** Creating " + addr);
        
        Object o = null;
        Object parent = addrs.get(parentAddr);
        Type ptype = typeInfo.get(parentAddr);
        
        if(parent instanceof Map) {
            
            o = createInstance(getParameterClass(ptype, 1));
            
        } else if(parent instanceof Collection) {
            
            o = createInstance(getParameterClass(ptype, 0));
            
        } else {
        
            try{
                Field f = MemberAccessor.getField(parent.getClass(), property);
                
                /*
                int mod = f.getModifiers();
                if(Modifier.isFinal(mod) || Modifier.isStatic(mod) || Modifier.isTransient(mod)) {
                    return null;
                }
                */
                
                f.setAccessible(true);
    
                o = createInstance(f.getType());
                
                if((o instanceof Map) || (o instanceof Collection)){
                    Type t = f.getGenericType();
                    typeInfo.put(addr, t);
                }
            } catch(NoSuchFieldException e) {
                // TODO listener
                //throw new IllegalStateException(e);
            }
        }
        if(o != null) addrs.put(addr, o);
        //System.out.println(" ** " + (o == null ? null : o.getClass().getName()));
        return o;
    }
    
    private Object createInstance(Class<?> cl) {
        if(concretes.containsKey(cl)) {
            cl = concretes.get(cl);
        }
        
        try {
            return cl.newInstance();
        } catch (Exception e) {
            // TODO listener
            //throw new IllegalStateException(e);
        }
        return null;
    }
    
    private void setObject(String addr, String prop, Object value) {
        Object o = addrs.get(addr);
        
        if(o instanceof Map) {
        
            @SuppressWarnings("unchecked")
            Map<Object, Object> m = (Map<Object, Object>) o;
            Type ptype = typeInfo.get(addr);
            Class<?> cl = getParameterClass(ptype, 1);
            value = TypeCoercer.coerce(value, cl);
            
            Class<?> kcl = getParameterClass(ptype, 0);
            Object key = TypeCoercer.coerce(prop, kcl);
            
            m.put(key, value);

        } else if(o instanceof Collection) {

            @SuppressWarnings("unchecked")
            Collection<Object> c = (Collection<Object>) o;
            Type ptype = typeInfo.get(addr);
            Class<?> cl = getParameterClass(ptype, 0);
            value = TypeCoercer.coerce(value, cl);
            c.add(value);
            
        } else {
            
            try{
                Field f = o.getClass().getDeclaredField(prop);
                f.setAccessible(true);
                value = TypeCoercer.coerce(value, f.getType());
                f.set(o, value);
            } catch(Exception e) {
                // TODO listener
                //throw new IllegalArgumentException(e);
            }
            
        }
        
    }
    
    private static Class<?> getParameterClass(Type t, int idx) {
        if(t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            
            Type[] pts = pt.getActualTypeArguments();
            Type compType = pts.length > idx ? pts[idx] : null;
            if(compType != null) {
                
                if(compType instanceof Class) {
                    return (Class<?>) compType;
                }
                
                if(compType instanceof ParameterizedType) {
                    pt = (ParameterizedType) compType;
                    return (Class<?>) pt.getRawType();
                }
            }
        }
        
        return null;
    }
    
}
