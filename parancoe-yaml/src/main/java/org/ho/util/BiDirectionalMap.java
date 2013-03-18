/**
 * Copyright (C) 2006-2010 The Parancoe Team <info@parancoe.org>
 *
 * This file is part of Parancoe Yaml - DISCONTINUED.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ho.util;

import java.util.HashMap;
import java.util.Map;

/**
 * BiDirectionalMap provides a getReverse() method in addition to the HashMap to
 * get a reverse mapping.
 * 
 * @param <K>
 *            the type of the keys
 * @param <V>
 *            the type of the values
 */
// TODO SOSTITUIRE CON org.apache.commons.collections.BidiMap
public class BiDirectionalMap<K, V> extends HashMap<K, V> {

    HashMap<V, K> reverse = new HashMap<V, K>();

    @Override
    public V put(K key, V value) {
        reverse.put(value, key);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        V v = super.remove(key);
        reverse.remove(v);
        return v;
    }

    public Map<V, K> getReverse() {
        return reverse;
    }

}
