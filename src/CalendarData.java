import java.io.Serializable;
import java.time.LocalDateTime;

public class CalendarData implements Serializable {
    public int ID;
    private String title;
    private String description;
    private String location;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean allDay;
    private RepeatData repeat;

    //DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

    public CalendarData(int id){
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
        private RepeatCycle repeatCycle;
        private Integer repeatEvery;
        private RepeatEnd repeatEnd;
        private Integer repeatAfter;
        private LocalDateTime repeatOn;

        public RepeatData() {
            repeatCycle = RepeatCycle.NONE;
            repeatEvery = 1;
            repeatEnd = RepeatEnd.INF;
            repeatAfter = 0;
            repeatOn = LocalDateTime.now();
        }

        public RepeatCycle getRepeatCycle() {
            return repeatCycle;
        }

        public void setRepeatCycle(RepeatCycle repeatCycle) {
            this.repeatCycle = repeatCycle;
        }

        public Integer getRepeatEvery() {
            return repeatEvery;
        }

        public void setRepeatEvery(Integer repeatEvery) {
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

        public Integer getRepeatAfter() {
            return repeatAfter;
        }

        public void setRepeatAfter(Integer repeatAfter) {
            this.repeatAfter = repeatAfter;
        }

        public LocalDateTime getRepeatOn() {
            return repeatOn;
        }

        public void setRepeatOn(LocalDateTime repeatOn) {
            this.repeatOn = repeatOn;
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
            return String.format("[cycle: %s, every: %d, end: %s, after: %s, on: %s]",
                repeatCycle, repeatEvery, repeatEnd, repeatAfter, repeatOn);
        }
    }
}
