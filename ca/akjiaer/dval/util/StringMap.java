/*
 * Copyright (c) 2011, Stefan Neubert <akjiaer@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.akjiaer.dval.util;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * @author Stefan Neubert
 * @version 1.1 2011-04-14
 * @since 0.10.0
 */
public class StringMap implements Iterable<StringMap.Entry> {

    private static int hash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }

    private static int indexFor(final int h, final int length) {
        return h & (length-1);
    }

 /* --------------------------- StringMap ---------------------------------- */

    private final static int MAXIMUM_CAPACITY = 1 << 30;
    private final float loadFactor;
    private Entry[] table;
    private int threshold;
    private int size;

    public StringMap() {
        table = new Entry[16];
        loadFactor = 0.75f;
        threshold = (int) (16 * 0.75f);
    }

    private void assureSize(final int keyCount) {
        if (keyCount > threshold) {
            int targetCapacity = (int) (keyCount / loadFactor + 1);
            if (targetCapacity > MAXIMUM_CAPACITY) {
                targetCapacity = MAXIMUM_CAPACITY;
            }
            int newCapacity = table.length;
            while (newCapacity < targetCapacity) {
               newCapacity <<= 1;
            }
            if (newCapacity > table.length) {
               resize(newCapacity);
            }
        }
    }

    public void clear() {
        for (int i = 0; i < table.length; i++) {
            table[i] = null;
        }
        size = 0;
    }

    public boolean contains(final String key) {
        return getEntry(key) != null;
    }

    public String get(final String key) {
        Entry e = getEntry(key);
        return e == null ? null : e.value;
    }

    protected Entry getEntry(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null!");
        }
        final int hash = hash(key.hashCode());
        for (Entry e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
               return e;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<Entry> iterator() {
        return new EntryIterator();
    }

    public void put(final String key, final String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null!");
        }
        final int hash = hash(key.hashCode());
        final int bucketIndex = indexFor(hash, table.length);
        for (Entry e = table[bucketIndex]; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                e.value = value;
                return;
            }
        }

        final Entry e = table[bucketIndex];
        table[bucketIndex] = new Entry(hash, key, value, e);
        if (size++ >= threshold) {
            resize(2 * table.length);
        }
    }

    public void putAll(final Map<String, String> map) {
        if (map.isEmpty()) return;
        assureSize(map.size());

        String k;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if ((k = e.getKey()) != null) {
                put(k, e.getValue());
            }
        }
    }

    public void putAll(final Properties p) {
        if (p.isEmpty()) return;
        assureSize(p.size());

        Object ok, ov = null;
        for (Map.Entry<Object, Object> e : p.entrySet()) {
            if ((ok = e.getKey()) instanceof String &&
                (ov = e.getValue()) instanceof String || ov == null) {
                put((String) ok, (String) ov);
            }
        }
    }

    public void putAll(final StringMap sm) {
        if (sm.isEmpty()) return;
        assureSize(sm.size());

        for (StringMap.Entry e : sm) {
            put(e.key, e.value);
        }
    }

    public void remove(final String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null!");
        }
        final int hash = hash(key.hashCode());
        final int i = indexFor(hash, table.length);
        Entry prev = table[i];
        Entry e = prev;
        Entry next;

        while (e != null) {
            next = e.next;
            if (e.hash == hash && key.equals(e.key)) {
                size--;
                if (prev == e) {
                    table[i] = next;
                } else {
                    prev.next = next;
                }
            }
            prev = e;
            e = next;
        }
    }

    private void resize(final int newCapacity) {
        final Entry[] oldT = table;
        if (oldT.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        final Entry[] newT = new Entry[newCapacity];
        Entry e, next;
        for (int i = 0; i < oldT.length; i++) {
            e = oldT[i];
            if (e != null) {
                oldT[i] = null;
                do {
                    next = e.next;
                    final int j = indexFor(e.hash, newCapacity);
                    e.next = newT[j];
                    newT[j] = e;
                    e = next;
                } while (e != null);
            }
        }
        table = newT;
        threshold = (int) (newCapacity * loadFactor);
    }

    public int size() {
        return size;
    }

 /* ------------------------------ Entry ----------------------------------- */

    public class Entry {

        protected final String key;
        protected final int hash;
        protected String value;
        protected Entry next;

        private Entry(int hash, String key, String value, Entry next) {
            this.value = value;
            this.next = next;
            this.hash = hash;
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public final boolean equals(final Object o) {
            if (o == null) return false;
            if (o == this) return true;
            if (o instanceof Entry) {
                return this.hashCode() == ((Entry) o).hashCode();
            }
            return false;
        }

        @Override
        public final int hashCode() {
            return key.hashCode() ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public final String toString() {
            return key + " = " + value;
        }

    }

 /* ----------------------------- Iterator --------------------------------- */

    private class EntryIterator implements Iterator<Entry> {

        private int index;      //current slot
        private Entry current;  //current entry
        private Entry next;     //next entry

        private EntryIterator() {
            if (size > 0) { // advance to first entry
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null) {}
            }
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Entry next() {
            final Entry e = next;
            if (e == null) {
                throw new NoSuchElementException();
            }
            if ((next = e.next) == null) {
                Entry[] t = table;
                while (index < t.length && (next = t[index++]) == null) {}
            }
            current = e;
            return e;
        }

        @Override
        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }
            final String key = current.key;
            current = null;
            StringMap.this.remove(key);
        }

    }

}
