package src.main;

import java.awt.Rectangle;

public class EventRect extends Rectangle{
    int eventRectDefaultX, eventRectDefaultY; // Default position of the event rectangle
    boolean eventDone = false; // To prevent multiple triggers
}
