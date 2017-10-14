package salientAPI.rules;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import javafx.util.Pair;
import mySql.SqlConnector;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by oliver on 2017/10/1.
 * Rules are used to specify a group of rule which can be directly determine which code element is salient.
 */
public class Rules {


    public void test(){
        Map<String , String[]> map = new HashMap<String , String[]>();
        Map<String , String> postMap = new HashMap<String , String>();

        SqlConnector conn = new SqlConnector("jdbc:mysql://localhost:3306/lyxtest" , "root" , "woxnsk" , "com.mysql.jdbc.Driver");
        String sql = "select postUuid , codeElements from markedpost";
        conn.start();
        conn.setPreparedStatement(sql);
        ResultSet rs = conn.executeQuery();

        try{
            while(rs.next()){
                String[] meta = new String[3];
                meta[2] = rs.getString(2);
                map.put(rs.getString(1) , meta);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            sql = "select SessionUuid , content from post where Uuid = ?";
            conn.setPreparedStatement(sql);

            for(String post : map.keySet()){
                String[] temp = map.get(post);

                conn.setString(1 , post);
                rs = conn.executeQuery();
                if(rs != null && rs.next()){
                    temp[0] = rs.getString(2);
                    postMap.put(post , rs.getString(1));
                }
                map.put(post , temp);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            conn.close();
            conn = new SqlConnector("jdbc:mysql://localhost:3306/stackoverflow" , "root" , "woxnsk" , "com.mysql.jdbc.Driver");
            conn.start();
            sql = "select Body from lucene_question where Id = ?";
            conn.setPreparedStatement(sql);
            for(String post : postMap.keySet()){
                String question = postMap.get(post).replace("ses" , "");
                conn.setString(1 , question);
                rs = conn.executeQuery();
                if(rs != null && rs.next()){
                    String[] temp = map.get(post);
                    temp[1] = rs.getString(1);
                    map.put(post , temp);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        for(String post : map.keySet()){
            String[] temp = map.get(post);
            if(temp[0] == null || temp[1] == null || temp[2] == null) continue;
            String question = temp[1];
            String answer = temp[0];
            String[] codes = temp[2].split(",");
            if(question == null || answer == null || question.equals("") || answer.equals("")) continue;
            if(post.equals( "pos34844248"))
            {
                int i = 1;
            }
            boolean[] isIn = isInChangedRow(question , answer , codes);
            System.out.print(post + ":");
            for(boolean in : isIn){
                if(in)
                    System.out.print(in + " ");
            }
            System.out.println("");
        }

    }

    public static void main(String[] args){

        new Rules().test();
        /*boolean[] tes = new boolean[3];
        tes[0] = true;
        //test(tes);
        System.out.println(tes[0]);

        //String str1 = "A\nB\nC\nA\nB\nB\nA";
        //String str2 = "C\nB\nA\nB\nA\nC";
        /*String str1 = "" , str2 = "";
        try {
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\oliver\\Desktop\\1.txt"));
            String line;
            while((line = reader.readLine()) != null){
                str1 += line + "\n";
            }
            reader = new BufferedReader(new FileReader("C:\\Users\\oliver\\Desktop\\2.txt"));
            while((line = reader.readLine()) != null){
                str2 += line + "\n";
            }
        }catch (Exception e){
            ;
        }*/

        //new Rules().diff(str1 , str2);


        /*try {
            Map<String , String> map = new HashMap<>();
            Map<String , String> posts = new HashMap<>();
            Map<String , String> APIs = new HashMap<>();
            SqlConnector conn = new SqlConnector("jdbc:mysql://localhost:3306/lyxtest", "root", "woxnsk", "com.mysql.jdbc.Driver");
            conn.start();
            String sql = "select postUuid , markedCodeElements from MarkedPost";
            conn.setPreparedStatement(sql);
            ResultSet rs = conn.executeQuery();
            String temp;
            while (rs.next()) {
                temp = rs.getString(1);
                temp = temp.substring(3);
                posts.put(temp , "");
                APIs.put(temp , rs.getString(2));
            }
            conn.close();
            conn = new SqlConnector("jdbc:mysql://localhost:3306/stackoverflow", "root", "woxnsk", "com.mysql.jdbc.Driver");
            conn.start();
            sql = "select ParentId , Body from lucene_answer where Id=?";
            conn.setPreparedStatement(sql);
            ResultSet trs;
            for(String p : posts.keySet()){
                conn.setString(1 , p);
                trs = conn.executeQuery();
                if(trs.next()){
                    map.put(p , trs.getString(1));
                    posts.put(p , trs.getString(2));
                }
            }

            sql = "select Body from lucene_question where Id=?";
            conn.setPreparedStatement(sql);
            String parent;
            for(String p : posts.keySet()){
                parent = map.get(p);
                conn.setString(1 , parent);
                trs = conn.executeQuery();
                if(trs.next()){
                    posts.put(p , posts.get(p) + trs.getString(1));
                }
            }


            int count = 0;
            int total = 0;

            int add= 0;
            int minus = 0;
            for(String p : posts.keySet()){
                total ++;
                String document = posts.get(p);
                List<String> codes = getCode(document);
                int length = codes.size();
                boolean b = true;
                for(int i = 0 ; i < length && b ; i ++){
                    for(int j = i + 1; j < length && b; j ++){
                        List<Pair<Character , String>> pairs = new Rules().diff(codes.get(i) , codes.get(j));
                        add = 0;
                        minus = 0;
                        for(Pair<Character , String> pair : pairs){
                            if(pair.getKey()== ' ') {// || pair.getKey() =='+'){
                                add ++;
                            }else{
                                minus ++;
                            }
                        }
                       // System.out.println("p: "+ p + "  " + add + " , " + minus);
                        if(add >=1 && minus < 3){
                            for(Pair<Character , String> pair : pairs){
                                String code = pair.getValue();
                                String[] words = APIs.get(p).split(",");
                                for(String word : words){
                                    if(word.length() == 0) continue;
                                    if(code.contains(word)){
                                        System.out.println(p);
                                        System.out.println(word + "\n");
                                        count ++;
                                    }
                                }
                            }
                            b = false;*/
                            /*System.out.println(p);
                            System.out.println("i:");
                            System.out.println(codes.get(i));
                            System.out.println("   ");
                            System.out.println(codes.get(j));*/
/*
                        }
                    }

                }

            }
            System.out.println("count " + count);


        }catch(Exception e){
            e.printStackTrace();
        }
        String code = "adfasdf<code>afasdf\n</code>asdfasdf\n<code> aaasdfdsfa</code>\n";
        getCode(code);*/
    }





    public boolean[] isInChangedRow(String str1 , String str2 , String[] codeElements){
        boolean[] result = new boolean[codeElements.length];

        List<String> codeSnippets1 = getCode(str1);
        List<String> codeSnippets2 = getCode(str2);
        for(String codeSnippet1 : codeSnippets1){
            for(String codeSnippet2 : codeSnippets2){
                List<Pair<Character , String>> pairs = diff(codeSnippet1, codeSnippet2);

                int count = 0;
                int add = 0;
                int remove = 0;
                int ori = 0 ;
                for(Pair<Character , String> pair: pairs){
                    char c = pair.getKey();
                    if(c == '+') add ++;
                    else if(c == '-') remove ++;
                    else if(c == ' ') {
                        String line = pair.getValue();
                        if(line.trim().length() > 5){
                            ori++;
                        }
                    }

                }
                if(ori > 2 && ori + remove > 4){
                    System.out.println("code: ");
                    for(Pair<Character , String> pair: pairs){
                        System.out.println(pair.getKey() + " " + pair.getValue());
                    }
                    System.out.println("   ");
                }
                isInChangedRow(pairs , codeElements , result);
            }
        }

        return result;
    }


    public static Set<String> getItemsSurroundedByComments(String codeSnippet){
        return null;
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




}
