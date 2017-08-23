package salientAPI.parser;

import salientAPI.codeGraph.Graph;
import salientAPI.parser.JATParser;

/**
 * Created by oliver on 2017/5/28.
 */
public class CodeParser {
    private JATParser codeParser;

    public CodeParser(){
        codeParser = new JATParser();
    }

    public void parseSegment(String segment){
        codeParser.parseCodeSegment(segment);
    }

    public void getSalientAPI(){
        codeParser.codeGraph.showGraph();
        codeParser.codeGraph.getSalientAPI();
    }
}
