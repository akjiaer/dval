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
 * @version 2.0.1 2011-04-07
 * @sine 0.10.0
 */
public class Version implements Comparable<Version>{

    public final String name;
    private final int[] numbers;
    private final int[] length;
    private final String build;

    public Version(String version, String name) {
        if (version == null || version.isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be empty or null!");
        }
        this.name = name;
        String[] sa = split(version, "[-]");
        this.build = sa.length > 1 ? sa[1] : null;
        sa = split(sa[0], "[.]");
        int[] num = new int[sa.length];
        int[] len = new int[sa.length];
        for (int i = 0; i < num.length; i++) {
            try {
                len[i] = sa[i].length() > 1 && sa[i].startsWith("0") ? (byte) sa[i].length() : 0;
                num[i] = Integer.parseInt(sa[i], 10);
            } catch (NumberFormatException ex) {
                len[i] = 0;
                num[i] = 0;
            }
        }
        this.length = len;
        this.numbers = num;
    }

    private String[] split(String source, String pattern) {
        String[] sa = Pattern.compile(pattern).split(source, 0);
        if (sa.length == 0) {
            throw new IllegalArgumentException("Version string must contain numbers!");
        }
        return sa;
    }

    @Override
    public int compareTo(Version v) {
        if (v == null) {
            throw new NullPointerException("Version to compare cannot be null!");
        }

        boolean b = true; //Switcher
        int[] lnum, snum = v.numbers;
        if (numbers.length >= snum.length) {
            lnum = numbers;
        } else {
            lnum = snum;
            snum = numbers;
            b = false;
        }

        for (int j, i = 0; i < lnum.length; i++) {
            j = i < snum.length ? snum[i] : 0;
            if (lnum[i] != j) {
                return b ? j - lnum[i] : lnum[i] - j;
            }
        }
        return 0;
    }

    public boolean isNewerAs(Version v) {
        return this.compareTo(v) < 0;
    }

    public boolean isOlderAs(Version v) {
        return this.compareTo(v) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (o instanceof Version) {
            return this.hashCode() == o.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        for (int i = 0; i < numbers.length; i++) {
            hash = 23 * hash + numbers[i];
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(numbers[0]);
        if (numbers.length > 1) {
            for (int i = 1; i < numbers.length; i++) {
                sb.append('.');
                if (length[i] == 0) {
                    sb.append(numbers[i]);
                } else {
                    sb.append(Integer.toString(((int) Math.pow(10, length[i])) + numbers[i]).substring(1));
                }
            }
        }
        if (build != null) {
            sb.append('-').append(build);
        }
        return sb.toString();
    }

}
