package Solution;

import Provided.GivenNotFoundException;
import Provided.StoryTester;
import Provided.ThenNotFoundException;
import Provided.WhenNotFoundException;
import org.junit.ComparisonFailure;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StoryTesterImpl implements StoryTester {

    /**
     * This method finds the right 'Given' method for the given sentence in the given test class and
     * returns it.
     * @param sentence
     * @param testClass
     * @return
     */
    private Method findGivenMethod(String sentence, Class<?> testClass) {
        //it's guaranteed that there is only ONE 'Given' method that fits (somewhere in the inheritance hierarchy)
        if (null == testClass)
            return null;
        Method[] declaredMethods = testClass.getDeclaredMethods();
        for (Method method : declaredMethods) {
            Given ann = method.getAnnotation(Given.class);
            if (null == ann) //not the right method
                continue;
            String value = ann.value();
            if (compareAnnotationValue(value, sentence))
                return method;
        }
        //if reached here, the right 'Given' method is not here, going to find it in super class
        return findGivenMethod(sentence, testClass.getSuperclass());
    }

    /**
     * This method invokes the given method with the given object.
     * Why have a special method for 'Given', you ask? This will be answered a few methods ahead.
     * @param object
     * @param method
     * @param args
     */
    private void invokeGivenMethod(Object object, Method method, List<String> args) {
        Object[] argsArray = parseArguments(args);
        method.setAccessible(true);
        try {
            method.invoke(object, argsArray);
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            //e.printStackTrace();
        }
    }

    /**
     * This method backs up the given object's fields in a map, using the 'clone > copy c'tor > as is'
     * that was described in the assignment, and returns the map.
     * @param object
     * @return
     */
    private Map<Field, Object> backup(Object object) {
        if (null == object)
            return null;
        Map<Field, Object> map = new HashMap<>();
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field f : fields) {
            try {
                f.setAccessible(true);
                Object o = f.get(object);
                if (o == null) {
                    map.put(f, o);
                    continue;
                }
                if (o instanceof Cloneable) { //try clone
                    Method clone = o.getClass().getDeclaredMethod("clone");
                    clone.setAccessible(true);
                    map.put(f, clone.invoke(o));
                } else { //if there's no clone then try copy constructor
                    Constructor<?> copy_constructor = o.getClass().getDeclaredConstructor(o.getClass());
                    copy_constructor.setAccessible(true);
                    map.put(f, copy_constructor.newInstance(o));
                }
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                //eventually, save basic reference
                try {
                    Object o = f.get(object);
                    map.put(f, o);
                } catch (Exception e1) {
                    //e1.printStackTrace(); //shouldn't get here according to the exercise
                }
            }
        }
        return map;
    }

    /**
     * This method takes the argument to the upcoming method (which are currently all strings),
     * parses them to String or Integer, stores them in an Object array and sends them away.
     * @param args
     * @return
     */
    private Object[] parseArguments(List<String> args) {
        Object[] act_args = new Object[args.size()];
        for (int i = 0; i < args.size(); i++) { //parsing arguments
            try {
                act_args[i] = Integer.parseInt(args.get(i));
            } catch (NumberFormatException e) {
                act_args[i] = args.get(i);
            }
        }
        return act_args;
    }

    /**
     * This method takes the given sentence, splits it to type II sub-sentences and creates a
     * list of arguments for every sub-sentence, which we will send when provoking the
     * corresponding method later.
     * We did a special method for "Given" sentence for some reason. We blame the system. ACAB.
     * @param sentence
     * @return
     */
    private List<String> fillGivenArguments(String sentence) {
        String[] and_split = sentence.split(" and ");
        List<String> args = new LinkedList<>();
        for (String inst : and_split) {
            String[] words = inst.split(" ");
            args.add(words[words.length - 1]);
        }
        return args;
    }

    /**
     * This method takes the given sentence, splits it to type II sub-sentences and creates a
     * list of arguments for every sub-sentence, which we will send when provoking the
     * corresponding method later.
     * @param sentence
     * @return
     */
    private List<List<String>> fillArguments(String sentence) {
        String[] or_split = sentence.split(" or ");
        List<List<String>> args = new LinkedList<>();
        for (int i = 0; i < or_split.length; i++) {
            args.add(new LinkedList<>());
            String[] or_args = or_split[i].split(" ");
            for (int j = 0; j < or_args.length; j++) {
                if (j + 1 == or_args.length || or_args[j + 1].equals("and"))
                    args.get(i).add(or_args[j]);
            }
        }
        return args;
    }

    /**
     * This method finds and invokes the method that fits the given sentence.
     * If this sentence starts with "then", this method will also create an instance of
     * StoryTestException and fill it correspondingly.
     * @param sentence
     * @param testClass
     * @param emptyInst
     * @return
     * @throws Exception
     */
    private StoryTestExceptionImpl findRightMethod(String sentence, Class<?> testClass, Object emptyInst) throws Exception {
        Method[] methods = testClass.getDeclaredMethods();
        String[] ors = sentence.split(" or ");
        for (int i = 1; i < ors.length; i++) {
            ors[i] = ors[0].substring(0, ors[0].indexOf(" ")).concat(" ").concat(ors[i]);
        }
        Class<?> annParam = getFirstWord(sentence);
        List<List<String>> args = fillArguments(sentence);
        int i = 0;
        LinkedList<String> expected = new LinkedList<>();
        LinkedList<String> fail = new LinkedList<>();
        for (String s : ors) {
            for (Method met : methods) {
                if (annParam.getSimpleName().equals("Given")) {
                    Given ann = met.getAnnotation(Given.class);
                    if (ann == null || !compareAnnotationValue(ann.value(), s)) {
                        continue;
                    }
                } else if (annParam.getSimpleName().equals("When")) {
                    When ann = met.getAnnotation(When.class);
                    if (ann == null || !compareAnnotationValue(ann.value(), s)) {
                        continue;
                    }
                } else {
                    Then ann = met.getAnnotation(Then.class);
                    if (ann == null || !compareAnnotationValue(ann.value(), s)) {
                        continue;
                    }
                }
                Object[] act_args = parseArguments(args.get(i));
                i++;
                met.setAccessible(true);
                try {
                    met.invoke(emptyInst, act_args);
                } catch (InvocationTargetException e) {
                    if (e.getTargetException().getClass().equals(ComparisonFailure.class)) {
                        ComparisonFailure cf = (ComparisonFailure) e.getTargetException();
                        expected.add(cf.getExpected()); // <- this is AMAZING!
                        fail.add(cf.getActual()); // <- this is AMAZING!
                        if (i == args.size()) {
                            //we have a failed 'Then' expression! (assuming it's the only exception that can be thrown according to PDF)
                            return new StoryTestExceptionImpl(sentence, expected, fail);
                        }
                        continue;
                    }
                    throw e;
                }
                return null;
            }
        }
        if (null == testClass.getSuperclass()) { //TODO: check if this exception might actually be thrown?
            String s = annParam.getSimpleName();
            throw s.equals("Given") ? new GivenNotFoundException() ://behold the beauty! the magnificent power of the ternary op.!:D
                    s.equals("When") ? new WhenNotFoundException() : new ThenNotFoundException();
        }
        return findRightMethod(sentence, testClass.getSuperclass(), emptyInst);
    }

    /**
     * This method finds the constructor for the test class and create a new, empty instance to use
     * for future method invokes.
     * The method knows to detect nested class and also find the right constructor for it.
     * @param testClass
     * @return
     */
    private Object createEmptyObject(Class<?> testClass) {
        if (null == testClass)
            return null;
        try {
            Constructor<?> c = testClass.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            Object enclosingObject = createEmptyObject(testClass.getEnclosingClass());
            try {
                Constructor<?> constructor = testClass.getDeclaredConstructor(enclosingObject.getClass());
                constructor.setAccessible(true);
                return constructor.newInstance(enclosingObject);
            } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException l) {
//                l.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Checks if a method, represented here by its annotation's value, fits the given sentence.
     * @param value
     * @param sentence
     * @return
     */
    private boolean compareAnnotationValue(String value, String sentence) {
        String[] valueL = value.split(" ");
        String[] sentenceL = sentence.split(sentence.substring(0, sentence.indexOf(" ") + 1))[1].split(" ");
        if (valueL.length != sentenceL.length) {
            return false;
        }
        for (int i = 0; i < valueL.length; i++) {
            if (valueL[i].charAt(0) == '&') {
                continue;
            }
            if (!valueL[i].equals(sentenceL[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * QoL function to return the annotation class that fits the given sentence.
     * @param sentence
     * @return
     */
    private Class<?> getFirstWord(String sentence) {
        String w = sentence.substring(0, sentence.indexOf(" "));
        return w.equals("Given") ? Given.class : (w.equals("When") ? When.class : Then.class);
    }

    /**
     * Restoring all fields to the object we are working on to their state before the last set of "when"
     * statements.
     * @param object
     * @param fieldObjectMap
     */
    private void restore(Object object, Map<Field, Object> fieldObjectMap) {
        if (null == object || fieldObjectMap.isEmpty())
            return;
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!fieldObjectMap.containsKey(field))
                continue; //shouldn't happen I think, and if happens then we need to be concerned :(
            field.setAccessible(true);
            Object fieldData = fieldObjectMap.get(field);
            try {
                field.set(object, fieldData);
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                //e.printStackTrace();
            }
        }
    }

    /**
     * For every sentence of the story, finds the right method (in the inheritance path) to run.
     * @param story contains the text of the story to test, the string is
     * divided to line using '\n'. each word in a line is separated by space
     * (' ').
     * @param testClass the test class which the story should be run on.
     * @throws Exception
     */
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if (story == null || testClass == null) {
            throw new IllegalArgumentException();
        }
        String[] sentences = story.split("\n");
        Object object = createEmptyObject(testClass);
        List<String> args = fillGivenArguments(sentences[0]);
        Method givenMethod = findGivenMethod(sentences[0], testClass);
        if (givenMethod == null) {
            throw new GivenNotFoundException();
        }
        invokeGivenMethod(object, givenMethod, args);
        args.clear();
        int failed = 0;
        StoryTestExceptionImpl save_first = null;
        Map<Field, Object> backupMap = backup(object);
        int i = 0;
        for (String sentence : sentences) {
            if (i == 0) { //meaning, it's a 'Given' sentence
                i++;
                continue;
            }
            if (sentence.startsWith("When") && (sentences[i - 1].startsWith("Then"))) {
                backupMap.clear();
                backupMap = backup(object);
            }
            i++;
            StoryTestExceptionImpl storyTestException = findRightMethod(sentence, testClass, object);
            if (null == storyTestException)
                continue;
            failed++;
            restore(object, backupMap);
            if (null != save_first)
                continue;
            save_first = storyTestException;
        }
        if (failed != 0) {
            save_first.setNumOfFails(failed);
            throw save_first;
        }
    }

    /**
     * Finds the (nested) class that fits the Given sentence, and runs the story on the found class.
     * @param story contains the text of the story to test, the string is
     * divided to line using '\n'. each word in a line is separated by space
     * (' ').
     * @param testClass the test class which the story should be run on.
     * @throws Exception
     */
    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if (null == story || null == testClass)
            throw new IllegalArgumentException();
        String given_sentence = story.split("\n")[0];
        if (null != findGivenMethod(given_sentence, testClass))
            testOnInheritanceTree(story, testClass);
        else {
            Class<?>[] sub_classes = testClass.getDeclaredClasses();
            for (Class<?> sub_class : sub_classes)
                testOnNestedClasses(story, sub_class);
        }
    }
}