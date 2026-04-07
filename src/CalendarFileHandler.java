import org.antlr.v4.runtime.misc.Pair;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
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
        List<String> eventList = new ArrayList<>();
        String finalString;
        var path = CURR_DIR + File.separator + EXPORTS + File.separator + fileName;
        String mainBody = "BEGIN:VCALENDAR\n" +
                "VERSION:2.0\n" +
                "PRODID://PWR//CalendarPrime//EN\n" +
                "CALSCALE:GREGORIAN\n" +
                "METHOD:PUBLISH\n" +
                "%s" + // Events go here
                "ENDVCALENDAR";
        Duration endOffset = Duration.between(loadedEntry.getStart(), loadedEntry.getEnd());
        // TODO
        // Calculate periods of time
        var repeatData = loadedEntry.getRepeat();
        if (!repeatData.getWithoutArray().isEmpty()) {
            LocalDateTime finalDate = null;
            switch (repeatData.getRepeatEnd()) {
                case INF -> finalDate = LocalDateTime.MAX;
                case ON -> finalDate = repeatData.getRepeatOn();
                case AFTER -> {
                    switch (repeatData.getRepeatCycle()) {
                        case DAILY -> finalDate = loadedEntry.getStart().plusDays(repeatData.getRepeatAfter());
                        case WEEKLY -> finalDate = loadedEntry.getStart().plusWeeks(repeatData.getRepeatAfter());
                        case MONTHLY -> finalDate = loadedEntry.getStart().plusMonths(repeatData.getRepeatAfter());
                        case YEARLY -> finalDate = loadedEntry.getStart().plusYears(repeatData.getRepeatAfter());
                    }
                }
            }
            var localStartDate = loadedEntry.getStart();
            var startDate = loadedEntry.getStart();
            var nonoDates = repeatData.getWithoutArray();
            boolean nonoCheck = false;
            // Event loop
            while (localStartDate.isBefore(finalDate)) {
                if (!nonoCheck) {
                    for (Pair<LocalDateTime, LocalDateTime> str : nonoDates) {
                        if (str.a.isBefore(localStartDate) && str.b.isAfter(localStartDate)) {
                            nonoCheck = true;
                            CalendarData.RepeatData newData = new CalendarData.RepeatData();
                            newData.setWeekdayMask(repeatData.getWeekdayMask());
                            newData.setRepeatCycle(repeatData.getRepeatCycle());
                            newData.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.ON);
                            newData.setRepeatEvery(repeatData.getRepeatEvery());
                            newData.setRepeatOn(str.a);
                            eventList.add(generateEventBody(fileName, startDate, endOffset, newData));
                            break;
                        }
                    }
                }
                else {
                    boolean exitCheck = true;
                    for (Pair<LocalDateTime, LocalDateTime> str : nonoDates) {
                        if (str.a.isBefore(localStartDate) && str.b.isAfter(localStartDate)) {
                            exitCheck = false;
                            break;
                        }
                    }
                    if (exitCheck) {
                        nonoCheck = false;
                        startDate = localStartDate;
                    }
                }
                switch (repeatData.getRepeatCycle()) {
                    case DAILY -> localStartDate = localStartDate.plusDays(repeatData.getRepeatEvery());
                    case WEEKLY -> localStartDate = localStartDate.plusWeeks(repeatData.getRepeatEvery());
                    case MONTHLY -> localStartDate = localStartDate.plusMonths(repeatData.getRepeatEvery());
                    case YEARLY -> localStartDate = localStartDate.plusYears(repeatData.getRepeatEvery());
                }
            }
            CalendarData.RepeatData newData = new CalendarData.RepeatData();
            newData.setWeekdayMask(repeatData.getWeekdayMask());
            newData.setRepeatCycle(repeatData.getRepeatCycle());
            newData.setRepeatEnd(CalendarData.RepeatData.RepeatEnd.ON);
            newData.setRepeatEvery(repeatData.getRepeatEvery());
            newData.setRepeatOn(finalDate);
            eventList.add(generateEventBody(fileName, startDate, endOffset, newData));
            StringBuilder eventString = new StringBuilder();
            for (String str : eventList) eventString.append(str);
            finalString = String.format(mainBody, eventString);
        }
        else {
            var icsEventBody = generateEventBody(fileName, loadedEntry.getStart(), endOffset, loadedEntry.getRepeat());
            finalString = String.format(mainBody, icsEventBody);
        }
        try
        {
            Files.writeString(Path.of(path), finalString, StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            System.err.printf("Cannot export ics file to file: %s\n", e);
        }
        System.out.println(finalString);
    }

    private String generateEventBody(String fileName, LocalDateTime start, Duration endOffset, CalendarData.RepeatData repeatData) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(fileName.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e) {
            System.err.println(e.toString());
            return "";
        }
        String icsBodyPreformatted = "BEGIN:VEVENT\n" +
                "UID:%s@pwr.edu.pl\n" +
                "DTSTAMP;TZID=Europe/Warsaw:%s\n" +
                "DTSTART;TZID=Europe/Warsaw:%s\n" +
                "DTEND;TZID=Europe/Warsaw:%s\n" +
                "SUMMARY:%s\n" +
                "DESCRIPTION:%s\n" +
                "LOCATION:%s\n" +
                "%s" + // Extra stuff
                "END:VEVENT\n";
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
        date = start;
        String startStr = String.format("%sT%sZ", date.format(formatBig), date.format(formatSmol));
        date = date.plusSeconds(endOffset.getSeconds());
        String endStr = String.format("%sT%sZ", date.format(formatBig), date.format(formatSmol));
        // Repeats
        StringBuilder repeatBuilder = new StringBuilder();
        if (repeatData.getRepeatCycle() != CalendarData.RepeatData.RepeatCycle.NONE) {
            repeatBuilder.append("RRULE:FREQ=").append(repeatData.getRepeatCycle());
            int mask = repeatData.getWeekdayMask();
            if (repeatData.getRepeatCycle() == CalendarData.RepeatData.RepeatCycle.WEEKLY && mask != 0b1111111) {
                repeatBuilder.append(";BYDAY=");
                List<String> days = getDays(mask);
                for (int i = 0; i < days.size() - 1; i++) {
                    repeatBuilder.append(String.format("%s,", days.get(i)));
                }
                repeatBuilder.append(String.format("%s", days.getLast()));
            }
            if (repeatData.getRepeatEvery() > 1) repeatBuilder.append(";INTERVAL=").append(repeatData.getRepeatEvery());
            switch (repeatData.getRepeatEnd()) {
                case AFTER -> repeatBuilder.append(";COUNT=").append(repeatData.getRepeatAfter());
                case ON -> {
                    date = repeatData.getRepeatOn();
                    String untilStr = date.format(formatBig);
                    repeatBuilder.append(";UNTIL=").append(untilStr);
                }
            }
            repeatBuilder.append("\n");
        }
        return String.format(icsBodyPreformatted,
                hashText, // UID
                createStr, // Create
                startStr, // Start
                endStr, // End
                loadedEntry.getTitle(), // Summary/title
                loadedEntry.getDescription(), // Desc
                loadedEntry.getLocation(), // Locat
                repeatBuilder
        );
    }

    private static List<String> getDays(int mask) {
        List<String> days = new ArrayList<>();
        if ((mask & CalendarData.RepeatData.MONDAY) > 0) {
            days.add("MO");
        }
        if ((mask & CalendarData.RepeatData.TUESDAY) > 0) {
            days.add("TU");
        }
        if ((mask & CalendarData.RepeatData.WEDNESDAY) > 0) {
            days.add("WE");
        }
        if ((mask & CalendarData.RepeatData.THURSDAY) > 0) {
            days.add("TH");
        }
        if ((mask & CalendarData.RepeatData.FRIDAY) > 0) {
            days.add("FR");
        }
        if ((mask & CalendarData.RepeatData.SATURDAY) > 0) {
            days.add("SA");
        }
        if ((mask & CalendarData.RepeatData.SUNDAY) > 0) {
            days.add("SU");
        }
        return days;
    }
}
