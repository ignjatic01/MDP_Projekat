package net.etf.gui;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.etf.chat.client.ChatClient;
import net.etf.client.BibliotekaClient;
import net.etf.client.BooksClient;
import net.etf.client.MailClient;
import net.etf.model.Book;
import net.etf.model.Image;
import net.etf.model.User;
import net.etf.multicast.MulticastClient;
import net.etf.service.ImageService;
import net.etf.service.LoggerUtil;

public class ClanMain extends Application {

	private static ArrayList<Book> selectedBooks = new ArrayList<Book>();
	private TextArea txtArea;
	public static final String MULTICAST_ADDRESS;
	public static final int PORT;
	private static String email;
	private static String username;
	
	static
	{
		Properties prop = new Properties();
		try (FileInputStream input = new FileInputStream("resources/config.properties")) {
            prop.load(input);
            MULTICAST_ADDRESS = prop.getProperty("MULTICAST_ADDRESS");
            PORT = Integer.parseInt(prop.getProperty("MULTICAST_PORT"));
        } catch (IOException e) {
            e.printStackTrace();
            LoggerUtil.logException("Greska u ocitavanju", e);
            throw new RuntimeException("Ne može se učitati konfiguracija!");
        }
	}
	
    @Override
    public void start(Stage primaryStage) 
    {
        showLoginScene(primaryStage);
        Thread ms = new Thread(this::startMulticastServer);
        ms.setDaemon(true);
        ms.start();
    }
    
    private MenuBar createMenuBar(Stage primaryStage) 
    {
        MenuBar menuBar = new MenuBar();
        Menu menuOpcije = new Menu("Opcije");

        MenuItem menuChat = new MenuItem("Chat");
        MenuItem menuMulticast = new MenuItem("Multicast");
        MenuItem menuKnjige = new MenuItem("Knjige");


        menuChat.setOnAction(e -> showChatScene(primaryStage));
        menuMulticast.setOnAction(e -> showMulticastScene(primaryStage));
        menuKnjige.setOnAction(e -> createBookTableScene(primaryStage));


        menuOpcije.getItems().addAll(menuMulticast, menuKnjige, menuChat);
        menuBar.getMenus().addAll(menuOpcije);

        return menuBar;
    }

    public void showLoginScene(Stage primaryStage) 
    {
        Label lblUsername = new Label("Korisničko ime:");
        TextField txtUsername = new TextField();
        
        Label lblPassword = new Label("Lozinka:");
        PasswordField txtPassword = new PasswordField();
        
        Button btnLogin = new Button("Prijavi se");
        Label lblMessage = new Label();
        
        Button btnRegister = new Button("Registracija");
        
        btnLogin.setOnAction(e -> {
            String username = txtUsername.getText();
            String password = txtPassword.getText();
            BibliotekaClient bc = new BibliotekaClient();
            String res = bc.login(username, password);
            System.out.println(res);
            if ("OK".equals(res)) {
                lblMessage.setText("Uspešno prijavljivanje!");
                lblMessage.setStyle("-fx-text-fill: green;");
                email = bc.getUserEmail(username);
                ClanMain.username = username;
                ChatClient cc = new ChatClient();
                cc.init();
                cc.register(username);
                cc.end();
                System.out.println(email);
                createBookTableScene(primaryStage);
            } else {
                lblMessage.setText("Neuspjesna prijava. Pokušajte ponovo.");
                lblMessage.setStyle("-fx-text-fill: red;");
            }
        });
        
        btnRegister.setOnAction(e -> {
        	showRegisterScene(primaryStage);
        });;

        VBox vbox = new VBox(10, lblUsername, txtUsername, lblPassword, txtPassword, btnLogin, btnRegister, lblMessage);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        
        Scene scene = new Scene(vbox, 300, 250);
        
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }
    
