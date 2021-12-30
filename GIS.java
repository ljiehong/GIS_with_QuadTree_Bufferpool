// On my honor:
//
// - I have not discussed the Java language code in my program with
// anyone other than my instructor or the teaching assistants
// assigned to this course.
//
// - I have not used Java language code obtained from another student,
// or any other unauthorized source, including the Internet, either
// modified or unmodified.
//
// - If any Java language code or documentation used in my program
// was obtained from another source, such as a text book or course
// notes, that has been clearly noted with a proper citation in
// the comments of my program.
//
// - I have not designed this program in such a way as to defeat or
// interfere with the normal operation of the grading code.
//
// <Student's Name>
// <Student's VT email PID>

import java.io.*;
import java.util.Map;

public class GIS {
    /**
     * Read the script file and execute the command
     * @param args <database file name> <command script file name> <log file name>
     * */
    public static void main(String[] args){
        String databaseFileName=args[0];
        String scriptFileName=args[1];
        String logFileName=args[2];
        FileWriter fwriter=null;
        boolean isContinue=true;


        try {
            IndexModel indexModel =new IndexModel(databaseFileName);
            fwriter=new FileWriter(logFileName);
            View view=new View(fwriter);
            BufferedReader in = new BufferedReader(new FileReader(scriptFileName));
            String str;
            int cmdNum=1;
            while (isContinue&&(str = in.readLine()) != null) {
                str=str.trim();
                if(str.startsWith(";")||str.length()==0){
                    fwriter.write(str+"\n");
                    continue;
                }
                String[] strings=str.split("\t");
                String func=strings[0];
                if(func.equals("world")){
                    fwriter.write(str+"\n\n");
                }else{
                    fwriter.write("Command "+(cmdNum++)+":  "+str+"\n\n");
                }
                switch (func) {
                    case "world":
                        long[] boundary= indexModel.commondWorld(strings[1],strings[2],strings[3],strings[4]);
                        view.viewWorld(databaseFileName,scriptFileName,logFileName,boundary);
                        break;
                    case "import":
                        long[] result= indexModel.commondImport(strings[1]);
                        view.viewImport(result);
                        break;
                    case "what_is":
                        Map<Long,String> records= indexModel.commondWhatIs(strings[1],strings[2]);
                        view.viewWhatIs(strings[1],strings[2],records);
                        break;
                    case "what_is_at":
                        records= indexModel.commondWhatIsAt(strings[1],strings[2]);
                        view.viewWhatIsAt(strings[1],strings[2],records);
                        break;
                    case "what_is_in":
                        records= indexModel.commondWhatIsIn(strings[1],strings[2],Long.parseLong(strings[3]),Long.parseLong(strings[4]));
                        view.viewWhatIsIn(strings[1],strings[2],Long.parseLong(strings[3]),Long.parseLong(strings[4]),records);
                        break;
                    case "debug":
                        indexModel.commondDebug(strings[1],fwriter);
                        break;
                    case "quit":
                        isContinue=false;
                        view.viewQuit();
                        break;
                }
                fwriter.write("--------------------------------------------------------------------------------\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(fwriter!=null){
                    fwriter.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }

    }
}
