package Solution;

import Provided.*;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.util.*;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        if (story == null || testClass == null) {
            throw new IllegalArgumentException();
        }
        //Class<?> emptyInst = (Class<?>) createEmptyObject(testClass);
        String[] sntncs = story.split("\n");
        //Method[] methods = testClass.getMethods();
        for (String sntnc : sntncs) {
            findRightMethod(sntnc, testClass);
        }
    }

    private void findRightMethod(String sentence, Class<?> testClass) throws Exception {
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
            for (int i = 0; i < args.size(); i++) {
                try {
                    act_args[i] = Integer.parseInt(args.get(i));
                } catch (NumberFormatException e) {
                    act_args[i] = args.get(i);
                }
            }
            met.setAccessible(true);
            try {
                met.invoke(emptyInst, act_args); //shouldn't throw exceptions...
            } catch (IllegalAccessException | InvocationTargetException e) {
                //e.printStackTrace();
            }
            return;
        }
        if (null == testClass.getSuperclass()) {
            String s = annParam.getSimpleName();
            throw s.equals("Given") ? new GivenNotFoundException() ://behold the beauty! the magnificent power of the ternary op.!:D
                    s.equals("When") ? new WhenNotFoundException() : new ThenNotFoundException();
        }
        findRightMethod(sentence, testClass.getSuperclass());
    }

    private Object createEmptyObject(Class<?> testClass) {
        try {
            Constructor<?> c = testClass.getConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch (Exception e) {
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

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        //TODO: implement
        return;
    }
}
