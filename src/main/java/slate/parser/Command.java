package slate.parser;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.v4.runtime.ParserRuleContext;

import slate.App;
import slate.Guard;
import slate.Inventory;
import slate.bases.RoomBase;
import slate.exceptions.ItemNotFoundException;
import slate.exceptions.ItemSizeException;

//Command Object
public class Command{

    //Reading in Command Usage Data
    static File file;
    static byte[] data;
    static String[] usageContent;
    static HashMap<Integer, String> usagesMap = new HashMap<Integer, String>();
    public App game;

    static {
        try {
            //Get file from res folder
            file = new File(Commands.class.getResource("../../commands/CommandUsage.slateinfo").toURI());

            //Place all bytes into array
            data = Files.readAllBytes(file.toPath());

            //Create an array of lines from byte array
            usageContent = new String(data, "UTF-8").split("\\r?\\n");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        //Usage information for each command
        usagesMap.put(SlateParser.HELP, usageContent[3]);
        usagesMap.put(SlateParser.SAY, usageContent[6]);
        usagesMap.put(SlateParser.SHOUT, usageContent[9]);
        usagesMap.put(SlateParser.PICKUP, usageContent[12]);
        usagesMap.put(SlateParser.LEAVE, usageContent[17]);
        usagesMap.put(SlateParser.CHECKDOORS, usageContent[21]);
        usagesMap.put(SlateParser.PEEK, usageContent[23]);
        usagesMap.put(SlateParser.MOVE, usageContent[26]);
        usagesMap.put(SlateParser.WAIT, usageContent[29]);
        usagesMap.put(SlateParser.SEARCH, usageContent[32]);
        usagesMap.put(SlateParser.OPEN, usageContent[35]);
        usagesMap.put(SlateParser.CLOSE, usageContent[38]);
        usagesMap.put(SlateParser.EXIT,  usageContent[41]);
    }

    int type;
    private ParserRuleContext context;
    SlateParser parser;
    SlateBaseVisitor visitor = new SlateBaseVisitor();

    //Constructor
    Command(int type,  ParserRuleContext context, SlateParser parser) {
        this.type = type;
        this.context = context;
        this.parser = parser;
    }

    //Validate User Input
    public boolean validate(){

        //If Valid Command, check syntax
        if(context!=null) {
            visitor.visit(context);

            //Returns true if no errors found
            if (context.exception == null){
                return true;
            }

            //Display Error Message
            printUsage();
            return false;
        }

        //Display Error Message
        System.out.println("Hmm... I don't know how to do that. For a list of commands and their usages, I should type help.");
        return false;
    }

    public void execute(){

        //Link types to commands
        HashMap<Integer, CommInterface> commandMap = new HashMap<Integer, CommInterface>();

        commandMap.put(SlateParser.PICKUP, new PickupCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.LEAVE, new LeaveCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.SAY, new SayCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.SHOUT, new ShoutCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.MOVE, new MoveCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.PEEK, new PeekCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.OPEN, new OpenCommand((context.getToken(SlateParser.TEXT,0))!=null?(context.getToken(SlateParser.TEXT,0)).getText():null));
        commandMap.put(SlateParser.WAIT, new WaitCommand());
        commandMap.put(SlateParser.CHECKDOORS, new CheckDoorsCommand());
        commandMap.put(SlateParser.SEARCH, new SearchCommand());
        commandMap.put(SlateParser.CLOSE, new CloseCommand());
        commandMap.put(SlateParser.HELP, new HelpCommand());
        commandMap.put(SlateParser.EXIT, new ExitCommand());

        //Execute proper command
        CommInterface command = commandMap.get(type);
        command.execute();
    }

    //Prints usage data for current command
    void printUsage(){
        System.out.printf("Bad Syntax, %s\n", usagesMap.get(type));
    }

    //Get command context
    public ParserRuleContext getContext(){
        return context;
    }

    //Command Interface
    interface CommInterface{

        //All Commands Must have an Execute Method
        public void execute();
    }

    //SAY
    class SayCommand implements CommInterface{

        String data;

        SayCommand(String data){
            this.data = data;
        }

        @Override
        public void execute(){

            //Print message
            System.out.println(data);
        }
    }

    //SHOUT
    class ShoutCommand implements CommInterface{

        String data;

        ShoutCommand(String data){
            this.data = data;
        }

        @Override
        public void execute(){

            //Print message
            System.out.println((data).toUpperCase());
        }
    }

    //PICK UP ITEM
    class PickupCommand implements CommInterface{

        String data;

        PickupCommand(String data){
            this.data = data;
        }

        @Override
        public void execute() {

            //If current active inventory is not player's pockets
            if (game.player.getFocused_inventory()!=game.player.getInventory()) {

                //Get item data in current active inventory
                Inventory.Stack[] itemInfo = game.player.getFocused_inventory().getAllNames();
                String itemName = data.substring(data.indexOf(" ") + 1);

                //Get number of items to take from optional player input
                int quantity;
                try {
                    quantity = Integer.parseInt(data.substring(0, data.indexOf(" ")));
                } catch (NumberFormatException e) {
                    quantity = 1;
                    itemName = data;
                }

                //Find item match
                for (Inventory.Stack item : itemInfo) {
                    if (item.name.equalsIgnoreCase(itemName)) {

                        /*Transfer items
                        Keep track of how many are taken*/
                        int numTaken = 0;
                        for (int i = 0; i < quantity; i++) {

                            //If any of this item are remaining in the container
                            if (item.count > 0) {
                                try {

                                    //Add to player's pockets
                                    game.player.getInventory().addItem(item.name, game.player.getFocused_inventory().getItem(item.name));

                                //If pockets become full
                                } catch (ItemSizeException e) {

                                    //If some were taken
                                    if(numTaken>0){
                                        System.out.println("There isn't enough room in my pockets, so I take " + numTaken + " instead.");
                                        return;
                                    }

                                    //If none were taken, because pockets were too full
                                    System.out.println("There's no space left in my pockets!");
                                } catch (ItemNotFoundException e) {
                                    e.printStackTrace();
                                }

                                //Remove from current active inventory
                                item.count -= 1;
                                numTaken++;
                            } else {

                                //Some items were taken, so print out number taken
                                System.out.println("There isn't enough of these, so I take " + numTaken + " instead.");
                                return;
                            }
                        }

                        //Best case scenario, all desired items are taken
                        System.out.println("I take " + quantity + " " + itemName + ".");
                        return;
                    }
                }

                //Desired item type does not exist
                System.out.println("I can't find any of those in here...");
                return;
            }

            //Tried to take an item from pockets
            System.out.println("I can't take things from myself!");
        }
    }

    //LEAVE ITEM
    class LeaveCommand implements CommInterface{

        String data;

        LeaveCommand(String data){
            this.data = data;
        }

        @Override
        public void execute() {

            //Targeted inventory is the room's root inventory by default
            Inventory target = game.current_map.nav.getCurrentRoom().getRoot_inventory();

            //If current active inventory is not the player's pockets, set it as the target
            if (game.player.getFocused_inventory()!=game.player.getInventory()) {
                target = game.player.getFocused_inventory();
            }

            //Get item info in player's pockets
            Inventory.Stack[] itemInfo = game.player.getInventory().getAllNames();
            String itemName = data.substring(data.indexOf(" ") + 1);

            //Get number of items to take from optional player input
            int quantity;
            try {
                quantity = Integer.parseInt(data.substring(0, data.indexOf(" ")));
            } catch (NumberFormatException e) {
                quantity = 1;
                itemName = data;
            }

            //Find item match
            for (Inventory.Stack item : itemInfo) {
                if (item.name.equalsIgnoreCase(itemName)) {

                    //Keep track of number of items put in
                    int numPut = 0;
                    for (int i = 0; i < quantity; i++) {

                        //If player has any more of the item
                        if (item.count > 0) {

                            try {

                                //Put item into targeted inventory
                                target.addItem(item.name, game.player.getInventory().getItem(item.name));

                            //If container full
                            } catch (ItemSizeException e) {

                                //If some items fit in
                                if(numPut>0){
                                    System.out.println("There's not enough space inside the " + target.getName() +", so I leave "+ numPut +" instead.");
                                    return;
                                }

                                //If no items fit in
                                System.out.println("There's no space inside the " + target.getName() +" to put this...");
                                return;
                            } catch (ItemNotFoundException e) {
                                e.printStackTrace();
                            }

                            //Remove from pockets
                            item.count -= 1;
                            numPut++;
                        } else {

                            //Player was only able to put some of the desired item into the container
                            System.out.println("I don't have enough of these, so I leave " + numPut + " instead.");
                            return;
                        }
                    }

                    //Best case scenario, player places the desired quantity into the target successfully
                    System.out.println("I leave " + quantity + " " + itemName + ".");
                    return;
                }
            }

            //Player does not have any of this item
            System.out.println("I can't find any of those in my pockets...");
        }
    }

    //PRINT HELP
    class HelpCommand implements CommInterface{

        @Override
        public void execute(){

            //Print help, line by line
           for(int i = 0; i < usageContent.length; i++){
               System.out.println(usageContent[i]);
           }
        }
    }

    //SEARCH
    class SearchCommand implements CommInterface{

        @Override
        public void execute(){

            //Search Room
            if(game.player.getFocused_inventory() == game.current_map.nav.getCurrentRoom().getRoot_inventory()) {

                //Get Loose Items
                Inventory.Stack[] items = game.player.getFocused_inventory().getAllNames();
                if (items.length > 0) {
                    System.out.printf("After a quick search of the room, I find the following %d item%s:\n", items.length, items.length > 1 ? "s" : "");
                    for (Inventory.Stack item : items) {
                        System.out.println(String.format("- %s x%d", item.name, item.count));
                    }
                }else{
                    System.out.println("I can't find any loose items here.");
                }

                ArrayList<Inventory> inventories = game.current_map.nav.getCurrentRoom().getInventories();
                //Get Open Inventories
                if (inventories.size() > 0) {
                    System.out.printf("I find %d container%s:\n", inventories.size(), inventories.size() > 1 ? "s" : "");
                    for (Inventory inv : inventories) {
                        System.out.println("- " + inv.getName());
                    }
                }
            }else{
                //Search container

                //Get Loose Items
                Inventory.Stack[] items = game.player.getFocused_inventory().getAllNames();
                if (items.length > 0) {
                    System.out.printf("Rummaging through the %s, I find the following %d item%s:\n", game.player.getFocused_inventory().getName(), items.length, items.length > 1 ? "s" : "");
                    for (Inventory.Stack item : items) {
                        System.out.println(String.format("- %s x%d", item.name, item.count));
                    }
                }
            }
        }
    }

    //MOVE
    class MoveCommand implements CommInterface{

        String data;

        MoveCommand(String data){
            this.data = data;
        }

        @Override
        public void execute(){

            //Check for room
            for(RoomBase r: game.current_map.nav.getCurrentRoom().getAttached_rooms()){

                //Go to correct room
                if(r.getName().equalsIgnoreCase(data)) {
                    game.current_map.nav.moveTo(r);
                    System.out.println("I move into the " + r.getName());
                    System.out.println(r.getRoomInfo());

                    //Set focused inventory to room root
                    game.player.setFocused_inventory(r.getRoot_inventory());

                    //Move Guards
                    for (RoomBase room : game.current_map.getAllRooms()) {
                        for (Guard g : room.getGuards()) {
                            g.patrol();
                        }
                    }
                    return;
                }
            }

            //Room not found
            System.out.println("There's no access to that room from here...");
        }
    }

    //PEEK
    class PeekCommand implements CommInterface{

        String data;

        PeekCommand(String data){
            this.data = data;
        }

        @Override
        public void execute(){

            //Check for room
            for(RoomBase r: game.current_map.nav.getCurrentRoom().getAttached_rooms()){

                //Peek correct room
                if(r.getName().equalsIgnoreCase(data))System.out.println(r.getPeekInfo());

                //Check for guards
                if(r.getGuards().size()>0){
                    System.out.println(String.format("There %s %d guard%s in there!", (r.getGuards().size()>1)?"are":"is", r.getGuards().size(), (r.getGuards().size()>1)?"s":""));
                    return;
                }

                System.out.println("Looks all clear...");
                return;
            }

            //Room not found
            System.out.println("I can't see that room from here...");
        }
    }

    //WAIT
    class WaitCommand implements CommInterface{

        @Override
        public void execute(){

            //Move Guards
            System.out.println("I'll wait here for now...");
            for(RoomBase r: game.current_map.getAllRooms()){
                for(Guard g: r.getGuards()){
                    g.patrol();
                }
            }
        }
    }

    //CHECK DOORS
    class CheckDoorsCommand implements CommInterface{

        @Override
        public void execute(){

            //Get possible paths
            ArrayList<RoomBase> rooms = game.current_map.nav.getCurrentRoom().getAttached_rooms();

           //Check for rooms attached to current room
            System.out.printf("I look around the room and see %d door%s:\n", rooms.size(), rooms.size()>1?"s":"");
            for(RoomBase r: rooms) {
                System.out.println("- " + r.getName());
            }
         }
    }

    //OPEN INVENTORY
    class OpenCommand implements CommInterface{
        String data;

        OpenCommand(String data){
            this.data = data;
        }

        @Override
        public void execute(){

            //List of inventories in the room
            ArrayList<Inventory> inventories = game.current_map.nav.getCurrentRoom().getInventories();

            //Check if input matches
            for(Inventory inventory : inventories){
                if(inventory.getName().equalsIgnoreCase(data)){

                    //Set focus to targeted inventory
                    System.out.println("I open up the " + inventory.getName());
                    game.player.setFocused_inventory(inventory);
                    return;
                }
            }

            //Player's pockets
            if(data.equalsIgnoreCase("Pockets")){

                //Set focus to targeted inventory
                System.out.println("I open up my pockets.");
                game.player.setFocused_inventory(game.player.getInventory());
                return;
            }

            //Container not found
            System.out.println("I can't find that container, maybe I just imagined it...");
        }
    }

    //CLOSE INVENTORY
    class CloseCommand implements CommInterface{

        @Override
        public void execute(){

            //Only close if not in room root
            if(game.player.getFocused_inventory()!=game.current_map.nav.getCurrentRoom().getRoot_inventory()) {

                //Close Focused Inventory
                System.out.println("I close the " + game.player.getFocused_inventory().getName());

                //Reopen Room Root
                game.player.setFocused_inventory(game.current_map.nav.getCurrentRoom().getRoot_inventory());
                return;
            }

            //In room root
            System.out.println("I haven't left anything open...");
        }
    }

    //EXIT GAME
    class ExitCommand implements CommInterface{

        @Override
        public void execute(){

            //Exit
            System.out.println("You are a terrible person.");
            System.exit(0);
        }
    }
}
