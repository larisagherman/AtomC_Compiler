import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SymbolTable {
    public ArrayList<Symbol> symbols;
    public Symbol crtStruct;
    public Symbol crtFunc;
    public int crtDepth = 0;


    public enum TypeBase {TB_INT, TB_DOUBLE, TB_CHAR, TB_STRUCT, TB_VOID}


    public enum CLAS {CLS_VAR, CLS_FUNC, CLS_EXTFUNC, CLS_STRUCT}


    public enum MEM {MEM_GLOBAL, MEM_ARG, MEM_LOCAL}


    public SymbolTable() {
        this.symbols = new ArrayList<Symbol>();
    }

    public Symbol findSymbol(String findName) {
        for (int i = 0; i < symbols.size(); i++) {
            Symbol s = symbols.get(i);
            if (s.name.equals(findName)) {
                return s;
            }
        }
        return null;
    }


    public Symbol addSymbol(String name, CLAS cls) {
        Symbol symbol = new Symbol(name, cls);
        symbol.depth = crtDepth;
        symbols.add(symbol);
        return symbol;
    }

    public Symbol addStructSymbol(String name) {
        Symbol symbol = new Symbol(name, CLAS.CLS_STRUCT);
        symbol.depth = crtDepth;
        symbol.members = new Members();
        symbols.add(symbol);
        return symbol;
    }

    public void addVar(String tkName, Type type, int line) {
        Symbol s = null;
        if (crtStruct != null) {
            if (crtStruct.findMember(tkName) != null) {
                System.out.println("Error: Symbol redefinition: " + tkName + " at line " + line);
                System.exit(0);
            }
            s = crtStruct.addMember(tkName, CLAS.CLS_VAR);
        } else if (crtFunc != null) {
            s = findSymbol(tkName);
            if (s != null && s.depth == crtDepth) {
                System.out.println("Error: Symbol redefinition: " + tkName + " at line " + line);
                System.exit(0);
            }
            s = addSymbol(tkName, CLAS.CLS_VAR);
            s.mem = MEM.MEM_LOCAL;
        } else {
            if (findSymbol(tkName) != null) {
                System.out.println("Error: Symbol redefinition: " + tkName + " at line " + line);
                System.exit(0);
            }
            s = addSymbol(tkName, CLAS.CLS_VAR);
            s.mem = MEM.MEM_GLOBAL;
        }
        s.type = type;
    }

    public Symbol addFuncSymbol(String tkName, Type type) {
        Symbol symbol = findSymbol(tkName);
        if (symbol != null)
            throw new RuntimeException("Symbol redefinition " + tkName);
        symbol = new Symbol(tkName, CLAS.CLS_FUNC);
        symbol.args = new Args();
        symbols.add(symbol);
        symbol.type = type;
        crtDepth++;
        return symbol;
    }

    public void deleteSymbolsAfter(Symbol lastSymbol) {
        if (lastSymbol == null) return;

        int lastIndex = symbols.indexOf(lastSymbol);
        if (lastIndex == -1) return;

        // Remove all symbols added after the lastSymbol
        for (int i = symbols.size() - 1; i > lastIndex; i--) {
            symbols.remove(i);
        }
    }

    public Symbol addFcArg(String tkName, Type type, int line) {
        Symbol arg = findSymbol(tkName);
        if (arg != null) {
            System.out.println("Error: Argument redefinition: " + tkName + " at line " + line);
            System.exit(0);
        }
        arg = addSymbol(tkName, CLAS.CLS_VAR);
        arg.mem = MEM.MEM_ARG;
        arg.type = type;
        crtFunc.args.args.add(arg);
        return arg;
    }


    public static class Symbol {
        public String name;
        CLAS cls;
        MEM mem;
        Type type;
        int depth;
        Args args;//used for functions
        Members members;//used for struct

        public Symbol(String name, CLAS cls) {
            this.name = name;
            this.cls = cls;
        }

        public Symbol findMember(String tkName) {
            for (int i = 0; i < members.members.size(); i++) {
                Symbol s = members.members.get(i);
                if (tkName.equals(s.name)) {
                    return s;
                }
            }
            return null;
        }

        public Symbol addMember(String tokenName, CLAS clsVar) {
            Symbol symbol = new Symbol(tokenName, cls);
            members.members.add(symbol);
            return symbol;
        }

        public Symbol addArg(Symbol symbol) {
            args.args.add(symbol);
            return symbol;
        }

        public Symbol findArg(String tkName) {
            for (int i = 0; i < args.args.size(); i++) {
                Symbol s = args.args.get(i);
                if (tkName.equals(s.name)) {
                    return s;
                }
            }
            return null;
        }


    }

    public static class Type {
        public TypeBase typeBase;
        public Symbol s;
        public int nrElements;

        public Type(TypeBase typeBase) {
            this.typeBase = typeBase;
        }

        public Type() {
            super();
        }

        public static void cast(Type dst, Type src) {
            if (dst == null || dst.typeBase == null) {
                throw new RuntimeException("Destination type is not set");
            }
            if (src == null || src.typeBase == null) {
                throw new RuntimeException("Source type is not set");
            }
            if (src.nrElements > -1) {
                if (dst.nrElements > -1) {
                    if (src.typeBase != dst.typeBase)
                        throw new RuntimeException("an array cannot be converted to an array of another type");
                } else {
                    throw new RuntimeException("a.n array cannot be converted to a non-array");
                }
            } else {
                if (dst.nrElements > -1) {
                    throw new RuntimeException("a non-array cannot be converted to an array");
                }
            }
            switch (src.typeBase) {
                case TB_CHAR:
                case TB_INT:
                case TB_DOUBLE:

                    switch (dst.typeBase) {
                        case TB_CHAR:
                        case TB_INT:
                        case TB_DOUBLE:
                            return;
                        default:
                            break;
                    }
                case TB_STRUCT:
                    if (dst.typeBase == TypeBase.TB_STRUCT) {
                        if (src.s != dst.s)
                            throw new RuntimeException("a structure cannot be converted to another one");
                        return;
                    }
                default:
                    break;
            }
            throw new RuntimeException("incompatible types");
        }
        public static Type createType(TypeBase enumType, int nrElems) {
            Type type = new Type(enumType);
            type.nrElements = nrElems;
            return type;
        }

    }

    public class Args {
        List<Symbol> args;

        public Args() {
            this.args = new ArrayList<>();
        }
    }

    public class Members {
        private List<Symbol> members;

        public Members() {
            this.members = new ArrayList<>();
        }


    }

    public static class RetVal {
        public SymbolTable.Type type;
        public boolean isLVal;
        public boolean isCtVal;
        public CtVal ctVal;

        public void makePrimitiv(SymbolTable.TypeBase typeBase, Object val, int nrElements) {
            this.type = new Type(typeBase);
            this.type.nrElements = nrElements;
            this.ctVal = new CtVal(val);
            this.isCtVal = true;
            this.isLVal = false;
        }
    }


    public static class CtVal {
        public Long i; // int, char
        public Double d; // double
        public String str;

        public CtVal(Object val) {
            if (val instanceof String)
                str = (String) val;
            else if (val instanceof Long)
                i = (Long) val;
            else if (val instanceof Integer)
                i = ((Integer) val).longValue();
            else
                d = (Double) val;
        }
    }


}
