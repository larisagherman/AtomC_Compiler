import java.io.IOException;
import java.util.List;

public class Main{
    private void print(){

    }
    public static void main(String[] args){
        String fileName="mine.c";

        try{
            LexicalAnalyzer lexicalAnalyzer=new LexicalAnalyzer(fileName);
//            System.out.println(lexicalAnalyzer.content);
            List<LexicalAnalyzer.Token> text=lexicalAnalyzer.tokenize();
//            LexicalAnalyzer.print(text);
            SyntacticalAnalyzer s=new SyntacticalAnalyzer(text);
            if(s.unit()){
                System.out.println("YAY No errors!");
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }
}