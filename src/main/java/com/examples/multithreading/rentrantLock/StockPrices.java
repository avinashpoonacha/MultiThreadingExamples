package com.examples.multithreading.rentrantLock;

import javafx.animation.AnimationTimer;
import javafx.animation.FillTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class StockPrices extends Application {

    public static void main(String[] args) {
        launch(args); // this is the main UI thread
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Stock Prices");

        GridPane grid = createGrid();
        Map<String, Label> labels = createStockPriceLabels();

        addLabelsToGrid(labels,grid);

        double width=300;
        double height=250;
        StackPane root = new StackPane();

        Rectangle background = createBGRectangle(width,height);
        root.getChildren().add(background);
        root.getChildren().add(grid);
        Scene scene = new Scene(root, width, height);
        scene.getRoot().setStyle("-fx-font-family: 'serif'");
        primaryStage.setScene(scene);

       /* primaryStage.setScene(new Scene(root,width,height));
        scene.getRoot().setStyle("-fx-font-family: 'serif'");*/
        PricesContainer pricesContainer  = new PricesContainer();
        PriceUpdater priceUpdater = new PriceUpdater(pricesContainer);


        AnimationTimer animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (pricesContainer.getLockObject().tryLock()) { // try lock is better than lock for performance as it is lock free
                    try {
                        Label bitcoinLabel = labels.get("BTC");
                        bitcoinLabel.setText(String.valueOf(pricesContainer.getBitCoinPrice()));

                        Label etherLabel = labels.get("APPL");
                        etherLabel.setText(String.valueOf(pricesContainer.getApplePrice()));

                        Label litecoinLabel = labels.get("GOOG");
                        litecoinLabel.setText(String.valueOf(pricesContainer.getGooglePrice()));

                        Label bitcoinCashLabel = labels.get("MSFT");
                        bitcoinCashLabel.setText(String.valueOf(pricesContainer.getMicrosoftPrice()));

                        Label rippleLabel = labels.get("NVDA");
                        rippleLabel.setText(String.valueOf(pricesContainer.getNvidiaPrice()));
                    } finally {
                        pricesContainer.getLockObject().unlock();
                    }
                }
            }
        };

        addWindowResizeListener(primaryStage, background);

        animationTimer.start();

        priceUpdater.start();

        primaryStage.show();
    }

    private void addWindowResizeListener(Stage stage, Rectangle background) {
        ChangeListener<Number> stageSizeListener = ((observable, oldValue, newValue) -> {
            background.setHeight(stage.getHeight());
            background.setWidth(stage.getWidth());
        });
        stage.widthProperty().addListener(stageSizeListener);
        stage.heightProperty().addListener(stageSizeListener);
    }

    private Rectangle createBGRectangle(double width, double height) {

        Rectangle bg= new Rectangle(width,height);
        FillTransition fT = new FillTransition(Duration.millis(1000),bg,Color.LIGHTSEAGREEN,Color.LIGHTSKYBLUE);
        fT.setCycleCount(Timeline.INDEFINITE);
        fT.setAutoReverse(true);
        fT.play();
        return bg;
    }

    private void addLabelsToGrid(Map<String, Label> labels, GridPane grid) {
        int row=0;
        for(Map.Entry<String,Label> entry : labels.entrySet()) {
            String stockName=entry.getKey();
            Label nameLabel = new Label(stockName);
            nameLabel.setTextFill(Color.BLUE);
            nameLabel.setOnMousePressed(event -> nameLabel.setTextFill(Color.RED));
            nameLabel.setOnMouseReleased(event -> nameLabel.setTextFill(Color.BLUE));
            grid.add(nameLabel,0,row);
            grid.add(entry.getValue(),1,row);
            row++;
        }
    }

    private Map<String, Label> createStockPriceLabels() {
        Label googlePrice = new Label("0");
        googlePrice.setId("GOOG");

        Label applePrice = new Label("0");
        applePrice.setId("APPL");

        Label nvidiaPrice = new Label("0");
        nvidiaPrice.setId("NVDA");

        Label msftPrice = new Label("0");
        msftPrice.setId("MSFT");

        Label bitCoinPrice = new Label("0");
        bitCoinPrice.setId("BTC");

        Map<String, Label> stockPriceLabels = new HashMap<String, Label>();
        stockPriceLabels.put("GOOG",googlePrice);
        stockPriceLabels.put("APPL",applePrice);
        stockPriceLabels.put("NVDA",nvidiaPrice);
        stockPriceLabels.put("MSFT",msftPrice);
        stockPriceLabels.put("BTC",bitCoinPrice);

        return stockPriceLabels;

    }

    private GridPane createGrid() {
        GridPane gp = new GridPane();
        gp.setHgap(15);
        gp.setVgap(15);
        gp.setAlignment(Pos.CENTER);
        return gp;
    }

    public static class PricesContainer{

        private Lock lockObject = new ReentrantLock();

        private double bitCoinPrice;
        private double googlePrice;
        private double applePrice;
        private double microsoftPrice;
        private double nvidiaPrice;

        public Lock getLockObject() {
            return lockObject;
        }

        public void setLockObject(Lock lockObject) {
            this.lockObject = lockObject;
        }

        public double getBitCoinPrice() {
            return bitCoinPrice;
        }

        public void setBitCoinPrice(double bitCoinPrice) {
            this.bitCoinPrice = bitCoinPrice;
        }

        public double getGooglePrice() {
            return googlePrice;
        }

        public void setGooglePrice(double googlePrice) {
            this.googlePrice = googlePrice;
        }

        public double getApplePrice() {
            return applePrice;
        }

        public void setApplePrice(double applePrice) {
            this.applePrice = applePrice;
        }

        public double getMicrosoftPrice() {
            return microsoftPrice;
        }

        public void setMicrosoftPrice(double microsoftPrice) {
            this.microsoftPrice = microsoftPrice;
        }

        public double getNvidiaPrice() {
            return nvidiaPrice;
        }

        public void setNvidiaPrice(double nvidiaPrice) {
            this.nvidiaPrice = nvidiaPrice;
        }
    }


    public static class PriceUpdater extends Thread {
        private PricesContainer pricesContainer;
        private Random random = new Random();

        public PriceUpdater(PricesContainer pricesContainer) {
            this.pricesContainer = pricesContainer;
        }

        @Override
        public void run() {
            while(true) {
                pricesContainer.getLockObject().lock();

                try {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    pricesContainer.setApplePrice(random.nextInt(1000000));
                    pricesContainer.setBitCoinPrice(random.nextInt(2000));
                    pricesContainer.setMicrosoftPrice(random.nextInt(300000));
                    pricesContainer.setNvidiaPrice(random.nextInt(209));
                    pricesContainer.setGooglePrice(random.nextInt(1000000000));
                } finally {
                    pricesContainer.getLockObject().unlock();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
