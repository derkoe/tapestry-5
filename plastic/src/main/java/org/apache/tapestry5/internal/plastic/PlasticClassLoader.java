// Copyright 2011 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry5.internal.plastic;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PlasticClassLoader extends ClassLoader
{
    final Lock classloaderLock = new ReentrantLock();

    private final ClassLoaderDelegate delegate;

    public PlasticClassLoader(ClassLoader parent, ClassLoaderDelegate delegate)
    {
        super(parent);

        this.delegate = delegate;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
    {
        classloaderLock.lock();

        try
        {
            Class<?> loadedClass = findLoadedClass(name);

            if (loadedClass != null)
                return loadedClass;

            if (delegate.shouldInterceptClassLoading(name))
            {
                Class<?> c = delegate.loadAndTransformClass(name);

                if (resolve)
                    resolveClass(c);

                return c;
            } else
            {
                return super.loadClass(name, resolve);
            }
        } finally
        {
            classloaderLock.unlock();
        }
    }

    Class<?> defineClassWithBytecode(String className, byte[] bytecode)
    {
        return defineClass(className, bytecode, 0, bytecode.length);
    }
}
