public class CalendarFileHandler {
    public boolean isLoaded = false;

    public void newEmpty(){
        System.out.println("Generate new class here");
        isLoaded = true;
    }

    public void deleteEntry(){
        isLoaded = false;
    }
}
