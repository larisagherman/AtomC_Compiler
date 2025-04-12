//currentToken- este pentru consume
//tokens.get(currentIndex) e ce avem in lista

import java.util.List;

public class SyntacticalAnalyzer {
    private List<LexicalAnalyzer.Token> tokens;
    private int currentIndex = 0;
    private LexicalAnalyzer.Token currentToken;

    public SyntacticalAnalyzer(List<LexicalAnalyzer.Token> tokens) {
        this.tokens = tokens;
    }

    private boolean consume(LexicalAnalyzer.Token.TokenType expected) {
        if (currentIndex < tokens.size()) {
            this.currentToken = tokens.get(currentIndex);
            if (currentToken.id == expected) {
                currentIndex++;
                return true;
            }
        }
        return false;
    }

    public boolean unit2() {
        while (currentIndex < tokens.size()) {
            if (declStruct()) {
                return true;
            } else if (declFunc()) {
                return true;
            } else if (declVar()) {
                return true;
            } else {
                break;
            }
        }
        return false;
    }

    public boolean unit() {
        while (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.END) {
            int startIndex = currentIndex;  // Save index for backtracking
            System.out.println("START INDEX " + currentIndex);

            if (declStruct()) {
                continue;
            } else {
                currentIndex = startIndex;  // backtrack
            }
            if (declFunc()) {
                continue;
            } else {
                currentIndex = startIndex;  // backtrack

            }

            if (declVar()) {
                continue;
            }

            // If none matched, and nothing was consumed, break
            if (currentIndex == startIndex) {
                System.out.println("Something is wrong.\t The file wasn't read all the way");
//                System.out.println(currentIndex);
                return false;
            }
        }

//        // Check for END token at the end
//        if (!consume(LexicalAnalyzer.Token.TokenType.END)) {
//            System.out.println("Error: Expected 'END' at the end of unit.");
//            return false;
//        }

        return true;
    }

    int startIndex;

