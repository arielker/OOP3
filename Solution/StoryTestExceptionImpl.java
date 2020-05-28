package Solution;

import Provided.StoryTestException;

import java.util.List;

public class StoryTestExceptionImpl extends StoryTestException {
    //TODO: add any necessary variables and correct the functions in StoryTesterImpl to keep track of things.
    private int numOfFails;
    private String sentence;
    private List<String> storyExpected;
    private List<String> testResult;

    public StoryTestExceptionImpl(String sentence, List<String> storyExpected, List<String> testResult) {
        this.numOfFails = 0;
        this.sentence = sentence;
        this.storyExpected = storyExpected;
        this.testResult = testResult;
    }

    public void setNumOfFails(int numOfFails) {
        this.numOfFails = numOfFails;
    }

    public void setSentence(String sentence) {
        this.sentence = sentence;
    }

    public void setStoryExpected(List<String> storyExpected) {
        this.storyExpected = storyExpected;
    }

    public void setTestResult(List<String> testResult) {
        this.testResult = testResult;
    }

    @Override
    public String getSentence() {
        return this.sentence;
    }

    @Override
    public List<String> getStoryExpected() {
        return this.storyExpected;
    }

    @Override
    public List<String> getTestResult() {
        return this.testResult;
    }

    @Override
    public int getNumFail() {
        return this.numOfFails;
    }
}
