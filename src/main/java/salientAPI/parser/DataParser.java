package salientAPI.parser;

import mySql.SqlConnector;
import mySql.SqlConnector.*;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oliver on 2017/6/1.
 */
public class DataParser {
    public static void main(String[] args){
        selectPostWithCode("<java>|<lucene>");
        //extractCodeFromPost();
    }

    public static void selectPostWithCode(String tagGroup){
        try {
            String[] tags = tagGroup.split("\\|");
            SqlConnector conn = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                    "root",
                    "woxnsk",
                    "com.mysql.jdbc.Driver");
            conn.start();

            int start = 0;
            String sql;
            int insertCount = 0;
            int tagCount = tags.length;
            ResultSet rs;
            while(true) {
                sql = "Select Id , Body , Tags from posts order by Id asc limit ";
                sql += start + ",100000" ;
                start += 100001;
                conn.setPreparedStatement(sql);
                rs = conn.executeQuery();

                boolean signal;
                String Id;
                String body;
                String postTags;
                while (rs.next()) {
                    Id = rs.getString(1);
                    body = rs.getString(2);
                    postTags = rs.getString(3);
                    signal = true;
                    if(postTags == null)
                        continue;
                    for (int i = 0; i < tagCount; i++) {
                        if (!postTags.contains(tags[i])) {
                            signal = false;
                            break;
                        }
                    }
                    if(signal) {
                        insertCount = insert(body, insertCount, Id);
                    }
                }
                rs.close();
                if(insertCount % 100 == 0)
                    System.out.println(insertCount);
                if(insertCount > 1000)
                    break;
            }
            //conn.setPreparedStatement(insertSql);
            //conn.execute();
            conn.close();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public static void extractCodeFromPost(){
        SqlConnector conn_extractor = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver"
        );
        conn_extractor.start();
        String sql_extractor;



        int startCount = 0;
        int count = 0;
        String Id;
        String body;
        while(true){
            sql_extractor = "select Id , Body from SOAnswerWithCode order by Id asc limit " + startCount + ", 1000";
            startCount += 1000;
            conn_extractor.setPreparedStatement(sql_extractor);
            ResultSet rs = conn_extractor.executeQuery();
            try{
            if(!rs.next())
                break;
            do{
                Id = rs.getString(1);
                body = rs.getString(2);
                count = insert(body , count , Id);
            }while(rs.next());

            rs.close();
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }

    }

    /**
     * extract code segments from the body, and insert them into database
     * @param body
     */
    private static int insert(String body , int insertIndex , String postId){
        String bodyTemp = new String(body);
        int startPosition;
        int endPosition;
        String code;

        String sql_insert = "Insert into SOAnswerWithCode (Id , Body , PostId) Values (?,?,?)";
        SqlConnector conn_insert = new SqlConnector("jdbc:mysql://127.0.0.1:3306/stackoverflow",
                "root",
                "woxnsk",
                "com.mysql.jdbc.Driver");
        conn_insert.start();
        conn_insert.setPreparedStatement(sql_insert);

        while(true){
            startPosition = bodyTemp.indexOf("<code>");
            if(startPosition > -1){
                endPosition = bodyTemp.indexOf("</code>") + "</code>".length();
                code = bodyTemp.substring(startPosition , endPosition);
                bodyTemp = bodyTemp.substring(endPosition);
                char[] array = code.toCharArray();
                int arrayLength = array.length;
                int semiColon = 0;

                for(int i = 0 ; i < arrayLength ; i++){
                    if(array[i] == ';')
                        semiColon ++;
                    if (semiColon > 5)
                        break;
                }
                if(semiColon > 5){
                    code = "<pre>" + code + "</pre>";

                    conn_insert.setInt(1 , insertIndex);
                    conn_insert.setString(2 , code);
                    conn_insert.setInt(3 , Integer.parseInt(postId));
                    conn_insert.execute();
                    insertIndex ++;
                }

            }else{
                break;
            }
        }
        conn_insert.close();
        return insertIndex;
    }
}
