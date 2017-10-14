package salientAPI.parser;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import salientAPI.codeGraph.Graph;
import salientAPI.parser.util.ReturnValue;

import java.io.*;
import java.util.*;

/**
 * Created by oliver on 2017/5/28.
 */
public class JATParser {
    public Graph codeGraph = new Graph();

    private ASTParser astParser = ASTParser.newParser(AST.JLS3); // 非常慢
    private static MethodInvocation sta;

    public static void main(String[] args) throws Exception {
        JATParser pa = new JATParser();

        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\oliver\\Desktop\\java.txt"));
        String line ;
        String lines = "";
        while((line = reader.readLine())!= null){
            lines += line;
        }

        Map<Integer , ASTNode> map = pa.parseCodeSnippet(lines);


        /*while(true) {
            JATParser jdt = new JATParser();
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\oliver\\Desktop\\test.java"));
            String line;
            //String completeLine = "";
            while ((line = reader.readLine()) != null) {
                jdt.parseCodeLine(line);
            }
            reader.close();
            jdt.codeGraph.showGraph();
            System.out.println(jdt.codeGraph.getSalientAPI());
        }*/




        /*JATParser jdt ;
        jdt = new JATParser();
        FileInputStream javaFileInputStream = new FileInputStream("C:\\Users\\oliver\\Desktop\\test.java");

        jdt.classDeclarationParser(javaFileInputStream);

        CompilationUnit result = jdt.getCompilationUnit(javaFileInputStream);
        List commentList = result.getCommentList();// 获取注释信息,包含 doc注释和单行注释
        PackageDeclaration package1 = result.getPackage();// 获取所在包信息
        // 如:"package readjavafile;"
        List importList = result.imports();// 获取导入的包
        TypeDeclaration type = (TypeDeclaration) result.types().get(0);// 获取文件中的第一个类声明(包含注释)
        FieldDeclaration[] fieldList = type.getFields();// 获取类的成员变量

        MethodDeclaration[] methodList = type.getMethods();// 获取方法的注释以及方法体
        Type method_type = methodList[0].getReturnType2();// 获取返回值类型 如 void
        SimpleName method_name = methodList[0].getName();// 获取方法名 main
        Javadoc o1 = methodList[0].getJavadoc();// 获取方法的注释
        List o4 = methodList[0].thrownExceptions();// 异常
        List o5 = methodList[0].modifiers();// 访问类型如:[public, static]
        List o6 = methodList[0].parameters();// 获取参数:[String[] args]
        Block method_block = methodList[0].getBody();// 获取方法的内容如:"{System.out.println("Hello");}"
        List statements = method_block.statements();// 获取方法内容的所有行

        jdt.codeGraph.showGraph();
        System.out.println(statements.get(0).getClass());

        // 获取第一行的内容
        Iterator it = statements.iterator();
        while(it.hasNext()){
            Object st = it.next();
            if(st instanceof VariableDeclarationStatement ){
                jdt.variableDeclarationStatementParser((VariableDeclarationStatement)st);
            }
        }
        VariableDeclarationStatement test =(VariableDeclarationStatement) statements.get(9);
        //VariableDeclarationStatement test= (VariableDeclarationStatement)(((ExpressionStatement)(statements.get(9))).getExpression());

        Type tttype = test.getType();
        System.out.println(tttype.getClass());

        //System.out.println(sta.);
        System.out.println(statements.get(0).toString());*/

    }

    /**
     * 获得java源文件的结构CompilationUnit
     */
    public CompilationUnit getCompilationUnit(FileInputStream javaFileInputStream) throws Exception {

        BufferedInputStream bufferedInputStream = new BufferedInputStream(
                javaFileInputStream);
        byte[] input = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(input);
        bufferedInputStream.close();
        this.astParser.setSource(new String(input).toCharArray());
        /**/
        CompilationUnit result = (CompilationUnit) (this.astParser
                .createAST(null)); // 很慢

        return result;
    }

