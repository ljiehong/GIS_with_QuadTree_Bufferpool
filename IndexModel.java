import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import cs3114.GIS.DS.hashTable;
import cs3114.GIS.DS.prQuadtree;


public class IndexModel {
    private prQuadtree<Point> tree;
    private final hashTable<nameEntry> table;
    private final LRUCache bufferPool;
    private final String databaseFilename;

    /**
     * The initial size of the hash table is 1024 and the load factor is 0.7.
     * The LRU cache has a capacity of 15.
     * @param databaseFilename The path to the database file
     * */
    public IndexModel(String databaseFilename) {
        this.databaseFilename=databaseFilename;
        tree=new prQuadtree<>(0,0,0,0);
        table = new hashTable<nameEntry>(1024, 0.7);
        bufferPool=new LRUCache(15);
    }

    /**
     * Take concatenation of the feature name and state abbreviation field of the data records as the key,
     * and the offset recorded in the file as the value.Put it in a hash table.
     * @param strings record
     * @param offset file offset
     * @return Returns true on successful insertion
     * */
    private boolean insertTable(String[] strings, long offset) {
        nameEntry entry = new nameEntry(strings[1] + "|" + strings[3], offset);
        nameEntry finded = table.find(entry);
        if (finded != null) {
            return finded.addLocation(offset);
        } else {
            return table.insert(entry);

        }
    }


    /**
     * The index entries held in the quadtree will store a geographic coordinate and a collection of the file offsets of the matching
     * GIS records in the database file.
     * @param strings record
     * @param offset file offset
     * @return Returns true on successful insertion
     * */
    private boolean insertTree(String[] strings, long offset) {
        Point point=new Point(DMS2Long(strings[8]), DMS2Long(strings[7]));
        point.addOffset(offset);
        Point pointInTree=tree.find(point);
        if(pointInTree==null){
            return tree.insert(point);
        }
        pointInTree.addOffset(offset);
        return true;
    }

