/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package slate;

import slate.bases.MapBase;
import slate.maps.GameMap;
import slate.parser.Command;
import slate.parser.Commands;

import java.io.IOException;

import static slate.parser.Commands.getInput;

public class App{

    static boolean keepPlaying = true;
    public static boolean chemDeath = false;

    //Player
    public Player player = Player.getInstance();

    // Define the map to use
    public MapBase current_map = new GameMap(this);

    public static void main(String[] args) {
        while((new App()).run());
    }

    App() {
        chemDeath = false;
        player = Player.resetInstance();
        //Set starting inventory
        player.setFocusedInventory(current_map.nav.getCurrentRoom().getRoot_inventory());
    }

    public boolean run() {
        // Clear the screen
        clearScreen();

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
                if(current_map.nav.getCurrentRoom().getGuards().size()>0 && player.invisTurns==0){
                    System.out.println("I ran into a guard.");
                    if(gameOver(false)){
                        return true;
                    }
                    keepPlaying = false;
                    return false;
                }

                //Acid Death
                if(chemDeath){
                    System.out.println("Dead is me.");
                    if(gameOver(false)){
                        return true;
                    }
                    keepPlaying = false;
                    return false;
                }

                //Win Game
                if(current_map.nav.getCurrentRoom().equals(current_map.nav.getDefaultRoom())&&player.getInventory().getStorage().containsKey("Artifact")){
                    System.out.println("I actually stole the priceless artifact that would have saved the lives of thousands of people! I won!");
                    if(gameOver(true)){
                        return true;
                    }
                    keepPlaying = false;
                    return false;
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

    public static boolean gameOver(boolean won){
        if(!won) {
            System.out.println("GAME OVER");
        }
        System.out.println("Play Again? [Y/N]");
        String response = Commands.sc.nextLine();
        if((response.length()>0) && (Character.toUpperCase(response.charAt(0))=='Y')){
            clearScreen();
            return true;
        }

        return false;
    }

    public static void clearScreen(){
        try{
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        }catch(IOException e){
            System.out.print("\033[H\033[2J");
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
