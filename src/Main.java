import java.io.IOException;
import java.util.List;
// files the testat 1.c 3.c 5.c
public class Main{
    public static void main(String[] args){
        String fileName="mine.c";
        try{
            LexicalAnalyzer lexicalAnalyzer=new LexicalAnalyzer(fileName);
            //System.out.println(lexicalAnalyzer.content);
            //System.out.println("haha");
            List<LexicalAnalyzer.Token> text=lexicalAnalyzer.tokenize();
            int prevLine=-1;
            for(int i=0;i< text.size();i++){
                if(text.get(i).line!=prevLine){
                    prevLine=text.get(i).line;
                    System.out.println();
                }
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