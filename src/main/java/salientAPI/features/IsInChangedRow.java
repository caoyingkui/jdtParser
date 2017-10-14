package salientAPI.features;

import javafx.util.Pair;
import salientAPI.rules.Rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by oliver on 2017/10/13.
 */
public class IsInChangedRow implements featureCalculate{

    @Override
    public float[] calculate(String questionDoc, String answerDoc, String[] codeElements) {
        float[] result = new float[codeElements.length];

        List<String> codeSnippets1 = getCode(questionDoc);
        List<String> codeSnippets2 = getCode(answerDoc);
        for(String codeSnippet1 : codeSnippets1){
            for(String codeSnippet2 : codeSnippets2){
                List<Pair<Character , String>> pairs = diff(codeSnippet1 , codeSnippet2);
                isInChangedRow(pairs , codeElements , result);
            }
        }
        return result;
    }

    public List<String> getCode(String code){
        if(code.equals("")) return null;
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("<code>([\\s\\S]*?)</code>");
        Matcher matcher = pattern.matcher(code);
        while(matcher.find()){
            String s1 = matcher.group(0);
            String s2 = matcher.group(1);
            result.add(matcher.group(1));
        }
        return result;
    }

    public void isInChangedRow(List<Pair<Character , String>> pairs , String[] codeElements , float[] result){
        int add = 0;
        int minus = 0;
        for(Pair<Character , String> pair : pairs){
            if(pair.getKey() == ' ') add ++;
            else if(pair.getKey() == '-') minus ++ ;
        }
        if(add >= 1 && minus <= 3){
            for(Pair<Character , String> pair : pairs){
                if(pair.getKey() != '-'){
                    String code = pair.getValue();
                    for(int i = 0 ; i < codeElements.length ; i++){
                        if(code.contains(codeElements[i]) && result[i] == 0){
                            result[i] = 1;
                        }
                    }
                }
            }
        }
    }

    private boolean equal(String str1 , String str2){
        boolean result = true;
        str1 = str1.trim();
        str2 = str2.trim();

        String[] words1 = str1.split("\\s+");
        String[] words2 = str2.split("\\s+");

        if(words1.length != words2.length){
            result = false;
        }else{
            int index = words1.length;
            while( -- index  > -1){
                if(words1[index].compareTo(words2[index]) != 0){
                    result =false;
                    break;
                }
            }
        }
        return result;
    }

    public List<Pair<Character , String>> diff(String formerString , String latterString ){
        List<Pair<Character , String>> result = new ArrayList<>();
        String[] formerSet = formerString.split("\n");
        String[] latterSet = latterString.split("\n");

        int rows_former = formerSet.length;
        int rows_latter = latterSet.length;

        // this algorithm is called Myers, there are blog : http://cjting.me/misc/how-git-generate-diff/
        int graph[][] = new int[rows_latter + 1 ][rows_former + 1]; //   size : (|len_latter|)
        int i , j ;
        for(i = 0 ; i < rows_latter ; i ++){
            for(j = 0 ; j < rows_former ; j++){
                graph[i + 1][j + 1] = equal(latterSet[i] , formerSet[j]) ? 0 : -1;
            }
        }

        for(i = 0 ; i <= rows_former ; i ++){
            graph[0][i] = i ;
        }
        for(i = 0 ; i <= rows_latter ; i ++){
            graph[i][0] = i;
        }
        int temp;
        for(i = 1 ; i <= rows_latter ; i ++){
            for(j = 1 ; j <= rows_former ; j ++){
                if(graph[i][j] == 0) graph[i][j] = graph[i - 1][j - 1];
                temp = graph[i - 1][j] <= graph[i][j - 1] ? graph[i - 1][j] + 1 : graph[i][j - 1] + 1;
                if(graph[i][j] == -1 || graph[i][j]  > temp) graph[i][j] = temp;
            }
        }

        List<Pair<Integer , Integer>> path = new ArrayList<Pair<Integer, Integer>>() ;
        path.add(new Pair(rows_former , rows_latter ));
        myers(graph , rows_former , rows_latter , path);

        Pair<Integer , Integer> former , latter;
        int former_x , former_y , latter_x , latter_y;
        former = path.get(0);
        former_x = former.getKey();
        former_y = former.getValue();
        for(i = 1 ; i < path.size() ; i++){
            latter = path.get(i);
            latter_x = latter.getKey();
            latter_y = latter.getValue();

            if(latter_x > former_x && latter_y > former_y){
                result.add(new Pair(' ' , latterSet[former_y]));
                // System.out.println("  " + latterSet[former_y]);
            }else if(latter_x == former_x){
                result.add(new Pair('+' , latterSet[former_y]));
                //System.out.println("+ " + latterSet[former_y]);
            }else if(latter_y == former_y){
                result.add(new Pair('-' , formerSet[former_x]));
                //System.out.println("- " + formerSet[former_x]);
            }
            former_x = latter_x;
            former_y = latter_y;
        }
        return result;
    }

    /** myers is the algorithm is used to calculate the best solution from start point (0 , 0) to end point(end_x , end_y)
     * graph : record the condition of the graph
     * end_x : horizontal coordinate of the end point
     * end_y : vertical coordinate of the end point
     *
     * return : pair of <D , K>
     */
    void myers(int [][] graph , int end_x , int end_y , List<Pair<Integer , Integer>> path){
        int x = 0 , y = 0;
        if(end_x ==0 && 0 == end_y) return ;
        else if(end_x == 0) {
            y = end_y - 1;
        }
        else if(end_y == 0) {
            x = end_x - 1;
        }
        else{
            if(graph[end_y][end_x] - graph[end_y - 1][end_x] == 1){
                x = end_x ;
                y = end_y - 1;
            }else if(graph[end_y][end_x] - graph[end_y][end_x - 1] == 1){
                x = end_x - 1;
                y = end_y ;
            }else {
                x = end_x - 1;
                y = end_y - 1;
            }

        }
        path.add(0 , new Pair(x , y));
        myers(graph , x , y , path);
    }
}

