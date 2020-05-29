package tests;
import Provided.*;
import Solution.StoryTesterImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


public class CatTest {


    /**
     * This is a minimal test. Write your own tests!
     * Please don't submit your tests!
     */


    private StoryTester tester;
    private String goodStory;
    private String badStory;
    private String derivedStory;
    private String nestedStory;
    private Class<?> testClass;
    private Class<?> derivedTestClass;

    @Before
    public void setUp() throws Exception {
        goodStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean";

        badStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is smelly";

        derivedStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 15\n"
                + "When the house is cleaned, the number of hours is 11\n"
                + "Then the house condition is clean";

        nestedStory = "Given a Cat that his age is 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean";
        testClass = CatStory.class;
        //derivedTestClass = DogStoryDerivedTest.class;
        tester = new StoryTesterImpl();
    }


    @Test
    public  void errors() throws Exception {

        try {
            tester.testOnInheritanceTree(null, testClass);
            Assert.assertTrue(false);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }

        try {
            tester.testOnInheritanceTree(goodStory ,null);
            Assert.assertTrue(false);
        } catch (IllegalArgumentException e) {
            Assert.assertTrue(true);
        }

        testClass = EmptyB.class;
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(false);
        } catch (GivenNotFoundException e) {
            Assert.assertTrue(true);
        }

        testClass = NoWhen.class;
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(false);
        } catch (WhenNotFoundException e) {
            Assert.assertTrue(true);
        }

        testClass = NoThen.class;
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(false);
        } catch (ThenNotFoundException e) {
            Assert.assertTrue(true);
        }


    }

    @Test
    public void test1() throws Exception {



        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }

        try {
            tester.testOnInheritanceTree(badStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
        }


        goodStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is smelly or the house condition is clean";
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }

        goodStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean or the house condition is smelly\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean or the house condition is smelly";

        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }

        goodStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean or the house condition is smelly\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean or the house condition is smelly\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is smelly";;
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
        }


        goodStory = "Given a Cat of age 6\n"
                + "When the Cat is not taken out for a walk, the number of hours is 10\n"
                + "Then the house condition is clean\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean";
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
        }

        goodStory = "Given a Cat of age 6 and name laki\n"
                + "When the Cat is not taken out for a walk, the number of hours is 10\n"
                + "Then the house condition is clean\n"
                + "When the Cat is not taken out for a walk, the number of hours is 5\n"
                + "Then the house condition is clean";
        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
        }
    }
    @Test
    public void test2() throws Exception{
        goodStory = "Given a Cat of age 6 and name laki\n"
                + "When the Cat did kaki of size 1\n"
                + "Then the kaki size is 1";

        try {
            tester.testOnInheritanceTree(goodStory, testClass);
            Assert.assertTrue(true);
        } catch (StoryTestException e) {
            Assert.assertTrue(false);
        }

       badStory = "Given a Cat of age 6 and name laki\n"
                + "When the Cat did kaki of size 1\n"
                + "Then the kaki size is 0";

        try {
            tester.testOnInheritanceTree(badStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
            Assert.assertTrue( e.getSentence().equals("Then the kaki size is 0") );

            List<String> ls = e.getStoryExpected();
            Assert.assertEquals(ls.size(), 1);
            Assert.assertEquals("0",ls.get(0));

            ls = e.getTestResult();
            Assert.assertEquals(ls.size(), 1);
            Assert.assertEquals("1",ls.get(0));

            Assert.assertEquals(e.getNumFail(),1);

        }

        try {
            tester.testOnInheritanceTree(badStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);
        }

        badStory = "Given a Cat of age 6 and name laki\n"
                + "When the Cat did kaki of size 1\n"
                + "Then the kaki size is 0 or the kaki size is 2\n" //fail
                + "When the Cat did kaki of size 1\n"
                + "Then the kaki size is 0 or the kaki size is 1\n"
                + "When the Cat did kaki of size 2\n"
                + "Then the kaki size is 0 or the kaki size is 1\n" //fail
                + "When backup 2\n"
                + "Then the kaki size is 1";
        try {
            tester.testOnInheritanceTree(badStory, testClass);
            Assert.assertTrue(false);
        } catch (StoryTestException e) {
            Assert.assertTrue(true);

            Assert.assertEquals("Then the kaki size is 0 or the kaki size is 2", e.getSentence()) ;
            List<String> ls = e.getStoryExpected();
            Assert.assertEquals(2, ls.size());
            Assert.assertEquals("0",ls.get(0));
            Assert.assertEquals("2",ls.get(1));
            ls = e.getTestResult();
            Assert.assertEquals(2, ls.size());
            Assert.assertEquals("1",ls.get(0));
            Assert.assertEquals("1",ls.get(1));

            Assert.assertEquals(e.getNumFail(),2);
        }
    }
}

