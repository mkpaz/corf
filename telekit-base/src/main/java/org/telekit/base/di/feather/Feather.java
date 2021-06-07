/*
 * Copyright 2017 Zsolt Herpai (https://github.com/zsoltherpai/feather)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.telekit.base.di.feather;

import org.telekit.base.di.Initializable;
import org.telekit.base.di.Provides;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Feather {

    private final Map<Key, Provider<?>> providers = new ConcurrentHashMap<>();
    private final Map<Key, Object> singletons = new ConcurrentHashMap<>();
    private final Map<Class, Object[][]> injectFields = new ConcurrentHashMap<>(0);

    /**
     * Constructs Feather with configuration modules
     */
    public static Feather with(Object... modules) {
        return new Feather(Arrays.asList(modules));
    }

    /**
     * Constructs Feather with configuration modules
     */
    public static Feather with(Iterable<?> modules) {
        return new Feather(modules);
    }

    private Feather(Iterable<?> modules) {
        providers.put(Key.of(Feather.class), new Provider() {
            @Override
            public Object get() {
                return this;
            }
        });
        for (final Object module : modules) {
            if (module instanceof Class) {
                throw new FeatherException(String.format("%s provided as class instead of an instance.", ((Class) module).getName()));
            }
            for (Method providerMethod : providers(module.getClass())) {
                providerMethod(module, providerMethod);
            }
        }
    }

    /**
     * @return an instance of type
     */
    public <T> T instance(Class<T> type) {
        return provider(Key.of(type), null).get();
    }

    /**
     * @return instance specified by key (type and qualifier)
     */
    public <T> T instance(Key<T> key) {
        return provider(key, null).get();
    }

    /**
     * @return provider of type
     */
    public <T> Provider<T> provider(Class<T> type) {
        return provider(Key.of(type), null);
    }

    /**
     * @return provider of key (type, qualifier)
     */
    public <T> Provider<T> provider(Key<T> key) {
        return provider(key, null);
    }

    /**
     * Injects fields to the target object
     */
    public void injectFields(Object target) {
        if (!injectFields.containsKey(target.getClass())) {
            injectFields.put(target.getClass(), injectFields(target.getClass()));
        }
        for (Object[] f : injectFields.get(target.getClass())) {
            Field field = (Field) f[0];
            Key key = (Key) f[2];
            try {
                field.set(target, (boolean) f[1] ? provider(key) : instance(key));
            } catch (Exception e) {
                throw new FeatherException(String.format("Can't inject field %s in %s", field.getName(), target.getClass().getName()));
            }
        }
    }

    private <T> Provider<T> provider(final Key<T> key, Set<Key> chain) {
        if (!providers.containsKey(key)) {
            final Constructor constructor = constructor(key);
            final Provider<?>[] paramProviders = paramProviders(
                    key,
                    constructor.getParameterTypes(),
                    constructor.getGenericParameterTypes(),
                    constructor.getParameterAnnotations(),
                    chain
            );
            providers.put(key, singletonProvider(key, key.type.getAnnotation(Singleton.class), (Provider) () -> {
                try {
                    Object o = constructor.newInstance(params(paramProviders));
                    if (o instanceof Initializable initializable) { initializable.initialize(); }
                    return o;
                } catch (Exception e) {
                    throw new FeatherException(String.format("Can't instantiate %s", key), e);
                }
            }));
        }
        return (Provider<T>) providers.get(key);
    }

    private void providerMethod(final Object module, final Method m) {
        final Key key = Key.of(m.getReturnType(), qualifier(m.getAnnotations()));
        if (providers.containsKey(key)) {
            throw new FeatherException(String.format("%s has multiple providers, module %s", key, module.getClass()));
        }
        Singleton singleton = m.getAnnotation(Singleton.class) != null ?
                m.getAnnotation(Singleton.class) :
                m.getReturnType().getAnnotation(Singleton.class);
        final Provider<?>[] paramProviders = paramProviders(
                key,
                m.getParameterTypes(),
                m.getGenericParameterTypes(),
                m.getParameterAnnotations(),
                Collections.singleton(key)
        );
        providers.put(key, singletonProvider(key, singleton, (Provider) () -> {
            try {
                return m.invoke(module, params(paramProviders));
            } catch (Exception e) {
                throw new FeatherException(String.format("Can't instantiate %s with provider", key), e);
            }
        }));
    }

    private <T> Provider<T> singletonProvider(final Key key, Singleton singleton, final Provider<T> provider) {
        return singleton != null ? () -> {
            if (!singletons.containsKey(key)) {
                synchronized (singletons) {
                    if (!singletons.containsKey(key)) {
                        singletons.put(key, provider.get());
                    }
                }
            }
            return (T) singletons.get(key);
        } : provider;
    }

    private Provider<?>[] paramProviders(final Key key,
                                         Class<?>[] parameterClasses,
                                         Type[] parameterTypes,
                                         Annotation[][] annotations,
                                         final Set<Key> chain
    ) {
        Provider<?>[] providers = new Provider<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; ++i) {
            Class<?> parameterClass = parameterClasses[i];
            Annotation qualifier = qualifier(annotations[i]);
            Class<?> providerType = Provider.class.equals(parameterClass) ?
                    (Class<?>) ((ParameterizedType) parameterTypes[i]).getActualTypeArguments()[0] :
                    null;
            if (providerType == null) {
                final Key newKey = Key.of(parameterClass, qualifier);
                final Set<Key> newChain = append(chain, key);
                if (newChain.contains(newKey)) {
                    throw new FeatherException(String.format("Circular dependency: %s", chain(newChain, newKey)));
                }
                providers[i] = () -> provider(newKey, newChain).get();
            } else {
                final Key newKey = Key.of(providerType, qualifier);
                providers[i] = () -> provider(newKey, null);
            }
        }
        return providers;
    }

    private static Object[] params(Provider<?>[] paramProviders) {
        Object[] params = new Object[paramProviders.length];
        for (int i = 0; i < paramProviders.length; ++i) {
            params[i] = paramProviders[i].get();
        }
        return params;
    }

    private static Set<Key> append(Set<Key> set, Key newKey) {
        if (set != null && !set.isEmpty()) {
            Set<Key> appended = new LinkedHashSet<>(set);
            appended.add(newKey);
            return appended;
        } else {
            return Collections.singleton(newKey);
        }
    }

    private static Object[][] injectFields(Class<?> target) {
        Set<Field> fields = fields(target);
        Object[][] fs = new Object[fields.size()][];
        int i = 0;
        for (Field f : fields) {
            Class<?> providerType = f.getType().equals(Provider.class) ?
                    (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0] :
                    null;

            Class<?> keyType = providerType != null ? providerType : f.getType();
            fs[i++] = new Object[]{f, providerType != null, Key.of(keyType, qualifier(f.getAnnotations()))};
        }
        return fs;
    }

    private static Set<Field> fields(Class<?> type) {
        Class<?> current = type;
        Set<Field> fields = new HashSet<>();
        while (!current.equals(Object.class)) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private static String chain(Set<Key> chain, Key lastKey) {
        StringBuilder chainString = new StringBuilder();
        for (Key key : chain) {
            chainString.append(key.toString()).append(" -> ");
        }
        return chainString.append(lastKey.toString()).toString();
    }

    private static Constructor constructor(Key key) {
        Constructor inject = null;
        Constructor noarg = null;
        for (Constructor c : key.type.getDeclaredConstructors()) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (inject == null) {
                    inject = c;
                } else {
                    throw new FeatherException(String.format("%s has multiple @Inject constructors", key.type));
                }
            } else if (c.getParameterTypes().length == 0) {
                noarg = c;
            }
        }
        Constructor constructor = inject != null ? inject : noarg;
        if (constructor != null) {
            constructor.setAccessible(true);
            return constructor;
        } else {
            throw new FeatherException(String.format("%s doesn't have an @Inject or no-arg constructor, or a module provider", key.type.getName()));
        }
    }

    private static Set<Method> providers(Class<?> type) {
        Class<?> current = type;
        Set<Method> providers = new HashSet<>();
        while (!current.equals(Object.class)) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Provides.class) && (type.equals(current) || !providerInSubClass(method, providers))) {
                    method.setAccessible(true);
                    providers.add(method);
                }
            }
            current = current.getSuperclass();
        }
        return providers;
    }

    private static Annotation qualifier(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                return annotation;
            }
        }
        return null;
    }

    private static boolean providerInSubClass(Method method, Set<Method> discoveredMethods) {
        for (Method discovered : discoveredMethods) {
            if (discovered.getName().equals(method.getName()) && Arrays.equals(method.getParameterTypes(), discovered.getParameterTypes())) {
                return true;
            }
        }
        return false;
    }
}
