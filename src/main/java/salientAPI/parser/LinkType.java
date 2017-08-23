package salientAPI.parser;

/**
 * Created by oliver on 2017/5/31.
 */
public enum LinkType {
    NULL, // two variables has no relation
    ARGUEMENT_OF_FUNCTION, //if variable v1 has been a argument of the function of variable v2, like v2.funciton(v1) , the v1 and v2 has this relation
    ARGUEMENT_OF_INITIALIZOR   //if a variable v1 is initialized by a function of which variable v2 is a argument , then we say v1 and v2 has this relation , like  v1 = function(v2)
}
