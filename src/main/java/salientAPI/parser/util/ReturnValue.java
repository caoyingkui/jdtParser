package salientAPI.parser.util;

import java.util.*;

/**
 * Created by oliver on 2017/6/4.
 */
public class ReturnValue {
    public enum ReturnValueType{
        ABOUTFUCTION,
        ABOUTVARIABLE
    }

    private ReturnValueType valueType;
    private String variableName = "";
    private String variableOriginType = "";
    private String variableCastType = "";

    private String qualifier = "";
    private String fuctionName = "";
    private Set<String> arguments; // <variableName>

    //region <constructor>
    public ReturnValue(ReturnValueType type){
        setValueType(type);
        arguments = new HashSet<String>();
    }
    //endregion <constructor>

    //region<getter>
    public ReturnValueType getValueType(){
        return valueType;
    }

    public String getVariableName(){
        return variableName;
    }

    public String getVariableOriginType(){
        return variableOriginType;
    }

    public String getVariableCastType(){
        return variableCastType;
    }

    public String getQualifier(){
        return qualifier;
    }

    public String getFuctionName(){
        return fuctionName;
    }

    public Set<String> getArguments (){
        return arguments;
    }
    //endregion<getter>

    //region <setter>
    private void setValueType(ReturnValueType type){
        this.valueType = type;
    }

    public void setVariableName(String variableName){
        this.variableName = variableName;
    }

    public void setVariableOriginType(String type){
        this.variableOriginType = type;
    }

    public void setVariableCastType(String type){
        this.variableCastType = type;
    }

    public void setQualifier(String qualifier){
        this.qualifier = qualifier;
    }

    public void setFunctionName(String functionName){
        this.fuctionName = functionName;
    }

    public boolean addArgument(String argumentName){
        boolean result = false;
        if(!arguments.contains(argumentName)){
            arguments.add(argumentName);
            result = true;
        }
        return result;
    }
    //endregion <setter>
}
