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

package ca.akjiaer.dval.util.txt;

import ca.akjiaer.dval.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class LineReader<V> implements Iterable<V>, Iterator<V> {

    private final BufferedReader reader;
    private final List<LineSkipper> skipper;
    private final boolean empty;
    private final LineParser<V> parser;
    private String next;

    public LineReader(final LineParser<V> parser, final File file) throws FileNotFoundException{
        this(parser, new FileInputStream(file), Charset.forName("UTF-8"));
    }

    public LineReader(final LineParser<V> parser, final InputStream is) {
        this(parser, is, Charset.forName("UTF-8"));
    }

    private LineReader(final LineParser<V> parser, final InputStream is, final Charset cs) {
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null!");
        }
        this.parser = parser;
        this.reader = new BufferedReader(new InputStreamReader(is, cs));
        this.skipper = new ArrayList(5);
        getNext();
        empty = next == null;
    }

    public void addLineSkip(final String match, final int condition) {
        if (match == null) {
            throw new IllegalArgumentException("Match string cannot be null!");
        }
        skipper.add(new LineSkipper(match, condition));
    }

    private void getNext() {
        try {
            while((next = reader.readLine()) != null && (next.isEmpty() || skip(next))) {}
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
    public Iterator<V> iterator() {
        return this;
    }

    @Override
    public V next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        String current = next;
        getNext();
        return parser.parse(current);
    }

    private boolean skip(final String line) {
        for(LineSkipper sk : skipper) {
            if (sk.skip(line)) return true;
        }
        return false;
    }

    @Override
    public void remove() {
        getNext();
    }

}
