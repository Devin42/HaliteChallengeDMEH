
import hlt.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class MyBot {

    public static void main(final String[] args) {
        final Networking networking = new Networking();
        final GameMap gameMap = networking.initialize("Tamagocchi");

        // We now have 1 full minute to analyse the initial map.
        final String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        final ArrayList<Move> moveList = new ArrayList<>();
        for (;;) {
            boolean allColonized = false;
            
            moveList.clear();
            networking.updateMap(gameMap);

            for (final Ship ship :  gameMap.getMyPlayer().getShips().values()) {
                if (ship.getDockingStatus() != Ship.DockingStatus.Undocked) {
                    continue;
                }
                
                Map<Double, Entity> entities_by_distance = gameMap.nearbyEntitiesByDistance(ship);
                Planet nearest_planet = null;
                for(Entity entity : entities_by_distance.values()) {
                    if(entity instanceof Planet) {
                        nearest_planet = (Planet) entity;
                        if(nearest_planet.isOwned() && nearest_planet.getOwner() != gameMap.getMyPlayer().getId()){
                            continue;
                        }
                        else if(nearest_planet.getOwner() == gameMap.getMyPlayer().getId() && (nearest_planet.getDockedShips().size() > 5 || nearest_planet.getDockingSpots()-nearest_planet.getDockedShips().size() == 0)) {
                            continue;
                        }
                        else {
                            break;
                        }
                    }
                }

                if (ship.canDock(nearest_planet)) {
                    moveList.add(new DockMove(ship, nearest_planet));
                    continue;
                }


                final ThrustMove newThrustMove = Navigation.navigateShipToDock(gameMap, ship, nearest_planet, Constants.MAX_SPEED);
                if (newThrustMove != null) {
                    moveList.add(newThrustMove);
                }

                continue;
            }
            
            Networking.sendMoves(moveList);
        }
    }
}
