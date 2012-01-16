package org.sleepless.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MemberAccessor {
    
    public static Field getField(Class<?> cl, String name) throws NoSuchFieldException {
        
        try{
            Field f = cl.getDeclaredField(name);
            
            if(f != null) {
                f.setAccessible(true);
                return f;
            }
        } catch(NoSuchFieldException e){}
        
        Class<?> scl = cl.getSuperclass();
        if(scl == null) {
            throw new NoSuchFieldException(name);
        }
        
        return getField(scl, name);
    }
    
    public static List<Field> getFields(Class<?> cl) {
        
        List<Field> list = new ArrayList<Field>();
        
        while(cl != null){
        
            for(Field f: cl.getDeclaredFields()) {
                f.setAccessible(true);
                list.add(f);
            }
            
            cl = cl.getSuperclass();
        }
        
        return list;
    }
    
    public static boolean hasField(Class<?> cl, String name) {
        try{
            Field f = cl.getDeclaredField(name);
            
            if(f != null) {
                return true;
            }
        } catch(NoSuchFieldException e){}
        
        Class<?> scl = cl.getSuperclass();
        if(scl == null) {
            return false;
        }
        
        return hasField(scl, name);
    }

    public static Method getMethod(Class<?> cl, String name, Class<?> ... params) throws NoSuchMethodException {
        
        try{
            Method m = cl.getDeclaredMethod(name, params);
            
            if(m != null) {
                m.setAccessible(true);
                return m;
            }
        } catch(NoSuchMethodException e){}
        
        Class<?> scl = cl.getSuperclass();
        if(scl == null) {
            throw new NoSuchMethodException(name);
        }
        
        return getMethod(scl, name, params);
    }
    
    public static List<Method> getMethods(Class<?> cl) {
        
        List<Method> list = new ArrayList<Method>();
        
        while(cl != null){
        
            for(Method m: cl.getDeclaredMethods()) {
                m.setAccessible(true);
                list.add(m);
            }
            
            cl = cl.getSuperclass();
        }
        
        return list;
    }

    public static boolean hasMethod(Class<?> cl, String name, Class<?> ... params) throws NoSuchMethodException {
        
        try{
            Method m = cl.getDeclaredMethod(name, params);
            
            if(m != null) {
                return true;
            }
        } catch(NoSuchMethodException e){}
        
        Class<?> scl = cl.getSuperclass();
        if(scl == null) {
            return false;
        }
        
        return hasMethod(scl, name, params);
    }
    

}
