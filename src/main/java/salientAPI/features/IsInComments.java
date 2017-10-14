package salientAPI.features;

/**
 * Created by oliver on 2017/10/13.
 */
public class IsInComments implements featureCalculate {
    static final String DELIMITER = "\n";
    //questionDoc is null by default
    @Override
    public float[] calculate(String questionDoc , String answerDoc , String[] codeElements){
        answerDoc = answerDoc.trim();
        if(codeElements.length < 1 || answerDoc == null || answerDoc.length() == 0){
            return null;
        }
        float[] result = new float[codeElements.length];
        String[] lines = answerDoc.split(DELIMITER);
        isInComments(lines , codeElements , result);
        return result;
    }

    private void isInComments(String[] lines , String[] codeElements , float[] result){

        for(String line : lines){
            if(containsComment(line)){
                int length = codeElements.length;
                for(int i = 0 ; i < length ; i++){
                    if(line.contains(codeElements[i]) && result[i] == 0){
                        result[i] = 1;
                    }
                }
            }
        }
    }

    private boolean containsComment(String codeLine){
        return codeLine.contains("//");
    }

}