    public boolean declStruct() {
        startIndex = currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.STRUCT)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                if (consume(LexicalAnalyzer.Token.TokenType.LACC)) {
                    if (tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RACC) {
                        while (declVar()) ;
                    }
                    if (consume(LexicalAnalyzer.Token.TokenType.RACC)) {
                        if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                            return true;
                        } else {
                            System.out.println("Error: Missing ';' after struct definition at line " + tokens.get(currentIndex).line);
                        }
                    } else {
                        System.out.println("Error: Missing '}' after struct definition at line " + tokens.get(currentIndex).line);
                    }
                } else {
                    System.out.println("Error: Missing '{' after struct definition at line " + tokens.get(currentIndex).line);
                }
            } else {
                System.out.println("Error: Missing struct name after 'struct' at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean declVar() {
        startIndex = currentIndex;
        if (typeBase()) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if it's an arrayDecl
                    arrayDecl();
                }
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.COMMA) {
                    while (consume(LexicalAnalyzer.Token.TokenType.COMMA)) {
                        if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                            if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if it's an arrayDecl
                                arrayDecl();
                            }
                        } else {
                            System.out.println("Error: Missing identifier after type at line " + tokens.get(currentIndex).line);
                        }
                    }
                }
                if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                    return true;
                } else {
                    System.out.println("Error: Missing ';' at line " + tokens.get(currentIndex).line);
                }
            } else {
                System.out.println("Error: Missing identifier after type at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean arrayDecl() {
        startIndex = currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.LBRACKET)) {
            // Only call expr() if there's something inside the brackets
            if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RBRACKET) {
                if (!expr()) {
                    System.out.println("Error (from arrayDecl): with expr() in array declaration at line " + tokens.get(currentIndex).line);
                }
            }
            if (consume(LexicalAnalyzer.Token.TokenType.RBRACKET)) {
                return true;
            } else {
                System.out.println("Error: Missing ']' in array declaration at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean declFunc() {
        startIndex = currentIndex;
        boolean verifyTypeBase = typeBase();
        if (consume(LexicalAnalyzer.Token.TokenType.VOID) || verifyTypeBase == true) {
            if (currentIndex < tokens.size() &&
                    tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.MUL &&
                    verifyTypeBase == true) {
                consume(LexicalAnalyzer.Token.TokenType.MUL);
            }
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
                    if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RPAR) {//if we have arguments
                        if (funcArg()) {
                            if (tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.COMMA) {
                                while (consume(LexicalAnalyzer.Token.TokenType.COMMA)) {
                                    if (!funcArg()) {
                                        System.out.println("Error: Missing argument after ',' at line " + tokens.get(currentIndex).line);
                                    }
                                }
                            }
                        } else {
                            System.out.println("Error: Invalid function argument at line " + tokens.get(currentIndex).line);
                        }
                    }
                    if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                        if (stmCompound()) {
                            return true;
                        } else {
                            System.out.println("Error: Invalid function body at line " + tokens.get(currentIndex).line);
                        }
                    } else {
                        System.out.println("Error: Missing ')' in function declaration at line " + tokens.get(currentIndex).line);
                    }
                } else {
                    System.out.println("Error: Missing '('at line " + tokens.get(currentIndex).line);
                }
            } else {
                System.out.println("Error: Missing ID after typeBase at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean stmCompound() {
        startIndex = currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.LACC)) {
            if (currentIndex < tokens.size() &&
                    currentToken.id != LexicalAnalyzer.Token.TokenType.RACC) {//If there is something inside the function body
                while (currentIndex < tokens.size() &&
                        tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RACC) {
                    if (!declVar()) {
                        if (!stm()) {
                            System.out.println("Error: with stm() at line " + tokens.get(currentIndex).line);
                            return false;
                        }
                    }
                }
            }
            if (consume(LexicalAnalyzer.Token.TokenType.RACC)) {
                return true;
            } else {
                System.out.println("Error: Missing '}' at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean stm() {
        startIndex = currentIndex;
        if (stmCompound()) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.IF)) {
            if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
                if (expr()) {
                    if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                        if (stm()) {
                            if (currentIndex < tokens.size() &&
                                    tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.ELSE) {
                                if (consume(LexicalAnalyzer.Token.TokenType.ELSE)) {
                                    if (!stm()) {
                                        System.out.println("Error: Missing '{ }'. at line " + tokens.get(currentIndex).line);
                                        return false;
                                    }
                                }
                            }
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } else if (consume(LexicalAnalyzer.Token.TokenType.WHILE)) {
            if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
                if (expr()) {
                    if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                        if (stm()) {
                            return true;
                        } else {
                            System.out.println("Error: with the stm() at line " + tokens.get(currentIndex).line);
                        }
                    } else {
                        System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
                    }
                }
            } else {
                System.out.println("Error: Missing '(' at line " + tokens.get(currentIndex).line);
            }
        } else if (consume(LexicalAnalyzer.Token.TokenType.FOR)) {
            if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
                if (currentIndex < tokens.size() &&
                        tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.SEMICOLON) {
                    expr();
                    System.out.println(tokens.get(currentIndex).id);

                }
                if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                    if (currentIndex < tokens.size() &&
                            tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.SEMICOLON) {
                        expr();
                        System.out.println(tokens.get(currentIndex).id);

                    }
                    if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                        if (currentIndex < tokens.size() &&
                                tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RPAR) {
                            expr();
                            System.out.println(tokens.get(currentIndex).id);

                        }
                        if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                            System.out.println("TRUEEE "+tokens.get(currentIndex).id);

                            if (stm()) {
                                System.out.println("TRUEEE "+tokens.get(currentIndex).id);

                                return true;
                            }
                        } else {
                            System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
                        }
                    } else {
                        System.out.println("Error 2: Missing ';' at line " + tokens.get(currentIndex).line);
                    }
                } else {
                    System.out.println(tokens.get(currentIndex).id);
                    System.out.println("Error 1: Missing ';' at line " + tokens.get(currentIndex).line);
                }
            } else {
                System.out.println("Error: Missing '(' at line " + tokens.get(currentIndex).line);
            }
        } else if (consume(LexicalAnalyzer.Token.TokenType.BREAK)) {
            if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                return true;
            } else {
                System.out.println("Error: Missing ';' at line " + tokens.get(currentIndex).line);
            }
        } else if (consume(LexicalAnalyzer.Token.TokenType.RETURN)) {
            if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.SEMICOLON) {
                expr();
            }
            if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                return true;
            } else {
                System.out.println("Error: Missing ';' at line " + tokens.get(currentIndex).line);
            }
        } else if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.SEMICOLON) {
            if (expr()) {
                if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                    return true;
                } else {
                    System.out.println("Error: Missing ';' at line " + tokens.get(currentIndex).line);
                }
            }
        }else{
            exprPrimary();
        }
        currentIndex = startIndex;
        return false;
    }


    private boolean funcArg() {
        startIndex=currentIndex;
        if (typeBase()) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if we have arguments
                    arrayDecl();
                }
                return true;
            }else{
                System.out.println("Error: Missing ID after typeBase at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex=startIndex;
        return false;
    }

    public boolean typeBase() {
        startIndex=currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.INT) || consume(LexicalAnalyzer.Token.TokenType.DOUBLE) || consume(LexicalAnalyzer.Token.TokenType.CHAR)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.STRUCT)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                return true;
            }else {
                System.out.println("Error: Missing ID after struct at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex=startIndex;
        return false;
    }


    public boolean expr() {
        return exprAssign();
    }

    public boolean exprAssignn() {
        startIndex = currentIndex;
        if (exprAssign1()) {
            return true;
        }
        currentIndex = startIndex;
//        System.out.println(tokens.get(currentIndex).id);
        if (exprOr()) {
            return true;
        }
        currentIndex = startIndex;
        return false;
    }
    public boolean exprAssign() {
        startIndex = currentIndex;
        if (exprUnary()&&consume(LexicalAnalyzer.Token.TokenType.ASSIGN)) {
            //left term is there now verify right term
            if(exprAssign()){
                return true;
            }else{
                currentIndex=startIndex;
                return false;
            }
        }else{
            currentIndex=startIndex;
            return exprOr();
        }
    }


    public boolean exprAssign1() {
        if (!exprUnary()) {
            return false;
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.ASSIGN)) {
//            System.out.println(tokens.get(currentIndex).id);
//            System.out.println("Error: Missing '=' at line " + tokens.get(currentIndex).line);
            return false;
        }

        exprAssign();
        return true;
    }


    public boolean exprOr() {
        if (exprAnd()) {
            return true;
        } else if (exprOr1()) {
//            System.out.println("Error: Invalid exprOr() at line " + tokens.get(currentIndex).line);
            return true;
        }
        return false;
    }

    boolean error;

    public boolean exprOr1() {
        startIndex = currentIndex;
        error = false;
        if (consume(LexicalAnalyzer.Token.TokenType.OR)) {
            if (!exprAnd()) {
                System.out.println("Error: Invalid exprAnd() at line " + tokens.get(currentIndex).line);
                error = true;
            }
            exprOr1();
        } else {
            return false;
        }
        if (error) currentIndex = startIndex;

        return true;
    }

    public boolean exprAnd() {

        if (exprEq()) {
            return true;
        } else if (exprAnd1()) {
//            System.out.println("Error: Invalid exprAnd1() at line " + tokens.get(currentIndex).line);
            return true;
        }
        return false;
    }

    public boolean exprAnd1() {
        startIndex = currentIndex;
        error = false;
        if (consume(LexicalAnalyzer.Token.TokenType.AND)) {
            if (!exprEq()) {
                System.out.println("Error: Invalid exprEq() at line " + tokens.get(currentIndex).line);
                error = true;
            }
            exprAnd1();
        } else {
            return false;
        }
        if (error) currentIndex = startIndex;

        return true;
    }

    public boolean exprEq() {

        if (exprRel()) {
            return true;
        } else if (exprEq1()) {
//            System.out.println("Error: Invalid exprEq1() at line " + tokens.get(currentIndex).line);
            return true;
        }
        return false;
    }

    public boolean exprEq1() {
        startIndex = currentIndex;
        error = false;
        if (consume(LexicalAnalyzer.Token.TokenType.EQUAL) || consume(LexicalAnalyzer.Token.TokenType.NOTEQ)) {
            if (!exprRel()) {
                System.out.println("Error: Invalid exprRel() at line " + tokens.get(currentIndex).line);
                error = true;
            }
            exprEq1();
        } else {
            return false;
        }
        if (error) currentIndex = startIndex;

        return true;
    }

    public boolean exprRel() {

        if (exprAdd()) {
            return true;
        } else if (exprRel1()) {
//            System.out.println("Error: Invalid exprAdd() at line " + tokens.get(currentIndex).line);
            return true;
        }
        return false;
    }

    public boolean exprRel1() {
        startIndex = currentIndex;

        if (consume(LexicalAnalyzer.Token.TokenType.LESS) || consume(LexicalAnalyzer.Token.TokenType.LESSEQ) || consume(LexicalAnalyzer.Token.TokenType.GREATER) || consume(LexicalAnalyzer.Token.TokenType.GREATEREQ)) {
            if (!exprAdd()) {
                System.out.println("Error: Invalid exprAdd() at line " + tokens.get(currentIndex).line);
                error = true;
            }
            exprRel1();

        } else {
            return false;
        }
        if (error) currentIndex = startIndex;
        return true;
    }

    public boolean exprAdd() {
        if (exprMul()) {
            return true;
        } else if (exprAdd1()) {
//            System.out.println("Error: Invalid exprAdd1() at line " + tokens.get(currentIndex).line);
            return true;
        }
        return false;
    }

    public boolean exprAdd1() {
        startIndex = currentIndex;
        error = false;
        if (consume(LexicalAnalyzer.Token.TokenType.ADD) || consume(LexicalAnalyzer.Token.TokenType.SUB)) {
            if (!exprMul()) {
                System.out.println("Error: Invalid exprMul() at line " + tokens.get(currentIndex).line);
                error = true;
            }
            exprAdd1();
        } else {
            return false;
        }
        if (error) currentIndex = startIndex;

        return true;
    }

    public boolean exprMul() {
        if (exprCast()) {
            return true;
        } else if (exprMul1()) {
//            System.out.println("Error: Invalid exprMul1() at line " + tokens.get(currentIndex).line);
            return true;
        }
        return false;
    }

    private boolean exprMul1() {
        startIndex = currentIndex;
        error = false;
        if (consume(LexicalAnalyzer.Token.TokenType.MUL) || consume(LexicalAnalyzer.Token.TokenType.DIV)) {
            if (!exprCast()) {
                System.out.println("Error: Invalid exprCast() at line " + tokens.get(currentIndex).line);
                error = true;
            }
            if (!exprMul1()) {
                System.out.println("Error: Invalid exprMul1() at line " + tokens.get(currentIndex).line);
                error = true;
            }
        }
        if (error || currentIndex == startIndex) {
            currentIndex = startIndex;
            return false;
        }
        return true;
    }

    public boolean exprCast() {
        startIndex = currentIndex;
        if (exprCast1()) {
            return true;
        }
        if (exprUnary()) {
            return true;
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean exprCast1() {

        if (!consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
            return false;
        }
        if (!typeName()) {
            System.out.println("Error: Invalid/No typeName() at line " + tokens.get(currentIndex).line);
            return false;
        }
        if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
            System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
            return false;
        }
        if (!exprCast()) {
            System.out.println("Error: exprCast() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    public boolean typeName() {
        startIndex = currentIndex;

        if (typeBase()) {
            if (consume(LexicalAnalyzer.Token.TokenType.LBRACKET)) {//daca  e declarare de array
                currentIndex--;
                if (!arrayDecl()) {
                    System.out.println("Error: at arrayDecl() .");
                } else {
                    return true;
                }
            } else {//daca nu e declarare de array
                return true;
            }
        }
        return false;
    }

    public boolean exprUnary() {
        startIndex = currentIndex;
        if (exprUnary1()) {
            return true;
        }
        currentIndex = startIndex;
        if (exprPostfix()) {
            return true;
        }

        currentIndex = startIndex;
        return false;
    }

    public boolean exprUnary1() {
        if (consume(LexicalAnalyzer.Token.TokenType.SUB) || consume(LexicalAnalyzer.Token.TokenType.NOT)) {
            if (!exprUnary()) {
                System.out.println("Error: Invalid exprUnary() expression at line " + tokens.get(currentIndex).line);
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean exprPostfix() {
        if (!exprPrimary()) {
            return false;
        }
        if (!exprPostfix1()) {
            return false;
        }
        return true;
    }

    public boolean exprPostfix1() {
        startIndex = currentIndex;
        if (exprPostfix1_1()) {
            return true;
        }
        currentIndex = startIndex;
        if (exprPostfix1_2()) {
            return true;
        }
        currentIndex = startIndex;
        return true;
    }

    private boolean exprPostfix1_1() {
        if (!consume(LexicalAnalyzer.Token.TokenType.LBRACKET)) {
            return false;
        }
        if (expr()) {
            return false;
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.RBRACKET)) {
            System.out.println("Error: Missing ']' at line " + tokens.get(currentIndex).line);
            return false;
        }

        if (!exprPostfix1()) {
            System.out.println("Error: Invalid exprPostfix1() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    private boolean exprPostfix1_2() {

        if (!consume(LexicalAnalyzer.Token.TokenType.DOT)) {
            return false;
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.ID)) {
            System.out.println("Error: Missing ID at line " + tokens.get(currentIndex).line);
            return false;
        }
        if (!exprPostfix1()) {
            System.out.println("Error: Invalid exprPostfix1() at line " + tokens.get(currentIndex).line);
            return false;
        }

        return true;
    }

    public boolean exprPrimary() {
        startIndex=currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
            if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LPAR) {//we check if there is a LPAR present
                if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
                    if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RPAR) {//we check if there is something inside ( )
                        if (expr()) {
                            if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.COMMA) {//we check if there is something after expr(), eg. max(x ,____)
                                while (consume(LexicalAnalyzer.Token.TokenType.COMMA)) {
                                    expr();
                                }
                            }
                        }
                    }
                    if (!consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                        System.out.println("Error: No ')' present at line " + tokens.get(currentIndex).line);
                    } else {
                        System.out.println(tokens.get(currentIndex).id);
                        return true;
                    }
                }
            }
            return true;
        }
        currentIndex=startIndex;

        if (consume(LexicalAnalyzer.Token.TokenType.CT_INT)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CT_REAL)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CT_CHAR)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CT_STRING)) {
            return true;
        }
        startIndex=currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
            if (expr()) {
                if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                    return true;
                } else {
                    System.out.println("Error: No ')' present at line " + tokens.get(currentIndex).line);
                }
            }else {
                System.out.println("Error: Invalid expr() at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex=startIndex;
//        System.out.println(tokens.get(currentIndex).id);

        return false;
    }


}
