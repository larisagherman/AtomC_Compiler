//currentToken- este pentru consume()
//tokens.get(currentIndex) e ce avem in lista

import java.util.List;

public class SyntacticalAnalyzer {
    private List<LexicalAnalyzer.Token> tokens;
    private int currentIndex = 0;
    private SymbolTable symbols = new SymbolTable();

    public SyntacticalAnalyzer(List<LexicalAnalyzer.Token> tokens) {
        this.tokens = tokens;
    }

    private boolean consume(LexicalAnalyzer.Token.TokenType expected) {
        LexicalAnalyzer.Token currentToken;
        if (currentIndex < tokens.size()) {
            currentToken = tokens.get(currentIndex);
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
                System.out.println("Something is wrong.\t The file wasn't read all the way for the syntactical part.");
                return false;
            }
        }


        return true;
    }


    public boolean declStruct() {
        if (consume(LexicalAnalyzer.Token.TokenType.STRUCT)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                String tkName = tokens.get(currentIndex - 1).text;
                if (consume(LexicalAnalyzer.Token.TokenType.LACC)) {
                    //if we find this symbol s id in our symbol list=> error: there cannot be two ids with the same name
                    if (symbols.findSymbol(tkName) != null) {
                        System.out.println("Error: Symbol redefinition: " + tkName + " at line " + tokens.get(currentIndex - 1).line);
                        System.exit(0);
                    }
                    //if there is no symbol with that id we are going to add it to our symbol list
                    symbols.crtStruct = symbols.addStructSymbol(tkName);

                    if (tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RACC) {
                        while (declVar()) ;
                    }
                    if (consume(LexicalAnalyzer.Token.TokenType.RACC)) {
                        if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                            symbols.crtStruct = null;
                            return true;
                        } else {
                            System.out.println("Error: Missing ';' after struct definition at line " + tokens.get(currentIndex).line);
                            System.exit(0);
                        }
                    } else {
                        System.out.println("Error: Missing '}' after struct definition at line " + tokens.get(currentIndex).line);
                        System.exit(0);
                    }
                } else {
                    return false;
                }
            } else {
                System.out.println("Error: Missing struct name after 'struct' at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        }
        return false;
    }

    public boolean declVar() {
        SymbolTable.Type t = new SymbolTable.Type();
        if (typeBase(t)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                String tkName = tokens.get(currentIndex - 1).text;
                int line = tokens.get(currentIndex - 1).line;
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if it's an arrayDecl
                    arrayDecl(t);
                } else {
                    t.nrElements = -1;
                }
                symbols.addVar(tkName, t, line);
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.COMMA) {
                    while (consume(LexicalAnalyzer.Token.TokenType.COMMA)) {
                        if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                            tkName = tokens.get(currentIndex - 1).text;
                            line = tokens.get(currentIndex - 1).line;

                            if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if it's an arrayDecl
                                arrayDecl(t);
                            } else {
                                t.nrElements = -1;
                            }
                            symbols.addVar(tkName, t, line);

                        } else {
                            System.out.println("Error: Missing identifier after type at line " + tokens.get(currentIndex).line);
                            System.exit(0);
                        }
                    }
                }
                if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                    return true;
                } else {
                    System.out.println("Error: Missing ';' at line " + (tokens.get(currentIndex).line - 1));
                    System.exit(0);
                }
            } else {
                System.out.println("Error: Missing identifier after type at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        }
        return false;
    }

    public boolean arrayDecl(SymbolTable.Type t) {
        if (consume(LexicalAnalyzer.Token.TokenType.LBRACKET)) {
            // Only call expr() if there's something inside the brackets
            if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RBRACKET) {
                if (!expr()) {
                    System.out.println("Error (from arrayDecl): with expr() in array declaration at line " + tokens.get(currentIndex).line);
                    System.exit(0);
                }
            } else {
                t.nrElements = 0; // array without given size
            }
            if (consume(LexicalAnalyzer.Token.TokenType.RBRACKET)) {
                return true;
            } else {
                System.out.println("Error: Missing ']' in array declaration at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        }
        return false;
    }

    public boolean declFunc() {

        SymbolTable.Type t = new SymbolTable.Type();
        boolean verifyTypeBase = typeBase(t);
        if (consume(LexicalAnalyzer.Token.TokenType.VOID) || verifyTypeBase) {
            if (currentIndex < tokens.size() &&
                    tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.MUL &&
                    verifyTypeBase) {
                consume(LexicalAnalyzer.Token.TokenType.MUL);
                t.nrElements = 0;
            } else {
                t.typeBase = SymbolTable.TypeBase.TB_VOID;
                t.nrElements = -1;
            }

            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                String tkName = tokens.get(currentIndex - 1).text;
                if (symbols.findSymbol(tkName) != null) {
                    System.out.println("Error: Symbol redefinition: " + tkName + " at line " + tokens.get(currentIndex - 1).line);
                    System.exit(0);
                }
                if (consume(LexicalAnalyzer.Token.TokenType.LPAR)) {
                    symbols.crtFunc = symbols.addFuncSymbol(tkName, t);

                    if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RPAR) {//if we have arguments
                        if (funcArg()) {
                            if (tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.COMMA) {
                                while (consume(LexicalAnalyzer.Token.TokenType.COMMA)) {
                                    if (!funcArg()) {
                                        System.out.println("Error: Missing argument after ',' at line " + tokens.get(currentIndex).line);
                                        System.exit(0);
                                    }
                                }
                            }
                        } else {
                            System.out.println("Error: Invalid function argument at line " + tokens.get(currentIndex).line);
                            System.exit(0);
                        }
                    }
                    if (consume(LexicalAnalyzer.Token.TokenType.RPAR)) {
                        symbols.crtDepth--;
                        if (stmCompound()) {
                            symbols.deleteSymbolsAfter(symbols.crtFunc);
                            symbols.crtFunc = null;
                            return true;
                        } else {
                            System.out.println("Error: with stmCompound() at line " + (tokens.get(currentIndex).line - 1));
                            System.exit(0);
                        }
                    } else {
                        System.out.println("Error: Missing ')' in function declaration at line " + tokens.get(currentIndex).line);
                        System.exit(0);
                    }
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public boolean stmCompound() {
        if (consume(LexicalAnalyzer.Token.TokenType.LACC)) {
            // Save the last symbol before entering the block
            SymbolTable.Symbol start = null;
            if (!symbols.symbols.isEmpty()) {
                start = symbols.symbols.get(symbols.symbols.size() - 1);
            }

            symbols.crtDepth++;  // enter new scope level
            if (currentIndex < tokens.size() &&
                    tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RACC) {//If there is something inside the function body
                while (currentIndex < tokens.size() &&
                        tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.RACC) {
                    if (!declVar()) {
                        if (!stm()) {
                            System.out.println("Error: with stm() at line " + tokens.get(currentIndex).line);
                            System.exit(0);
                            return false;
                        }
                    }
                }
            }
            if (consume(LexicalAnalyzer.Token.TokenType.RACC)) {
                symbols.crtDepth--; // exit scope
                // Remove symbols declared inside this block
                symbols.deleteSymbolsAfter(start);
                return true;
            } else {
                System.out.println("Error: Missing '}' at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        }
        return false;
    }


    public boolean stm() {
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
                                        System.exit(0);
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
                            System.exit(0);
                        }
                    } else {
                        System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
                        System.exit(0);
                    }
                }
            } else {
                System.out.println("Error: Missing '(' at line " + tokens.get(currentIndex).line);
                System.exit(0);
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
                            return stm();
                        } else {
                            System.out.println("Error: Missing ')' at line " + tokens.get(currentIndex).line);
                            System.exit(0);
                        }
                    } else {
                        System.out.println("Error : Missing ';' at line " + tokens.get(currentIndex).line);
                        System.exit(0);
                    }
                } else {
                    System.out.println("Error : Missing ';' at line " + tokens.get(currentIndex).line);
                    System.exit(0);
                }
            } else {
                System.out.println("Error: Missing '(' at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        } else if (consume(LexicalAnalyzer.Token.TokenType.BREAK)) {
            if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                return true;
            } else {
                System.out.println("Error: Missing ';' at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        } else if (consume(LexicalAnalyzer.Token.TokenType.RETURN)) {
            if (currentIndex < tokens.size() && tokens.get(currentIndex).id != LexicalAnalyzer.Token.TokenType.SEMICOLON) {
                expr();
            }
            if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                return true;
            } else {
                System.out.println("Error: Missing ';' at line " + (tokens.get(currentIndex).line - 1));
                System.exit(0);
            }
        } else if (expr()) {
            if (consume(LexicalAnalyzer.Token.TokenType.SEMICOLON)) {
                return true;
            } else {
                System.out.println("Error: Missing ';' at line " + (tokens.get(currentIndex).line - 1));
                System.exit(0);
            }
        }
//        currentIndex = startIndex;
        return false;
    }


    private boolean funcArg() {
        SymbolTable.Type t = new SymbolTable.Type();
        if (typeBase(t)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                String tkName = tokens.get(currentIndex - 1).text;
                int line = tokens.get(currentIndex - 1).line;
                if (currentIndex < tokens.size() && tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//if we have arguments
                    arrayDecl(t);
                } else {
                    t.nrElements = -1;
                }
                SymbolTable.Symbol s = symbols.addFcArg(tkName, t, line);
                return true;
            } else {
                System.out.println("Error: Missing ID after typeBase at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        }
        return false;
    }

    public boolean typeBase(SymbolTable.Type typeBase) {
        if (consume(LexicalAnalyzer.Token.TokenType.INT)) {
            typeBase = new SymbolTable.Type(SymbolTable.TypeBase.TB_INT);
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.DOUBLE)) {
            typeBase = new SymbolTable.Type(SymbolTable.TypeBase.TB_DOUBLE);
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.CHAR)) {
            typeBase = new SymbolTable.Type(SymbolTable.TypeBase.TB_CHAR);
            return true;
        } else if (consume(LexicalAnalyzer.Token.TokenType.STRUCT)) {
            if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
                String tkName = tokens.get(currentIndex - 1).text;
                SymbolTable.Symbol s = symbols.findSymbol(tkName);
                if (s == null) {
                    System.out.println("Error: undefined symbol: " + tkName + " at line " + tokens.get(currentIndex - 1).line);
                    System.exit(0);
                }
                if (s.cls != SymbolTable.CLAS.CLS_STRUCT) {
                    System.out.println("Error: " + tkName + " is not a struct at line " + tokens.get(currentIndex - 1).line);
                    System.exit(0);
                }
                SymbolTable.Type type = new SymbolTable.Type(SymbolTable.TypeBase.TB_STRUCT);
                type.s = s;
                typeBase = type.s.type;
                return true;
            } else {
                System.out.println("Error: Missing ID after struct at line " + tokens.get(currentIndex).line);
                System.exit(0);
            }
        }
        return false;
    }

    public boolean typeName(SymbolTable.Type retType) {

        if (typeBase(retType)) {
            if (tokens.get(currentIndex).id == LexicalAnalyzer.Token.TokenType.LBRACKET) {//daca  e declarare de array
                consume(LexicalAnalyzer.Token.TokenType.LBRACKET);
                if (arrayDecl(retType)) {
                    retType.nrElements = 0;
                    return true;
                }
            } else {//daca nu e declarare de array
                return true;
            }
        }
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
            SymbolTable.RetVal rve = new SymbolTable.RetVal();
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
        SymbolTable.Type type = new SymbolTable.Type();
        if (!typeName(type)) {
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
        if (!exprPrimary()) {
            return false;
        }
        return exprPostfix1(); // allow chaining after a primary
    }

    public boolean exprPostfix1() {
        while (true) {
            int startIndex = currentIndex;

            if (exprPostfix1_1()) {
                continue;
            }
            if (exprPostfix1_2()) {
                continue;
            }

            // If neither matched, we're done with postfix
            currentIndex = startIndex;
            break;
        }

        return true; // it's okay to have no postfix
    }

    private boolean exprPostfix1_1() {
        int startIndex = currentIndex;

        if (!consume(LexicalAnalyzer.Token.TokenType.LBRACKET)) {
            return false;
        }
        if (!expr()) {
            currentIndex = startIndex;
            return false;
        }

        if (!consume(LexicalAnalyzer.Token.TokenType.RBRACKET)) {
            System.out.println("Error: Missing ']' at line " + tokens.get(currentIndex).line);
            currentIndex = startIndex;
            return false;
        }

        return true;
    }

    private boolean exprPostfix1_2() {
        int startIndex = currentIndex;

        if (!consume(LexicalAnalyzer.Token.TokenType.DOT)) {
            return false;
        }
        if (!consume(LexicalAnalyzer.Token.TokenType.ID)) {
            System.out.println("Error: Missing ID after '.' at line " + tokens.get(currentIndex).line);
            currentIndex = startIndex;
            System.exit(0);
            return false;
        }
        String tkName = tokens.get(currentIndex - 1).text;
        return true;
    }

    public boolean exprPrimary() {
        if (consume(LexicalAnalyzer.Token.TokenType.ID)) {
            String tkName = tokens.get(currentIndex - 1).text;
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
                        System.exit(0);
                        return false;
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
                    System.exit(0);
                }
            }
        }


        return false;
    }


}
