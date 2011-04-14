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

/**
 * @author Stefan Neubert
 * @version 1.0 2011-04-14
 * @since 0.11.0
 */
public class LineSkipper {

    public static final int SKIP_IF_AT_START = 0;
    public static final int SKIP_IF_AT_END = 1;
    public static final int SKIP_IF_CONTAINS = 2;
    public static final int SKIP_IF_EQUALS = 3;
    public static final int SKIP_IF_EQUALS_IGNORE_CASE = 4;

    private final int condition;
    private final String match;

    protected LineSkipper(final String match, final int condition) {
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
