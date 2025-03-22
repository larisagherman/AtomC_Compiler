import java.util.List;

public class SyntacticalAnalyzer {
    private List<LexicalAnalyzer.Token> tokens;
    private int currentIndex=0;
    private LexicalAnalyzer.Token currentToken;

    public SyntacticalAnalyzer(List<LexicalAnalyzer.Token> tokens){
        this.tokens=tokens;
    }
    private boolean consume(LexicalAnalyzer.Token.TokenType expected){
        if(currentIndex<tokens.size()){
            currentToken=tokens.get(currentIndex);
            if(currentToken.id==expected){
                currentIndex++;
                return true;
            }
        }
        return false;
    }
    public boolean unit(){
        while(currentIndex<tokens.size()){
            if(declStruct()){
                return true;
            }else {
                break;
            }
        }
        return false;
    }
    public boolean declStruct(){
        if(consume(LexicalAnalyzer.Token.TokenType.STRUCT)){
            if (consume(LexicalAnalyzer.Token.TokenType.ID)){
                if(consume(LexicalAnalyzer.Token.TokenType.LACC)){
                    while(declVar());
                    if(consume(LexicalAnalyzer.Token.TokenType.RACC)){
                        if(consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)){
                            return true;
                        }else {
                            System.out.println("Error: Missing ';' after struct definition.");
                            return false;
                        }
                    }else {
                        System.out.println("Error: Missing '}' after struct definition.");
                        return false;
                    }
                }else {
                    System.out.println("Error: Missing '{' after struct definition.");
                    return false;
                }
            }else {
                System.out.println("Error: Missing struct name after 'struct'.");
                return false;
            }
        }else {
            System.out.println("Error: Missing 'struct' keyword.");
            return false;
        }
    }
    private boolean declVar(){
        if (!typeBase()) {
            System.out.println("Error: Missing type for variable.");
            return false;
        }
        if(!consume(LexicalAnalyzer.Token.TokenType.ID)) {
            System.out.println("Error: Missing identifier after type.");
            return false;
        }
//        arrayDecl();
        while (consume(LexicalAnalyzer.Token.TokenType.COMMA)){
            if(!consume(LexicalAnalyzer.Token.TokenType.ID)) {
                System.out.println("Error: Missing identifier after type.");
                return false;
            }
//            arrayDecl();
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
            System.out.println("Error: Missing ';' .");
            return false;
        }
        return true;
    }
    private boolean typeBase(){
        if(consume(LexicalAnalyzer.Token.TokenType.INT)||consume(LexicalAnalyzer.Token.TokenType.DOUBLE)||consume(LexicalAnalyzer.Token.TokenType.CHAR)||consume(LexicalAnalyzer.Token.TokenType.STRUCT)){
            return true;
        }
        return false;
    }
    private boolean arrayDecl(){
        if(!consume(LexicalAnalyzer.Token.TokenType.LBRACKET)){
            System.out.println("Error: Missing '[' .");
            return false;
        }
//        expr();
        if(!consume(LexicalAnalyzer.Token.TokenType.RBRACKET)){
            System.out.println("Error: Missing ']' .");
            return false;
        }
        return true;
    }
    private boolean expr(){
        if(!exprAssign()){
            System.out.println("Error: expr() .");
            return false;
        }
        return true;
    }
    private boolean exprAssign(){
        if(!exprUnary()){
            System.out.println("Error: Wrong exprAssign() fromat.");
            return false;
        }
        return true;
    }
    private boolean exprUnary(){
        if(!consume(LexicalAnalyzer.Token.TokenType.SUB)||!consume(LexicalAnalyzer.Token.TokenType.NOT)){
            System.out.println("Error: No '-' or '!' .");
            return false;
        }
        if(!exprUnary()|| !exprPostfix()){
            System.out.println("Error: Wrong exprUnary()||exprPostfix()  expression.");
            return false;
        }
        return true;
    }
    private boolean exprPostfix1(){
        if(!exprPostfix()){
            System.out.println("Error: Wrong exprUnary() expression.");
            return false;
        }
        if(!consume(LexicalAnalyzer.Token.TokenType.LBRACKET)){
            System.out.println("Error: No '{' present.");
            return false;
        }
        if(!expr()){
            System.out.println("Error: Wrong expr() expression.");
            return false;
        }
        if(!consume(LexicalAnalyzer.Token.TokenType.RBRACKET)){
            System.out.println("Error: No '}' present.");
            return false;
        }
        return true;
    }
    private boolean exprPostfix2(){
        if(!exprPostfix()){
            System.out.println("Error: invalid expr() format.");
            return false;
        }
        if(!consume(LexicalAnalyzer.Token.TokenType.DOT)){
            System.out.println("Error: No '.' .");
            return false;
        }
        if(!consume(LexicalAnalyzer.Token.TokenType.ID)){
            System.out.println("Error: No id .");
            return false;
        }
        return true;
    }
    private boolean exprPostfix(){
        if(!exprPostfix1()||!exprPostfix2()||exprPrimary()){
            System.out.println("Error: invalid expr() format.");
            return false;
        }
        return true;
    }
    private




}
