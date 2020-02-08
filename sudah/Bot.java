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


    public Bot(GameState gameState) {
        this.gameState = gameState;
        gameState.getGameMap();
    }


    public String run() {

        //Jika jumlah Bangunan Attack musuh lebih besar dari 2 x Bangunan defense kita dalam satu baris, dan ada lahan kosong mulai dari column >=3 maka akan dibangun bangunan defense
        // Jika lebih dari 1 garis yang memenuhi kondisi tersebut, digunakan algoritma greedy berdasarkan selisih paling tinggi
        int Optimum = 0;
        int index = -999;
        int Column = 10;
        for (int i = 0; i < gameState.getGameDetails().mapHeight; i++) {
            int enemyAttackOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.B,BuildingType.ATTACK, i);
            int myDefenseOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.A, BuildingType.DEFENSE, i);

            if ((enemyAttackOnRow - 2 * myDefenseOnRow) > Optimum && canAffordBuilding(BuildingType.DEFENSE)) {
                if (getXfromPlace(i, false) >= 3 && getXfromPlace(i, false) != 10) {
                    index = i;
                    Optimum = enemyAttackOnRow - 2 * myDefenseOnRow;
                }
            }
        }

        if (index != -999) {
            return placeDefenseBuilding(index);
        } else {
            //Akan diatur sedemikian rupa sehingga dalam permainan , player memiliki jumlah bangunan energi tepat 8 buah
            int allMyEnergy = getAllBuildingsForPlayerSize(PlayerType.A, BuildingType.ENERGY);
            if (allMyEnergy < 8) {
                //Jika ada satu baris yang tidak ada bangunan Attack musuh atau jumlah bangunan Defense + 2 x bangunan Attack Player  lebih besar dari 2 dan lebih besar pula dari 2 x bangunan attack musuh maka akan dibangun bangunan energi
                //Jika lebih dari satu baris yang memenuhi kondidi tersebut maka akan dipilih secara greedy baris dengan peletakkan column paling belakang
                for (int i = gameState.getGameDetails().mapHeight - 1; i >= 0; i--) {
                    int enemyAttackOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.B, BuildingType.ATTACK, i);
                    int myEnergyOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.A,  BuildingType.ENERGY, i);
                    int myDefenseOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.A,  BuildingType.DEFENSE, i);
                    int myAttackOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.A,BuildingType.ATTACK, i);

                    if (canAffordBuilding(BuildingType.ENERGY) && ((enemyAttackOnRow == 0 && myEnergyOnRow == 0) || ((myDefenseOnRow + myAttackOnRow >= 2 * enemyAttackOnRow) && myDefenseOnRow + myAttackOnRow > 2 && myEnergyOnRow < 2))) {
                        if (getXfromPlace(i, true) < Column) {
                            Column = getXfromPlace(i, true);
                            index = i;
                        }
                    }
                }
            }
        }

        if (index != -999) {
            return placeEnergyBuilding(Column, index);
        } else {
            //Jika 3 x bangunan attack  kita lebih besar dari 2 x bangunan attack + bangunan defense musuh dan pada baris tersebut bangunan attack kita jumlahnya dibawah 4, maka akan ditambah bangunan attack
            //Jika terdapat lebih dari satu baris yang sama, maka akan dipilih secara greedy baris dengan keunggulan serangan paling tinggi
            for (int i = 0; i < gameState.getGameDetails().mapHeight; i++) {
                int enemyAttackOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.B,BuildingType.ATTACK, i);
                int myAttackOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.A,  BuildingType.ATTACK, i);
                int enemyDefenseOnRow = getAllBuildingsForPlayeronRowySize(PlayerType.B,  BuildingType.DEFENSE, i);
                if (canAffordBuilding(BuildingType.ATTACK) && ((3 * myAttackOnRow) - 2 * enemyAttackOnRow + enemyDefenseOnRow >= Optimum) && myAttackOnRow < 4) {
                    index = i;
                    Optimum = (3 * myAttackOnRow) - 2 * enemyAttackOnRow + enemyDefenseOnRow + 1;
                }
            }
        }

        if (index != -999) {
            return placeAttackBuilding(index);
        }

        return "";
    }


    private String placeDefenseBuilding(int y) {
        //Menggunakan konsep greedy untuk sebisa mungkin menempatkan di column paling dekat dengan lawan
        for (int i = (gameState.getGameDetails().mapWidth / 2) - 1; i >= 0; i--) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, BuildingType.DEFENSE);
            }
        }
        return "";
    }

    private String placeEnergyBuilding(int x, int y) {

        return buildCommand(x, y, BuildingType.ENERGY);
    }

    private String placeAttackBuilding(int y) {
        //Menggunakan konsep greedy untuk sebisa mungkin menempatkan di column dengan prioritas column terurut : 2, 3, 4,5,6,7,0,1
        for (int i = 2; i < gameState.getGameDetails().mapWidth / 2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, BuildingType.ATTACK);
            }
        }
        for (int i = 0; i < 2; i++) {
            if (isCellEmpty(i, y)) {
                return buildCommand(i, y, BuildingType.ATTACK);
            }
        }
        return "";
    }

    private int getXfromPlace(int y, Boolean asc) {
        // Mengembalikkan posisi column kosong pertama pada baris y dari belakang jika asc true atau dari depan jika asc false
        if (asc) {
            for (int i = 0; i < gameState.getGameDetails().mapWidth / 2; i++) {
                if (isCellEmpty(i, y)) {
                    return i;
                }
            }
        } else {
            for (int i = gameState.getGameDetails().mapWidth / 2 - 1; i >= 0; i--) {
                if (isCellEmpty(i, y)) {
                    return i;
                }
            }
        }
        return 10;
    }


    private String buildCommand(int x, int y, BuildingType buildingType) {
        //Memberi perintah membangun bangunan buildingType pada colom x dan baris y
        return String.format("%s,%d,%s", String.valueOf(x), y, buildingType.getType());
    }

    private int getAllBuildingsForPlayeronRowySize(PlayerType playerType, BuildingType type, int y) {
        //Mengembalikan jumlah building player playerType di baris y
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType && c.y == y)
                .flatMap(c -> c.getBuildings().stream())
                .filter( b -> b.buildingType == type)
                .collect(Collectors.toList())
                .size();
    }

    private int getAllBuildingsForPlayerSize(PlayerType playerType, BuildingType type) {
        //Mengembalikan jumlah building player playerType di permainan
        return gameState.getGameMap().stream()
                .filter(c -> c.cellOwner == playerType)
                .flatMap(c -> c.getBuildings().stream())
                .filter( b -> b.buildingType == type)
                .collect(Collectors.toList())
                .size();
    }


    private boolean isCellEmpty(int x, int y) {
        //Mengecek apakah cell tersebut kosong dari building
        Optional<CellStateContainer> cellOptional = gameState.getGameMap().stream()
                .filter(c -> c.x == x && c.y == y)
                .findFirst();

        if (cellOptional.isPresent()) {
            CellStateContainer cell = cellOptional.get();
            return cell.getBuildings().size() <= 0;
        } else {
            System.out.println("No cell selected");
        }
        return true;
    }

    private boolean canAffordBuilding(BuildingType buildingType) {
        //Mengecek apakah energi saat ini cukup untuk membeli bangunan bertipe buildingType
        return getEnergy(PlayerType.A) >= getPriceForBuilding(buildingType);
    }


    private int getEnergy(PlayerType playerType) {
        //Mendapatkan nilai energi player playerType
        return gameState.getPlayers().stream()
                .filter(p -> p.playerType == playerType)
                .mapToInt(p -> p.energy)
                .sum();
    }

    private int getPriceForBuilding(BuildingType buildingType) {
        //Mengembalikan harga building bertipe buildingType
        return gameState.getGameDetails().buildingsStats.get(buildingType).price;
    }
}
