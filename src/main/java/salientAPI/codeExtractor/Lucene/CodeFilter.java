package salientAPI.codeExtractor.Lucene;

import mySql.SqlConnector;

import java.sql.ResultSet;
import java.util.*;

/**
 * Created by oliver on 2017/8/23.
 */
public class CodeFilter {
    private Map<String , String> classes;
    private Map<String , String> methods;

    public static void main(String[] args){
        CodeFilter filter = new CodeFilter();
        String text = "Not sure of the exact API, but it's changed to an instance object. All QueryParsers are now instance objects.\n" +
                "\n" +
                "var qp = new QueryParser(new StandardAnalyzer(),fields);\n" +
                "qp.Parse(inputString,fields);";
        List<String> result = filter.getCodeElement(text);
        for(String code : result){
            System.out.println(code);
        }
    }

    public CodeFilter(){
        init();
    }

    private void init(){
        classes = new HashMap<>();
        methods = new HashMap<>();

        ResourceBundle bundle = ResourceBundle.getBundle("configuration");
        String databaseUrl = bundle.getString("databaseUrl");
        String databaseUser = bundle.getString("databaseUser");
        String databasePwd = bundle.getString("databasePwd");
        String databaseDriver = bundle.getString("databaseDriver");

        String apiTable = bundle.getString("databaseTable");

        SqlConnector conn = new SqlConnector(databaseUrl , databaseUser , databasePwd , databaseDriver);
        conn.start();
        String sql = "select qualifiedName , name from " + apiTable + " where type='CLASS'";
        conn.setPreparedStatement(sql);
        ResultSet rs = conn.executeQuery();
        if(rs != null) {
            try {
                while (rs.next()) {
                    String qualifiedName = rs.getString(1);
                    String name = rs.getString(2);
                    if(qualifiedName != null && qualifiedName.length() > 0){
                        if(classes.containsKey(name))
                            classes.put(name , classes.get(name) + " | " + qualifiedName);
                        else
                            classes.put(name , qualifiedName);
                    }else{
                        System.out.println("There is class without package");
                    }
                }
                rs.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        sql = "select qualifiedName , name from " + apiTable + " where type='METHOD'";
        conn.setPreparedStatement(sql);
        rs = conn.executeQuery();
        if(rs != null){
            try{
                while(rs.next()){
                    String qualifiedName = rs.getString(1);
                    String name = rs.getString(2);
                    if(classes.containsKey(name)){
                        methods.put(name , methods.get(name) + " | " + qualifiedName);
                    }else{
                        methods.put(name , qualifiedName);
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public boolean containInClass(String name){
        return classes.containsKey(name);
    }

    public boolean containInMethod(String name){
        return methods.containsKey(name);
    }

    public boolean containsKey(String name){
        return containInClass(name) || containInMethod(name);
    }

    public List<String> getCodeElement(String text){
        List<String> result = new ArrayList<>();
        String[] tokens = text.split("[^a-zA-Z0-9_]");
        for(String token : tokens){
            if(containsKey( token )){
                result.add(token);
            }
        }
        return  result;
    }

}
