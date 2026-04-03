import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// TODO
// https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file

public class CalendarFileHandler {

    private final String CURR_DIR;
    private final String DIRECTORY = "data";

    private List<Long> entryList;

    public boolean isLoaded = false;
    private CalendarData loadedEntry = null;

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

    public boolean checkForFile(long id) {
        return entryList.contains(id);
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
            entryList.add(Long.parseLong(f.getName()));
        }
        return true;
    }

    private long getNewFileIndex(){
        long index = 0;
        while (index < entryList.size()) {
            if (!entryList.contains(index)) { break; }
            index++;
        }
        return index;
    }

    public Long newEmpty(){
        long index = getNewFileIndex();
        var newData = new CalendarData((int) index);
        var path = CURR_DIR + File.separator + DIRECTORY + File.separator + index;
        File file = new File(path);
        try {
            if (file.createNewFile()) {
                try(ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream(path)))
                {
                    write.writeObject(newData);
                    loadedEntry = newData;
                    isLoaded = true;
                }
                catch (Exception e) {
                    System.err.printf("Cannot save to file: %s\n", e);
                }
            }
        }
        catch (Exception e) {
            System.err.printf("Cannot create file: %s\n", e);
            return null;
        }
        return index;
    }

    public void saveCurrent() {
        var path = CURR_DIR + File.separator + DIRECTORY + File.separator + loadedEntry.ID;
        try(ObjectOutputStream write = new ObjectOutputStream(new FileOutputStream(path)))
        {
            write.writeObject(loadedEntry);
        }
        catch (Exception e) {
            System.err.printf("Cannot load to file: %s\n", e);
        }
    }

    public String currentToString(){
        return loadedEntry.toString();
    }

    public void deleteEntry(){
        isLoaded = false;
    }

    public void updateCurrentTitle(String t) {
        loadedEntry.setTitle(t);
    }

    public void updateCurrentDesc(String d) {
        loadedEntry.setDescription(d);
    }

    public void updateCurrentLocat(String l) {
        loadedEntry.setLocation(l);
    }

    public void updateCurrentStart(LocalDateTime start) {
        loadedEntry.setStart(start);
    }

    public void updateCurrentEnd(LocalDateTime end) {
        loadedEntry.setEnd(end);
    }

    public CalendarData loadFile(long id) {
        var path = CURR_DIR + File.separator + DIRECTORY + File.separator + id;
        try(ObjectInputStream read = new ObjectInputStream(new FileInputStream(path)))
        {
            loadedEntry = (CalendarData) read.readObject();
            isLoaded = true;
        }
        catch (Exception e) {
            System.err.printf("Cannot load to file: %s\n", e);
            return null;
        }
        return loadedEntry;
    }

    public long currentID() {
        return loadedEntry.ID;
    }

    public void updateCurrentAllDay(boolean b) {
        loadedEntry.setAllDay(b);
    }

    public CalendarData.RepeatData getCurrentRepeatData(){
        return loadedEntry.getRepeat();
    }

    public void setCurrentRepeatData(CalendarData.RepeatData data){
        loadedEntry.setRepeat(data);
    }
}
