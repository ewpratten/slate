package slate.prototypes;

import slate.Room;

/**
 * To do anything in the current room:
 * Navigator.getInstance().getCurrentRoom().whatever
 * 
 * To move to a new room
 * Navigator.getInstance().moveTo(Navigator.getInstance().getCurrentRoom().getRoom("Name
 * of room from user"))
 */
public interface INavigator {

    public Room getCurrentRoom();

    public void moveTo(Room room);

}