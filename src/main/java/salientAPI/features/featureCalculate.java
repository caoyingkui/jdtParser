package salientAPI.features;

/**
 * Created by oliver on 2017/10/13.
 * all the features class should implement this interface
 */
public interface featureCalculate {
    /**
     * a feature class should implements this method to calculate the value of a feature;
     * @param questionDoc: the text representing the question document
     * @param answerDoc : the text representing the answer document
     * @param codeElements : the code elements set for which we need to calculate the value of features
     * @return the feature values of the corresponding code element , the size of the return value is the same as the codeElements'
     */
    public  abstract float[] calculate(String questionDoc , String answerDoc , String[] codeElements);
}
