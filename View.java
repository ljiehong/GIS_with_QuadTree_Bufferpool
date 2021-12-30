
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

public class View {
    private FileWriter fwriter;

    //constructor
    public View(FileWriter writer){
        this.fwriter=writer;
    }

    /**
     * The output format of the world command
     * */
    public void viewWorld(String databaseFileName,String scriptFileName,String logFileName,long[] boundary) throws IOException {
        fwriter.write("GIS Program\n\n");
        fwriter.write(String.format("dbFile:     %s\n",databaseFileName));
        fwriter.write(String.format("script:     %s\n", scriptFileName));
        fwriter.write(String.format("log:        %s\n",logFileName));
        fwriter.write(String.format("Start time: %s\n", new Date().toString()));
        fwriter.write("Quadtree children are printed in the order SW  SE  NE  NW\n");
        fwriter.write("--------------------------------------------------------------------------------\n\n");
        fwriter.write("Latitude/longitude values in index entries are shown as signed integers, in total seconds.\n\n");
        fwriter.write("World boundaries are set to:\n");
        fwriter.write(String.format("              %d\n", boundary[0]));
        fwriter.write(String.format("   %d                %d\n",boundary[1],boundary[2]));
        fwriter.write(String.format("              %d\n", boundary[3]));
    }

    /**
     * The output format of the import command
     * */
    public void viewImport(long[] result) throws IOException {
        fwriter.write(String.format("Imported Features by name: %d\n", result[0]));
        fwriter.write(String.format("Imported Locations:        %d\n",result[1]));
        fwriter.write(String.format("Average name length:       %d\n",result[2]));
    }

    /**
     * The output format of the what_is command
     * */
    public void viewWhatIs(String name,String state,Map<Long,String> records) throws IOException {
        if(records.size()==0){
            fwriter.write(String.format("No records match %s and %s\n",name,state ));
            return ;
        }
        for(Long offset:records.keySet()){
            String[] strings=records.get(offset).split("\\|");
            fwriter.write(String.format("   %d:  %s  (%s, %s)\n", offset,strings[5],DMSFormatStr(strings[8]),DMSFormatStr(strings[7])));
        }
    }

    /**
     * The output format of the what_is_at command
     * */
    public void viewWhatIsAt(String latDMS,String lngDMS, Map<Long,String> records) throws IOException {
        if(records.size()==0){
            fwriter.write(String.format("   Nothing was found at (%s, %s)\n", DMSFormatStr(lngDMS),DMSFormatStr(latDMS)));
            return ;
        }
        fwriter.write(String.format("   The following features were found at (%s, %s):\n", DMSFormatStr(lngDMS),DMSFormatStr(latDMS)));
        for(Long offset:records.keySet()){
            String[] strings=records.get(offset).split("\\|");
            fwriter.write(String.format("   %d:  %s  %s  %s\n", offset,strings[1],strings[5],strings[3]));
        }
    }

    /**
     * The output format of the what_is_in command
     * */
    public void viewWhatIsIn(String centerLatDMS,String centerLngDMS,long halfHeight,long halfWidth,Map<Long,String> records) throws IOException {
        if(records.size()==0){
            fwriter.write(String.format("   Nothing was found in (%s +/- %d, %s +/- %d)\n",
                    DMSFormatStr(centerLngDMS),
                    halfWidth,
                    DMSFormatStr(centerLatDMS),
                    halfHeight));
            return ;
        }
        fwriter.write(String.format("   The following %d features were found in (%s +/- %d, %s +/- %d)\n",
                records.size(),
                DMSFormatStr(centerLngDMS),
                halfWidth,
                DMSFormatStr(centerLatDMS),
                halfHeight));
        for(Long offset:records.keySet()){
            String[] strings=records.get(offset).split("\\|");
            fwriter.write(String.format("   %d: %s  %s  (%s, %s)\n", offset,strings[1],strings[3],DMSFormatStr(strings[8]),DMSFormatStr(strings[7])));
        }
    }

    /**
     * The output format of the quit command
     * */
    public void viewQuit() throws IOException {
        fwriter.write("Terminating execution of commands.\n");
        fwriter.write(String.format("End time: %s\n", new Date().toString()));
    }

    /**
     * DMS string conversion
     * */
    private String DMSFormatStr(String dms){
        if(dms.equals("Unknown")){
            return dms;
        }
        if(dms.length()==7){
            return String.format("%dd %dm %ds %s",
                    Integer.parseInt(dms.substring(0,2)),
                    Integer.parseInt(dms.substring(2,4)),
                    Integer.parseInt(dms.substring(4,6)),
                    dms.charAt(6)=='N'?"North":"Sourth");
        }else if(dms.length()==8){
            return String.format("%dd %dm %ds %s",
                    Integer.parseInt(dms.substring(0,3)),
                    Integer.parseInt(dms.substring(3,5)),
                    Integer.parseInt(dms.substring(5,7)),
                    dms.charAt(7)=='W'?"West":"East");
        }
        return null;
    }

}
