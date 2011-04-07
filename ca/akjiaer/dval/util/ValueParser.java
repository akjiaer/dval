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

import java.util.regex.Pattern;

/**
 * @author Stefan Neubert
 * @version 1.1 2011-04-05
 * @since 0.10.0
 */
public class ValueParser { //TODO: mit chars trennen asds

    private final Pattern pattern;

    public ValueParser() {
        pattern = Pattern.compile("[=:\\s]+");
    }

    public ValueParser(final String regex) {
        pattern = Pattern.compile(regex);
    }

    public String[] split(String s) {
        if ((s = s.trim()).endsWith("=") || s.endsWith(":")) {
            return new String[] {s.substring(0, s.length() - 2).trim(), ""};
        } else {
            final String[] entry = pattern.split(s, 2);
            if (entry.length > 1) {
                entry[0] = entry[0].trim();
                entry[1] = entry[1].trim();
            } else {
                return new String[] {entry[0].trim(), null};
            }
            return entry;
        }    
    }

}
