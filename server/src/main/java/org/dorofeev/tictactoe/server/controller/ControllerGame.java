package org.dorofeev.tictactoe.server.controller;

import org.dorofeev.tictactoe.core.Game;
import org.dorofeev.tictactoe.core.GameBoardSize;
import org.dorofeev.tictactoe.core.GameRegime;

import org.dorofeev.tictactoe.core.exception.TicTacToeException;
import org.dorofeev.tictactoe.server.model.GameStatus;
import org.dorofeev.tictactoe.server.model.GameResponse;
import org.dorofeev.tictactoe.server.model.MakeMoveRequest;
import org.springframework.web.bind.annotation.*;

// TODO:  session mechanism need to be added ! Each user should play in his own session. A session stores game object.
// A session is deleted after expiration time.
/**
 * @author Yury Dorofeev
 * @since 02/08/16
 */
@RestController
@RequestMapping(value = "/tictactoe")
public class ControllerGame {
    private static final int MAX_NUMBER_OF_GAMES = 39000;
    private Game game;

    @RequestMapping(value = "/startNewGame", method = RequestMethod.POST)
    public void startNewGame() throws TicTacToeException {
        game = new Game(GameBoardSize.SMALL, GameRegime.BATTLE);
        System.out.println("..new game started");
    }

    @RequestMapping(value = "/makeNewMoveWithPosition", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    GameResponse makeNewMoveWithPosition(@RequestBody MakeMoveRequest request) throws TicTacToeException {
        if(request == null) {
            throw new TicTacToeException("request is empty");
        }
        if(request.getFigure() == null) {
            throw new TicTacToeException("figure parameter is empty");
        }
        if(request.getPosition() == null) {
            throw new TicTacToeException("position parameter is empty");
        }
        if(game == null) {
            startNewGame();
        }
        game.makeNewMove(mapGameFigure(request.getFigure()), Integer.valueOf(request.getPosition()));
        System.out.println("..new move is made for " + request.getFigure() + " at position " + request.getPosition());
        return getGameStatusResponse(Integer.valueOf(request.getPosition()));
    }

    @RequestMapping(value = "/makeNewMove", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody
    GameResponse makeNewMove(@RequestBody MakeMoveRequest request) throws TicTacToeException {
        if(request == null) {
            throw new TicTacToeException("request is empty");
        }
        if(request.getFigure() == null) {
            throw new TicTacToeException("figure parameter is empty");
        }
        if(game == null) {
            startNewGame();
        }
        int position = game.makeNewMove(mapGameFigure(request.getFigure()));
        System.out.println("..new move is made for " + request.getFigure() + " at position " + position);
        return getGameStatusResponse(position);
    }

    @RequestMapping(value = "/makeComputerSmart", method = RequestMethod.POST)
    public void makeSmart() throws TicTacToeException {
        int numberOfGames = 0;
        if(game == null) {
            startNewGame();
        }
        System.out.println("..learning regime");
        while (numberOfGames < MAX_NUMBER_OF_GAMES) {
            if (playNewGame(game, org.dorofeev.tictactoe.core.GameFigure.X)) {
                numberOfGames++;
            }
        }
    }

    private org.dorofeev.tictactoe.core.GameFigure mapGameFigure(final String figure) {
        if (figure.equals("O") || figure.equals("o")) {
            return org.dorofeev.tictactoe.core.GameFigure.O;
        } else {
            return org.dorofeev.tictactoe.core.GameFigure.X;
        }
    }

    private GameResponse mapGameStatusResponse(org.dorofeev.tictactoe.core.GameStatus status, int position) throws TicTacToeException {
        return new GameResponse(mapGameStatus(status), String.valueOf(position));
    }

    private String mapGameStatus(org.dorofeev.tictactoe.core.GameStatus status) throws TicTacToeException {
        switch (status) {
            case WIN:
                return GameStatus.WIN.name();
            case DRAW:
                return GameStatus.DRAW.name();
            case CONTINUE:
                return GameStatus.CONTINUE.name();
        }
        throw new TicTacToeException("Status not found");
    }

    private GameResponse getGameStatusResponse(int position) throws TicTacToeException {
        org.dorofeev.tictactoe.core.GameStatus status = game.getGameStatus();
        if(status != org.dorofeev.tictactoe.core.GameStatus.CONTINUE) {
            game.gameOver(status);
            System.out.println("..game is over");
        }
        return mapGameStatusResponse(status, position);
    }

    private boolean playNewGame(Game game, org.dorofeev.tictactoe.core.GameFigure figure) throws TicTacToeException {
        org.dorofeev.tictactoe.core.GameStatus status = game.getGameStatus();
        if(status == org.dorofeev.tictactoe.core.GameStatus.CONTINUE) {
            game.makeNewMove(figure);
            return playNewGame(game, figure == org.dorofeev.tictactoe.core.GameFigure.X ? org.dorofeev.tictactoe.core.GameFigure.O : org.dorofeev.tictactoe.core.GameFigure.X);
        } else {
            game.gameOver(status);
            return true;
        }
    }

}

