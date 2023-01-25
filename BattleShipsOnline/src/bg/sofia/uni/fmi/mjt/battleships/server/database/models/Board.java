package bg.sofia.uni.fmi.mjt.battleships.server.database.models;

import java.util.*;

public class Board {

    private static final Map<ShipType, Integer> SHIPS_COUNTS = Map.of(
        ShipType.HUGE, 1,
        ShipType.LARGE, 2,
        ShipType.MEDIUM, 3,
        ShipType.SMALL, 4
    );

    public static final int FILES_COUNT = 10;
    public static final int RANKS_COUNT = 10;

    private List<Tile> board;

    private List<Ship> ships;

    private Board() {
        initialiseBoard();
        this.ships = new ArrayList<>();
    }

    public Board(boolean randomizedBoard) {
        this();

        if (randomizedBoard) {
            randomizeBoard(this);
        }
    }

    private void initialiseBoard() {
        this.board = new ArrayList<>();

        for (var rank : BoardRank.values()) {
            for (int file = 1; file <= FILES_COUNT; file++) {
                board.add(new Tile(rank, file, TileStatus.EMPTY));
            }
        }
    }

    public void addShip(Ship ship) {
        this.ships.add(ship);
    }

    public void changeTile(TilePos pos, TileStatus status) {
        Tile tile = this.board.stream().filter(x -> x.pos().equals(pos)).findFirst().orElseGet(() -> null);

        if (tile != null) {
            tile.status = status;
        }
    }

    public void changeTile(BoardRank rank, int file, TileStatus status) {
        changeTile(new TilePos(rank, file), status);
    }

    public Tile getTile(BoardRank rank, int file) {
        return getTile(new TilePos(rank, file));
    }

    public Tile getTile(TilePos pos) {
        var tile = this.board.stream().filter(x -> x.pos().equals(pos)).findFirst().orElseGet(() -> null);

        return tile;
    }

    public List<Tile> board() {
        return Collections.unmodifiableList(board);
    }

    public List<Ship> ships() {
        return Collections.unmodifiableList(ships);
    }

    private boolean shipTilesAreTaken(List<TilePos> shipTiles, Board board) {
        for (var tilePos : shipTiles) {
            if (board.getTile(tilePos).status != TileStatus.EMPTY) {
                return true;
            }
        }

        return false;
    }
    private List<List<TilePos>> getPossiblePositionsHorizontal(ShipType shipType, Board board) {
        var shipSize = shipType.size();

        //Get all possible horizontal positions for the given ship type
        List<List<TilePos>> possiblePositionsHorizontal = new ArrayList<>();

        for (var rank : BoardRank.values()) {
            for (int file = 1; file <= Board.FILES_COUNT - shipSize + 1; file++) {
                List<TilePos> shipTiles = new ArrayList<>();

                for (int tileFile = file; tileFile < file + shipSize; tileFile++) {
                    shipTiles.add(new TilePos(rank, tileFile));
                }

                //Check if any of the tiles have already been taken. If so, do not add the position
                if (!shipTilesAreTaken(shipTiles, board)) {
                    possiblePositionsHorizontal.add(shipTiles);
                }
            }
        }

        return possiblePositionsHorizontal;
    }
    private List<List<TilePos>> getPossiblePositionsVertical(ShipType shipType, Board board) {
        var shipSize = shipType.size();

        //Get all possible horizontal positions for the given ship type
        List<List<TilePos>> possiblePositionsVertical = new ArrayList<>();

        var boardRanks = BoardRank.values();

        for (int file = 1; file <= Board.FILES_COUNT; file++) {
            for (int rank = 0; rank < Board.RANKS_COUNT - shipSize + 1; rank++) {
                List<TilePos> shipTiles = new ArrayList<>();

                for (int tileRank = rank; tileRank < rank + shipSize; tileRank++) {

                    var tileBoardRank = boardRanks[tileRank];
                    shipTiles.add(new TilePos(tileBoardRank, file));
                }

                //Check if any of the tiles have already been taken. If so, do not add the position
                if (!shipTilesAreTaken(shipTiles, board)) {
                    possiblePositionsVertical.add(shipTiles);
                }
            }
        }

        return possiblePositionsVertical;
    }

    private void randomizeBoard(Board board) {
        var random = new Random();

        for (var shipType : SHIPS_COUNTS.keySet()) {
            var shipsCount = SHIPS_COUNTS.get(shipType);

            //Get all possible horizontal positions for the given ship type
            List<List<TilePos>> possiblePositionsHorizontal = getPossiblePositionsHorizontal(shipType, board);

            //Get all possible vertical positions for the given ship type
            List<List<TilePos>> possiblePositionsVertical = getPossiblePositionsVertical(shipType, board);

            List<List<List<TilePos>> > possiblePositions = List.of(
                possiblePositionsHorizontal,
                possiblePositionsVertical
            );

            //Randomly place the ship
            for (int i = 0; i < shipsCount; i++) {

                int orientation = random.nextInt(0,possiblePositions.size()); //Determine if ship will be horizontal or vertical

                var shipPositions = possiblePositions.get(orientation);

                int position = random.nextInt(0, shipPositions.size());

                var shipTiles = shipPositions.get(position);

                for (var tilePos : shipTiles) {
                    board.addShip(new Ship(shipTiles, shipType, ShipStatus.AFLOAT));
                    board.changeTile(tilePos, TileStatus.SHIP);
                }
            }
        }
    }

    public void randomizeBoard() {
        randomizeBoard(this);
    }

    @Override
    public String toString() {
        var stringBuilder = new StringBuilder();

        var orderedBoard = this.board.stream()
            .sorted((first, second) -> {
                var firstRank = first.pos().rank().ordinal();
                var secondRank = second.pos().rank().ordinal();

                var compareRanks = Integer.compare(firstRank, secondRank);

                if (compareRanks != 0) {
                    return compareRanks;
                }

                var firstFile = first.pos().file();
                var secondFile = second.pos().file();

                return Integer.compare(firstFile, secondFile);
            })
            .toList();

        BoardRank lastRank = null;

        for (var tile : orderedBoard) {
            var curRank = tile.pos().rank();

            if (curRank != lastRank && lastRank != null) {
                stringBuilder.append("|\n");
            }

            stringBuilder.append("|" + tile.status.toString());

            lastRank = curRank;
        }

        stringBuilder.append("|\n");

        return stringBuilder.toString();
    }

}