    /**
     * Given a code snippet, which is represented by a String codeSnippet, then parse the code.
     * @param codeSnippet: the code snippet
     * @return <kind , ASTNode> if succeed: kind will take on of the 4 values which represent the type of the astCode
     *     1、K_CLASS_BODY_DECLARATIONS: Kind constant used to request that the source be parsed as a sequence of class body declarations.
     *     2、K_COMPILATION_UNIT: Kind constant used to request that the source be parsed as a compilation unit.
     *     3、K_EXPRESSION: Kind constant used to request that the source be parsed as a single expression.
     *     4、K_STATEMENTS: Kind constant used to request that the source be parsed as a sequence of statements.
     *
     *     null, if failed.
     */

    public Map<Integer , ASTNode> parseCodeSnippet(String codeSnippet){
        Map<Integer , ASTNode> result = new HashMap<>();
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setSource(codeSnippet.toCharArray());

        class InternalParser{
            boolean parse(Map<Integer , ASTNode> result ,  int kind ){
                parser.setKind(kind);
                ASTNode node ;
                try{
                    node = parser.createAST(null);
                    result.put(kind , node);
                } catch(IllegalStateException e){
                    result.clear();
                    return false;
                }
                return true;
            }
        }

        InternalParser temp = new InternalParser();
        if(!(
                temp.parse(result , ASTParser.K_STATEMENTS) ||
                temp.parse(result , ASTParser.K_COMPILATION_UNIT )||
                temp.parse(result , ASTParser.K_CLASS_BODY_DECLARATIONS) ||

                temp.parse(result , ASTParser.K_EXPRESSION)
            )
        ){
            result = null;
        }
        return result;
    }

    public Block getBlock(String sentence){
        ASTParser sentenceParser = ASTParser.newParser(AST.JLS8);
        sentenceParser.setSource(sentence.toCharArray());
        sentenceParser.setKind(ASTParser.K_STATEMENTS);
        Block block = (Block)sentenceParser.createAST(null);
        return block;
    }

    public void parseCodeSegment(String segment){
        String[] lines = segment.split("\n");
        int lineCount = lines.length;
        for(String line : lines){
            parseCodeLine(line);
        }
    }

    public boolean parseCodeLine(String line){
        if(line.trim().length() == 0)
            return false;
        Block block = getBlock(line);
        if(block.statements().size() == 0)
            return false;
        Statement sentenceStatement = (Statement)(block.statements().get(0));
        statementParser(sentenceStatement);
        return true;
    }

