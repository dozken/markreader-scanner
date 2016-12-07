/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package markreaderservice;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

/**
 *
 * @author balancy
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private Button open;

    @FXML
    private Label pathLabel;

    @FXML
    private TextField pathText;

    final DirectoryChooser fileChooser = new DirectoryChooser();
    final ImageProcessing imageProcessing = new ImageProcessing();

    @FXML
    private void handleOpenAction(ActionEvent event) {
        File file = fileChooser.showDialog(open.getScene().getWindow());
        if (file != null) {
            file.getAbsolutePath();
            pathText.setText(file.getAbsolutePath());
            //openFile(file);
        }
        System.out.println("handleOpenAction!");

    }

    @FXML
    private void handleScanAction(ActionEvent event) {
        List<File> files = listFilesForFolder(new File(pathText.getText()));
        for (File file : files) {

        }
        Path folder = Paths.get(pathText.getText());
        Thread thread = new Thread() {
            @Override
            public void run() {
                imageProcessing.watchDirectoryPath(folder);
            }
        };
        thread.setDaemon(true);
        thread.start();
        System.out.println("handleScanAction!");

    }

    @FXML
    private void handleReadAction(ActionEvent event) {
        List<File> files = listFilesForFolder(new File(pathText.getText()));
        for (File file : files) {
            imageProcessing.process(file);
        }

        System.out.println("handleReadAction!");

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public List<File> listFilesForFolder(final File folder) {
        List<File> files = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                //listFilesForFolder(fileEntry);
            } else {
                System.out.println(fileEntry.getName());
                files.add(fileEntry);
            }
        }
        return files;
    }

}
