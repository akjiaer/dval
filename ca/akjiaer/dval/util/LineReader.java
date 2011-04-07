/*
 * Copyright 2011 Stefan Neubert <akjiaer@gmail.com>
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

import ca.akjiaer.dval.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author Stefan Neubert
 * @version 1.0.1 2011-04-07
 * @since 0.10.0
 */
public class LineReader implements Iterable<String>, Iterator<String> {

    public static final int SKIP_IF_AT_START = 0;
    public static final int SKIP_IF_AT_END = 1;
    public static final int SKIP_IF_CONTAINS = 2;
    public static final int SKIP_IF_EQUALS = 3;
    public static final int SKIP_IF_EQUALS_IGNORE_CASE = 4;

    private final BufferedReader reader;
    private final List<Skipper> skipper;
    private final boolean empty;
    private String next;

    public LineReader(final InputStream is) {
        this(is, Charset.forName("UTF-8"));
    }

    public LineReader(final InputStream is, final Charset cs) {
        this.reader = new BufferedReader(new InputStreamReader(is, cs));
        this.skipper = new ArrayList(5);
        getNext();
        empty = next == null;
    }

    public void addLineSkip(final String match, final int condition) {
        if (match == null) {
            throw new IllegalArgumentException("Match string cannot be null!");
        }
        skipper.add(new Skipper(match, condition));
    }

    private void getNext() {
        try {
            while((next = reader.readLine()) == null || next.isEmpty() || skip(next)) {}
        } catch (IOException ex) {
            Log.error(LineReader.class, "Cannot read line!", ex);
            next = null;
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    public boolean isEmpty() {
        return empty;
    }

    @Override
    public Iterator<String> iterator() {
        return this;
    }

    @Override
    public String next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        String current = next;
        getNext();
        return current;
    }

    private boolean skip(final String line) {
        for(Skipper sk : skipper) {
            if (sk.skip(line)) return true;
        }
        return false;
    }

    @Override
    public void remove() {
        getNext();
    }

    private class Skipper {

        private final int condition;
        private final String match;

        public Skipper(final String match, final int condition) {
            this.condition = condition;
            this.match = match;
        }

        public boolean skip(String line) {
            switch (condition) {
                case SKIP_IF_AT_START:           return line.startsWith(match);
                case SKIP_IF_CONTAINS:           return line.contains(match);
                case SKIP_IF_AT_END:             return line.endsWith(match);
                case SKIP_IF_EQUALS:             return line.equals(match);
                case SKIP_IF_EQUALS_IGNORE_CASE: return line.equalsIgnoreCase(match);
                default: return false;
            }
        }

    }

}
