package slate.bases;

import java.util.ArrayList;

public class RoomBase {
    ArrayList<ItemBase> items = new ArrayList<ItemBase>();
    protected ArrayList<RoomBase> attached_rooms = new ArrayList<RoomBase>();
    public boolean visited = false;

    protected String name, peek_info, room_info = "";

    public String getName() {
        return name;
    }

    public String getPeekInfo() {
        return peek_info;
    }

    public String getRoomInfo() {
        return room_info;
    }

    public RoomBase() {

    }

    /**
     * Copy a room
     * 
     * @param room
     */
    public RoomBase(RoomBase room) {
        items = new ArrayList<ItemBase>(room.items);
        attached_rooms = new ArrayList<RoomBase>(room.attached_rooms);
        visited = room.visited;
        name = room.name;
        peek_info = room.peek_info;
        room_info = room.room_info;

    }

    /**
     * Add a pathway attached to this room
     * 
     * @param room      New room
     */
    public void addPathway(RoomBase room) {
        // This will override pre-existing rooms
        attached_rooms.add(room);
    }

    /**
     * Add an item to the room
     * 
     * @param item Item to add
     */
    public void addItem(ItemBase item) {
        items.add(item);

    }

    /**
     * Get a list of all items in the room
     * 
     * @return List of all item names
     */
    public String[] getItemNames() {
        ArrayList<String> output = new ArrayList<String>();

        for (ItemBase item : items) {
            output.add(item.name);
        }

        return output.toArray(new String[output.size()]);
    }

    /*
    * Get attached rooms
     */
    public ArrayList<RoomBase> getAttached_rooms(){
        return attached_rooms;
    }

    public boolean equals(RoomBase room) {
        return room.getName() == getName() && room.getRoomInfo() == getRoomInfo() && room.getPeekInfo() == getPeekInfo()
                && attached_rooms.equals(room.attached_rooms) && items.equals(room.items) && visited == room.visited;
    }

}