package net.etf.gui;

import java.awt.Image;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.etf.client.BookClient;
import net.etf.client.LocalClient;
import net.etf.client.UserClient;
import net.etf.model.Book;
import net.etf.model.User;
import net.etf.mq.Sender;
import net.etf.multicast.MulticastClient;
import net.etf.service.ImageService;
import net.etf.service.LoggerUtil;

public class Main extends Application {

    private ArrayList<String> order = new ArrayList<>();
    private ArrayList<String> orderView = new ArrayList<>();
    private ObservableList<User> users;
    private VBox orderBox;
    private Label titleLabel;
    private TextField fieldCount;
    private TableView<Book> tableView;
    private TableView<User> tableViewUser;
    private TextArea txtArea;
    
    public static final String MULTICAST_ADDRESS;
    public static final int PORT;  
    
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
    
    private Stage primaryStage; 

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage; // Čuvamo referencu
        primaryStage.setScene(createMainScene());
        primaryStage.setTitle("Knjige - Narudžba");
        primaryStage.show();
        Thread ms = new Thread(this::startMulticastServer);
        ms.setDaemon(true);
        ms.start();
    }

    private Scene createMainScene() {
        VBox layout = new VBox(0);
        //layout.setPadding(new Insets(10));

        MenuBar menuBar = createMenuBar();
        tableView = createTableView(true);
        HBox orderControls = createOrderControls();
        orderBox = createOrderBox();
        Button btnPosalji = createSendOrderButton();
        
        orderControls.setPadding(new Insets(10));
        orderBox.setPadding(new Insets(10));
        VBox.setMargin(btnPosalji, new Insets(10));

        layout.getChildren().addAll(menuBar, tableView, orderControls, orderBox, btnPosalji);
        return new Scene(layout, 820, 700);
    }

    private Scene createMulticastScene() 
    {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(10));

        MenuBar menuBar = createMenuBar();
        Label label = new Label("Multicast komunikacija");
        
        HBox hbox = new HBox(10);
        
        TextField msg = new TextField();
        msg.setPromptText("Unesite poruku");
        
        Button send = new Button("Posalji");
        send.setOnAction(e -> {
        	MulticastClient mc = new MulticastClient();
        	mc.sendMessage(msg.getText());
        	msg.clear();
        });
        
        hbox.getChildren().addAll(msg, send);
        
        txtArea = new TextArea();
        txtArea.setEditable(false);
        
        
        layout.getChildren().addAll(menuBar, label, hbox, txtArea);
        return new Scene(layout, 820, 700);
    }
    
    private Scene createClanoviScene() 
    {
        VBox layout = new VBox();

        MenuBar menuBar = createMenuBar();
        Label label = new Label("Clanovi");
        
        tableViewUser = createTableUser(true);
        
        HBox controls = createUserControls();
        
        layout.getChildren().addAll(menuBar, label, tableViewUser, controls);
        return new Scene(layout, 820, 700);
    }
    
    private Scene createOdobravanjeScene() 
    {
        VBox layout = new VBox();

        MenuBar menuBar = createMenuBar();
        Label label = new Label("Odobravanje");
        
        tableViewUser = createTableUser(false);
        
        HBox controls = createUserAllowControls();
        
        layout.getChildren().addAll(menuBar, label, tableViewUser, controls);
        return new Scene(layout, 820, 700);
    }

    private Scene createBooksScene() 
    {
        VBox layout = new VBox(0);

        MenuBar menuBar = createMenuBar();
        tableView = createTableView(false);

        Label lblDodajKnjigu = new Label("Dodaj knjigu:");
        lblDodajKnjigu.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnDodaj = new Button("Dodaj knjigu");
        btnDodaj.setOnAction(e -> prikaziFormuZaDodavanjeKnjige());
        
        VBox formaDodaj = new VBox(5, lblDodajKnjigu, btnDodaj);
        formaDodaj.setPadding(new Insets(10));
        formaDodaj.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 10px;");

        Separator separator1 = new Separator(); 

        Label lblUpravljanje = new Label("Upravljanje knjigama:");
        lblUpravljanje.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button btnIzmjeni = new Button("Izmjeni");
        Button btnObrisi = new Button("Obriši");

        btnIzmjeni.setOnAction(e -> izmjeniKnjigu());
        btnObrisi.setOnAction(e -> obrisiKnjigu());

        HBox btnBox = new HBox(10, btnIzmjeni, btnObrisi);
        btnBox.setPadding(new Insets(10));

        VBox upravljanjeKnjigama = new VBox(5, lblUpravljanje, btnBox);
        upravljanjeKnjigama.setPadding(new Insets(10));
        upravljanjeKnjigama.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-padding: 10px;");

        Separator separator2 = new Separator();
        
        layout.getChildren().addAll(menuBar, tableView, separator1, formaDodaj, separator2, upravljanjeKnjigama);

        return new Scene(layout, 820, 700);
    }
    
    private HBox createUserAllowControls()
    {
    	Button btnAllow = new Button("Dozvoli");
    	Button btnDelete = new Button("Obrisi");
    	
        titleLabel = new Label();
        tableViewUser.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titleLabel.setText(newSelection.getUsername());
                btnDelete.setDisable(false);
                btnAllow.setDisable(false);
                btnDelete.setOnAction(e -> {
                	UserClient uc = new UserClient();
                	uc.deleteUser(newSelection.getUsername());
                	btnDelete.setDisable(true);
                });
                
                btnAllow.setOnAction(e -> {
                	UserClient uc = new UserClient();
                	uc.allowUser(newSelection.getUsername());
                	tableViewUser.getSelectionModel().clearSelection();
                	titleLabel.setText("");
                	users.remove(newSelection);
                	btnAllow.setDisable(true);
                	btnDelete.setDisable(true);
                	tableViewUser.refresh();
                });
            }
        });

        HBox hbox = new HBox(15, titleLabel, btnAllow, btnDelete);
        hbox.setPadding(new Insets(10));
        return hbox;
    }
    
    private HBox createUserControls() 
    {
    	Button btnBlock = new Button("Blokiraj");
    	Button btnUnblock = new Button("Odblokiraj");
    	Button btnDelete = new Button("Obrisi");
    	
        titleLabel = new Label();
        tableViewUser.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titleLabel.setText(newSelection.getUsername());
                btnDelete.setDisable(false);
                btnDelete.setOnAction(e -> {
                	UserClient uc = new UserClient();
                	uc.deleteUser(newSelection.getUsername());
                	btnDelete.setDisable(true);
                });
                
                btnUnblock.setOnAction(e -> {
                	UserClient uc = new UserClient();
                	uc.unblockUser(newSelection.getUsername());
                	tableViewUser.getSelectionModel().clearSelection();
                	titleLabel.setText("");
                	newSelection.setBlocked(false);
                	btnBlock.setDisable(true);
                	btnUnblock.setDisable(true);
                	tableViewUser.refresh();
                });
                
                btnBlock.setOnAction(e -> {
                	UserClient uc = new UserClient();
                	uc.blockUser(newSelection.getUsername());
                	tableViewUser.getSelectionModel().clearSelection();
                	titleLabel.setText("");
                	newSelection.setBlocked(true);
                	btnBlock.setDisable(true);
                	btnUnblock.setDisable(true);
                	tableViewUser.refresh();
                });
                
                if(newSelection.isBlocked())
                {
                	btnBlock.setDisable(true);
                	btnUnblock.setDisable(false);
                }
                else
                {
                	btnBlock.setDisable(false);
                	btnUnblock.setDisable(true);
                }
            }
        });

        HBox hbox = new HBox(15, titleLabel, btnBlock, btnUnblock, btnDelete);
        hbox.setPadding(new Insets(10));
        return hbox;
    }

    private void prikaziFormuZaDodavanjeKnjige() 
    {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Dodaj knjigu");

        VBox vbox = new VBox(10);

        Label lblNaslov = new Label("Naslov:");
        TextField txtNaslov = new TextField();
        
        Label lblAutor = new Label("Autor:");
        TextField txtAutor = new TextField();
        
        Label lblDatum = new Label("Datum:");
        TextField dpDatum = new TextField();
        
        Label lblJezik = new Label("Jezik:");
        TextField txtJezik = new TextField();
        
        Label lblBrojPrimjeraka = new Label("Broj primjeraka:");
        TextField txtBrojPrimjeraka = new TextField();
        
        Label lblLinkSlike = new Label("Link slike:");
        TextField txtLinkSlike = new TextField();
        
        Label lblSadrzaj = new Label("Sadržaj:");
        TextArea txtSadrzaj = new TextArea();
        txtSadrzaj.setPrefRowCount(5);
        
        vbox.getChildren().addAll(lblNaslov, txtNaslov, lblAutor, txtAutor, lblDatum, dpDatum, lblJezik, txtJezik,
                                  lblBrojPrimjeraka, txtBrojPrimjeraka, lblLinkSlike, txtLinkSlike, lblSadrzaj, txtSadrzaj);

        ButtonType btnDodaj = new ButtonType("Dodaj", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Otkaži", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(btnDodaj, btnCancel);
        
        dialog.getDialogPane().setContent(vbox);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnDodaj) {
                dodajKnjigu(txtNaslov, txtAutor, dpDatum, txtJezik, txtBrojPrimjeraka, txtLinkSlike, txtSadrzaj);
            }
            return null;
        });
        
        dialog.showAndWait();
    }

    
    private void dodajKnjigu(TextField naslov, TextField autor, TextField datum, TextField jezik, TextField brojPrimjeraka, TextField link, TextArea sadrzaj) 
    {
        System.out.println("Dodano: " + naslov.getText());
        Book book = new Book(naslov.getText(), System.currentTimeMillis(), autor.getText(), datum.getText(), jezik.getText(), null, Integer.parseInt(brojPrimjeraka.getText()));
        book.setFileContent(sadrzaj.getText().getBytes());
        book.setImageLink(link.getText());
        BookClient bc = new BookClient();
        bc.addOrUpdateBook(book);
        tableView.setItems(FXCollections.observableArrayList(new BookClient().getBooks()));
    }

    private void izmjeniKnjigu() 
    {
        Book selectedBook = tableView.getSelectionModel().getSelectedItem();
        /*if (selectedBook == null) {
            showAlert("Greška", "Morate izabrati knjigu za izmenu!");
            return;
        }*/
        System.out.println(selectedBook.getImageLink());
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Izmjena knjige");

        GridPane form = new GridPane();
        form.setPadding(new Insets(10));
        form.setHgap(10);
        form.setVgap(10);

        TextField txtNaslov = new TextField(selectedBook.getTitle());
        TextField txtAutor = new TextField(selectedBook.getAuthor());
        TextField dpDatum = new TextField(selectedBook.getPublishDate());
        TextField txtJezik = new TextField(selectedBook.getLanguage());
        TextField txtBrojPrimjeraka = new TextField(String.valueOf(selectedBook.getCount()));
        TextField txtLinkSlike = new TextField(selectedBook.getImageLink());
        Charset charset = Charset.forName("UTF-8");
        TextArea txtSadrzaj = new TextArea(new String(selectedBook.getFileContent(), charset));
        
        Button btnSacuvaj = new Button("Sačuvaj");
        btnSacuvaj.setOnAction(e -> {
            selectedBook.setTitle(txtNaslov.getText());
            selectedBook.setAuthor(txtAutor.getText());
            selectedBook.setPublishDate(dpDatum.getText());
            selectedBook.setLanguage(txtJezik.getText());
            selectedBook.setCount(Integer.parseInt(txtBrojPrimjeraka.getText()));
            //selectedBook.setLinkSlike(txtLinkSlike.getText());
            selectedBook.setImageLink(txtLinkSlike.getText());
            selectedBook.setFileContent(txtSadrzaj.getText().getBytes());
            
            BookClient bc = new BookClient();
            bc.addOrUpdateBook(selectedBook);
            
            tableView.refresh();
            popupStage.close();
        });
        
        

        VBox vbox = new VBox(10, txtNaslov, txtAutor, dpDatum, txtJezik, txtLinkSlike, txtBrojPrimjeraka,txtSadrzaj, btnSacuvaj);
        vbox.setPadding(new Insets(10));
        Scene scene = new Scene(vbox, 400, 500);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    
    private void obrisiKnjigu() 
    {
        Book selectedBook = tableView.getSelectionModel().getSelectedItem();
        if (selectedBook != null) 
        {
            System.out.println("Obrisana knjiga: " + selectedBook.getTitle());
            tableView.getItems().remove(selectedBook);
            BookClient bc = new BookClient();
            System.out.println(bc.deleteBook(selectedBook.getId()));
        } 
        else 
        {
            System.out.println("Nema selektovane knjige!");
        }
    }



    private MenuBar createMenuBar() 
    {
        MenuBar menuBar = new MenuBar();
        Menu menuOpcije = new Menu("Knjige");
        Menu menuClanovi = new Menu("Članovi");

        MenuItem menuNarudzba = new MenuItem("Slanje narudžbe");
        MenuItem menuMulticast = new MenuItem("Multicast");
        MenuItem menuKnjige = new MenuItem("Knjige");
        MenuItem menuRegistrovaniClanovi = new MenuItem("Registrovani članovi");
        MenuItem menuOdobravanje = new MenuItem("Prihvatanje članova");

        menuNarudzba.setOnAction(e -> primaryStage.setScene(createMainScene()));
        menuMulticast.setOnAction(e -> primaryStage.setScene(createMulticastScene()));
        menuKnjige.setOnAction(e -> primaryStage.setScene(createBooksScene()));
        menuRegistrovaniClanovi.setOnAction(e -> primaryStage.setScene(createClanoviScene()));
        menuOdobravanje.setOnAction(e -> primaryStage.setScene(createOdobravanjeScene()));

        menuOpcije.getItems().addAll(menuNarudzba, menuMulticast, menuKnjige);
        menuClanovi.getItems().addAll(menuRegistrovaniClanovi, menuOdobravanje);
        menuBar.getMenus().addAll(menuOpcije, menuClanovi);

        return menuBar;
    }
    
    private TableView<User> createTableUser(boolean allowed) 
    {
        TableView<User> tableView = new TableView<>();

        TableColumn<User, String> nameColumn = new TableColumn<>("Ime");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(100);

        TableColumn<User, String> surnameColumn = new TableColumn<>("Prezime");
        surnameColumn.setCellValueFactory(new PropertyValueFactory<>("surname"));
        surnameColumn.setPrefWidth(150);

        TableColumn<User, String> addressColumn = new TableColumn<>("Adresa");
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        addressColumn.setPrefWidth(170);

        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(160);

        TableColumn<User, String> usernameColumn = new TableColumn<>("Korisničko ime");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setPrefWidth(170);
        
        TableColumn<User, String> blockedColumn = new TableColumn<>("Blokiran");
        blockedColumn.setCellValueFactory(cellData -> {
            boolean isBlocked = cellData.getValue().isBlocked();
            return new SimpleStringProperty(isBlocked ? "Da" : "Ne");
        });
        blockedColumn.setPrefWidth(80);

        tableView.getColumns().addAll(nameColumn, surnameColumn, addressColumn, emailColumn, usernameColumn, blockedColumn);

        users = FXCollections.observableArrayList();
        UserClient uc = new UserClient();
        List<User> allUsers;
        if(allowed)
        {
        	allUsers = uc.getAllowedUsers();
        }
        else
        {
        	allUsers = uc.getNotAllowedUsers();
        }

        for (User user : allUsers) 
        {
        	System.out.println(user);
        	users.add(user);
        }

        tableView.setItems(users);
        return tableView;
    }


    private TableView<Book> createTableView(boolean narudzba) 
    {
        TableView<Book> tableView = new TableView<>();

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

        if(narudzba)
        {
        	tableView.getColumns().addAll(titleColumn, authorColumn, publishDateColumn, languageColumn);
        	tableView.setItems(FXCollections.observableArrayList(new LocalClient().getBooks()));
        }
        else
        {
        	tableView.getColumns().addAll(titleColumn, authorColumn, publishDateColumn, languageColumn, countColumn);
        	tableView.setItems(FXCollections.observableArrayList(new BookClient().getBooks()));
        }
        
        
        tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        return tableView;
    }

    private HBox createOrderControls() 
    {
        titleLabel = new Label();
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                titleLabel.setText(newSelection.getTitle());
            }
        });

        fieldCount = new TextField();
        fieldCount.setMinWidth(200);
        fieldCount.setPromptText("Unesite količinu");

        Button btnDodaj = new Button("Dodaj u selektovane");
        btnDodaj.setOnAction(e -> addToOrder());

        HBox hbox = new HBox(15, titleLabel, fieldCount, btnDodaj);
        hbox.setPadding(new Insets(10));
        return hbox;
    }

    private VBox createOrderBox() 
    {
        VBox orderBox = new VBox(10);
        Label orderLabel = new Label("Narudžba:");
        orderLabel.setStyle("-fx-font-size: 20px;");
        orderBox.getChildren().add(orderLabel);
        return orderBox;
    }

    private Button createSendOrderButton() 
    {
        Button btnPosalji = new Button("Pošalji narudžbu");
        btnPosalji.setOnAction(e -> {
        	order.forEach(System.out::println);
        	String msg = "GET_BOOKS";
        	for(String s : order)
        	{
        		msg += s;
        	}
        	Sender.sendMessage(msg);
        	orderBox.getChildren().clear();
        	order = new ArrayList<String>();
        });
        return btnPosalji;
    }

    private void addToOrder() 
    {
        Book b = tableView.getSelectionModel().getSelectedItem();
        if (b != null && !"".equals(fieldCount.getText())) {
            order.add("#" + b.getId() + "," + fieldCount.getText());
            orderView.add(b.getTitle() + " x " + fieldCount.getText());

            Label label = new Label(b.getTitle() + " x " + fieldCount.getText());
            orderBox.getChildren().add(label);
            fieldCount.setText("");
            tableView.getSelectionModel().clearSelection();
        }
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

    public static void main(String[] args) 
    {
        launch(args);
    }
}
