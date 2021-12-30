import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

/**
 *  Class for read and write database file
 * */
public class DatabaseFile implements Closeable {
    private static DatabaseFile instance;
    private RandomAccessFile fileHandler;
    private String filePath;

    /**
     * Singleton pattern
     * @param path c
     * @return DatabaseFile object
     * */
    public static DatabaseFile getInstance(String path) {
        if(instance==null){
            instance=new DatabaseFile(path);
        }
        return instance;
    }

    /**
     * Constructor. Writes the header of the database file format.
     * @param path DatabaseFile object
     * */
    private DatabaseFile(String path){
        filePath=path;
        try(FileWriter tmp=new FileWriter(filePath,false)){
            tmp.write("FEATURE_ID|FEATURE_NAME|FEATURE_CLASS|STATE_ALPHA|STATE_NUMERIC|COUNTY_NAME|COUNTY_NUMERIC|PRIMARY_LAT_DMS|PRIM_LONG_DMS|PRIM_LAT_DEC|PRIM_LONG_DEC|SOURCE_LAT_DMS|SOURCE_LONG_DMS|SOURCE_LAT_DEC|SOURCE_LONG_DEC|ELEV_IN_M|ELEV_IN_FT|MAP_NAME|DATE_CREATED|DATE_EDITED\n");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Get the file handle
     * @return Handle to the database file
     * */
    public DatabaseFile open() throws IOException{
        fileHandler=new RandomAccessFile(filePath,"rw");
        return this;
    }

    /**
     * Batch write record
     * @param records A list of records to be written
     * */
    public void importRecords(List<String> records) throws IOException{
        fileHandler.seek(fileHandler.length());
        for(String record:records){
            fileHandler.write((record+"\n").getBytes());
        }
    }

    /**
     * Gets the current database file size.
     * @return database file size
     * */
    public long length() throws IOException{
        return fileHandler.length();
    }

    /**
     * Reads a record from the specified offset.
     * @return Record string
     * */
    public String readRecord(long offset)throws IOException{
        fileHandler.seek(offset);
        return fileHandler.readLine();
    }

    /**
     * Close the file handle
     * */
    @Override
    public void close() throws IOException {
        if(fileHandler!=null){
            fileHandler.close();
        }
    }
}