    public void classDeclarationParser(FileInputStream fileStream) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(fileStream);
        byte[] input = new byte[bis.available()];
        bis.read(input);
        bis.close();
        classDeclarationParser(new String(input).toCharArray());
    }

    public void classDeclarationParser(String code){
        classDeclarationParser(code.toCharArray());
    }


    public void classDeclarationParser(char[] code) {
        this.astParser.setSource(code);
        CompilationUnit TypeUnit = (CompilationUnit)this.astParser.createAST(null);
        Iterator typesIterator = TypeUnit.types().iterator();
        if(typesIterator != null){
            while(typesIterator.hasNext()){
                Object temp = typesIterator.next();
                if(temp instanceof TypeDeclaration){
                    typeDeclarationParser((TypeDeclaration)temp);
                }
            }
        }

    }

    public void typeDeclarationParser(TypeDeclaration declaration){

        //try to find all the global variables
        //region <extract the global variables>
        try{
            Iterator bodyDeclarationIterator = declaration.bodyDeclarations().iterator();
            if(bodyDeclarationIterator != null) {
                while (bodyDeclarationIterator.hasNext()) {
                    Object body = bodyDeclarationIterator.next();
                    if (body instanceof FieldDeclaration) {
                        Type baseType = ((FieldDeclaration) body).getType();
                        String globalVariableTypeName = getTypeName(baseType);
                        String globalVariableName;
                        Iterator declarationIterator = ((FieldDeclaration) body).fragments().iterator();
                        while(declarationIterator.hasNext()){
                            VariableDeclarationFragment variable =(VariableDeclarationFragment)declarationIterator.next();
                            globalVariableName = getName(variable.getName());
                            codeGraph.addGlobalVariable(globalVariableName , globalVariableTypeName);
                        }
                    }
                }
            }
        }catch (Exception e){
            System.out.println("Error from function: typeDeclarationParser");
        }
        //endregion<extract the global variables>

        //region <extract methodDeclarations>
        try{
            Iterator bodyDeclarationIterator = declaration.bodyDeclarations().iterator();
            while(bodyDeclarationIterator.hasNext()){
                Object temp = bodyDeclarationIterator.next();
                if(temp instanceof MethodDeclaration){
                    codeGraph.initialize();
                    ReturnValue method = methodDeclarationParser((MethodDeclaration)temp);
                    System.out.println("Method " + method.getFuctionName() + ": " + codeGraph.getSalientAPI());
                    codeGraph.showGraph();
                }
            }

        }catch (Exception e){
            System.out.println("Error from function: typeDeclarationParser");
        }
        //endregion<extract methodDeclarations>

    }

    public ReturnValue methodDeclarationParser(MethodDeclaration declaration){
        ReturnValue result = new ReturnValue(ReturnValue.ReturnValueType.ABOUTFUCTION);
        String methodName = getName(declaration.getName());
        result.setFunctionName(methodName);
        //region<get parameters of the method>
        try{
            Iterator parameterIterator = declaration.parameters().iterator();
            while(parameterIterator.hasNext()){
                Object temp = parameterIterator.next();
                if(temp instanceof SingleVariableDeclaration){
                    ReturnValue returnValue = singleValueDeclarationParser((SingleVariableDeclaration)temp);
                    String variableName = returnValue.getVariableName();
                    String variableTypeName = returnValue.getVariableOriginType();
                    codeGraph.addVariable(variableName , variableTypeName);
                }
            }
        }catch(Exception e){
            System.out.println("Error from function: methodDeclarationParser");
        }
        //endregion<get parameters of the model>

        //region <parse the statement of the method>
        try{
            Block block = declaration.getBody();
            blockParser(block);
        }catch(Exception e){
            System.out.println("Error from function: methodDeclarationParser");
        }
        //endregion<parse the statement of the method>
        return result;
    }

    public void blockParser(Block block){
        try{
            Iterator<Statement> statementIterator = block.statements().iterator();
            while(statementIterator.hasNext()){
                Statement statement = statementIterator.next();
                statementParser(statement);
            }
        }catch (Exception e){
            System.out.println("Error from function: blockParser");
        }
    }

    public void statementParser(Statement statement){
        if(statement instanceof VariableDeclarationStatement){
            variableDeclarationStatementParser((VariableDeclarationStatement) statement);
        }else if(statement instanceof ExpressionStatement){
            expressionStatementParser((ExpressionStatement) statement);
        }else if(statement instanceof TryStatement){
            tryStatementParser((TryStatement)statement);
        }else if(statement instanceof  WhileStatement){
            whileStatementParser((WhileStatement)statement);
        }else if(statement instanceof IfStatement){
            ifStatementParser((IfStatement)statement);
        }else if(statement instanceof DoStatement){
            doStatementParser((DoStatement)statement);
        }
        else{
            System.out.println("There are other type of statment:" + statement.getClass().toString());
            System.out.println(statement.toString());
        }
    }

    public void tryStatementParser(TryStatement statement){
        try{

            //region<parse try block>
            Block tryBody = statement.getBody();
            blockParser(tryBody);
            //endregion<parse try block>

            //region <parse catch block>
            Iterator<CatchClause> catchIterator = statement.catchClauses().iterator();
            while(catchIterator.hasNext()){
                CatchClause clause = catchIterator.next();
                SingleVariableDeclaration e = clause.getException();
                singleValueDeclarationParser(e);

                Block catchBody = clause.getBody();
                blockParser(catchBody);
            }
            //endregion<parse catch block>

            //region <parse finally block>
            Block finallyBlock = statement.getFinally();
            if(finallyBlock != null) {
                blockParser(finallyBlock);
            }
            //endregion<parse finally block>

        }catch(Exception e){
            System.out.println("Error from function: tryStatementParser" );
        }
    }

    public void whileStatementParser(WhileStatement statement){
        try{
            Expression condition = statement.getExpression();

            Statement whileBlock = statement.getBody();
            if(whileBlock instanceof Block){
                blockParser((Block)whileBlock);
            }else{
                statementParser(whileBlock);
            }
        }catch(Exception e){
            System.out.println("Error from function: whileStatementParser");
        };
    }

    public void ifStatementParser(IfStatement statement){
        try{
            Statement ifBlock = statement.getThenStatement();
            if(ifBlock instanceof Block){
                blockParser((Block)ifBlock);
            }else{
                statementParser(ifBlock);
            }

            Expression condition = statement.getExpression();
            //TODO

            Statement optionElse = statement.getElseStatement();
            if(optionElse != null){
                if(optionElse instanceof IfStatement){ // else if() {} statement
                    ifStatementParser((IfStatement) optionElse);
                }else if(optionElse instanceof Block){ // else { } statement
                    blockParser((Block)optionElse);
                }else { // else ; statement
                    statementParser(optionElse);
                }
            }
        }catch (Exception e){
            System.out.println("Error from function: ifStatementParser");
        }
    }

    public void doStatementParser(DoStatement statement){
        try{
            Statement doBlock = statement.getBody();
            if(doBlock instanceof Block){
                blockParser((Block)doBlock);
            }else{
                statementParser(doBlock);
            }
            Expression condition = statement.getExpression();
        }catch(Exception e){
            System.out.println("Error from function: doStatementParser");
        }
    }


    public ReturnValue singleValueDeclarationParser(SingleVariableDeclaration declaration){
        ReturnValue result = new ReturnValue(ReturnValue.ReturnValueType.ABOUTVARIABLE);
        String variableName = getName(declaration.getName());
        String variableTypeName = getTypeName(declaration.getType());
        result.setVariableName(variableName);
        result.setVariableOriginType(variableTypeName);
        return result;
    }

    //region<statement parsers>
    public void expressionStatementParser(ExpressionStatement statement){
        Expression expression = statement.getExpression();
        if(expression instanceof MethodInvocation){
            methodInvocationParser((MethodInvocation)expression);
        }else if(expression instanceof Assignment){
            assignmentParser((Assignment)expression);
        }else{
            ;
        }
    }

    public void variableDeclarationStatementParser(VariableDeclarationStatement statement) {
        try {
            String variableTypeName = "";
            String variableName = "";
            Type baseType = statement.getType();
            int variableIndex ;
            //region<get the base type of the variable>
            variableTypeName = getTypeName(baseType);
            //endregion

            /*Name typeName;
            if (baseType instanceof SimpleType) {
                typeName = ((SimpleType) baseType).getName();
                if (typeName instanceof SimpleName) {
                    variableTypeName = ((SimpleName) typeName).getIdentifier();
                }
            }
            else {
                System.out.println("there some basetype in the function: variableDeclarationStatementParser");
                System.out.println("the statement is:" + statement.toString());
            }*/

            //region <get the variableDeclarationFragments>
            if (statement.fragments().size() == 1) {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
                variableName = fragment.getName().getIdentifier();

                Expression optionalInitializer = fragment.getInitializer();
                if(optionalInitializer != null) {
                    if (optionalInitializer instanceof MethodInvocation) {
                        Set<String> arguments = methodInvocationParser((MethodInvocation) optionalInitializer).getArguments();
                        variableIndex = codeGraph.addVariable(variableName , variableTypeName);
                        int argumentIndex;
                        String argumentName;
                        Iterator<String> it = arguments.iterator();
                        while(it.hasNext()){
                            argumentName = it.next();
                            argumentIndex = codeGraph.indexOf(argumentName);
                            codeGraph.buildLink(argumentIndex , variableIndex , LinkType.ARGUEMENT_OF_INITIALIZOR);
                        }
                    } else if (optionalInitializer instanceof SimpleName) {
                        ;
                    } else if(optionalInitializer instanceof ClassInstanceCreation) {
                        variableTypeName = getTypeName(((ClassInstanceCreation) optionalInitializer).getType());
                        Set<String> arguments = classInstanceCreationParser((ClassInstanceCreation) optionalInitializer).getArguments();
                        variableIndex = codeGraph.addVariable(variableName , variableTypeName);
                        int argumentIndex;
                        String argumentName;
                        Iterator<String> it = arguments.iterator();
                        while(it.hasNext()){
                            argumentName = it.next();
                            argumentIndex = codeGraph.indexOf(argumentName);
                            codeGraph.buildLink(argumentIndex , variableIndex , LinkType.ARGUEMENT_OF_INITIALIZOR);
                        }
                    }else if (optionalInitializer instanceof ArrayCreation){
                        variableTypeName = getTypeName(((ArrayCreation) optionalInitializer).getType().getElementType());
                        variableIndex = codeGraph.addVariable(variableName , variableTypeName);

                    }else if (optionalInitializer instanceof NumberLiteral ||
                            optionalInitializer instanceof StringLiteral ||
                            optionalInitializer instanceof NullLiteral) {
                        codeGraph.addVariable(variableName , variableTypeName);
                    }else if(optionalInitializer instanceof InfixExpression){
                        //@INCOMPLETE
                        infixExpressionParser((InfixExpression)optionalInitializer);
                    }else{
                        System.out.println("There is another type of initializer in funtion variableDeclarationStatementParser :" + statement.toString() );
                        System.out.println("There intializer's type is: " + optionalInitializer.getClass().toString());
                    }
                }else{
                    codeGraph.addVariable(variableName , variableTypeName);
                }
            }
            //endregion of <get the variableDeclarationFragments>

        }catch(Exception e){
            System.out.println("error in function : varibaleDeclarrationParser");
        }
        //SimpleType
    }

    //endregion of <statement parser>



    //region<expression parsers>

    // must make sure that arguments of this list have been stored into the graph
    public ReturnValue methodInvocationParser(MethodInvocation expression){
        ReturnValue result = new ReturnValue(ReturnValue.ReturnValueType.ABOUTFUCTION);
        Expression optionalExpression = expression.getExpression();
        String variableName = "";
        String methodName;

        //region<get the variable name>
        //a.c() or a.b.c() , not c()
        if(optionalExpression != null){
            if(optionalExpression instanceof  SimpleName){
                String name = ((SimpleName) optionalExpression).getIdentifier();
                if(codeGraph.containsVariable(name)){
                    result.setVariableName(name );
                }else{
                    //this function is start with a static function of a
                    if(IsAAPI_Class(name)){
                        codeGraph.addVariable(name , name); // we just add the static class to the code graph, of which variable and variable type all are the static type.
                        result.setVariableName( name );
                    }else{
                        //@NO_DECLARATION
                        codeGraph.addVariable(name , "");
                        result.setVariableName(name);
                        System.out.println("there is a variable which are never been declaration:" + name);
                    }
                }
            }else if(optionalExpression instanceof QualifiedName){
                //TODO
            }else if(optionalExpression instanceof ParenthesizedExpression){
                ReturnValue temp = parenthesizedExpressionParser((ParenthesizedExpression)optionalExpression);
                result.setVariableName(temp.getVariableName());
                result.setVariableCastType(temp.getVariableCastType());
            }else if(optionalExpression instanceof MethodInvocation){
                // 这里有问题afasdfas
                ReturnValue temp = methodInvocationParser((MethodInvocation)optionalExpression);
                result.setVariableName(temp.getVariableName());
                result.setFunctionName(temp.getFuctionName() + "()");
            }else if(optionalExpression instanceof FieldAccess){
                Expression fieldQualifier = ((FieldAccess) optionalExpression).getExpression();
                if(fieldQualifier instanceof ThisExpression){
                    SimpleName field = ((FieldAccess)optionalExpression).getName();
                    String fieldName = getName(field);
                    if(codeGraph.containsVariable(fieldName)){
                        result.setVariableName(fieldName);
                    }
                }
            }else{
                System.out.println("there some other type of optionalExpression: " + optionalExpression.getClass().toString() );
            }
        }
        //endregion<get the variable name>

        //region<get the method name >
        methodName = expression.getName().getIdentifier();
        if(result.getFuctionName().length() > 0){
            methodName = result.getFuctionName() + "." + methodName + "()";
        }
        result.setFunctionName(methodName);
        //endregion<get the method name >

        //region<get the list of argument>
        List arguments = expression.arguments();
        variableName = result.getVariableName();
        if(arguments != null && variableName != null && variableName.length() > 0){
            Iterator iterator= arguments.iterator();
            while(iterator.hasNext()){
                Object temp = iterator.next();
                if(temp instanceof SimpleName){
                    result.addArgument(((SimpleName) temp).getFullyQualifiedName());
                    if(variableName.length() > 0){
                        codeGraph.increaseVariableValue(variableName , 1); // @rule
                    }
                }else if(temp instanceof StringLiteral||
                        temp instanceof  NumberLiteral ||
                        temp instanceof  NullLiteral) {
                    // if temp is "string" , the quotation masks has meaning, indicating this is a string constant
                    //((StringLiteral) temp).getEscapedValue();  this function will return "string"
                    //((StringLiteral) temp).getLiteralValue();  this function will return string
                    ;
                }else if(temp instanceof ClassInstanceCreation){
                    // when the arguments like,    new a()
                    //TODO
                    ;
                }else if(temp instanceof QualifiedName){
                    String argumentName = getName((QualifiedName)temp);
                    if(!codeGraph.containsVariable(argumentName)){
                        //@NO_DECLARATION
                        codeGraph.addVariable(argumentName , "");
                    }
                    result.addArgument(argumentName);
                }else if(temp instanceof InfixExpression){
                    infixExpressionParser((InfixExpression) temp);
                }
                else if(temp instanceof TypeLiteral){
                    //TODO
                    ;
                }else if(temp instanceof MethodInvocation){
                    ReturnValue value = methodInvocationParser((MethodInvocation)temp);
                    if(value.getVariableName().length() > 0){
                        String functionName = value.getVariableName();
                        if(codeGraph.containsVariable(functionName)){
                            result.addArgument(functionName);
                        }
                    }
                }
                else{
                    System.out.println("there is another arguments type in function: methodInvocation" );
                    System.out.println("the argument type is:" + temp.getClass().toString());
                }
            }
        }
        //endregion<get the list of argument>

        //region <if the function of this invocation is one of the variable , then we need to build up links between variable and argument>
        if( variableName != null && variableName.length() > 0){
            Set<String> argumentNames = result.getArguments();
            int variableIndex ;
            if(codeGraph.containsVariable(variableName)){
                variableIndex = codeGraph.indexOf(variableName);
            }else{
                variableIndex = codeGraph.addVariable(variableName , "");// optimally , it should not be allowed when the argument has no type
            }
            int argumentSize = argumentNames.size();
            int argumentIndex = -1;
            Iterator<String> it = argumentNames.iterator();
            String argument;
            while(it.hasNext()){
                argument = it.next();
                if(codeGraph.containsVariable(argument)){
                    argumentIndex = codeGraph.indexOf(argument);
                }else{
                    argumentIndex = codeGraph.addVariable(argument , ""); // optimally , it should not be allowed when the argument has no type
                }
                codeGraph.buildLink(argumentIndex , variableIndex , LinkType.ARGUEMENT_OF_FUNCTION);
            }
        }
        //endregion <if the function of this invocation is one of the variable , then we need to build up links between variable and argument>
        return result ;
    }

    public ReturnValue classInstanceCreationParser(ClassInstanceCreation expression){
        ReturnValue result = new ReturnValue(ReturnValue.ReturnValueType.ABOUTFUCTION);
        //List<String> argumentNames = new ArrayList<String>();
        try{
            result.setFunctionName(getTypeName(expression.getType()));
            List arguments = expression.arguments();
            Iterator it = arguments.iterator();
            while(it.hasNext()){
                Object argument = it.next();
                //region <instance of SimpleName>
                if(argument instanceof SimpleName){
                    String name = ((SimpleName) argument).getIdentifier();
                    if(codeGraph.containsVariable(name)){
                        result.addArgument(name);
                    }else{
                        //@NO_DECLARATION
                        codeGraph.addVariable(name , "");
                        result.addArgument(name);
                    }
                }else if(argument instanceof NumberLiteral ||
                        argument instanceof StringLiteral ||
                        argument instanceof NullLiteral){

                }//endregion <instance of SimpleName>
                else if(argument instanceof QualifiedName){
                    String argumentName = getName((QualifiedName)argument);
                    if(!codeGraph.containsVariable(argumentName)){
                        //@NO_DECLARATION
                        codeGraph.addVariable(argumentName , "");
                    }
                    result.addArgument(argumentName);
                }
                else if(argument instanceof MethodInvocation){
                    ReturnValue temp = methodInvocationParser((MethodInvocation) argument);
                    result.addArgument(temp.getFuctionName());
                }
                //region <instance of default>
                else{
                    //TODO
                    System.out.println("There are other argument types in function classInstanceCreationParser" + expression.toString());
                    System.out.println("The argument type is: " + argument.getClass().toString());
                }
                //endregion <instance of defualt>
            }
        }catch (Exception e){
            System.out.println("error from function: classInstanceCreationParser");
            System.out.println();
        }finally {
            return result;
        }

    }

    public void assignmentParser(Assignment expression){

        try{
            String variableName = "";
            int variableIndex = -1;
            Expression leftHandSide = expression.getLeftHandSide();
            if(leftHandSide instanceof SimpleName){
                variableName = ((SimpleName) leftHandSide).getIdentifier();
                if(!codeGraph.containsVariable(variableName)){
                    variableIndex = codeGraph.addVariable(variableName , "");
                }else{
                    variableIndex = codeGraph.indexOf(variableName);
                }
            }else if(leftHandSide instanceof FieldAccess){
                Expression fieldQualifier = ((FieldAccess) leftHandSide).getExpression();
                String fieldName = getName( ((FieldAccess)leftHandSide).getName() );
                if(fieldQualifier instanceof ThisExpression){
                    if(codeGraph.containsVariable(fieldName)){
                        variableName = fieldName;
                        variableIndex = codeGraph.indexOf(variableName);
                    }
                }else{
                    System.out.println("There are other type of FiledAccess in function: assignmentParser");
                    System.out.println("the type is: " + fieldQualifier.getClass().toString());
                }
            }else{
                System.out.println("There are other type of the leftside of assignment in function: assignmentParser");
                System.out.println("the type is: " + leftHandSide.getClass().toString());
            }


            Expression rightHandSide = expression.getRightHandSide();
            if(rightHandSide instanceof MethodInvocation){
                methodInvocationParser((MethodInvocation)rightHandSide);
            }else if(rightHandSide instanceof ClassInstanceCreation){
                ;
            }else if(rightHandSide instanceof SimpleName){
                ;
            }else if(rightHandSide instanceof MethodInvocation){
                Set<String> arguments = methodInvocationParser((MethodInvocation) rightHandSide).getArguments();
                Iterator<String> it = arguments.iterator();
                String argumentName;
                while(it.hasNext()){
                    int argumentIndex;
                    argumentName = it.next();
                    argumentIndex = codeGraph.indexOf(argumentName);
                    codeGraph.buildLink(argumentIndex , variableIndex , LinkType.ARGUEMENT_OF_INITIALIZOR);
                }
            }

        }catch(Exception e){
            System.out.println("error in function assignmentParser :" + expression.toString());
        }

    }

    public void whileParser(WhileStatement expression){
        try{
            Expression whileExpression = expression.getExpression();
            if(whileExpression instanceof MethodInvocation){
                methodInvocationParser((MethodInvocation)whileExpression);
            }else {
                System.out.println("there are some case which have not been taken account in Fuction: whileParser");
            }
        }catch(Exception e){

        }
    }

    public ReturnValue parenthesizedExpressionParser(ParenthesizedExpression expression){
        ReturnValue result = null;

        Expression parenthesizedExpression = expression.getExpression();
        if(parenthesizedExpression instanceof CastExpression){
            String castType = getTypeName( ((CastExpression) parenthesizedExpression).getType() ) ;
            Expression castedBody = ((CastExpression) parenthesizedExpression).getExpression();
            if(castedBody instanceof SimpleName){
                String variableName = ((SimpleName) castedBody).getIdentifier();
                int variableIndex ;
                if(codeGraph.containsVariable(variableName)){
                    variableIndex = codeGraph.indexOf(variableName);
                }else{
                    //@NO_DECLARATION
                    variableIndex = codeGraph.addVariable(variableName , "");
                    System.out.println("there is a variable which has never been declared:" + variableName);
                }
                result = new ReturnValue(ReturnValue.ReturnValueType.ABOUTVARIABLE);
                result.setVariableName( variableName);
                result.setVariableCastType(castType);
            }else{
                result = null;
            }
        }else{
            result = null;
        }

        return result;
    }

    public Set<String> infixExpressionParser(InfixExpression expression){
        Set<String> result = new HashSet<String>();

        return result;
    }
    //endregion

    //region<get the type>
    public String getTypeName(Type type){
        String result = "";
        try {
            if (type instanceof PrimitiveType) {
                result = type.toString();
            } else if (type instanceof SimpleType) {
                result = type.toString();
            } else if (type instanceof QualifiedType) {
                //Type qualifiedName = ((Type) type).getQualifier(); // may will be used in the future
                SimpleName name = ((QualifiedType) type).getName();
                result = getName(name);
            } else if (type instanceof NameQualifiedType) {
                Name qualifier = ((NameQualifiedType) type).getQualifier();
                SimpleName name = ((NameQualifiedType) type).getName();
                result = getName(name);
            } else if (type instanceof ArrayType) {
                Type typeTemp = ((ArrayType) type).getElementType();
                result = getTypeName(typeTemp);
            } else if (type instanceof ParameterizedType) {
                Type typeTemp = ((ParameterizedType) type).getType();
                result = getTypeName(typeTemp);
            } else if (type instanceof UnionType) {
                System.out.println("there are UnionType in function: getTypeName");
                System.out.println("the type example is:" + type.toString());
            } else if (type instanceof IntersectionType) {
                System.out.println("there are UnionType in function: IntersectionType");
                System.out.println("the type example is:" + type.toString());
                ;
            } else {
                System.out.println("there are other type of Type in function: getTypeName");
                System.out.println("the type is:" + type.getClass().toString());
            }
        }catch(Exception e){
            System.out.println("error from function:"+ this.getClass().toString() + ".getTypeName");
            System.out.println();
        }finally {
            return result;
        }
    }

    public String getName(Name name){
        String result = "";
        try{
            if(name instanceof SimpleName){
                result = ((SimpleName) name).getIdentifier();
            } else if (name instanceof QualifiedName) {
                Name qualifier = ((QualifiedName) name).getQualifier();
                //result = ((QualifiedName) name).getName().getIdentifier();
                result = ((QualifiedName) name).toString();
            } else{
                System.out.println("there are some other name type in function: getName");
                System.out.println("the type is :" + name.getClass().toString());
            }
        }catch (Exception e){
            System.out.println("error from function:" + this.getClass().toString() + ".getName");
            System.out.println();
        }finally {
            return result;
        }
    }
    //endregion <get the type>

    // region<judge one word whether is a static class of project or not>
    private boolean IsInCamelCase(String word){
        boolean result = false;
        if(word.length() > 0) {
            char startChar = word.charAt(0);
            if(startChar >= 'A' && startChar <= 'Z'){
                result = true;
            }
        }
        return result;
    }

    private boolean IsAPIOfProject(String word){
        return false;
    }

    public boolean IsAAPI_Class(String word){
        return IsInCamelCase(word) || IsAPIOfProject(word);
    }
    //endregion<judge one word whether is a static class of project or not>
}
