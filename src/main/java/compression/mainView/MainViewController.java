package compression.mainView;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import compression.rle.RLE;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainViewController implements Initializable {
    @FXML
    private JFXRadioButton compressionRadioButton;

    @FXML
    private JFXRadioButton decompressionRadioButton;

    @FXML
    private Label infoLabel;

    @FXML
    private VBox imagesContainer;

    @FXML
    private JFXButton actionButton;

    @FXML
    private JFXButton selectDirectoryButton;

    @FXML
    private JFXButton selectFileButton;

    @FXML
    private JFXButton selectDestinationButton;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private TextField pathTextField;

    @FXML
    private Label progressLabel;

    private Stage stage;
    public Thread thread;
    public volatile boolean running = false;
    private ToggleGroup toggleGroup;
    private int type = 0; // 0 -> compression et 1 -> decompression
    private File destinationDirectory;
    private ObservableList<FileChooser.ExtensionFilter> compressionExtensions = FXCollections.observableArrayList(
            new FileChooser.ExtensionFilter("Image files", "*.png", "*.jpg", "*.bmp", "*.jpeg")
    );
    private ObservableList<FileChooser.ExtensionFilter> decompressionExtensions = FXCollections.observableArrayList(
            new FileChooser.ExtensionFilter("RLE compressed imaged", "*.rles")
    );

    public void init(Stage stage) {
        this.stage = stage;
        typeChanged();
    }

    @FXML
    void action(ActionEvent event) {
        if (running) {
            actionButton.setText("Stopping..");
            running = false;
            actionButton.setDisable(true);
        } else {
            progressBar.setProgress(0.0);
            progressLabel.setText("0.00 %");
            if (imagesContainer.getChildren().size() > 0) {

                thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        setDisabled(true);
                        int i = 1;
                        if ((int) toggleGroup.getSelectedToggle().getUserData() == 0) {
                            for (Node node : imagesContainer.getChildren()) {
                                if (!running) {
                                    break;
                                }
                                FileRow fileRow = (FileRow) node;

                                runCompression(fileRow,i);

                                i++;
                                System.gc();
                            }
                        } else {
                            for (Node node : imagesContainer.getChildren()) {
                                if (!running) {
                                    break;
                                }
                                FileRow fileRow = (FileRow) node;

                                File outputFile = new File(destinationDirectory.getAbsolutePath() + File.separator + fileRow.getSourceFile().getName() + ".bmp");
                                RLE.fileDecompression(fileRow.getSourceFile(), outputFile);

                                fileRow.setOutputFile(outputFile);
                                float progress = (float) i / imagesContainer.getChildren().size();
                                setProgress(progress);
                                i++;
                            }
                        }
                        setDisabled(false);
                    }

                });
                thread.start();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("No image");
                alert.setHeaderText(null);
                alert.setContentText("Please select an image");
                alert.showAndWait();
            }
        }
    }
    private void setProgress(float value) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress(value);
                progressLabel.setText(String.format("%.2f %s", value * 100.0, "%"));
            }
        });
    }

    private void runCompression(FileRow fileRow, int index) {
        File outputFile = new File(destinationDirectory.getAbsolutePath() + File.separator + fileRow.getSourceFile().getName() + ".rles");
        try {
            RLE.fileCompress(fileRow.getSourceFile(), outputFile);
            fileRow.setOutputFile(outputFile);
            float progress = (float) index / imagesContainer.getChildren().size();
            setProgress(progress);
        } catch (Exception e) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Unsupported format");
                    alert.setContentText(e.getMessage());
                    alert.show();
                }
            });
        }
    }

    @FXML
    void selectFiles(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        if (type == 0) {
            fileChooser.getExtensionFilters().addAll(compressionExtensions);
        } else {
            fileChooser.getExtensionFilters().addAll(decompressionExtensions);
        }
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            FileRow fileRow = new FileRow(imagesContainer, file, type);
            imagesContainer.getChildren().add(0, fileRow);
        }
    }

    @FXML
    void selectFolder(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            File[] files = selectedDirectory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                if (type == 0) {
                    for (String ext : compressionExtensions.get(0).getExtensions()) {
                        if (file.getName().matches(".*\\." + ext.split("\\.")[1])) {
                            FileRow fileRow = new FileRow(imagesContainer, file, type);
                            imagesContainer.getChildren().add(0, fileRow);
                        }
                    }
                } else {
                    for (FileChooser.ExtensionFilter ef : decompressionExtensions) {
                        if (file.getName().matches(".*\\." + ef.getExtensions().get(0).split("\\.")[1])) {
                            FileRow fileRow = new FileRow(imagesContainer, file, type);
                            imagesContainer.getChildren().add(0, fileRow);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        pathTextField.setText("");
        // init toggleGroup
        toggleGroup = new ToggleGroup();
        compressionRadioButton.setToggleGroup(toggleGroup);
        compressionRadioButton.setUserData(0);
        decompressionRadioButton.setToggleGroup(toggleGroup);
        decompressionRadioButton.setUserData(1);
        compressionRadioButton.setSelected(true);

        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                type = (int) newValue.getUserData();
                typeChanged();
            }
        });

        // destination folder
        destinationDirectory = new File(System.getProperty("user.home") + File.separator + "Pictures");
        pathTextField.setText(destinationDirectory.getAbsolutePath());
    }

    private void typeChanged() {
        if (type == 0) {
            actionButton.setText("Compress");
            infoLabel.setText("Accepted formats : jpg, bmp, png");
            imagesContainer.getChildren().clear();
            progressBar.setProgress(0.0);
            progressLabel.setText("0.0 %");
            stage.setTitle("Image compression");
        } else {
            actionButton.setText("Decompression");
            infoLabel.setText("Accepted formats : rles");
            imagesContainer.getChildren().clear();
            progressBar.setProgress(0.0);
            progressLabel.setText("0.0 %");
            stage.setTitle("Images decompression");
        }
    }

    private void setDisabled(boolean bool) {
        running = bool;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {

                compressionRadioButton.setDisable(bool);
                decompressionRadioButton.setDisable(bool);
                //imagesContainer.setDisable(bool);
                actionButton.setText(bool ? "Stop" : (((int) toggleGroup.getSelectedToggle().getUserData()) == 0) ? "Compress" : "Decompress");
                actionButton.setDisable(bool);
                selectDestinationButton.setDisable(bool);
                selectDirectoryButton.setDisable(bool);
                selectFileButton.setDisable(bool);
            }
        });
    }


    public void chooseDestinationFolder(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Destination folder");
        directoryChooser.setInitialDirectory(destinationDirectory);
        File file = directoryChooser.showDialog(stage);
        if (file != null) {
            destinationDirectory = file;
            pathTextField.setText(destinationDirectory.getAbsolutePath());
        }
    }
}