    public void showRegisterScene(Stage primaryStage) 
    {
        Label lblName = new Label("Ime:");
        TextField txtName = new TextField();

        Label lblSurname = new Label("Prezime:");
        TextField txtSurname = new TextField();

        Label lblAddress = new Label("Adresa:");
        TextField txtAddress = new TextField();

        Label lblEmail = new Label("Email:");
        TextField txtEmail = new TextField();

        Label lblUsername = new Label("Korisničko ime:");
        TextField txtUsername = new TextField();

        Label lblPassword = new Label("Lozinka:");
        PasswordField txtPassword = new PasswordField();
        
        Label lblPasswordCheck = new Label("Ponovite lozinku:");
        PasswordField txtPasswordCheck = new PasswordField();

        Button btnRegister = new Button("Registruj se");
        Label lblMessage = new Label();
        
        Button btnLogin = new Button("Prijava");

        btnRegister.setOnAction(e -> {
            String name = txtName.getText();
            String surname = txtSurname.getText();
            String address = txtAddress.getText();
            String email = txtEmail.getText();
            String username = txtUsername.getText();
            String password = txtPassword.getText();
            String password2 = txtPasswordCheck.getText();

            if (name.isEmpty() || surname.isEmpty() || address.isEmpty() || 
                email.isEmpty() || username.isEmpty() || password.isEmpty() || password2.isEmpty()) {
                lblMessage.setText("Sva polja moraju biti popunjena!");
                lblMessage.setStyle("-fx-text-fill: red;");
            }
            else if(!password.equals(password2))
            {
            	lblMessage.setText("Neispravno unsesna lozinka!");
                lblMessage.setStyle("-fx-text-fill: red;");
            }
            else { 
                BibliotekaClient bc = new BibliotekaClient();
                String res = bc.register(new User(name, surname, address, email, username, password));
                if("ACCEPTED".equals(res))
                {
                	lblMessage.setText("Uspešna registracija!");
                    lblMessage.setStyle("-fx-text-fill: green;");
                }
            }
        });
        
        btnLogin.setOnAction(e -> {
        	showLoginScene(primaryStage);
        });

        VBox vbox = new VBox(10, lblName, txtName, lblSurname, txtSurname, 
                lblAddress, txtAddress, lblEmail, txtEmail, 
                lblUsername, txtUsername, lblPassword, txtPassword, lblPasswordCheck, txtPasswordCheck,
                btnRegister, btnLogin, lblMessage);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox, 350, 550);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Registracija");
        primaryStage.show();
    }
    
    public void showMulticastScene(Stage primaryStage)
    {
    	VBox layout = new VBox(15);
        layout.setPadding(new Insets(10));

        MenuBar menuBar = createMenuBar(primaryStage);
        Label label = new Label("Multicast komunikacija");
        
        HBox hbox = new HBox(10);
        
        TextField msg = new TextField();
        msg.setPromptText("Unesite naziv knjige");
        
        Button send = new Button("Posalji");
        send.setOnAction(e -> {
        	MulticastClient mc = new MulticastClient();
        	mc.sendMessage("Da li ce biti dostupna knjiga: " + msg.getText());
        	msg.clear();
        });
        
        hbox.getChildren().addAll(msg, send);
        
        txtArea = new TextArea();
        txtArea.setEditable(false);
        
        
        layout.getChildren().addAll(menuBar, label, hbox, txtArea);
        
        Scene scene = new Scene(layout, 700, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Registracija");
        primaryStage.show();
    }
    
    public void showChatScene(Stage primaryStage)
    {
    	VBox layout = new VBox(15);
        layout.setPadding(new Insets(10));
        
        ChatClient cc = new ChatClient();
        cc.init();

        MenuBar menuBar = createMenuBar(primaryStage);
        Label labelInbox = new Label("Inbox: ");
        Label labelUsers = new Label("Korisnici: ");
        
        HBox hbox = new HBox(10);
        
        TextField receiver = new TextField();
        receiver.setPromptText("Unesite primaoca");
        
        TextField msg = new TextField();
        msg.setPromptText("Unesite sadrzaj poruke");
        
        Button send = new Button("Posalji");
        send.setOnAction(e -> {
        	ChatClient cc2 = new ChatClient();
        	cc2.init();
        	cc2.sendMessage(receiver.getText(), username, msg.getText());
        	cc2.end();
        	msg.setText("");
        	receiver.setText("");
        });
        
        TextArea inbox = new TextArea();
        inbox.setEditable(false);
        
        TextArea users = new TextArea();
        users.setEditable(false);
        
        Button refresh = new Button("Osvjezi");
        refresh.setOnAction(e -> {
        	ChatClient cc2 = new ChatClient();
        	cc2.init();
        	cc2.getUsers(users);
        	cc2.getMessages(username, inbox);
        	cc2.end();
        });
        
        hbox.getChildren().addAll(receiver, msg, send, refresh);
        
        cc.getUsers(users);
        cc.getMessages(username, inbox);
        
        layout.getChildren().addAll(menuBar, labelUsers, users, labelInbox, inbox, hbox);
        
        Scene scene = new Scene(layout, 700, 400);

        cc.end();
        primaryStage.setScene(scene);
        primaryStage.setTitle("Registracija");
        primaryStage.show();
    }
    
    
    
    public void createBookTableScene(Stage primaryStage) 
    {
    	MenuBar menu = createMenuBar(primaryStage);
        TableView<Book> tableView = new TableView<>();
        tableView.setEditable(true);
        ObservableList<Book> bookList = FXCollections.observableArrayList();
        
        TableColumn<Book, Boolean> selectColumn = new TableColumn<>("Izbor");

        selectColumn.setCellValueFactory(param -> {
            Book book = param.getValue();
            BooleanProperty selected = new SimpleBooleanProperty(selectedBooks.contains(book));  
            selected.addListener((observable, oldValue, newValue) -> {
                if (newValue) 
                {
                    selectedBooks.add(book);
                } 
                else 
                {
                    selectedBooks.remove(book);
                }
            });

            return selected;
        });

        selectColumn.setEditable(true);

        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setPrefWidth(50);

        TableColumn<Book, String> titleColumn = new TableColumn<>("Naslov");
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        titleColumn.setPrefWidth(200);

        TableColumn<Book, String> authorColumn = new TableColumn<>("Autor");
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        authorColumn.setPrefWidth(200);

        TableColumn<Book, String> publishDateColumn = new TableColumn<>("Datum");
        publishDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPublishDate()));
        publishDateColumn.setPrefWidth(200);

        TableColumn<Book, String> languageColumn = new TableColumn<>("Jezik");
        languageColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLanguage()));
        languageColumn.setPrefWidth(150);
        
        TableColumn<Book, String> countColumn = new TableColumn<>("Količina");
        countColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCount() + ""));
        countColumn.setPrefWidth(70);

        TableColumn<Book, Void> detailsCol = new TableColumn<>("Detalji");
        detailsCol.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = new Button("Detalji");
            {
                detailsButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    showBookDetails(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        detailsCol.setPrefWidth(60);
        
        TableColumn<Book, Void> downloadCol = new TableColumn<>("Preuzmi");
        downloadCol.setCellFactory(param -> new TableCell<>() {
            private final Button downloadButton = new Button("Preuzmi");
            {
                downloadButton.setOnAction(event -> {
                    Book book = getTableView().getItems().get(getIndex());
                    downloadBook(book);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(downloadButton);
                }
            }
        });
        downloadCol.setPrefWidth(70);

        tableView.getColumns().addAll(selectColumn, titleColumn, authorColumn, publishDateColumn, languageColumn, countColumn, detailsCol, downloadCol);
        tableView.setItems(bookList);

        TextField searchField = new TextField();
        searchField.setPromptText("Pretraži po naslovu...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterBooks(newVal, tableView, bookList));
        searchField.setMaxWidth(300);
        BooksClient bc = new BooksClient();
        List<Book> bks = bc.getBooksNoContent();

        bookList.addAll(bks);
        
        Button btnOrderBooks = new Button("Rezervisi knjige");
        btnOrderBooks.setOnAction(e -> {
        	System.out.println("Selektovane knjige:");
        	selectedBooks = (ArrayList<Book>) selectedBooks.stream()
            	                .distinct()
            	                .collect(Collectors.toList());
        	BooksClient bc2 = new BooksClient();
        	for (Book book : selectedBooks) 
        	{
        	    System.out.println(book.getTitle());
        	    int count = book.getCount();
        	    count--;
        	    book.setCount(count);
        	    bc2.addOrUpdateBook(book);
        	}
        	MailClient mc = new MailClient();
        	String msg = mc.sendMail(selectedBooks, email);
        	if("SENT".equals(msg))
        	{
        		Alert alert = new Alert(AlertType.INFORMATION);
        		alert.setTitle("Poruka");
        		alert.setContentText("Narudzba uspjesno proslijedjena!");
        		alert.showAndWait();
        	}
        	else
        	{
        		Alert alert = new Alert(AlertType.ERROR);
        		alert.setTitle("Poruka");
        		alert.setContentText("Greska pri proslijedjivanju narudzbe!");
        		alert.showAndWait();
        	}
        });

        VBox vbox = new VBox(10, menu, tableView, searchField, btnOrderBooks);
        Scene scene = new Scene(vbox, 1000, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Registracija");
        primaryStage.show();
    }
    
    private static void downloadBook(Book book)
    {
    	BooksClient bc = new BooksClient();
    	bc.downloadBook(book.getId());
    }

    private static void showBookDetails(Book book) 
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Detalji knjige");
        alert.setHeaderText(book.getTitle());

        System.out.println(book.getImageLink());
        Image img = ImageService.downloadImage(book.getImageLink());
        ByteArrayInputStream inputStream = new ByteArrayInputStream(img.getImageData());

        javafx.scene.image.Image fxImage = new javafx.scene.image.Image(inputStream);
        ImageView imageView = new ImageView(fxImage);
        imageView.setFitWidth(200);  // sirina slike
        imageView.setPreserveRatio(true);

        VBox imageContainer = new VBox(imageView);
        imageContainer.setAlignment(javafx.geometry.Pos.CENTER);  
        
        BooksClient bc = new BooksClient();
        
        List<String> lines = bc.getFirst100(book.getId());
        StringBuilder sb = new StringBuilder();
        
        for(String s : lines)
        {
        	sb.append(s + "\n");
        }

        String details = sb.toString();

        Label detailsLabel = new Label(details);
        detailsLabel.setWrapText(true); 

        VBox content = new VBox(15);
        content.setAlignment(javafx.geometry.Pos.CENTER);
        content.getChildren().addAll(imageContainer, detailsLabel);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true); 
        scrollPane.setPrefHeight(400);  

        alert.getDialogPane().setContent(scrollPane);
        alert.getDialogPane().setPrefWidth(450);

        alert.showAndWait();
    }

    // Filtriranje knjiga po naslovu
    /*private static void filterBooks(String searchText, TableView<Book> tableView, ObservableList<Book> originalList) {
        ObservableList<Book> filteredList = FXCollections.observableArrayList();
        for (Book book : originalList) {
            if (book.getTitle().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(book);
            }
        }
        tableView.setItems(filteredList);
    }*/
    
    private static void filterBooks(String searchText, TableView<Book> tableView, ObservableList<Book> originalList) 
    {
        ObservableList<Book> filteredList = FXCollections.observableArrayList();

        for (Book book : originalList) 
        {
            if (book.getTitle().toLowerCase().contains(searchText.toLowerCase())) 
            {
                filteredList.add(book);
            }
        }

        for (Book book : filteredList) 
        {
            if (selectedBooks.contains(book)) 
            {
                selectedBooks.add(book);
            }
        }
        
        tableView.setItems(filteredList);
    }
    
    private void startMulticastServer() 
    {
        System.out.println("Multicast server pokrenut.");
        try (MulticastSocket socket = new MulticastSocket(PORT)) 
        {
            InetAddress address = InetAddress.getByName(MULTICAST_ADDRESS);
            socket.joinGroup(address);

            byte[] buffer = new byte[1024];
            while (true) 
            {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet); 

                String message = new String(packet.getData(), 0, packet.getLength());

                Platform.runLater(() -> {
                    txtArea.appendText(message + "\n");
                });
            }
        } 
        catch (IOException e) 
        {
        	LoggerUtil.logException("Greska u multicast komunikaciji", e);
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
