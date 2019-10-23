package slate.bases;

import slate.Guard;
import slate.Inventory;
import slate.exceptions.ItemSizeException;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public class RoomBase {
    ArrayList<ItemBase> items = new ArrayList<ItemBase>();
    ArrayList<Inventory> inventories = new ArrayList<Inventory>();
    protected ArrayList<RoomBase> attached_rooms = new ArrayList<RoomBase>();
    public CopyOnWriteArrayList<Guard> guards = new CopyOnWriteArrayList<Guard>();
    public boolean visited;

    protected String name, peek_info, room_info = "";

    protected Inventory root_inventory = new Inventory("Room", 100);

    public int locks;

    public void unlock(){
        locks--;
    }

    public String getName() {
        return name;
    }

    public String getPeekInfo() {
        return peek_info;
    }

    public String getRoomInfo() {
        return room_info;
    }

    /**
     * Empty, optional constructor to be overridden
     */
    public RoomBase() {

    }

    /**
     * Copy a room
     * 
     * @param room
     */
    public RoomBase(RoomBase room) {
        root_inventory = room.root_inventory;
        items = room.items;
        inventories = room.inventories;
        guards = room.guards;
        attached_rooms = room.attached_rooms;
        visited = room.visited;
        name = room.name;
        peek_info = room.peek_info;
        room_info = room.room_info;

    }

    public void addPathway(RoomBase... rooms) {
        for (RoomBase room : rooms) {
            addPathway(room);

        }
    }

    /**
     * Add a pathway attached to this room
     * 
     * @param room New room
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
        try{
            root_inventory.addItem(item.getName(), item);
        } catch (ItemSizeException e) {
            System.out.println(e);
        }

    }

    /**
     * Spawn a guard into a room
     *
     * @param num Number to add
     */
    public void addGuards(int num) {
        for(int i = 0; i<num; i++) {
            this.guards.add(new Guard(this));
        }
    }

    /**
     * Get a list of names of all items in the room
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
    public ArrayList<RoomBase> getAttached_rooms() {
        return attached_rooms;
    }

    public boolean equals(RoomBase room) {
        return room.getName() == getName() && room.getRoomInfo() == getRoomInfo() && room.getPeekInfo() == getPeekInfo()
                && attached_rooms.equals(room.attached_rooms) && items.equals(room.items) && visited == room.visited;
    }

    /*
     * Get guards
     */
    public CopyOnWriteArrayList<Guard> getGuards(){
        return guards;
    }

    public Inventory getRoot_inventory() {
        return root_inventory;
    }

    /**
     * Get a list of inventories in the room
     *
     * @return List of all inventories
     */
    public ArrayList<Inventory> getInventories() {
        return inventories;
    }

    /**
     * Add an inventory to the room
     *
     * @param inventory Item to add
     */
    public void addInventory(Inventory inventory) {
        inventories.add(inventory);
    }

    
    /**
     * Set the room name, and get the room. This can be used as an optional, chainable, constructor.
     * 
     * @param name Room name
     * @return Current room
     */
    public RoomBase named(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the room name, and get the room. This can be used as an optional, chainable, constructor.
     *
     * @param num locks needed
     * @return Current room
     */
    public RoomBase withLocks(int num) {
        this.locks = num;
        return this;
    }

    public int getLocks() {
        return locks;
    }
}