    /**
     * Add all the valid GIS records in the specified file to the database file. This means that the records will be appended to
     * the existing database file, and that those records will be indexed in the manner described earlier.
     *
     * @param filePath GIS record file
     * */
    public long[] commondImport(String filePath) {
        long treeCount=0,tableCount=0,nameLenSum=0;

        try (BufferedReader fin = new BufferedReader(new FileReader(filePath));
             DatabaseFile databaseFile = DatabaseFile.getInstance(databaseFilename).open()
        ) {
            String str = fin.readLine();//read header
            List<String> records = new ArrayList<>();
            long offset = databaseFile.length();
            while ((str = fin.readLine()) != null) {
                String[] strings = str.trim().split("\\|");
                records.add(str);
                nameLenSum+=strings[1].length();
                if(insertTree(strings,offset)){//Within the boundaries of the coordinate space
                    treeCount++;
                }
                if(insertTable(strings, offset)){
                    tableCount++;
                }
                offset += (str.length() + 1);
            }
            databaseFile.importRecords(records);
            return new long[] {tableCount,treeCount,nameLenSum/records.size()};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Specifie the boundaries of the coordinate space to be
     * modeled.
     * @param eastLong the vertical boundaries of the coordinate space.
     * @param westLong the vertical boundaries of the coordinate space.
     * @param northLat the horizontal boundaries of the coordinate space.
     * @param southLat the horizontal boundaries of the coordinate space.
     * @return the vertical and horizontal boundaries of the coordinate space.
     * */
    public long[] commondWorld(String westLong,String eastLong,String southLat,String northLat){
        long xMin= DMS2Long(westLong);
        long xMax= DMS2Long(eastLong);
        long yMin= DMS2Long(southLat);
        long yMax= DMS2Long(northLat);
        tree=new prQuadtree<>(xMin,xMax,yMin,yMax);
        return new long[] {yMax,xMin,xMax,yMin};
    }

    /**
     * Log the contents of the specified index structure in a fashion that makes the internal structure and contents of the index
     * clear.
     * @param type quad | hash | pool
     * @param fileWriter log file
     * */
    public void commondDebug(String type,FileWriter fileWriter) throws IOException {
        switch (type) {
            case "quad":
                tree.display(fileWriter);
                break;
            case "hash":
                table.display(fileWriter);
                break;
            case "pool":
                bufferPool.display(fileWriter);
                break;
        }
    }

    /**
     * Gets a record in the database file based on the offset
     * @param databaseFile Database file processing objects
     * @param offset file offset
     * @return record string
     * */
    private String getRecord(DatabaseFile databaseFile,long offset) throws IOException{
        String record=bufferPool.get(offset);
        if(record==null){
            record=databaseFile.readRecord(offset);
            bufferPool.put(offset,record);
        }
        return record;
    }

    /**
     * Get every GIS record in the database file that matches the given feature name
     *     and state abbreviation
     * @param name feature name
     * @param state state abbreviation
     * @return key:offset;value:record
     * */
    public Map<Long,String> commondWhatIs(String name,String state){
        String key=name+"|"+state;
        Map<Long,String> result=new TreeMap<>();
        nameEntry entry = table.find(new nameEntry(key, 0L));
        if (entry != null) {
            try(DatabaseFile databaseFile = DatabaseFile.getInstance(databaseFilename).open()){
                for(Long offset:entry.locations()){
                    String record=getRecord(databaseFile,offset);
                    result.put(offset,record);
                }
            }catch (IOException e){
                e.printStackTrace();
            }

        }
        return result;
    }

    /**
     * Get every GIS record in the database file that matches the given geographic coordinate.
     * @param latDMS Primary Latitude DMS
     * @param lngDMS Primary Longitude DMS
     * @return key:offset;value:record
     * */
    public Map<Long,String> commondWhatIsAt(String latDMS,String lngDMS){
        Point point=new Point(DMS2Long(lngDMS), DMS2Long(latDMS));
        Map<Long,String> result=new TreeMap<>();
        Point pointInTree=tree.find(point);
        if(pointInTree!=null){
            try(DatabaseFile databaseFile = DatabaseFile.getInstance(databaseFilename).open()){
                for(Long offset:pointInTree.getOffsets()){
                    String record=getRecord(databaseFile,offset);
                    result.put(offset,record);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Get every GIS record in the database file whose coordinates fall
     * within the closed rectangle with the specified height
     * and width, centered at the geographic coordinate.
     * @param centerLatDMS Primary Latitude DMS of centre
     * @param centerLngDMS Primary Longitude DMS of centre
     * @param halfHeight Half the height of the rectangle
     * @param halfWidth Half the width of the rectangle
     * @return key:offset;value:record
     * */
    public Map<Long,String> commondWhatIsIn(String centerLatDMS,String centerLngDMS,long halfHeight,long halfWidth){
        Map<Long,String> result=new TreeMap<>();
        long centerX= DMS2Long(centerLngDMS);
        long centerY= DMS2Long(centerLatDMS);
        Vector<Point> points=tree.find(centerX-halfWidth,centerX+halfWidth,centerY-halfHeight,centerY+halfHeight);
        if(points!=null){
            try(DatabaseFile databaseFile = DatabaseFile.getInstance(databaseFilename).open()){
                for(Point point:points){
                    for(Long offset:point.getOffsets()){
                        String record=getRecord(databaseFile,offset);
                        result.put(offset,record);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Converts a DMS format string to an integer.
     * @param dms coverted string
     * @return Radians in seconds
     * */
    private long DMS2Long(String dms) {

        if (dms == null || dms.equals("Unknown")) {
            return 181*3600;

        }

        char type=dms.charAt(dms.length()-1);
        long result = 0;
        if (type=='W') {

            String w1 = dms.substring(0, 3);
            String w2 = dms.substring(3,5);
            String w3 = dms.substring(5,7);
            result -= Long.parseLong(w1)*3600;
            result -= (Long.parseLong(w2) * 60);
            result -= Long.parseLong(w3);

        } else if (type=='N') {
            String n1 = dms.substring(0, 2);
            String n2 = dms.substring(2,4);
            String n3 = dms.substring(4,6);
            result += Long.parseLong(n1)*3600;
            result += (Long.parseLong(n2) * 60);
            result += Long.parseLong(n3);
        }

        return result;

    }


}
