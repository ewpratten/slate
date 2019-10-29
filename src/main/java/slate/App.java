/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package slate;

import slate.bases.MapBase;
import slate.maps.GameMap;
import slate.parser.Command;

import static slate.parser.Commands.getInput;

public class App {

    //Player
    public Player player = Player.getInstance();

    // Define the map to use
    public MapBase current_map = new GameMap(this);

    public static void main(String[] args) {
        new App();
    }

    App() {

        //Print header at game start
        System.out.println(" _____ _       ___ _____ _____ \n" +
                "/  ___| |     / _ \\_   _|  ___|\n" +
                "\\ `--.| |    / /_\\ \\| | | |__  \n" +
                " `--. \\ |    |  _  || | |  __| \n" +
                "/\\__/ / |____| | | || | | |___ \n" +
                "\\____/\\_____/\\_| |_/\\_/ \\____/ \n" +
                "-------------------------------------------------------");

        // Pint the map introduction text
        System.out.println(current_map.getDescription());

        //Set starting inventory
        player.setFocusedInventory(current_map.nav.getCurrentRoom().getRoot_inventory());

        while (true) {
            Command[] comms = getInput();
            for (Command comm : comms) {
                comm.game = this;

                //If command is valid, execute it.
                if (comm.validate()) {
                    comm.execute();
                }else{
                    break;
                }

                //Handle running into guards
                if(current_map.nav.getCurrentRoom().getGuards().size()>0){
                    System.out.println("I ran into a guard, I'm lucky nobody has added any penalties to the game yet.");
                }

                //Win Game
                if(current_map.nav.getCurrentRoom().equals(current_map.nav.getDefaultRoom())&&player.getInventory().getStorage().containsKey("Artifact")){
                    System.out.println("I won!");
                }
            }
        }
    }

    public void moveGuards(){
        int moved = 0;
        for(Room r: current_map.getAllRooms()){
            for(Guard g: r.getGuards()){
                moved += g.patrol();
            }
            r.getGuards().removeAll(r.getMovedGuards());
        }

        if(player.getInventory().getStorage().containsKey("Guard Radio")&&moved>0){
            System.out.println(String.format("My radio alerts me, telling me %d guard%s have moved position.", moved, moved>1?"s":""));
        }
    }
}
