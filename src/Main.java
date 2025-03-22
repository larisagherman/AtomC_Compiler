import java.io.IOException;
import java.util.List;

public class Main{
    public static void main(String[] args){
        String fileName="mine.c";
        try{
            LexicalAnalyzer lexicalAnalyzer=new LexicalAnalyzer(fileName);
            //System.out.println(lexicalAnalyzer.content);
            //System.out.println("haha");
            List<LexicalAnalyzer.Token> text=lexicalAnalyzer.tokenize();
            for(int i=0;i< text.size();i++){
                System.out.print(text.get(i).toString()+" ");
            }
            System.out.println();

            SyntacticalAnalyzer s=new SyntacticalAnalyzer(text);
            System.out.println(s.unit());

        }catch (IOException e){
            e.printStackTrace();
        }

    }
}