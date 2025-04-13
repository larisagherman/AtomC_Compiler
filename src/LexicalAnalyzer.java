import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
public class LexicalAnalyzer {
    public static String content;
    public static int currentPosition=0;
    public static int line=1;
    public LexicalAnalyzer(String file) throws IOException {
        this.content= FileReader.readFile(file);
    }
    static class Token{
        public enum TokenType {
            ID,
            BREAK,CHAR,DOUBLE,ELSE,FOR,IF,INT,RETURN,STRUCT,VOID,WHILE,
            CT_INT, CT_REAL, CT_CHAR, CT_STRING,
            ADD, SUB, MUL, DIV, ASSIGN, EQUAL, NOTEQ, LESS, LESSEQ, GREATER, GREATEREQ,
            DOT,AND,NOT,OR,
            SEMICOLON, COMMA, LPAR, RPAR, LBRACKET, RBRACKET, LACC, RACC,
            LINECOMMENT,COMMENT,
            END

        }
        TokenType id;
        String text;
        int line;
        public Token(TokenType id,String text,int line){
            this.id=id;
            this.text=text;
            this.line=line;
        }
        @Override
        public String toString(){
            return this.id+" ";
        }
    }
    private static final int INITIAL_STATE = 0;
    private static final int IDENTIFIER_STATE = 1;
    private static final int NUMBER_STATE = 2;
    private static final int EQUAL_OR_ASSIGN_STATE = 4;
    private static final int DIGIT_NUMBER_STATE = 5;
    private static final int OCTA_OR_HEXA_STATE = 6;
    private static final int HEXA_STATE = 7;
    private static final int OCTAL_STATE= 8;
    private static final int EXPONENT_NUMBER_STATE = 9;
    private static final int EXPONENT_PART_STATE = 10;
    private static final int NOT_OR_NOTEQ_STATE = 11;
    private static final int OR_STATE = 12;
    private static final int LESS_OR_LESSEQ_STATE = 13;
    private static final int GREATER_OR_GREATERQ_STATE = 14;
    private static final int SINGLE_QUOTE_STATE = 15;
    private static final int DOUBLE_QUOTE_STATE = 16;
    private static final int SINGLE_QUOTE_END_STATE = 17;
    private static final int DOUBLE_QUOTE_END_STATE = 18;
    private static final int SINGLE_QUOTE_ESC_STATE = 19;
    private static final int DOUBLE_QUOTE_ESC_STATE = 20;
    private static final int DIV_OR_LINE_COMMENT_STATE = 21;
    private static final int LINE_COMMENT_TEXT_STATE = 22;
    private static final int LINE_COMMENT_ESC_STATE = 23;
    private static final int MULTILINE_COMMENT_STATE = 24;
    private static final int MULTILINE_COMMENT_END_STATE = 25;
    private static final int AND_STATE = 26;
    private static final int HEXA_STATE2 = 27;



