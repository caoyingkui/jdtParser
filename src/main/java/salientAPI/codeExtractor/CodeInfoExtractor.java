package salientAPI.codeExtractor;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * Created by oliver on 2017/8/21.
 */
public class CodeInfoExtractor {
    public String getNameOfType (Type type){
        if(type == null)
            return "";
        String typeName ="";
        if(type instanceof AnnotatableType){
            if(type instanceof PrimitiveType){
                typeName = ((PrimitiveType) type).getPrimitiveTypeCode().toString();
            }else if(type instanceof SimpleType){
                typeName = getName(((SimpleType) type).getName());
            }else if(type instanceof QualifiedType){
                Type qualifier = ((QualifiedType) type).getQualifier();
                Name lastName = ((QualifiedType) type).getName();
                typeName = getNameOfType(qualifier) + "." + getName(lastName);
            }else if(type instanceof NameQualifiedType){
                Name qualifier = ((NameQualifiedType) type).getQualifier();
                Name lastName = ((NameQualifiedType) type).getName();
                typeName = getName(qualifier) + "." + getName(lastName);
            }else if(type instanceof WildcardType){
                System.out.println("i do not know how to deal with WildcardType");
            }else{
                System.out.println("there are other types");
            }
        }else if(type instanceof ArrayType){
            Type temp = ((ArrayType) type).getElementType();
            String tempName = getNameOfType(temp);
            typeName = tempName + "[]";
        }else if(type instanceof ParameterizedType){ //  Type < Type { , Type } >
            Type typeHead = ((ParameterizedType) type).getType();
            typeName = getNameOfType(typeHead) + " <";
            List<Type> arguments = ((ParameterizedType) type).typeArguments();
            for(Type argument : arguments){
                typeName += " ";
                String argumentName = getNameOfType(argument);
                typeName += " ,";
            }
            typeName = typeName.substring(0 , typeName.length() - 1) ; // eliminate the last comma
            typeName += ">";
        }else if(type instanceof UnionType){
            System.out.println("I do not think there is need to deal with UnionType");
        }else if(type instanceof IntersectionType){
            System.out.println("I do not think there is need to deal with IntersectionType");
        }else{
            System.out.println("there are other types");
        }
        return typeName;
    }

    public String getName(Name nameNode){
        String name ="";
        if(nameNode instanceof SimpleName){
            name = ((SimpleName)nameNode).getIdentifier();
        }else if(nameNode instanceof QualifiedName){
            nameNode = (QualifiedName)nameNode;
            name = getName(((QualifiedName) nameNode).getQualifier()) + "." + getName(((QualifiedName) nameNode).getName());
        }
        return name;
    }
}
