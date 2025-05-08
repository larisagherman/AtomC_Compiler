import java.util.ArrayList;
import java.util.List;

public class SymbolTable {
    public Symbols symbols;
    public enum TypeBase{TB_INT,TB_DOUBLE,TB_CHAR,TB_STRUCT,TB_VOID};
    public enum CLAS{CLS_VAR,CLS_FUNC,CLS_EXTFUNC,CLS_STRUCT};
    public enum MEM{MEM_GLOBAL,MEM_ARG,MEM_LOCAL};
    public class Type{
        public TypeBase typeBase;
        public Symbol s;
        public int nrElements;
        public Type(TypeBase typeBase,Symbol s,int nrElements) {
            this.typeBase=typeBase;
            this.s=s;
            this.nrElements=nrElements;
        }
        public Type(TypeBase typeBase){
            this(typeBase,null,-1);
        }


    }
    public class Symbol{
       public String name;
       CLAS cls;
       MEM mem;
       Type type;
       int depth;
       Symbols args;//used for funtions
       Symbols members;//used for struct
        public Symbol(String name,CLAS cls){
            this.name=name;
            this.cls=cls;
        }


    }
    public class Symbols{
        public List<Symbol> list;
        public Symbols(){
            list=new ArrayList<Symbol>();
        }

        public void addSymbol(String name, CLAS cls){
            Symbol s=new Symbol(name,cls);
            list.add(s);
        }
        public Symbol findSymbol(String findName){
            for(int i=0;i<list.size();i++){
                Symbol s=list.get(i);
                if(s.name.equals(findName)){
                    return s;
                }
            }
            return null;
        }
    }
    public SymbolTable(){
        this.symbols=new Symbols();
    }
}
