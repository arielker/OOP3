package Solution;

import Provided.StoryTester;
import Provided.WhenNotFoundException;
import Provided.WordNotFoundException;

import java.lang.annotation.*;
import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.util.*;

public class StoryTesterImpl implements StoryTester {
    @Override
    public void testOnInheritanceTree(String story, Class<?> testClass) throws Exception {
        //TODO: Implement
    }

    private Object createEmptyObject(Class<?> testClass){
        try{
            Constructor<?> c = testClass.getConstructor();
            c.setAccessible(true);
            return c.newInstance();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean compareAnnotationValue(String value, String sentence, List<String> args){
        String[] valueL = value.split(" ");
        String[] sentenceL = sentence.split("given ")[1].split(" ");
        if(valueL.length != sentenceL.length){
            return false;
        }
        for(int i=0;i<valueL.length;i++){
            if(valueL[i].charAt(0) == '&'){
                args.add(sentenceL[i]);
                continue;
            }
            if(!valueL[i].equals(sentenceL[i])){
                return false;
            }
        }
        return true;
    }

    private Class<?> getFirstWord(String sentence) {
        String w = sentence.substring(0, sentence.indexOf(" "));
        return w.equals("Given") ? Given.class : w.equals("When") ? When.class : Then.class;
    }


    private Object nestedGiven(String sentence, Class<?> test)

    @Override
    public void testOnNestedClasses(String story, Class<?> testClass) throws Exception {
        if(story == null || testClass == null){
            throw new IllegalArgumentException();
        }
        Class<?> emptyInst = (Class<?>) createEmptyObject(testClass);
        String[] sntncs = story.split("\n");
        Method[] methods = testClass.getMethods();
        for(String sntnc : sntncs){
            String annParam = sntnc.split(" ")[0];
            List<String> args = new LinkedList<>();
            if(annParam.equals("Given")){
                for(Method met : methods){
                    Given ann = met.getAnnotation(Given.class);
                    if(ann == null){
                        continue;
                    }
                    if(!compareAnnotationValue(ann.value(), sntnc, args)){
                        args.clear();
                        continue;
                    }
                    Object[] act_args = new Object[args.size()];
                    for(int i=0;i<args.size();i++){
                        try{
                            act_args[i] = Integer.parseInt(args.get(i));
                        } catch(NumberFormatException e){
                            act_args[i] = args.get(i);
                        }
                    }
                    met.invoke(emptyInst, act_args);
                }
            }
        }
    }
}
