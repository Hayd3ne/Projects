package rules;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class RuleLoader {
    public static List<Object> loadRules(String packageName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Object> rules = new ArrayList<>();
        //ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Package packageObj = Package.getPackage(packageName);
        System.out.println(packageObj);

        for (Class<?> clazz : getClassesForPackage(packageObj)) {
            if (rules.Rule.class.isAssignableFrom(clazz)) {
                Constructor<?> constructor = clazz.getConstructor();
                Object rule = constructor.newInstance();
                rules.add(rule);
            }
        }

        return rules;
    }

    private static Class<?>[] getClassesForPackage(Package pkg) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String packageName = pkg.getName();
        String path = packageName.replace('.', '/');
        Class<?>[] classes = new Class[0];
        try {
            for (java.net.URL resource : java.util.Collections.list(classLoader.getResources(path))) {
                java.io.File directory = new java.io.File(resource.getFile());
                if (directory.exists()) {
                    String[] files = directory.list();
                    for (String file : files) {
                        if (file.endsWith(".class")) {
                            classes = java.util.Arrays.copyOf(classes, classes.length + 1);
                            String className = java.util.Arrays.stream(files)
                                    .filter(f -> f.endsWith(".class"))
                                    .map(f -> packageName + '.' + f.substring(0, f.length() - 6))
                                    .findFirst()
                                    .orElse("");
                            classes[classes.length - 1] = Class.forName(className);
                        }
                    }
                }
            }
        } catch (java.io.IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return classes;
    }
}