package hive.ivangeevo.mindsigner;
public class CSEventMessage {
    private String eventName;
    private String eventData;

    public CSEventMessage(String eventName, String eventData) {
        this.eventName = eventName;
        this.eventData = eventData;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventData() {
        return eventData;
    }

    public String getEvent() {
        return null;
    }
}