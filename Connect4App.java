package Connect4pak;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Connect4App extends Application {
    private static final int TILE_SIZE = 80;
    private static final int COLUMNS = 7;
    private static final int ROWS = 6;
    private boolean redMove = true;
    private Disc[][] grid = new Disc[COLUMNS][ROWS];
    private Pane discRoot = new Pane();


    private Parent createContent() {
        Pane root = new Pane();
        root.getChildren().add(discRoot);
        Shape gridShape = makeGrid();
        root.getChildren().add(gridShape);
        root.getChildren().addAll(makeColumns());

        return root;
    }

    private Shape makeGrid() {
        Shape shape = new Rectangle((COLUMNS + 1) * TILE_SIZE, (ROWS + 1) * TILE_SIZE);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLUMNS; x++) {
                Circle circle = new Circle(TILE_SIZE / 2);
                circle.setCenterX(TILE_SIZE / 2);
                circle.setCenterY(TILE_SIZE / 2);
                circle.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
                circle.setTranslateY(y * (TILE_SIZE + 5) + TILE_SIZE / 4);
                shape = Shape.subtract(shape, circle);
            }
        }
        shape.setFill(Color.BLACK);
        return shape;
    }

    private List<Rectangle> makeColumns() {
        List<Rectangle> list = new ArrayList<>();
        for (int x = 0; x < COLUMNS; x++) {
            Rectangle rect = new Rectangle(TILE_SIZE, (ROWS + 1) * TILE_SIZE);
            Rectangle r = new Rectangle(TILE_SIZE, TILE_SIZE + 20);
            rect.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
            r.setTranslateX(x * (TILE_SIZE + 5) + TILE_SIZE / 4);
            rect.setFill(Color.TRANSPARENT);
            r.setFill(Color.TRANSPARENT);
            rect.setOnMouseEntered(e -> r.setFill(Color.rgb(250, 0, 0, 0.5)));
            rect.setOnMouseExited(e -> r.setFill(Color.TRANSPARENT));
            final int column = x;
            rect.setOnMouseClicked(e -> {
                placeDisc(new Disc(redMove), column);
                
            });
            list.add(rect);
            list.add(r);
        }
        return list;
    }

    private void placeDisc(Disc disc, int column) {
        int row = ROWS - 1;
        do {
            if (!getDisc(column, row).isPresent())
                break;
            row--;
        } while (row >= 0);
        if (row < 0)
            return;
        grid[column][row] = disc;
        discRoot.getChildren().add(disc);
        disc.setTranslateX(column * (TILE_SIZE + 5) + TILE_SIZE / 4);
        final int currentRow = row;
        TranslateTransition animation = new TranslateTransition(Duration.seconds(0.5), disc);
        animation.setToY(row * (TILE_SIZE + 5) + TILE_SIZE / 4);
        animation.setOnFinished(e -> {
            if (gameEnded(column, currentRow)) {
            	
                gameOver();
            }
            redMove = !redMove;
        });
        animation.play();
    }

    private boolean gameEnded(int column, int row) {
        List<Point2D> vertical = IntStream.rangeClosed(row - 3, row + 3)
                .mapToObj(r -> new Point2D(column, r))
                .collect(Collectors.toList());
        List<Point2D> horizontal = IntStream.rangeClosed(column - 3, column + 3)
                .mapToObj(c -> new Point2D(c, row))
                .collect(Collectors.toList());
        Point2D topLeft = new Point2D(column - 3, row - 3);
        List<Point2D> diagonal1 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> topLeft.add(i, i))
                .collect(Collectors.toList());
        Point2D botLeft = new Point2D(column - 3, row + 3);
        List<Point2D> diagonal2 = IntStream.rangeClosed(0, 6)
                .mapToObj(i -> botLeft.add(i, -i))
                .collect(Collectors.toList());
        return checkRange(vertical) || checkRange(horizontal)
                || checkRange(diagonal1) || checkRange(diagonal2);
    }

    private boolean checkRange(List<Point2D> points) {
        int chain = 0;
        for (Point2D p : points) {
            int column = (int) p.getX();
            int row = (int) p.getY();
            Disc disc = getDisc(column, row).orElse(new Disc(!redMove));
            if (disc.red == redMove) {
                chain++;
                if (chain == 4) {
                    return true;
                }
            } else {
                chain = 0;
            }
        }
        return false;
    }
    
    
    boolean close = false;
    
    private void gameOver() {
    	
        Stage s1 = new Stage();
        Pane sp = new Pane();
        
        
        Text t1 = new Text(String.format("Winner: %S", (redMove? "Red" : "Yellow")));
        t1.setStyle("-fx-font: 36 arial;");
        t1.setX(80);
        t1.setY(100);
        
        
        Button b1 = new Button();
        b1.setText("End Game");
        b1.setTranslateX(20);
        b1.setTranslateY(150);
        b1.setOnMouseClicked(e -> {
            close = true;
            s1.close();
            
            try {
   			start(new Stage());
            } catch (Exception e1) {
   			
            }
            
        });
        
        Button b2 = new Button();
        b2.setTranslateX(300);
        b2.setTranslateY(150);
        b2.setText("New Game");
        b2.setOnMouseClicked(e -> {
        	s1.close();
         });
        
        //Adding Buttons and Text
        sp.getChildren().add(t1);
        sp.getChildren().add(b1);
        sp.getChildren().add(b2);
        
        s1.setScene(new Scene(sp,400,200));
        s1.show();
        resetGame();
        
    }
    

    private Optional<Disc> getDisc(int column, int row) {
        if (column < 0 || column >= COLUMNS
                || row < 0 || row >= ROWS)
            return Optional.empty();
        return Optional.ofNullable(grid[column][row]);
    }

    
    private static class Disc extends Circle {
        private final boolean red;

        public Disc(boolean red) {
            super(TILE_SIZE / 2, red ? Color.RED : Color.YELLOW);
            this.red = red;
            setCenterX(TILE_SIZE / 2);
            setCenterY(TILE_SIZE / 2);
        }
    }

    private void resetGame() {
        for (int i = 0; i < COLUMNS; i++) {
            for (int j = 0; j < ROWS; j++) {
                grid[i][j] = null;
            }
        }
        redMove = true;
        discRoot.getChildren().clear();
        
    }

    

    

    @Override
    public void start(Stage stage) throws Exception {
    	   if (close==true)
           {
           	stage.close();
           System.exit(0);
           }
        stage.setTitle("WentWorth Connect 4");
        stage.setScene(new Scene(createContent()));
        stage.show();
     

    }
    

    public static void main(String[] args) {
        launch(args);
    }
}
