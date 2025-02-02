package Solution;

import Provided.StoryTestException;

import java.util.List;

public class StoryTestExceptionImpl extends StoryTestException {
    private int numOfFails;
    private final String sentence;
    private final List<String> storyExpected;
    private final List<String> testResult;

    public StoryTestExceptionImpl(String sentence, List<String> storyExpected, List<String> testResult) {
        this.numOfFails = 0;
        this.sentence = sentence;
        this.storyExpected = storyExpected;
        this.testResult = testResult;
    }

    public void setNumOfFails(int numOfFails) {
        this.numOfFails = numOfFails;
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