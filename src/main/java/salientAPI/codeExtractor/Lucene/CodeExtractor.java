package salientAPI.codeExtractor.Lucene;
import mySql.SqlConnector;
import org.eclipse.jdt.core.dom.*;
import salientAPI.codeExtractor.CodeInfoExtractor;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.*;

/**
 * Created by oliver on 2017/8/20.
 */
public class CodeExtractor extends CodeInfoExtractor{

    private ASTParser astParser = ASTParser.newParser(AST.JLS8);

    SqlConnector conn ;
    String luceneRoot;
    String sql_classInsert;
    String sql_interfaceInsert;
    String sql_methodInsert;

    private void init(){

        ResourceBundle bundle = ResourceBundle.getBundle("configuration");

        luceneRoot = bundle.getString("rootOfLucene");

        String databaseUrl = bundle.getString("databaseUrl");
        String databaseUser = bundle.getString("databaseUser");
        String databasePwd = bundle.getString("databasePwd");
        String databaseDriver = bundle.getString("databaseDriver");

        conn = new SqlConnector(databaseUrl , databaseUser , databasePwd , databaseDriver);
        conn.start();

        sql_classInsert = "insert into api (name , qualifiedName , type , extends , implements) values ( ? , ? , ? , ? , ?)";
        sql_methodInsert = "insert into api (name , qualifiedName , type , returnType ,argumentCount , argumentTypes , argumentNames) values (? , ? , ? , ? , ? , ? , ?)";
    }




    public static void main(String[] args) throws Exception{



        //FileInputStream javaFileInputStream = new FileInputStream(new File("D:\\apache\\lucene\\java\\5.5.3\\lucene-5.5.3-src\\lucene-5.5.3\\analysis\\common\\src\\java\\org\\apache\\lucene\\analysis\\ar\\ArabicAnalyzer.java"));
        CodeExtractor ccc = new CodeExtractor();
        ccc.init();
        //ccc.getCompliationUnit(new File("D:\\apache\\lucene\\java\\5.5.3\\lucene-5.5.3-src\\lucene-5.5.3\\analysis\\common\\src\\java\\org\\apache\\lucene\\analysis\\br\\BrazilianStemmer.java"));
        ccc.traverseDirector("D:\\apache\\lucene\\java\\5.5.3\\lucene-5.5.3-src\\lucene-5.5.3");

    }

    public void traverseDirector(String director){
        File dir = new File(director);
        if(dir.isDirectory())
            traverseDirector(dir , "");
    }

    public void traverseDirector(File director , String temp){
        if(director.isDirectory()) {
            File[] files = director.listFiles();
            for(File file : files){
                if(file.isDirectory()){
                    System.out.println(temp + file.getName());
                    traverseDirector(file , "  ");
                }else{
                    String fileName = file.getName();
                    String postfix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if(postfix.equals("java"))
                        getCompliationUnit(file);
                }

            }
        }
    }

    public CompilationUnit getCompliationUnit(File file){
        CompilationUnit result = null ;

        try{
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            byte[] input = new byte[bufferedInputStream.available()];
            bufferedInputStream.read(input);
            bufferedInputStream.close();
            astParser.setSource(new String(input).toCharArray());
            result = (CompilationUnit)(astParser.createAST(null));

            PackageDeclaration pack = result.getPackage();
            String packageName = "";
            if(pack != null){
                packageName = getName(pack.getName());
            }

            List<Object> declarations = result.types();
            for(Object declaration : declarations){
                if(declaration instanceof TypeDeclaration){
                    getInfoOfTypeDeclaration((TypeDeclaration)declaration , packageName);
                }else{
                    System.out.println("文件中除了类声明外，还有其他声明！");
                    System.out.println(declaration.getClass());
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            result = null;
        }finally {
            return result;
        }
    }

    public void getInfoOfTypeDeclaration(TypeDeclaration typeDeclaration , String qualifiedName){

        //get class name
        Name clazz = typeDeclaration.getName();
        String className = getName(clazz);

        //get super class
        String superClassName = "";
        Type superClass = typeDeclaration.getSuperclassType();
        if(superClass != null)
            superClassName = getNameOfType(superClass);

        //get implement interfaces
        List<Type> interfaces = typeDeclaration.superInterfaceTypes();
        String interfacesName = "";
        if(interfaces.size() > 0) {
            for (Type i : interfaces) {
                interfacesName += getNameOfType(i) + " ";
            }
            interfacesName = interfacesName.substring(0, interfacesName.length() - 1);
        }

        sql_classInsert = "insert into api (name , qualifiedName , type , extends , implements) values ( ? , ? , ? , ? , ?)";
        conn.setPreparedStatement(sql_classInsert);
        conn.setString(1 , className );
        conn.setString(2 , qualifiedName);
        conn.setString(4 , superClassName);
        conn.setString(5 , interfacesName);
        if(!typeDeclaration.isInterface()){
            conn.setString(3 , "CLASS");
        }else{
            conn.setString(3 , "INTERFACE");
        }
        conn.execute();

        //get method declaration
        List<BodyDeclaration> bodyDeclarations = typeDeclaration.bodyDeclarations();
        for(BodyDeclaration declaration:bodyDeclarations){
            if(declaration instanceof MethodDeclaration){
                getInfoOfMethodDeclaration((MethodDeclaration)declaration , qualifiedName + "." + className);
            }else if(declaration instanceof TypeDeclaration){
                getInfoOfTypeDeclaration((TypeDeclaration)declaration , qualifiedName + "." + className);
            }
        }
    }

    public void getInfoOfMethodDeclaration(MethodDeclaration methodDeclaration , String qualifiedName){
        //get method name
        Name method = methodDeclaration.getName();
        String methodName = getName(method);

        //get return type
        Type returnType = methodDeclaration.getReturnType2();
        String returnTypeName = "";
        if(returnType != null) {
            returnTypeName = getNameOfType(returnType);
        }else{
            returnTypeName = "null";
        }


        //get parameters
        String parameter_typeNames = "";
        String parameter_variableNames = "";
        List<Object> parameters = methodDeclaration.parameters();
        int count = 0;
        if(parameters != null && parameters.size() > 0) {
            count = parameters.size();
            for (Object parameter : parameters) {
                if (parameter instanceof SingleVariableDeclaration){
                    Variable variable =  getInfoOfSingleVariableDeclaration((SingleVariableDeclaration)parameter);
                    parameter_typeNames += (variable.type + " | ");
                    parameter_variableNames += (variable.name + " | ");
                }else{
                    System.out.println("方法定义时有其他的参数类型！");
                }
            }
            parameter_typeNames = parameter_typeNames.substring(0 , parameter_typeNames.length() - 3); // eliminate the last space
            parameter_variableNames = parameter_variableNames.substring(0 , parameter_variableNames.length() - 3); // eliminate the last space
        }

        try {
            conn.setPreparedStatement(sql_methodInsert);
            conn.setString(1, methodName);
            conn.setString(2, qualifiedName);
            conn.setString(3, "METHOD");
            conn.setString(4 , returnTypeName);
            conn.setInt(5, count);
            conn.setString(6 , parameter_typeNames);
            conn.setString(7 , parameter_variableNames );
            conn.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Variable getInfoOfSingleVariableDeclaration(SingleVariableDeclaration singleVariableDeclaration){
        //get type name
        Type type = singleVariableDeclaration.getType();
        String typeName = getNameOfType(type);
        String variableName = getName(singleVariableDeclaration.getName());

        Variable result = new Variable();
        result.type = typeName;
        result.name = variableName;
        return result;
    }

    public class Variable{
        public String type = "";
        public String name = "";
    }
}
