package net.etf.gui;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.etf.dobavljac.client.Client;
import net.etf.dobavljac.client.ClientBiblioteka;
import net.etf.model.Account;
import net.etf.model.Book;
import net.etf.mq.Receiver;
import net.etf.rmi.RMIClient;

public class Main extends Application
{
	private List<Book> books;
	private static String currentMessage = "";
	
	@Override
    public void start(Stage primaryStage) 
	{
        primaryStage.setTitle("Meni sa knjigama");
        
        ClientBiblioteka clb = new ClientBiblioteka();
        clb.sendBooks(false);

        MenuBar menuBar = new MenuBar();

        MenuItem knjigeMenuItem = new MenuItem("Knjige");
        MenuItem narudzbeMenuItem = new MenuItem("Narudzbe");

        menuBar.getMenus().add(new Menu("Opcije", null, knjigeMenuItem, narudzbeMenuItem));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        showBooksContent(root);

        knjigeMenuItem.setOnAction(e -> showBooksContent(root));
        narudzbeMenuItem.setOnAction(e -> showContent(root, "Narudzbe"));

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
	
	public void showContent(BorderPane root, String section) 
	{
	    if (!"Narudzbe".equals(section)) return;

	    Button dohvatiNarudzbuBtn = new Button("Dohvati narudžbu");
	    dohvatiNarudzbuBtn.setDisable(false);

	    ListView<String> narudzbaListView = new ListView<>();
	    narudzbaListView.setPrefHeight(250);

	    Button prihvatiBtn = new Button("Prihvati");
	    Button odbijBtn = new Button("Odbij");
	    
	    prihvatiBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
	    odbijBtn.setStyle("-fx-background-color: red; -fx-text-fill: white;");
	    
	    HBox options = new HBox(10, prihvatiBtn, odbijBtn);
	    prihvatiBtn.setDisable(true);
	    odbijBtn.setDisable(true);

	    dohvatiNarudzbuBtn.setOnAction(e -> {
	    	String msg = Receiver.getOneMessage();
	        if(!"".equals(msg))
	        {
	        	currentMessage = msg;
	        	String req = "NAMES_" + msg;
	        	Client cl = new Client();
	        	ArrayList<String> items = cl.getSpecificBookNames(req); 
	        	narudzbaListView.getItems().setAll(items);

		        prihvatiBtn.setDisable(false);
		        odbijBtn.setDisable(false);

		        dohvatiNarudzbuBtn.setDisable(true);
	        }
	    });

	    prihvatiBtn.setOnAction(e -> {
	        System.out.println("Narudžba prihvaćena!");
	        if(!"".equals(currentMessage))
	        {
	        	Client cl = new Client();
	        	ArrayList<Book> books = cl.getSpecificBooks(currentMessage);
	        	ArrayList<String> items = cl.getSpecificBookNames("NAMES_" + currentMessage);
	        	Account acc = new Account();
	        	items.forEach(item -> {
	        		String[] params = item.split(" x ");
	        		acc.addBook(params[0], Integer.parseInt(params[1]));
	        	});
	        	for(int i = 0; i < books.size(); i++)
	        	{
	        		books.get(i).setCount(Integer.parseInt(items.get(i).split(" x ")[1]));
	        	}
	        	double pdv = RMIClient.sendAccount(acc);
	        	System.out.println(pdv);
	        	Alert alert = new Alert(AlertType.INFORMATION);
	        	alert.setTitle("Informacija");
	        	alert.setHeaderText(null);
	        	alert.setContentText("Iznos PDV-a je " + pdv + " KM");
	        	alert.showAndWait();
	        	ClientBiblioteka cb = new ClientBiblioteka();
	        	cb.sendOrder(books);
	        }
	        resetForm(dohvatiNarudzbuBtn, narudzbaListView, prihvatiBtn, odbijBtn);
	    });

	    odbijBtn.setOnAction(e -> {
	        System.out.println("Narudžba odbijena!");
	        resetForm(dohvatiNarudzbuBtn, narudzbaListView, prihvatiBtn, odbijBtn);
	    });

	    VBox layout = new VBox(10, dohvatiNarudzbuBtn, narudzbaListView, options);
	    layout.setStyle("-fx-padding: 20px;");
	    
	    root.setCenter(layout);
	}

	private void resetForm(Button dohvatiNarudzbuBtn, ListView<String> narudzbaListView, Button prihvatiBtn, Button odbijBtn) {
	    narudzbaListView.getItems().clear();
	    prihvatiBtn.setDisable(true);
	    odbijBtn.setDisable(true);
	    dohvatiNarudzbuBtn.setDisable(false);
	}

    private void showBooksContent(BorderPane root) {
        TableView<Book> tableView = new TableView<>();

        TableColumn<Book, String> titleColumn = new TableColumn<>("Naslov");
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));

        TableColumn<Book, String> authorColumn = new TableColumn<>("Autor");
        authorColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAuthor()));

        TableColumn<Book, String> publishDateColumn = new TableColumn<>("Datum");
        publishDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPublishDate()));

        TableColumn<Book, String> languageColumn = new TableColumn<>("Jezik");
        languageColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLanguage()));
        
        double tableWidth = tableView.getWidth();
        titleColumn.setPrefWidth(200); 
        authorColumn.setPrefWidth(200);
        publishDateColumn.setPrefWidth(200);
        languageColumn.setPrefWidth(150);

        tableView.getColumns().addAll(titleColumn, authorColumn, publishDateColumn, languageColumn);

        Client cl = new Client();
        books = cl.connect();
        
        tableView.getItems().setAll(books);

        TextField linkInputField = new TextField();
        linkInputField.setMinWidth(200);
        linkInputField.setPromptText("Unesite link");
        Button submitButton = new Button("Submit");
        
        submitButton.setOnAction(e -> {
            String link = linkInputField.getText(); 
            linkInputField.setText("");
            Book temp = cl.addBook(link);
            books.add(temp);
            tableView.getItems().setAll(books);
        });
        
        HBox inputBox = new HBox(10, linkInputField, submitButton);
        inputBox.setStyle("-fx-padding: 10px;");

        VBox vbox = new VBox(10, tableView, inputBox);
        vbox.setStyle("-fx-padding: 20px;");
        vbox.setSpacing(10);

        root.setCenter(vbox);
    }
	public static void main(String[] args) 
	{
        launch(args);
    }
}
