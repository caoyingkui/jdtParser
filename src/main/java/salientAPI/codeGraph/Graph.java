package salientAPI.codeGraph;

import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import salientAPI.parser.LinkType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oliver on 2017/5/28.
 */
public class Graph {
    private final int PAGERANK_ITERATION = 10;
    private final int ADDLENGTH = 20;
    private final int FEATURE_COUNT = 2;
    private int maxSize ;
    private int size;
    private LinkType[][] graph ;
    private String[][] variables ;
    private int[][] variableFeatures ;
    private double[] values = null;
    private Map<String , Integer> variableIndexs ;
    private Map<String , String> globalVariables;

    public Graph(){
        size = 0;
        maxSize = ADDLENGTH;
        graph = new LinkType[ADDLENGTH][ADDLENGTH];
        variables = new String[ADDLENGTH][2];
        values = new double[ADDLENGTH];
        variableFeatures = new int[ADDLENGTH][FEATURE_COUNT];
        variableIndexs = new HashMap<String , Integer>();
        globalVariables = new HashMap<String ,String>();
    }

    public void extendGraph(){
        /*for(int i = 0 ; i < 10 ; i ++){
            for(int j = 0 ; j < 10 ; j ++){
                graph[i][j] = 10 * i + j ;
            }
        }*/
        //region<graph extent>
        LinkType[][] graphTemp = new LinkType[maxSize + ADDLENGTH][maxSize + ADDLENGTH];
        for(int i = 0 ; i < maxSize ; i++ ){
            System.arraycopy(graph[i] , 0 , graphTemp[i] , 0 , maxSize);
        }
        graph = graphTemp;
        //endregion

        //region<variables extend>
        String [][] variableTemp = new String[maxSize + 20][2];
        for(int i = 0 ; i < maxSize ; i ++){
            System.arraycopy(variables[i] , 0 , variableTemp[i] , 0 , 2);
        }
        variables = variableTemp;
        //endregion

        //region<variable features extend>
        int[][] featureTemp = new int[maxSize + ADDLENGTH ][FEATURE_COUNT];
        for(int i = 0 ; i < maxSize ; i ++){
            System.arraycopy(variableFeatures[i] , 0 , featureTemp[i] , 0 , FEATURE_COUNT);
        }
        variableFeatures = featureTemp;
        //endregion

        double[] valuesTemp = new double[maxSize + ADDLENGTH];
        System.arraycopy(values , 0 , valuesTemp , 0 , maxSize);
        values = valuesTemp;

        maxSize += ADDLENGTH;
    }

    /**
     * Insert a variable into the graph
     * @param name the variable
     * @param type the type of the varibale
     * @return the index in which the variable will be stored
     *          -1 , if the variable has been stored in the graph.
     */
    public int addVariable(String name , String type ){
        if(variableIndexs.containsKey(name))
            return -1;
        if(size == maxSize){
            extendGraph();
        }
        int index = size;
        size ++;
        variableIndexs.put(name , index);
        values[index] = 1;

        variables[index][0] = name;
        variables[index][1] = type;

        variableFeatures[index][0] = variableFeatures[index][1] = 0;

        return index;
    }

    public boolean addGlobalVariable(String name , String type){
        boolean result = false;
        name = name.trim();
        if(name.length() > 0){
            if(!globalVariables.containsKey(name)) {
                globalVariables.put(name, type);
                result = true;
            }
        }
        return result;
    }

    public void clean(){
        size = 0;
        maxSize = ADDLENGTH;
        graph = new LinkType[ADDLENGTH][ADDLENGTH];
        variables = new String[ADDLENGTH][2];
        values = new double[ADDLENGTH];
        variableFeatures = new int[ADDLENGTH][FEATURE_COUNT];
        variableIndexs = new HashMap<String , Integer>();
        globalVariables = new HashMap<String ,String>();
    }

    public void initialize(){
        if(size != 0)
            clean();
        for(String key : globalVariables.keySet()){
            addVariable(key , globalVariables.get(key));
        }
    }

    public boolean updateVariableFeature(int index , int feature , int featureValue ){
        boolean result = false;
        if(index > -1 && index < size){
            if(feature > -1 && feature < FEATURE_COUNT){
                variableFeatures[index][feature] = featureValue;
                result = true;
            }
        }
        return result;
    }

    public boolean buildLink(int srcNode , int desNode , LinkType type ){
        boolean result = false;
        if(srcNode > -1 && desNode > -1 && srcNode < size && desNode < size && srcNode != desNode)
        {
            result = true;
            graph[srcNode][desNode] = type;
        }
        return result;
    }

