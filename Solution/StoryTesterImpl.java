package Solution;

import Provided.*;
import jdk.dynalink.linker.ConversionComparator;
import org.junit.ComparisonFailure;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.util.*;

public class StoryTesterImpl implements StoryTester {

    private Method findGivenMethod(String sentence, Class<?> testClass, List<String> args){
        //it's guaranteed that there is only ONE 'Given' method that fits (somewhere in the inheritance hierarchy)
        if (null == testClass)
            return null;
        Method[] declaredMethods = testClass.getDeclaredMethods();
        for (Method method : declaredMethods){
            Given ann = method.getAnnotation(Given.class);
            if (null == ann) //not the write method
                continue;
            String value = ann.value();
            if (compareAnnotationValue(value, sentence, args))
                return method;
            args.clear();
        }
        //if reached here, the right 'Given' method is not here, going to find it in super class
        return findGivenMethod(sentence, testClass.getSuperclass(), args);
    }

    private void invokeGivenMethod(Object object, Method method, List<String> args){
        Object[] argsArray = new String[args.size()];
        for (int i = 0; i < argsArray.length ; i++)
            argsArray[i] = args.get(i);
        method.setAccessible(true);
        try {
            method.invoke(object, argsArray);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Map<Field, Object> backup(Object object){
        if (null == object)
            return null;
        Map<Field, Object> map = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields){
            try {
                f.setAccessible(true);
                Object o = f.get(object);
                if (o instanceof Cloneable) { //try clone
                    Method clone = o.getClass().getDeclaredMethod("clone");
                    clone.setAccessible(true);
                    map.put(f, clone.invoke(o));
                } else { //if there's no clone then try copy constructor
                    Constructor copy_constructor = o.getClass().getDeclaredConstructor(o.getClass());
                    copy_constructor.setAccessible(true);
                    map.put(f, copy_constructor.newInstance(o));
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e){
                //eventually, save basic reference
                try {
                    Object o = f.get(object);
                    map.put(f, o);
                } catch (Exception e1){
                    e1.printStackTrace(); //shouldn't get here according to the exercise
                }
            }
        }
        return map;
    }

    private StoryTestExceptionImpl findRightMethod(String sentence, Class<?> testClass) throws Exception {
        if (null == sentence || null == testClass){
            throw new IllegalArgumentException();
        }
        Class<?> emptyInst = (Class<?>) createEmptyObject(testClass);
        Method[] methods = testClass.getMethods();
        Class<?> annParam = getFirstWord(sentence);
        List<String> args = new LinkedList<>();
        for (Method met : methods) {
            if (annParam.getSimpleName().equals("Given")) {
                Given ann =  met.getAnnotation(Given.class);
                if (ann == null) {
                    continue;
                }
                if (!compareAnnotationValue(ann.value(), sentence, args)) {
                    args.clear();
                    continue;
                }
            } else if(annParam.getSimpleName().equals("When")){
                When ann =  met.getAnnotation(When.class);
                if (ann == null) {
                    continue;
                }
                if (!compareAnnotationValue(ann.value(), sentence, args)) {
                    args.clear();
                    continue;
                }
            } else {
                Then ann =  met.getAnnotation(Then.class);
                if (ann == null) {
                    continue;
                }
                if (!compareAnnotationValue(ann.value(), sentence, args)) {
                    args.clear();
                    continue;
                }
            }
            Object[] act_args = new Object[args.size()];
            for (int i = 0; i < args.size(); i++) { //parsing arguments
                try {
                    act_args[i] = Integer.parseInt(args.get(i));
                } catch (NumberFormatException e) {
                    act_args[i] = args.get(i);
                }
            }
            met.setAccessible(true);
            try {
                met.invoke(emptyInst, act_args);
            } catch (ComparisonFailure e) {
                //we have a failed 'Then' expression! (assuming it's the only exception that can be thrown according to PDF)
                LinkedList<String> expected = new LinkedList<>();
                LinkedList<String> fail = new LinkedList<>();
                expected.add(e.getExpected()); // <- this is AMAZING!
                fail.add(e.getActual()); // <- this is AMAZING!
                return new StoryTestExceptionImpl(sentence, expected, fail);
            }
            return null;
        }
        if (null == testClass.getSuperclass()) { //TODO: check if this exception might actually be thrown?
            String s = annParam.getSimpleName();
            throw s.equals("Given") ? new GivenNotFoundException() ://behold the beauty! the magnificent power of the ternary op.!:D
                  s.equals("When") ? new WhenNotFoundException() : new ThenNotFoundException();
        }
        return findRightMethod(sentence, testClass.getSuperclass());
    }

    private Object createEmptyObject(Class<?> testClass) {
        if (null == testClass)
            return null;
        try {
            Constructor<?> c = testClass.getConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception e) {
            //TODO: what if cannot create new instance?
            //TODO: solution to the above (?) - create a new instance using parent class
            e.printStackTrace();
        }
        return null;
    }

    private boolean compareAnnotationValue(String value, String sentence, List<String> args) {
        String[] valueL = value.split(" ");
        String[] sentenceL = sentence.split(sentence.substring(0, sentence.indexOf(" ")))[1].split(" ");
        if (valueL.length != sentenceL.length) {
            return false;
        }
        for (int i = 0; i < valueL.length; i++) {
            if (valueL[i].charAt(0) == '&') {
                args.add(sentenceL[i]);
                continue;
            }
            if (!valueL[i].equals(sentenceL[i])) {
                return false;
            }
        }
        return true;
    }

    private Class<?> getFirstWord(String sentence) {
        String w = sentence.substring(0, sentence.indexOf(" "));
        return w.equals("Given") ? Given.class : (w.equals("When") ? When.class : Then.class);
    }

    private void restore (Object object, Map<Field, Object> fieldObjectMap){
        if (null == object || fieldObjectMap.isEmpty())
            return;
        Map<Field, Object> temp = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!fieldObjectMap.containsKey(field))
                continue; //shouldn't happen I think, and if happens then we need to be concerned :(
            field.setAccessible(true);
            Object fieldData = fieldObjectMap.get(field);
            try {
                Class<?> c = fieldData.getClass();
                if (fieldData instanceof Cloneable) { //if cloneable
                    try {
                        Method method = c.getDeclaredMethod("clone");
                        method.setAccessible(true);
                        temp.put(field, method.invoke(fieldData));
                    } catch (Exception e){
                        //TODO: lol, what to do here? :D
                        e.printStackTrace();
                    }
                } else {
                    try { //is not Cloneable -> copy or save reference...
                        Constructor constructor = c.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        temp.put(field, constructor.newInstance(fieldData));
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                        temp.put(field, fieldData); //is it safe and OK?
                    }
                }
            } catch (SecurityException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
        fieldObjectMap.clear();
        fieldObjectMap = temp;
    }

    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if (story == null || testClass == null) {
            throw new IllegalArgumentException();
        }
        String[] sentences = story.split("\n");
        Object object = createEmptyObject(testClass);
        List<String> args = new LinkedList<>();
        Method givenMethod = findGivenMethod(sentences[0], testClass, args);
        invokeGivenMethod(object, givenMethod, args);
        args.clear();
        int failed = 0;
        StoryTestExceptionImpl save_first = null;
        Map<Field, Object> backupMap = backup(object);
        int i = 0;
        for (String sentence : sentences) {
            if (i == 0){ //meaning, it's a 'Given' sentence
                i++;
                continue;
            }
            if (sentence.startsWith("When") && sentences[i - 1].startsWith("Then")) {
                backupMap.clear();
                backupMap = backup(object);
            }
            try {
                StoryTestExceptionImpl storyTestException = findRightMethod(sentence, testClass);
                if (null == storyTestException)
                    continue;
                failed++;
                restore(object, backupMap);
                if (null != save_first)
                    continue;
                save_first = storyTestException;
            } catch (WordNotFoundException w){
                //TODO: what should I do if this happened?
            }
        }
        if (failed != 0) {
            save_first.setNumOfFails(failed);
            throw save_first;
        }
    }

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        //TODO: implement
    }
}
