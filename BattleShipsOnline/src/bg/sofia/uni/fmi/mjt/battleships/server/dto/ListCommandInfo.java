package bg.sofia.uni.fmi.mjt.battleships.server.dto;

import bg.sofia.uni.fmi.mjt.battleships.common.ClientRequest;
import bg.sofia.uni.fmi.mjt.battleships.common.Predicate;
import bg.sofia.uni.fmi.mjt.battleships.server.database.models.game.Game;

import java.util.List;
import java.util.function.Function;

public record ListCommandInfo(Predicate<Game, ClientRequest> filter, Function<List<Game>, String> listFunction) {
}
