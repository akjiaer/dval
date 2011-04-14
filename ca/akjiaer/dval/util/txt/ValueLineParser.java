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

import java.util.regex.Pattern;

/**
 * @author Stefan Neubert
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class ValueLineParser implements LineParser<ValueLineParser.Value> {

    private final Pattern pattern;
    private final String[] separator;

    public ValueLineParser(final String... separator) {
        if (separator.length == 0) {
            throw new IllegalArgumentException("Seperator cannot be empty!");
        }
        final StringBuffer sb = new StringBuffer();
        sb.append("[");
        for(String c : separator) {
            if (c == null || c.isEmpty()) {
                throw new IllegalArgumentException("Seperator cannot be null or empty!");
            }
            sb.append(c);
        }
        sb.append("]+");
        this.separator = separator;
        this.pattern = Pattern.compile(sb.toString());
    }

    @Override
    public Value parse(String s) {
        boolean endsWith = false;
        s = s.trim();
        for (String c : separator) {
            if (s.endsWith(c)) {
                endsWith = true;
                break;
            }
        }
        if (endsWith) {
            return new Value(s.substring(0, s.length() - 2).trim(), "");
        } else {
            final String[] sa = pattern.split(s, 2);
            return new Value(sa[0].trim(), sa.length > 1 ? sa[1].trim() : null);
        }
    }

    public class Value {

        public final String key;
        public final String value;

        private Value(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

    }

}
