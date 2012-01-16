package org.sleepless.util;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TypeCoercer {

    public interface TypeCoercion<K, L> {
        public L coerce(K k);
    }
    
    private static final Map<Class<?>, Map<Class<?>, TypeCoercion<?, ?>>> coercions;
    
    static {
        coercions = new HashMap<Class<?>, Map<Class<?>, TypeCoercion<?, ?>>>();
        
        // * -> Object (default coercion)
        addCoercion(Object.class, Object.class, new TypeCoercion<Object, Object>(){
            public Object coerce(Object k) {
                return k;
            }
            public String toString() { return "* -> Object"; }
        });
        
        // * -> String
        addCoercion(Object.class, String.class, new TypeCoercion<Object, String>(){
            public String coerce(Object k) {
                return k == null ? null : k.toString();
            }
            public String toString() { return "* -> String"; }
        });
        
        // String -> byte[]
        addCoercion(String.class, byte[].class, new TypeCoercion<String, byte[]>(){
            public byte[] coerce(String k) {
                return k == null ? null : k.getBytes();
            }
            public String toString() { return "String -> byte[]"; }
        });

        // String -> Char
        addCoercion(String.class, Character.class, new TypeCoercion<String, Character>(){
            public Character coerce(String k) {
                if(k == null) {
                    return null;
                }
                if(k.length() != 1) {
                    throw new IllegalArgumentException("String " + k + " cannot be coerced to character");
                }
                return k.charAt(0);
            }
            public String toString() { return "String -> Character"; }
        });

        // String -> Integer
        addCoercion(String.class, Integer.class, new TypeCoercion<String, Integer>(){
            public Integer coerce(String k) {
                return k == null ? null : Integer.valueOf(k);
            }
            public String toString() { return "String -> Integer"; }
        });
        
        // String -> Long
        addCoercion(String.class, Long.class, new TypeCoercion<String, Long>(){
            public Long coerce(String k) {
                return k == null ? null : Long.valueOf(k);
            }
            public String toString() { return "String -> Long"; }
        });
        
        // String -> Float
        addCoercion(String.class, Float.class, new TypeCoercion<String, Float>(){
            public Float coerce(String k) {
                return k == null ? null : Float.valueOf(k);
            }
            public String toString() { return "String -> Float"; }
        });
        
        // String -> Double
        addCoercion(String.class, Double.class, new TypeCoercion<String, Double>(){
            public Double coerce(String k) {
                return k == null ? null : Double.valueOf(k);
            }
            public String toString() { return "String -> Double"; }
        });
        
        // Date -> Long
        TypeCoercer.addCoercion(Date.class, Long.class, new TypeCoercer.TypeCoercion<Date, Long>(){
            public Long coerce(Date k) {
                return k.getTime();
            }
            public String toString() { return "Date -> Long"; }
        });
        
        // Long -> Date
        TypeCoercer.addCoercion(Long.class, Date.class, new TypeCoercer.TypeCoercion<Long, Date>(){
            public Date coerce(Long k) {
                return new Date(k);
            }
            public String toString() { return "Long -> Date"; }
        });

        // String -> Timestamp
        TypeCoercer.addCoercion(String.class, Timestamp.class, new TypeCoercer.TypeCoercion<String, Timestamp>(){
            public Timestamp coerce(String k) {
                return Timestamp.valueOf(k);
            }
            public String toString() { return "String -> Timestamp"; }
        });

        // Long -> Timestamp
        TypeCoercer.addCoercion(Long.class, Timestamp.class, new TypeCoercer.TypeCoercion<Long, Timestamp>(){
            public Timestamp coerce(Long k) {
                return new Timestamp(k);
            }
            public String toString() { return "Long -> Timestamp"; }
        });
    }
    
    public static <K, L> L coerce(K o, Class<L> to) {

        //System.out.println("Coercing " + o + " to " + to);
        
        if(o == null) return null;
        
        Class<?> cl = box(to);
        
        if(to.isAssignableFrom(o.getClass())) {
            return to.cast(o);
        }
        
        @SuppressWarnings("unchecked")
        TypeCoercion<K, L> c = (TypeCoercion<K, L>) findCoercion(o.getClass(), cl);
        if(c == null){
            throw new IllegalArgumentException("Cannot coerce " + o.getClass().getName() + " to " + cl.getName());
        }
        
        //System.out.println("Coercing: " + o + " to " + to.getSimpleName() +" (" + c + ")");
        try{
            return c.coerce(o);
        } catch(Exception e) {
            throw new IllegalStateException("Coercer " + c.toString() + " has thrown an exception", e);
        }
    }
    
    
    public static <K, L> void addCoercion(Class<K> from, Class<L> to, TypeCoercion<K, L> c) {
        Map<Class<?>, TypeCoercion<?, ?>> m = coercions.get(box(from));
        if(m == null) {
            coercions.put(box(from), m = new HashMap<Class<?>, TypeCoercion<?,?>>());
        }
        m.put(box(to), c);
    }
    
    private static TypeCoercion<?, ?> findCoercion(Class<?> from, Class<?> to) {
        
        TypeCoercion<?, ?> c = null;

        // find direct
        Map<Class<?>, TypeCoercion<?, ?>> m;
        //System.out.println("Searching from " + cl);
        Class<?> fromc = from;
        do {
            m = coercions.get(fromc);
            //System.out.println("Getting from " + cl);

            //System.out.println("From: " + m);
            if(m != null) {
                c = findTo(to, m);
            }
            
            fromc = fromc.getSuperclass();
        } while(c == null && fromc != null);
        //System.out.println("Found " + m);
        
        // find chain
        if(c == null) {
            //System.out.println("No direct coercers found, finding chained");
            c = createChain(from, to);
            if(c != null) {
                // add chain to static list
                if(m == null) {
                    coercions.put(from, m = new HashMap<Class<?>, TypeCoercion<?,?>>());
                }
                m.put(to, c);
            }
        }
        
        return c;
    }

    private static TypeCoercion<?, ?> findTo(Class<?> cl, Map<Class<?>, TypeCoercion<?, ?>> m) {
        if(m.containsKey(cl)) {
            return m.get(cl);
        }
        
        for(Map.Entry<Class<?>, TypeCoercion<?, ?>> e: m.entrySet()) {
            if(cl.isAssignableFrom(e.getKey())){
                return e.getValue();
            }
        }
        
        return null;
    }

    private static Class<?> box(Class<?> cl) {
        // box primitive type
        if(cl.isPrimitive()) {
            if(cl == Integer.TYPE)        cl = Integer.class;
            else if(cl == Long.TYPE)      cl = Long.class;
            else if(cl == Byte.TYPE)      cl = Byte.class;
            else if(cl == Character.TYPE) cl = Character.class;
            else if(cl == Float.TYPE)     cl = Float.class;
            else if(cl == Double.TYPE)    cl = Double.class;
        }

        return cl;
    }
    
    private static ChainedCoercion createChain(Class<?> from, Class<?> to) {
        CoercionChain chain = findChain(null, from, to, Integer.MAX_VALUE);
        if(chain == null) return null;
        //System.out.println("The winner is: (" + chain.getSize() + "): " + chain);
        return new ChainedCoercion(chain);
    }
    
    private static CoercionChain findChain(CoercionChain parentChain, Class<?> from, Class<?> to, int maxSize) {
        
        Map<Class<?>, TypeCoercion<?, ?>> m;
        
        CoercionChain theChain = null;
        
        do {
            //System.out.println("Finding chain: " + from + " -> " + to);
            m = coercions.get(from);
            
            if(m != null) {
                
                TypeCoercion<?, ?> c = findTo(to, m);
                if(c != null) {
                    //System.out.println("End found: " + from + " -> " + to);
                    return new CoercionChain(parentChain, c, from, to);
                }
                
                if(parentChain != null && parentChain.getSize() + 1 >= maxSize) {
                    //System.out.println("Dropping from search due to maxSize restriction");
                    return null;
                }
                for(Map.Entry<Class<?>, TypeCoercion<?, ?>> e: m.entrySet()) {
                    // for each branch
                    if(parentChain != null && parentChain.containsFrom(e.getKey())) {
                        // excluding already chained coercions
                        //System.out.println(e.getKey().getName() + " already covered in the chain");
                        continue;
                    }
                    CoercionChain chain = new CoercionChain(parentChain, e.getValue(), from, e.getKey());
                    
                    //System.out.println("Recursing: " + chain.fromClass + " -> " + chain.toClass);
                    chain = findChain(chain, e.getKey(), to, maxSize);
                    if(chain == null) continue;
                    
                    //System.out.println(" !!! Chain found (" + chain.getSize() + "): " + chain);
                    if(chain.getSize() < maxSize) {
                        maxSize = chain.getSize();
                        theChain = chain;
                    }
                }
            }
            
            from = from.getSuperclass();
        } while(theChain == null && from != null);
        
        return theChain;
    }
    
    private static class CoercionChain {
        
        final CoercionChain parent;
        final TypeCoercion<?, ?> coercion;
        final Class<?> fromClass;
        final Class<?> toClass;
        
        final Class<?> parentFromClass;
        final int pSize;
        
        CoercionChain(CoercionChain parent, TypeCoercion<?, ?> coercion, Class<?> fromClass, Class<?> toClass) {
            this.parent = parent;
            this.coercion = coercion;
            this.fromClass = fromClass;
            this.toClass = toClass;
            
            parentFromClass = parent == null ? fromClass : parent.parentFromClass;
            pSize = parent == null ? 0 : parent.getSize();
        }

        public int getSize(){
            return pSize + 1;
        }
        
        public boolean containsFrom(Class<?> cl){
            if(this.parentFromClass.equals(cl)) return true;
            if(this.toClass.equals(cl)) return true;
            if(parent == null) return false;
            return parent.containsFrom(cl);
        }
        
        public String toString(){
            return (parent == null ? "" : parent.toString() + ", ") + fromClass.getSimpleName() + " -> " + toClass.getSimpleName();
        }
    }
    
    private static class ChainedCoercion implements TypeCoercion<Object, Object> {

        private CoercionChain chain;

        public ChainedCoercion(CoercionChain chain) {
            this.chain = chain;
        }
        
        public Object coerce(Object k) {
            return coerce(this.chain, k);
        }
        
        @SuppressWarnings("unchecked")
        private Object coerce(CoercionChain chain, Object o) {
            return ((TypeCoercion<Object, ?>)chain.coercion).coerce(chain.parent == null ? o : coerce(chain.parent, o));
        }
        
        public String toString(){
            return chain.toString();
        }
        
    }
}
