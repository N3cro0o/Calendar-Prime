import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

// TODO
// https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file

public class CalendarFileHandler {

    private final String CURR_DIR;
    private final String DIRECTORY = "data";

    private List<Integer> entryList;

    public boolean isLoaded = false;
    public Integer loadedEntry = null;

    public CalendarFileHandler(){
        CURR_DIR = System.getProperty("user.dir");
        entryList = new ArrayList<>();
        var check = checkDir();
        if (!check){
            if (!createDir()) System.err.println("Cannot create 'data' directory");
        }
        var _ = loadEntries();
    }

    private boolean checkDir(){
        Path directory = Paths.get(CURR_DIR + File.separator + DIRECTORY);
        return Files.exists(directory);
    }

    private boolean createDir(){
        var path = CURR_DIR + File.separator + DIRECTORY;
        File dirToMake = new File(path);
        return dirToMake.mkdir();
    }

    private boolean loadEntries(){
        var path = CURR_DIR + File.separator + DIRECTORY;
        File directory = new File(path);
        File[] files = directory.listFiles();

        if (files == null) return false;
        for(File f : files){
            if (f == null) return false;
            System.out.println(f.getName());
            entryList.add(Integer.parseInt(f.getName()));
        }
        return true;
    }

    private int getNewFileIndex(){
        int index = 0;
        while (index < entryList.size()) {
            if (!entryList.contains(index)) { break; }
            index++;
        }
        return index;
    }

    public void newEmpty(){
        int index = getNewFileIndex();
        var newData = new CalendarData(index);
        var path = CURR_DIR + File.separator + DIRECTORY + File.separator + index;
        File file = new File(path);
        try {
            if (file.createNewFile()) {
                try(ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream(path)))
                {
                    write.writeObject(newData);
                    loadedEntry = index;
                    isLoaded = true;
                }
                catch (Exception e) {
                    System.err.printf("Cannot save to file: %s\n", e);
                }
            }
        }
        catch (Exception e) {
            System.err.printf("Cannot create file: %s\n", e);
        }
    }

    public void deleteEntry(){
        isLoaded = false;
    }
}