    static List<Token> tokenize(){
        Set<Character> escapeChar=Set.of('a', 'b', 'f', 'n', 'r', 't', 'v', '\'', '?', '"', '\\', '0');

        int state=INITIAL_STATE;
        StringBuilder currentText=new StringBuilder();
        char currentChar;
        List<Token> tokens = new ArrayList<>();

        while(currentPosition<content.length()) {
            currentChar=content.charAt(currentPosition);
            switch (state) {
                case INITIAL_STATE:
                    if(Character.isLetter(currentChar)||currentChar=='_'){
                        state=IDENTIFIER_STATE;
                        currentText.append(currentChar);
                    }else if(currentChar=='0'){
                        state=OCTA_OR_HEXA_STATE;
                        currentText.append(currentChar);
                    }else if(Character.isDigit(currentChar)){
                        state=NUMBER_STATE;
                        currentText.append(currentChar);
                    } else if (currentChar==';') {
                        tokens.add(new Token(Token.TokenType.SEMICOLON,String.valueOf(currentChar),line));
                    } else if (currentChar==',') {
                        tokens.add(new Token(Token.TokenType.COMMA,String.valueOf(currentChar),line));
                    }else if (currentChar=='\n') {
                        line++;
                    } else if (currentChar=='=') {
                        currentText.append(currentChar);
                        state=EQUAL_OR_ASSIGN_STATE;
                    } else if (currentChar=='+') {
                        tokens.add(new Token(Token.TokenType.ADD,String.valueOf(currentChar),line));
                    }else if (currentChar=='-') {
                        tokens.add(new Token(Token.TokenType.SUB,String.valueOf(currentChar),line));
                    }else if (currentChar=='*') {
                        tokens.add(new Token(Token.TokenType.MUL,String.valueOf(currentChar),line));
                    }else if (currentChar=='/') {
                        currentText.append(currentChar);
                        state=DIV_OR_LINE_COMMENT_STATE;
                    }else if (currentChar=='&') {
                        currentText.append(currentChar);
                        state=AND_STATE;
                    }else if (currentChar=='!') {
                        currentText.append(currentChar);
                        state=NOT_OR_NOTEQ_STATE;
                    }else if (currentChar=='.') {
                        tokens.add(new Token(Token.TokenType.DOT,String.valueOf(currentChar),line));
                    }else if (currentChar=='(') {
                        tokens.add(new Token(Token.TokenType.LPAR,String.valueOf(currentChar),line));
                    }else if (currentChar==')') {
                        tokens.add(new Token(Token.TokenType.RPAR,String.valueOf(currentChar),line));
                    }else if (currentChar=='[') {
                        tokens.add(new Token(Token.TokenType.LBRACKET,String.valueOf(currentChar),line));
                    }else if (currentChar==']') {
                        tokens.add(new Token(Token.TokenType.RBRACKET,String.valueOf(currentChar),line));
                    }else if (currentChar=='{') {
                        tokens.add(new Token(Token.TokenType.LACC,String.valueOf(currentChar),line));
                    }else if (currentChar=='}') {
                        tokens.add(new Token(Token.TokenType.RACC,String.valueOf(currentChar),line));
                    }else if (currentChar=='|') {
                        currentText.append(currentChar);
                        state=OR_STATE;
                    }else if (currentChar=='<') {
                        currentText.append(currentChar);
                        state=LESS_OR_LESSEQ_STATE;
                    }else if (currentChar=='>') {
                        currentText.append(currentChar);
                        state=GREATER_OR_GREATERQ_STATE;
                    }else if (currentChar=='\'') {
                        currentText.append(currentChar);
                        state=SINGLE_QUOTE_STATE;
                    }else if (currentChar=='\"') {
                        currentText.append(currentChar);
                        state=DOUBLE_QUOTE_STATE;
                    }
                    break;
                case IDENTIFIER_STATE:
                    if(Character.isLetterOrDigit(currentChar)||currentChar=='_'){
                        currentText.append(currentChar);
                    }else{

                        state=INITIAL_STATE;
                        String currentTextAsString=currentText.toString();
                        if(currentTextAsString.equals("break")){
                            tokens.add(new Token(Token.TokenType.BREAK,currentText.toString(),line));
                        } else if (currentTextAsString.equals("char")) {
                            tokens.add(new Token(Token.TokenType.CHAR,currentText.toString(),line));
                        } else if (currentTextAsString.equals("double")) {
                            tokens.add(new Token(Token.TokenType.DOUBLE,currentText.toString(),line));
                        }else if (currentTextAsString.equals("else")) {
                            tokens.add(new Token(Token.TokenType.ELSE,currentText.toString(),line));
                        }else if (currentTextAsString.equals("for")) {
                            tokens.add(new Token(Token.TokenType.FOR,currentText.toString(),line));
                        }else if (currentTextAsString.equals("if")) {
                            tokens.add(new Token(Token.TokenType.IF,currentText.toString(),line));
                        }else if (currentTextAsString.equals("int")) {
                            tokens.add(new Token(Token.TokenType.INT,currentText.toString(),line));
                        }else if (currentTextAsString.equals("return")) {
                            tokens.add(new Token(Token.TokenType.RETURN,currentText.toString(),line));
                        }else if (currentTextAsString.equals("struct")) {
                            tokens.add(new Token(Token.TokenType.STRUCT,currentText.toString(),line));
                        }else if (currentTextAsString.equals("while")) {
                            tokens.add(new Token(Token.TokenType.WHILE,currentText.toString(),line));
                        }else if (currentTextAsString.equals("void")) {
                            tokens.add(new Token(Token.TokenType.VOID,currentText.toString(),line));
                        }else {
                            tokens.add(new Token(Token.TokenType.ID, currentText.toString(), line));
                        }
                        currentText = new StringBuilder();
                        currentPosition--;
                    }

                    break;
                case NUMBER_STATE:
                    if(Character.isDigit(currentChar)){
                        currentText.append(currentChar);
                    } else if (currentChar=='.') {
                        state=DIGIT_NUMBER_STATE;
                        currentText.append(currentChar);
                    } else if (currentChar=='e'||currentChar=='E') {
                        currentText.append(currentChar);
                        state=EXPONENT_PART_STATE;
                    } else{
                        state=INITIAL_STATE;
                        tokens.add(new Token(Token.TokenType.CT_INT,currentText.toString(),line));
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case OCTA_OR_HEXA_STATE:
                    if(currentChar=='x'||currentChar=='X'){
                        currentText.append(currentChar);
                        state=HEXA_STATE;
                    }else if(currentChar>='0' &&currentChar<='7'){
                        currentText.append(currentChar);
                        state=OCTAL_STATE;
                    } else if (currentChar=='.') {
                        currentText.append(currentChar);
                        state=DIGIT_NUMBER_STATE;
                    } else{
                        tokens.add(new Token(Token.TokenType.CT_INT,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case HEXA_STATE:
                    if((currentChar>='A'&&currentChar<='F')||(currentChar>='a'&&currentChar<='f')||Character.isDigit(currentChar)){
                        currentText.append(currentChar);
                        state=HEXA_STATE2;
                    } else if ( currentChar>'f') {
                        throw new RuntimeException("!!!Invalid hexa digit at line " + line+"!!!");
                    }else {
                        throw new RuntimeException("!!!Invalid hexa digit at line " + line+"!!!");
                    }
                    break;
                case HEXA_STATE2:
                    if((currentChar>='A'&&currentChar<='F')||(currentChar>='a'&&currentChar<='f')||Character.isDigit(currentChar)){
                        currentText.append(currentChar);
                    } else if ( currentChar>'f') {
                        throw new RuntimeException("!!!Invalid hexa digit at line " + line+"!!!");
                    }else {
                        tokens.add(new Token(Token.TokenType.CT_INT,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case OCTAL_STATE:
                    if(currentChar>='0' && currentChar<='7'){
                        currentText.append(currentChar);
                    } else if (Character.isDigit(currentChar)) {
                        throw new RuntimeException("!!!Invalid octal digit at line " + line+"!!!");
                    }else {
                        tokens.add(new Token(Token.TokenType.CT_INT,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case DIGIT_NUMBER_STATE:
                    if(Character.isDigit(currentChar)){
                        currentText.append(currentChar);
                    }else if (currentChar=='e'||currentChar=='E'){
                        state=EXPONENT_PART_STATE;
                        currentText.append(currentChar);
                    }else{
                        tokens.add(new Token(Token.TokenType.CT_REAL,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case EXPONENT_PART_STATE:
                    if(Character.isDigit(currentChar)){
                        currentText.append(currentChar);
                    } else if (currentChar=='+'||currentChar=='-') {
                        currentText.append(currentChar);
                        state=EXPONENT_NUMBER_STATE;
                    }else{
                        tokens.add(new Token(Token.TokenType.CT_REAL,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case EXPONENT_NUMBER_STATE:
                    if(Character.isDigit(currentChar)){
                        currentText.append(currentChar);
                    }else{
                        tokens.add(new Token(Token.TokenType.CT_REAL,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case EQUAL_OR_ASSIGN_STATE:
                    if(currentChar=='='){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.EQUAL,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }else{
                        tokens.add(new Token(Token.TokenType.ASSIGN,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case NOT_OR_NOTEQ_STATE:
                    if(currentChar=='='){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.NOTEQ,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }else {
                        tokens.add(new Token(Token.TokenType.NOT,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case OR_STATE:
                    if (currentChar=='|'){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.OR,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }else {
                        throw new RuntimeException("!!!Invalid character at line "+line+"!!!");
                    }
                    break;
                case AND_STATE:
                    if(currentChar=='&'){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.AND,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    } else{
                        throw new RuntimeException("!!!Invalid AND sign format at line "+line+" !!!");
                    }
                    break;
                case LESS_OR_LESSEQ_STATE:
                    if(currentChar=='='){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.LESSEQ,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }else {
                        tokens.add(new Token(Token.TokenType.LESS,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case GREATER_OR_GREATERQ_STATE:
                    if(currentChar=='='){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.GREATEREQ,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }else {
                        tokens.add(new Token(Token.TokenType.GREATER,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case SINGLE_QUOTE_STATE:
                    if(Character.isLetterOrDigit(currentChar) ||!Character.isLetterOrDigit(currentChar) ){
                        currentText.append(currentChar);
                        state=SINGLE_QUOTE_END_STATE;
                    }else if (currentChar=='\\'){
                        currentText.append(currentChar);
                        state=SINGLE_QUOTE_ESC_STATE;
                    }
                    break;
                case SINGLE_QUOTE_END_STATE:
                    if(currentChar=='\''){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.CT_CHAR,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }
                    else{
                        throw new RuntimeException("!!!Invalid char at line "+line+" !!!");
                    }
                    break;
                case SINGLE_QUOTE_ESC_STATE:
                    if(escapeChar.contains(currentChar)){
                        currentText.append(currentChar);
                        state=SINGLE_QUOTE_END_STATE;
                    }else {
                        throw new RuntimeException("!!!Invalid escape char format at line "+line+" !!!");
                    }
                    break;
                case DOUBLE_QUOTE_STATE:
                    if(Character.isLetterOrDigit(currentChar)){
                        currentText.append(currentChar);
                    }else if (currentChar=='\\'){
                        currentText.append(currentChar);
                        state=DOUBLE_QUOTE_ESC_STATE;
                    }else if (currentChar=='\"'){
                        currentText.append(currentChar);
                        tokens.add(new Token(Token.TokenType.CT_STRING,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }
                    break;
                case DOUBLE_QUOTE_ESC_STATE:
                    if(escapeChar.contains(currentChar)){
                        currentText.append(currentChar);
                        state=DOUBLE_QUOTE_STATE;
                    }else{
                        throw new RuntimeException("!!!Invalid escape char format at line "+line+" !!!");
                    }
                    break;
                case DIV_OR_LINE_COMMENT_STATE:
                    if (currentChar=='/'){
                        currentText.append(currentChar);
                        state=LINE_COMMENT_TEXT_STATE;
                    } else if (currentChar=='*') {
                        currentText.append(currentChar);
                        state=MULTILINE_COMMENT_STATE;
                    } else {
                        tokens.add(new Token(Token.TokenType.DIV,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                        currentPosition--;
                    }
                    break;
                case LINE_COMMENT_TEXT_STATE:
                    if (Character.isLetterOrDigit(currentChar)){
                        currentText.append(currentChar);
                    }else if (currentChar=='\\'){
                        state=LINE_COMMENT_ESC_STATE;
                    } else if (currentChar=='\n'){
//                      tokens.add(new Token(Token.TokenType.LINECOMMENT,currentText.toString(),line));
                      state=INITIAL_STATE;
                      currentText=new StringBuilder();
                      currentPosition--;
                    }
                    break;
                case LINE_COMMENT_ESC_STATE:
                    if(currentChar=='n'|| currentChar=='r'||currentChar=='0'){
                        throw new RuntimeException("!!!Invalid line comment format!!!");
                    }
                    break;
                case MULTILINE_COMMENT_STATE:
                    if(Character.isLetterOrDigit(currentChar)){
                        currentText.append(currentChar);
                    }else if (currentChar=='*'){
                        currentText.append(currentChar);
                        state=MULTILINE_COMMENT_END_STATE;
                    } else if (currentChar=='\n') {
                        line++;
                    }
                    break;
                case MULTILINE_COMMENT_END_STATE:
                    if (currentChar=='/'){
                        currentText.append(currentChar);
//                        tokens.add(new Token(Token.TokenType.COMMENT,currentText.toString(),line));
                        state=INITIAL_STATE;
                        currentText=new StringBuilder();
                    }else {
                        currentText.append(currentChar);
                        state=MULTILINE_COMMENT_STATE;
                    }
                    break;
            }

            currentPosition++;
        }
        if(state!=INITIAL_STATE){
            if(state==DOUBLE_QUOTE_STATE||state==DOUBLE_QUOTE_END_STATE||state==DOUBLE_QUOTE_ESC_STATE){
                throw new RuntimeException("!!!Invalid string format!!!");
            }
        }
        if(currentPosition>=content.length()){
            line++;
            tokens.add(new LexicalAnalyzer.Token(LexicalAnalyzer.Token.TokenType.END,"END",line));

        }
        return tokens;
    }

    static void print(List<LexicalAnalyzer.Token> text){
        int prevLine=-1;
        for(int i=0;i< text.size();i++){
            if(text.get(i).line!=prevLine){
                prevLine=text.get(i).line;
                System.out.println();
            }
            System.out.print(text.get(i).toString()+" ");
        }
        System.out.println();
    }

}
