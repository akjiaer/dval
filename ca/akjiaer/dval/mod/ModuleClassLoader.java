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

package ca.akjiaer.dval.mod;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Stefan Neubert
 * @version 1.0 2011-02-23
 * @since 0.10.0
 */
public class ModuleClassLoader extends ClassLoader {

    protected ModuleClassLoader() {
        super(ClassLoader.getSystemClassLoader());
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        for (Module m : Module.modules.values()) {
            if (m.cache.containsKey(name)) {
                return m.cache.get(name);
            }
        }
        for (Module m : Module.modules.values()) {
            if (m.classes.containsKey(name)) {
                byte[] ba = m.classes.get(name).read();
                Class c = defineClass(name, ba, 0, ba.length);
                m.cache.put(name, c);
                m.classes.remove(name);
                return c;
            }  
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (Module m : Module.modules.values()) {
            if (m.resources.containsKey(name)) {
                return new ByteArrayInputStream(m.resources.get(name).read());
            }
        }
        return null;
    }

}
