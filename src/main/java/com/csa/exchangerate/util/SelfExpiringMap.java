package com.csa.exchangerate.util;

import java.util.Map;

/**
*
* @author Brian Zied
* @param <K> the Key type
* @param <V> the Value type
*/
public interface SelfExpiringMap<K, V> extends Map<K, V> {

   /**
    * Renews the specified key, setting the life time to the initial value.
    *
    * @param key
    * @return true if the key is found, false otherwise
    */
   public boolean renewKey(K key);

   /**
    * Expires the specified key.
    *
    * @param key
    */
   public void expireValue(V value);

   /**
    * Expires the specified key after specified time in milliseconds.
    *
    * @param value
    * @param timeInMillis
    */
   public void expireValueAfter(V value, long timeInMillis);

   /**
    * Expires the specified key.
    *
    * @param key
    */
   public void expireKey(K key);
   
   /**
    * Expires the specified key after specified time in milliseconds.
    *
    * @param key
    * @param timeInMillis
    */
   public void expireKeyAfter(K key, long timeInMillis);
   
   /**
    * Associates the given key to the given value in this map, with the specified life
    * times in milliseconds.
    *
    * @param key
    * @param value
    * @param lifeTimeMillis
    * @return a previously associated object for the given key (if exists).
    */
   public V put(K key, V value, long lifeTimeMillis);
       
}