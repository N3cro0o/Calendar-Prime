import org.antlr.v4.runtime.misc.Pair;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class CalendarData implements Serializable {
    public long ID;
    private String title;
    private String description;
    private String location;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean allDay;
    private RepeatData repeat;

    public CalendarData(long id){
        ID = id;
        title = "lorem ipsum";
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.";
        location = "Lorem ipsum dolor sit amet";
        start = LocalDateTime.now();
        end = LocalDateTime.now();
        allDay = false;
        repeat = new RepeatData();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public boolean isAllDay() {
        return allDay;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public RepeatData getRepeat() {
        return repeat;
    }

    public void setRepeat(RepeatData repeat) {
        this.repeat = repeat;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, title: %s, description: %s, location: %s, start: %s, end: %s, all day: %b, repeat: %s",
                ID, title, description, location, start, end, allDay, repeat);
    }

    static class RepeatData implements Serializable {
        static final int MONDAY = 0b0000001;
        static final int TUESDAY = 0b0000010;
        static final int WEDNESDAY = 0b0000100;
        static final int THURSDAY = 0b0001000;
        static final int FRIDAY = 0b0010000;
        static final int SATURDAY = 0b0100000;
        static final int SUNDAY = 0b1000000;

        private RepeatCycle repeatCycle;
        private Long repeatEvery; // Interval IG too lazy to change name
        private int weekday = 0b1111111;
        private RepeatEnd repeatEnd;
        private Long repeatAfter;
        private LocalDateTime repeatOn;
        private ArrayList<Pair<LocalDateTime, LocalDateTime>> withoutPairArray;

        public RepeatData() {
            repeatCycle = RepeatCycle.NONE;
            repeatEvery = 1L;
            repeatEnd = RepeatEnd.INF;
            repeatAfter = 0L;
            repeatOn = LocalDateTime.now();
            withoutPairArray = new ArrayList<>();
        }

        public RepeatCycle getRepeatCycle() {
            return repeatCycle;
        }

        public void setRepeatCycle(RepeatCycle repeatCycle) {
            this.repeatCycle = repeatCycle;
            this.weekday = 0b1111111;
        }

        public int getWeekdayMask() {
            return this.weekday;
        }

        public void setWeekdayMask(int weekdayMask) {
            this.weekday = weekdayMask;
        }

        public Long getRepeatEvery() {
            return repeatEvery;
        }

        public void setRepeatEvery(Long repeatEvery) {
            if (repeatEvery > 0) {
                this.repeatEvery = repeatEvery;
            }
        }

        public RepeatEnd getRepeatEnd() {
            return repeatEnd;
        }

        public void setRepeatEnd(RepeatEnd repeatEnd) {
            this.repeatEnd = repeatEnd;
        }

        public Long getRepeatAfter() {
            return repeatAfter;
        }

        public void setRepeatAfter(Long repeatAfter) {
            this.repeatAfter = repeatAfter;
        }

        public LocalDateTime getRepeatOn() {
            return repeatOn;
        }

        public void setRepeatOn(LocalDateTime repeatOn) {
            this.repeatOn = repeatOn;
        }

        public ArrayList<Pair<LocalDateTime, LocalDateTime>> getWithoutArray() {
            return withoutPairArray;
        }

        public void pushWithoutPair(LocalDateTime from, LocalDateTime to) {
            this.withoutPairArray.add(new Pair<>(from, to));
        }

        public void resetWithoutArray() {
            withoutPairArray.clear();
        }

        private String withoutToString() {
            StringBuilder str = new StringBuilder("[");
            for (Pair<LocalDateTime, LocalDateTime> pair : withoutPairArray) {
                str.append(String.format("|from: %s to: %s|", pair.a, pair.b));
            }
            str.append("]");
            return str.toString();
        }


        enum RepeatCycle { // I miss Rust enums, they are so goated...
            NONE,
            DAILY,
            WEEKLY,
            MONTHLY,
            YEARLY
        }

        enum RepeatEnd {
            INF,
            AFTER,
            ON
        }

        @Override
        public String toString() {
            return String.format("[cycle: %s (%s), every: %d, end: %s, after: %s, on: %s, without: %s]",
                repeatCycle, Integer.toBinaryString(weekday), repeatEvery, repeatEnd, repeatAfter, repeatOn, withoutToString());
        }
    }
}