    public double initPageValue(int index){
        return values[index];
    }

    private void pageRank(){
        int variableCount = variableIndexs.size();
        String[] variables = new String[variableCount];
        for(String variable : variableIndexs.keySet()){
            int index = variableIndexs.get(variable);
            variables[index] = variable;
        }

        values = new double[size];
        double[] valueTemp = new double[size];
        int[] linkCount = new int[size];

        for(int i = 0 ; i < size ; i ++){
            values[i] = initPageValue(i);
        }

        //count the output links for all variables
        for(int i = 0 ; i < size ; i++){
            linkCount[i] = 1; // we take one node to itself into account
            for(int j = 0 ; j < size ; j ++){
                if(graph[i][j] != null && graph[i][j] != LinkType.NULL)
                    linkCount[i] ++;
            }
        }

        int variableValue;
        for(int i = 0 ; i < size ; i++){
            System.out.print(String.format("%20s  " , variables[i]));
        }
        System.out.println();
        for(int iteration = 0 ; iteration < PAGERANK_ITERATION ; iteration++){


            for(int i = 0 ; i < size ; i++){
                System.out.print(String.format("%20s  " , values[i] + ""));
            }
            System.out.println();

            // update catch for value change
            for(int i = 0 ; i < size ; i ++){
                valueTemp[i] = 0;
            }

            for(int from = 0 ; from < size ; from ++){
                // only when variable i has output link , we will split the value of i to other variables.
                if(linkCount[from] > 0) {
                    for (int to = 0; to < size; to++) {
                        if (graph[from][to] != null && graph[from][to] != LinkType.NULL || from == to) {
                            valueTemp[to] += values[from] / linkCount[from];
                        }
                    }
                }
            }
            System.arraycopy(valueTemp , 0 , values , 0 , size);
        }
    }

    public String getSalientAPI(){
        if(values == null)
            pageRank();
        int max = 0;
        for(int i = 1 ; i < size ; i ++){
            if(values[max] < values[i] ){
                max = i;
            }
        }

        return variables[max][1] + "  " + variables[max][0];
    }

    /**
     * Find whether a variableName has been stored in the graph.
     * @param variableName
     * @return true , if it has been stored by the graph.
     *          false , otherwise.
     */
    public boolean containsVariable(String variableName){
        return variableIndexs.containsKey(variableName);
    }

    /**
     * Get the index of a variable
     * @param variableName the variable
     * @return index of the variable , if it has been stored in the map;
     *          -1 , otherwise.
     */
    public int indexOf(String variableName){
        int index = -1;
        if(variableIndexs.containsKey(variableName)){
            index = variableIndexs.get(variableName);
        }
        return index;
    }

    public void setVariableInitValue(int variableIndex , int value){
        values[variableIndex] = value;
    }

    public void increaseVariableValue(int variableIndex , int increasement){
        values[variableIndex] += increasement;
    }

    public void increaseVariableValue(String variableName , int increasement){
        int variableIndex = indexOf(variableName);
        if(variableIndex > -1) {
            values[variableIndex] += increasement;
        }
    }

    public void showGraph(){
        int variableCount = variableIndexs.size();
        String[] variables = new String[variableCount];
        for(String variable : variableIndexs.keySet()){
            int index = variableIndexs.get(variable);
            variables[index] = variable;
        }
        System.out.print(String.format("%20s" , ""));
        for(int i =0 ; i< variableCount ; i++){
            System.out.print("  " + variables[i]);
        }
        System.out.println();
        for(int i = 0 ; i < variableCount ; i++){
            System.out.print(String.format("%20s" , variables[i]));
            for(int j = 0 ; j < variableCount ; j++){
                String value;
                if(graph[i][j] == null){
                    value = "";
                }else{
                    value = graph[i][j].ordinal() + "";
                }

                System.out.print(String.format("  %" +variables[j].length()+"s" , value +""));
            }
            System.out.print("\n");
        }

        for(int i =0 ; i< variableCount ; i++){
            System.out.print("  " + variables[i]);
        }
        System.out.println();
        for(int i = 0 ; i < variableCount ; i ++){
            System.out.print(String.format("  %" +variables[i].length()+"s" , values[i] +""));
        }
        System.out.println();
    }

    public static void main(String args[]){
        ///Graph p = new Graph();
        //p.extendGraph();
    }

}
