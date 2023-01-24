package bg.sofia.uni.fmi.mjt.battleships.server.command;

public class CommandExecutor {
//    public static final String INVALID_ARGS_COUNT_MESSAGE_FORMAT =
//        "Invalid count of arguments: \"%s\" expects %d arguments. Example: \"%s\"";
//
//    public static final String DISCONNECT = "disconnect";

//    public static final String CREATE_GAME = "create-game <game-name>";
//    public static final String JOIN_GAME = "join-game [<game-name>]";
//    public static final String SAVED_GAMES = "saved-games";
//    public static final String LOAD_GAME = "load-game <game-name>";
//    public static final String DELETE_GAME = "delete-game";

//    private CocktailStorage storage;
//
//    public CommandExecutor(CocktailStorage storage) {
//        this.storage = storage;
//    }
//
    public String execute(Command cmd) {
        String cmdString = cmd.command();

//        if (cmdString.equals(CREATE)) {
//            return this.createCocktail(cmd.arguments());
//        }
//        else if (cmdString.equals(GET)) {
//            cmdString += " " + (cmd.arguments().length > 0 ? cmd.arguments()[0] : "");
//            var args = Arrays.stream(cmd.arguments()).skip(1).toArray(String[]::new);
//
//            if (cmdString.equals(GET_ALL)) {
//                return this.getAll(args);
//            }
//            else if (cmdString.equals(GET_BY_NAME)) {
//                return this.getByName(args);
//            }
//            else if (cmdString.equals(GET_BY_INGREDIENT)) {
//                return this.getByIngredient(args);
//            }
//        }

        return "Unknown command";
    }

//    private String createCocktail(String[] args) {
//        if (args.length < 2) {
//            return String.format("Invalid count of arguments: \"%s\" expects %d or more arguments. Example: \"%s\""
//                , CREATE, 2, CREATE + " <cocktail_name> [<ingredient_name>=<ingredient_amount> ...]");
//        }
//
//        String cocktailName = args[0];
//        Set<Ingredient> ingredients = new HashSet<>();
//
//        for (String arg : Arrays.stream(args).skip(1).toList()) {
//            String[] argSplit = arg.split("=");
//
//            String ingredientName = argSplit[0];
//            String ingredientAmount = argSplit[1];
//
//            ingredients.add(new Ingredient(ingredientName, ingredientAmount));
//        }
//
//        try {
//            this.storage.createCocktail(new Cocktail(cocktailName, ingredients));
//        } catch (CocktailAlreadyExistsException e) {
//            return "Could not add the new cocktail!\nError: " + e.getMessage();
//        }
//
//        return "The new cocktail has been successfully created!";
//    }

}
