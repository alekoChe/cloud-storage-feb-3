package com.geekbrains.cloud.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class MainController implements Initializable { // с конструкторами нуно быть осторожно в хандлерах, поэтому используем
                                                    // implements Initializable
    private static final int BUFFER_SIZE = 8192;

    public TextField clientPath;
    public TextField serverPath;
    public ListView<String> clientView;
    public ListView<String> serverView;
    private File currentDirectory;
    private File serverDirectory; ////////////////////////////////////////////////

    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buf;

    // Platform.runLater(() -> {})
    private void updateClientView() {
        Platform.runLater(() -> {
            clientPath.setText(currentDirectory.getAbsolutePath());
            clientView.getItems().clear();
            clientView.getItems().add("...");
            clientView.getItems()
                    .addAll(currentDirectory.list()); // добавляем усе файлы (список файлов) из текущей директории
        });
    }
    private void updateServerView() {  //////////////////////////////////////
        serverPath.setText(serverDirectory.getAbsolutePath());
        serverView.getItems().clear();
        serverView.getItems().addAll(serverDirectory.list());
    }

    public void download(ActionEvent actionEvent) throws IOException{  /////////////////////////////////////
    }

    // upload file to server
    public void upload(ActionEvent actionEvent) throws IOException {
        String item = clientView.getSelectionModel().getSelectedItem();
        File selected = currentDirectory.toPath().resolve(item).toFile();
        if (selected.isFile()) {
            serverView.getItems().addAll(item);   /////////////////////////
            os.writeUTF("#file_message#");
            os.writeUTF(selected.getName());
            os.writeLong(selected.length());
            try (InputStream fis = new FileInputStream(selected)) {
                while (fis.available() > 0) {
                    int readBytes = fis.read(buf);
                    os.write(buf, 0, readBytes);
                }
            }
            os.flush();
        }
    }

    private void initNetwork() {
        try {
            buf = new byte[BUFFER_SIZE];
            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentDirectory = new File(System.getProperty("user.home"));
        serverDirectory = new File("server"); //////////////////////////////////////////////////
        //FileSystems.getDefault().getFileStores().forEach(System.out::println); // просмотреть
        //FileSystems.getDefault().getFileStores().forEach(f -> System.out.println(f.name()));


        // run in FX Thread
        // :: - method reference
        updateClientView();
        updateServerView();     /////////////////
        initNetwork();
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = clientView.getSelectionModel().getSelectedItem();
                if (item.equals("...")) {
                    currentDirectory = currentDirectory.getParentFile();
                    updateClientView();
                    updateServerView();    /////////////////////////////////
                } else {
                    File selected = currentDirectory.toPath().resolve(item).toFile();
                    if (selected.isDirectory()) {
                        currentDirectory = selected;
                        updateClientView();
                        updateServerView();  //////////////////////////////
                    }
                }
            }
        });
        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String item = serverView.getSelectionModel().getSelectedItem();
                File selected =  serverDirectory.toPath().resolve(item).toFile();
                if (selected.isDirectory()) {
                    serverDirectory = selected;
                    updateClientView();
                    updateServerView();  //////////////////////////////
                }
            }
        });
    }
}
