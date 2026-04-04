import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// TODO
// https://stackoverflow.com/questions/10654236/java-save-object-data-to-a-file

public class CalendarFileHandler {

    private final String CURR_DIR;
    private final String DIRECTORY = "data";
    private final String EXPORTS = "export";

    private List<Long> entryList;

    public boolean isLoaded = false;
    private CalendarData loadedEntry = null;

    public CalendarFileHandler(){
        CURR_DIR = System.getProperty("user.dir");
        entryList = new ArrayList<>();
        var check = checkDir(DIRECTORY);
        if (!check){
            if (!createDir(DIRECTORY)) System.err.println("Cannot create 'data' directory");
        }
        check = checkDir(EXPORTS);
        if (!check){
            if (!createDir(EXPORTS)) System.err.println("Cannot create 'export' directory");
        }
        var _ = loadEntries();
    }

    private boolean checkDir(String dir){
        Path directory = Paths.get(CURR_DIR + File.separator + dir);
        return Files.exists(directory);
    }

    public boolean checkForFile(long id) {
        return entryList.contains(id);
    }

    private boolean createDir(String dir){
        var path = CURR_DIR + File.separator + dir;
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
        var newData = new CalendarData(index);
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

    public void pushWithoutDates(LocalDateTime from, LocalDateTime to) {
        var repeat = loadedEntry.getRepeat();
        repeat.pushWithoutPair(from, to);
    }

    public void pushWithoutDates(LocalDateTime date) {
        pushWithoutDates(date, date);
    }

    public void resetWithoutDates(){
        var repeat = loadedEntry.getRepeat();
        repeat.resetWithoutArray();
    }

    public void exportICS() {
        String fileName = String.format("%d-%s.ics", loadedEntry.ID, loadedEntry.getTitle());
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(fileName.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.err.println(e.toString());
            return;
        }
            var path = CURR_DIR + File.separator + DIRECTORY + File.separator + fileName;
        String icsBodyPreformatted = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID://PWR//CalendarPrime//EN\n" +
                "CALSCALE:GREGORIAN\n" +
                "METHOD:PUBLISH\n" +
                "BEGIN:VEVENT\n" +
                "UID:%s@pwr.edu.pl\n" +
                "DTSTAMP:%s\n" +
                "DTSTART:%s\n" +
                "DTEND:%s\n" +
                "SUMMARY:%s\n" +
                "DESCRIPTION:%s\n" +
                "LOCATION:%s\n" +
                "END:VEVENT\nENDVCALENDAR";
        // UID
        byte[] digest = md.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        StringBuilder hashText = new StringBuilder(bigInt.toString(16));
        while (hashText.length() < 32) {
            hashText.insert(0, "0");
        }
        // Dates
        final DateTimeFormatter formatBig = DateTimeFormatter.ofPattern("yyyyMMdd");
        final DateTimeFormatter formatSmol = DateTimeFormatter.ofPattern("HHmmss");
        LocalDateTime date = LocalDateTime.now();
        String createStr = String.format("%sT%sZ", date.format(formatBig), date.format(formatSmol));
        date = loadedEntry.getStart();
        String startStr = String.format("%sT%sZ", date.format(formatBig), date.format(formatSmol));
        date = loadedEntry.getEnd();
        String endStr = String.format("%sT%sZ", date.format(formatBig), date.format(formatSmol));
        // Other goes down here
        String icsBody = String.format(icsBodyPreformatted,
                hashText, // UID
                createStr, // Create
                startStr, // Start
                endStr, // End
                loadedEntry.getTitle(), // Summary/title
                loadedEntry.getDescription(), // Desc
                loadedEntry.getLocation() // Locat
                );
        System.out.println(icsBody);
    }
}
