package compression.mainView;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileRow extends HBox {

    private File sourceFile;
    private File outputFile;
    private ImageView imageView;
    private Label status;
    private Label rate;
    private VBox vBox;
    private int type;
    private VBox containerVBox;
    public double fileSizeInput = 0.0;

    public FileRow(VBox container, File sourceFile, int type){
        super();
        setHeight(80);
        setWidth(470);
        setSpacing(5);
        setStyle("-fx-background-color: #acd3ff;" +
                "-fx-border-color: black");
        this.containerVBox = container;
        this.sourceFile = sourceFile;
        this.type = type;
        initNode();
    }

    private void initNode(){
        // imageView
        imageView = new ImageView(new Image(getClass().getResourceAsStream("/res/empty.png"),80,80,false,false));
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPickOnBounds(true);
        getChildren().add(imageView);

        // init vbox
        vBox = new VBox();
        Label cheminLabel = new Label("Path : "+sourceFile.getAbsolutePath());
        cheminLabel.setTooltip(new Tooltip(sourceFile.getAbsolutePath()));
        vBox.getChildren().add(cheminLabel);

        if (type == 0){
            try {
                BufferedImage bi = ImageIO.read(sourceFile);
                fileSizeInput = (bi.getColorModel().getPixelSize() * bi.getWidth()*bi.getHeight())/8.0d;
                bi = null;
                System.gc();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            fileSizeInput = sourceFile.length();
        }
        fileSizeInput = fileSizeInput / 1024.0d;
        String sFileSize = "Size : ";
        if(fileSizeInput > 1024){
            sFileSize += String.format("%.2f MB",fileSizeInput/1024.0d);
        }else{
            sFileSize += String.format("%.2f KB",fileSizeInput);
        }
        vBox.getChildren().add(new Label("File name : "+sourceFile.getName()+"\t"+sFileSize));
        status = new Label("Status : Ready");
        status.setTextFill(Paint.valueOf("#0000FF"));
        vBox.getChildren().add(status);
        rate = new Label("Ratio : -.-- %\tDestination size : -.-- KB");
        vBox.setPrefHeight(80);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setSpacing(3);
        vBox.getChildren().add(rate);

        // add vbox
        getChildren().add(vBox);

        status = new Label("");
        rate = new Label("");
        if (type == 0){
            try {
                imageView.setImage(new Image(new FileInputStream(sourceFile),80,80,false,false));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else {
            imageView.setImage(new Image(getClass().getResourceAsStream("/res/rles.png"),80,80,false,false));
        }
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem("Delete");
        contextMenu.getItems().add(menuItem);
        this.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show((Node)event.getSource(),event.getScreenX(),event.getScreenY());
            }
        });
        this.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                contextMenu.hide();
            }
        });
        menuItem.setOnAction(e -> {
            containerVBox.getChildren().remove(this);
        });

    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                vBox.getChildren().remove(2,4);
                status.textProperty().set("ٍStatus : "+((type==0)?"Compress":"Decompress"));
                status.setTextFill(Paint.valueOf("#00FF00"));
                double fileSize = outputFile.length() / 1024.0d;
                String sFileSize = "Taille destination : ";
                if(fileSize > 1024){
                    sFileSize += String.format("%.2f MB",fileSize/1024.0d);
                }else{
                    sFileSize += String.format("%.2f KB",fileSize);
                }
                double taux = ((double)outputFile.length() / fileSizeInput)/100.0;
                rate.setText(String.format("Ratio : %.2f\t%s",taux,sFileSize));
                vBox.getChildren().addAll(status,rate);
            }
        });
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Label getStatus() {
        return status;
    }

    public void setStatus(Label status) {
        this.status = status;
    }

    public Label getRate() {
        return rate;
    }

    public void setRate(Label rate) {
        this.rate = rate;
    }

    public VBox getvBox() {
        return vBox;
    }

    public void setvBox(VBox vBox) {
        this.vBox = vBox;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public VBox getContainerVBox() {
        return containerVBox;
    }

    public void setContainerVBox(VBox containerVBox) {
        this.containerVBox = containerVBox;
    }
}
