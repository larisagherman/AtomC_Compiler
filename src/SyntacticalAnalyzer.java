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


    public boolean unit() {
        while (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.END) {
            int startIndex = currentIndex;  // Save index for backtracking

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


    public boolean declStruct() {
        int startIndex = currentIndex;
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
        int startIndex = currentIndex;
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
                    System.out.println("Error: Missing ';' at line " + (tokens.get(currentIndex).line+1));
                }
            } else {
                System.out.println("Error: Missing identifier after type at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean arrayDecl() {
        int startIndex = currentIndex;
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
        int startIndex = currentIndex;
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
                }
            } else {
                System.out.println("Error: Missing ID after typeBase at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean stmCompound() {
        int startIndex = currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.LACC)) {
            if (currentIndex < tokens.size() &&
                    currentToken.id != LexicalAnalyzer.Token.TokenType.RACC) {//If there is something inside the function body
                while (currentIndex < tokens.size() &&
                        tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RACC) {
                    if (!declVar()) {
                        if (!stm()) {
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
        int startIndex = currentIndex;
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
                }
                if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                    if (currentIndex < tokens.size() &&
                            tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.SEMICOLON) {
                        expr();
                    }
                    if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                        if (currentIndex < tokens.size() &&
                                tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RPAR) {
                            expr();
                        }
                        if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                            if (stm()) {
                                return true;
                            }
                        } else {
                            System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
                        }
                    } else {
                        System.out.println("Error : Missing ';' at line " + tokens.get(currentIndex).line);
                    }
                } else {
                    System.out.println(tokens.get(currentIndex).id);
                    System.out.println("Error : Missing ';' at line " + tokens.get(currentIndex).line);
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
        }
        currentIndex = startIndex;
        return false;
    }


    private boolean funcArg() {
        int startIndex = currentIndex;
        if (typeBase()) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if we have arguments
                    arrayDecl();
                }
                return true;
            } else {
                System.out.println("Error: Missing ID after typeBase at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean typeBase() {
        int startIndex = currentIndex;
        if (consume(LexicalAnalyzer.Token.TokenType.INT) || consume(LexicalAnalyzer.Token.TokenType.DOUBLE) || consume(LexicalAnalyzer.Token.TokenType.CHAR)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.STRUCT)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                return true;
            } else {
                System.out.println("Error: Missing ID after struct at line " + tokens.get(currentIndex).line);
            }
        }
        currentIndex = startIndex;
        return false;
    }


    public boolean expr() {
        return exprAssign();
    }

    public boolean exprAssign() {
        int startIndex = currentIndex;
        if (exprAssign1()) {
            return true;
        }
        currentIndex = startIndex;
        if (exprOr()) {
            return true;
        }
        currentIndex = startIndex;
        return false;
    }

    public boolean exprAssign1() {
        if (!exprUnary()) {
            return false;
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.ASSIGN)) {
            return false;
        }

        exprAssign();
        return true;
    }


    public boolean exprOr() {
        if (!exprAnd()) {
            return false;
        }
        if (!exprOr1()) {
            System.out.println("Error: Invalid exprOr() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }


    public boolean exprOr1() {
        if (consume(LexicalAnalyzer.Token.TokenType.OR)) {
            if (!exprAnd()) {
                return false;
            }
            exprOr1();
        }
        return true;
    }

    public boolean exprAnd() {

        if (!exprEq()) {
            return false;
        }
        if (!exprAnd1()) {
            System.out.println("Error: Invalid exprAnd1() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    public boolean exprAnd1() {
        if (consume(LexicalAnalyzer.Token.TokenType.AND)) {
            if (!exprEq()) {
                return false;
            }
            exprAnd1();
        }
        return true;
    }

    public boolean exprEq() {

        if (!exprRel()) {
            return false;
        }
        if (!exprEq1()) {
            System.out.println("Error: Invalid exprEq1() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    public boolean exprEq1() {
        if (consume(LexicalAnalyzer.Token.TokenType.EQUAL) || consume(LexicalAnalyzer.Token.TokenType.NOTEQ)) {
            if (!exprRel()) {
                return false;
            }
            exprEq1();
        }

        return true;
    }

    public boolean exprRel() {

        if (!exprAdd()) {
            return false;
        }
        if (!exprRel1()) {
            System.out.println("Error: Invalid exprAdd() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    public boolean exprRel1() {

        if (consume(LexicalAnalyzer.Token.TokenType.LESS) || consume(LexicalAnalyzer.Token.TokenType.LESSEQ) || consume(LexicalAnalyzer.Token.TokenType.GREATER) || consume(LexicalAnalyzer.Token.TokenType.GREATEREQ)) {
            if (!exprAdd()) {
                return false;
            }
            exprRel1();
        }
        return true;
    }

    public boolean exprAdd() {
        if (!exprMul()) {
            return false;
        }
        if (!exprAdd1()) {
            System.out.println("Error: Invalid exprAdd1() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    public boolean exprAdd1() {

        if (consume(LexicalAnalyzer.Token.TokenType.ADD) || consume(LexicalAnalyzer.Token.TokenType.SUB)) {
            if (!exprMul()) {
                return false;
            }
            exprAdd1();
        }

        return true;
    }

    public boolean exprMul() {
        if (!exprCast()) {
            return false;
        }
        if (!exprMul1()) {
            System.out.println("Error: Invalid exprMul1() at line " + tokens.get(currentIndex).line);
            return false;
        }
        return true;
    }

    private boolean exprMul1() {

        if (consume(LexicalAnalyzer.Token.TokenType.MUL) || consume(LexicalAnalyzer.Token.TokenType.DIV)) {
            if (!exprCast()) {
                return false;
            }
            exprMul1();
        }
        return true;
    }

    public boolean exprCast() {
        int startIndex = currentIndex;
        if (exprCast1()) {
            return true;
        }
        return exprUnary();
    }

    public boolean exprCast1() {
        if (!consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
            return false;
        }
        if (!typeName()) {
            System.out.println("Error: Invalid/No typeName() at line " + tokens.get(currentIndex).line);
            return false;
        }
        System.out.println(tokens.get(currentIndex).id);

        if (!consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
            System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
            return false;
        }
        exprCast();
        return true;
    }

    public boolean typeName() {

        if (typeBase()) {
            if (tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//daca  e declarare de array
                consume(LexicalAnalyzer.Token.TokenType.LBRACKET);
                if (arrayDecl()){
                    return true;
                }
            } else {//daca nu e declarare de array
                return true;
            }
        }
        return false;
    }

    public boolean exprUnary() {
        if (exprUnary1()) {
            return true;
        }
        if (exprPostfix()) {
            return true;
        }
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
        int startIndex = currentIndex;

        if (!exprPrimary()) {
            return false;
        }

        // After a primary expression, try to parse any postfix chain
        while (true) {
            int backup = currentIndex;
            if (!exprPostfix1()) {
                currentIndex = backup; // backtrack and stop chaining
                break;
            }
        }

        return true;
    }


    public boolean exprPostfix1() {
        if (exprPostfix1_1()) {
            return true;
        }
        if (exprPostfix1_2()) {
            return true;
        }
        return false;
    }

    private boolean exprPostfix1_1() {//array decl
        if (!consume(LexicalAnalyzer.Token.TokenType.LBRACKET)) {
            return false;
        }
        if (!expr()) {
            return false;
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.RBRACKET)) {
            System.out.println("Error: Missing ']' at line " + tokens.get(currentIndex).line);
            return false;
        }

        exprPostfix1();
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
        exprPostfix1();

        return true;
    }

    public boolean exprPrimary() {
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
                        } else {
                            return false;
                        }
                    }
                    if (!consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                        System.out.println("Error: No ')' present at line " + tokens.get(currentIndex).line);
                    } else {
                        return true;
                    }
                }
            }
            return true;
        }

        if (consume(LexicalAnalyzer.Token.TokenType.CT_INT)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CT_REAL)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CT_CHAR)) {
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CT_STRING)) {
            return true;
        }
        if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
            if (expr()) {
                if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                    return true;
                } else {
                    System.out.println("Error: No ')' present at line " + tokens.get(currentIndex).line);
                }
            }
        }


        return false;
    }


}
