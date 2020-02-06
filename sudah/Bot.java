package za.co.entelect.challenge;

import za.co.entelect.challenge.entities.Building;
import za.co.entelect.challenge.entities.CellStateContainer;
import za.co.entelect.challenge.entities.GameState;
import za.co.entelect.challenge.enums.BuildingType;
import za.co.entelect.challenge.enums.PlayerType;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Bot {

    private GameState gameState;

    /**
     * Constructor
     *
     * @param gameState the game state
     **/
    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameState.getGameMap();
    }

    /**
     * Run
     *
     * @return the result
     **/
    public String run() {

        //Jika jumlah Bangunan attak musuh lebih besar dari 2 x Bangunan defense kita dalam satu baris, dan ada lahan kosong mulai dari column >=3 maka akan dibangun bangunan defense
        // Jika lebih dari 1 garis yang memenuhi kondisi tersebut, digunakan algoritma greedy berdasarkan selisih paling tinggi
        int Optimum = 0;
        int index =-999;
        int Column = 10;
        for (int i = 0; i < gameState.getGameDetails().mapHeight; i++) {
            int enemyAttackOnRow = getAllBuildingsForPlayeronRowy(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
            int myDefenseOnRow = getAllBuildingsForPlayeronRowy(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();

            if ((enemyAttackOnRow - 2*myDefenseOnRow )>Optimum && canAffordBuilding(BuildingType.DEFENSE)) {
                if (getXfromPlace(i, false)>=3&&getXfromPlace(i, false)!=10) {
                    index = i;
                    Optimum = enemyAttackOnRow - 2 * myDefenseOnRow;
                }
            }
        }

        if (index!=-999){
            return placeDefenseBuilding(index);
        }else{
            //Akan diatur sedemikian rupa sehingga dalam permainan , player memiliki jumlah bangunan energi tepat 8 buah
            int allenergy = getAllBuildingsForPlayer(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY).size();
            if (allenergy<8){
                //Jika ada satu baris yang tidak ada bangunan serang musuh atau jumlah bangunan bertahan + 2 x bangunan serang  lebih besar dari 2 dan 2 x bangunan attack musuh maka akan dibangun bangunan energi
                //Jika lebih dari satu baris yang memenuhi kondidi tersebut maka akan dipilih secara greedy baris dengan peletakkan column paling belakang
                for (int i = gameState.getGameDetails().mapHeight-1; i >=0 ; i--) {
                    int enemyAttackOnRow = getAllBuildingsForPlayeronRowy(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                    int myEnergyOnRow = getAllBuildingsForPlayeronRowy(PlayerType.A, b -> b.buildingType == BuildingType.ENERGY, i).size();
                    int myDefenseOnRow = getAllBuildingsForPlayeronRowy(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                    int myAttackOnRow = getAllBuildingsForPlayeronRowy(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();

                    if (canAffordBuilding(BuildingType.ENERGY)&&((enemyAttackOnRow == 0 && myEnergyOnRow == 0) ||((myDefenseOnRow+myAttackOnRow >= 2*enemyAttackOnRow)&& myDefenseOnRow+myAttackOnRow >2 && myEnergyOnRow <2))){
                        if(getXfromPlace(i,true)<Column){
                            Column = getXfromPlace(i,true);
                            index = i;
                        }
                    }
                }
            }
        }

        if(index != -999){
            return placeEnergyBuilding(Column, index);
        }
        else{
            //Jika 3 x bangunan attack  kita lebih besar dari 2 x bangunan attack + bangunan defense musuh dan pada baris tersebut bangunan attack kita jumlahnya dibawah 4, maka akan ditambah bangunan attack
            //Jika terdapat lebih dari satu baris yang sama, maka akan dipilih secara greedy baris dengan keunggulan serangan paling tinggi
            for (int i = 0; i < gameState.getGameDetails().mapHeight; i++) {
                int enemyAttackOnRow = getAllBuildingsForPlayeronRowy(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int myAttackOnRow = getAllBuildingsForPlayeronRowy(PlayerType.A, b -> b.buildingType == BuildingType.ATTACK, i).size();
                int DefenseOnRow = getAllBuildingsForPlayeronRowy(PlayerType.B, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                int myDefenseOnRow = getAllBuildingsForPlayeronRowy(PlayerType.A, b -> b.buildingType == BuildingType.DEFENSE, i).size();
                //int enemyEnergyOnRow = getAllBuildingsForPlayeronRowy(PlayerType.B, b -> b.buildingType == BuildingType.ATTACK, i).size();
                if (canAffordBuilding(BuildingType.ATTACK)&& ((3*myAttackOnRow)-2*enemyAttackOnRow+DefenseOnRow>=Optimum) &&myAttackOnRow<4) {
                    index = i;
                    Optimum = (3*myAttackOnRow)-2*enemyAttackOnRow+DefenseOnRow+1;
                }
            }
        }

        if (index!=-999){
            return placeAttackBuilding(index);
        }

        return "";
    }

    /**
     * Place building in a random row nearest to the back
     *
     * @param buildingType the building type
     * @return the result
     **/


    /**
     * Place building in a random row nearest to the front
     *
     * @param buildingType the building type
     * @return the result
     **/


    /**
     * Place building in row y nearest to the front
     *
     * @param buildingType the building type
     * @param y            the y
     * @return the result
     **/
    private String placeDefenseBuilding(int y) {
        //Menggunakan konsep greedy untuk sebisa mungkin menempatkan di column paling dekat dengan lawan
        for (int i = (gameState.getGameDetails().mapWidth / 2) - 1; i >= 0; i--) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, BuildingType.DEFENSE);
            }
        }
        return "";
    }

    /**
     * Place building in row y nearest to the back
     *
     * @param buildingType the building type
     * @param y            the y
     * @return the result
     **/
    private String placeEnergyBuilding( int x, int y) {
        return buildCommand(x, y, BuildingType.ENERGY);
    }
    private String placeAttackBuilding(int y) {
        //Menggunakan konsep greedy untuk sebisa mungkin menempatkan di column dengan prioritas column terurut : 2, 3, 4,5,6,7,0,1
        for (int i = 2; i < gameState.getGameDetails().mapWidth / 2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, BuildingType.ATTACK);
            }
        }
        for (int i = 0; i <  2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, BuildingType.ATTACK);
            }
        }
        return "";
    }

    private int getXfromPlace(int y, Boolean asc){
        if (asc){
            for (int i = 0; i < gameState.getGameDetails().mapWidth / 2; i++) {
                if (isCellEmpty(i, y)) {
                    return i;
                }
            }
        }
        else{
            for (int i = gameState.getGameDetails().mapWidth / 2 -1; i >= 0; i--) {
                if (isCellEmpty(i, y)) {
                    return i;
                }
            }
        }
        return 10;
    }


    /**
     * Construct build command
     *
     * @param x            the x
     * @param y            the y
     * @param buildingType the building type
     * @return the result
     **/
    private String buildCommand(int x, int y, BuildingType buildingType) {
        return String.format("%s,%d,%s", String.valueOf(x), y, buildingType.getType());
    }

    /**
     * Get all buildings for player in row y
     *
     * @param playerType the player type
     * @param filter     the filter
     * @param y          the y
     * @return the result
     **/
    private List<Building> getAllBuildingsForPlayeronRowy(PlayerType playerType, Predicate<Building> filter, int y) {
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType && c.y == y)
                .flatMap(c -> c.getBuildings().stream())
                .filter(filter)
                .collect(Collectors.toList());
    }
    private List<Building> getAllBuildingsForPlayer(PlayerType playerType, Predicate<Building> filter) {
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType)
                .flatMap(c -> c.getBuildings().stream())
                .filter(filter)
                .collect(Collectors.toList());
    }






    /**
     * Get all empty cells for column x
     *
     * @param x the x
     * @return the result
     **/
    private List<CellStateContainer> getListOfEmptyCellsForColumn(int x) {
        return gameState.getGameMap().stream()
                .filter(c -> c.x == x && isCellEmpty(x, c.y))
                .collect(Collectors.toList());
    }

    /**
     * Checks if cell at x,y is empty
     *
     * @param x the x
     * @param y the y
     * @return the result
     **/
    private boolean isCellEmpty(int x, int y) {
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();

        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        } else {
            System.out.println("Invalid cell selected");
        }
        return true;
    }

    /**
     * Checks if building can be afforded
     *
     * @param buildingType the building type
     * @return the result
     **/
    private boolean canAffordBuilding(BuildingType buildingType) {
        return getEnergy(PlayerType.A) >= getPriceForBuilding(buildingType);
    }

    /**
     * Gets energy for player type
     *
     * @param playerType the player type
     * @return the result
     **/
    private int getEnergy(PlayerType playerType) {
        return gameState.getPlayers().stream()
                .filter(p -> p.playerType == playerType)
                .mapToInt(p -> p.energy)
                .sum();
    }

    /**
     * Gets price for building type
     *
     * @param buildingType the player type
     * @return the result
     **/
    private int getPriceForBuilding(BuildingType buildingType) {
        return gameState.getGameDetails().buildingsStats.get(buildingType).price;
    }

    /**
     * Gets price for most expensive building type
     *
     * @return the result
     **/
    private int getMostExpensiveBuildingPrice() {
        return gameState.getGameDetails().buildingsStats
                .values().stream()
                .mapToInt(b -> b.price)
                .max()
                .orElse(0);
    }
}
