import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.bson.Document;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Train extends Application {

    final static int SEAT_CAPACITI = 42;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        //[2] means to badull & to colombo two destinations (two trips)
        //[30] means customer can book seat for 30 days
        //[42] rows number of seats
        //[6] user details 6(name,surnamee,seat number,NIC,contact number,address,email)
        String[][][][] booking = new String[2][30][SEAT_CAPACITI][7];

        //destinations
        String[] destination = {"To Badulla", "To Colombo"};

        // database connectivity
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE); // e.g. or Log.WARNING, etc.
        MongoClient mongo = new MongoClient("localhost", 27017);
        MongoDatabase database = mongo.getDatabase("dumbaraManikeTrain");
        MongoCollection<Document> toBadulla = database.getCollection("badulla");
        MongoCollection<Document> toColombo = database.getCollection("Colombo");
        Document document = new Document();

        //get dates
        LocalDate minDate = LocalDate.now().plusDays(1);    //book start date
        LocalDate maxDate = LocalDate.now().plusDays(30);   //book end date
        LocalDate nowDate = LocalDate.now();
        System.out.println("Today date : " + nowDate);
        Scanner sc = new Scanner(System.in);
        menu:
        while (true) {
            System.out.println("***************************************************************************");
            System.out.println("============= DENUWARA MANIKE A/C COMPARTMENT SEAT BOOKING ================");
            System.out.println("***************************************************************************\n");
            System.out.println("\"A\" Add customer seat ");
            System.out.println("\"V\" View all seats ");
            System.out.println("\"E\" View empty seat ");
            System.out.println("\"D\" Delete customer from seat ");
            System.out.println("\"F\" Find the seat for given customer name ");
            System.out.println("\"S\" Store data ");
            System.out.println("\"L\" Load data ");
            System.out.println("\"O\" View seats ordered alphabetically by name ");
            System.out.println("\"Q\" exit \n");

            System.out.print("Enter your option : ");
            //option contain what the user chose and make the input to lower case
            String option = sc.next().toLowerCase();

            //use the switch case to call other method according to user choice
            switch (option) {
                case "a":
                    addCustomer(minDate, maxDate, destination, nowDate, booking);
                    break;
                case "v":
                    viewAll(minDate, maxDate, destination, nowDate, booking);
                    break;
                case "e":
                    viewEmpty(minDate, maxDate, destination, nowDate, booking);
                    break;
                case "d":
                    deleteCustomer(sc, minDate, maxDate, destination, nowDate, booking);
                    break;
                case "f":
                    findCustomer(sc, minDate, maxDate, destination, nowDate, booking);
                    break;
                case "s":
                    storeData(booking, nowDate, toBadulla, toColombo, document);
                    break;
                case "l":
                    loadData(booking, nowDate, toBadulla, toColombo, document);
                    break;
                case "o":
                    sorting(minDate, maxDate, destination, nowDate, booking);
                    break;
                case "q":
                    break menu;
                default:
                    System.out.println("Please enter correct input!!");
            }
        }
    }

    private void addCustomer(LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking) {
        Stage stage = new Stage();
        Pane pane = new Pane();
        Label label2 = new Label("select date and destination");
        Label label = new Label("you can book seat between" + now.plusDays(1) + " and " + now.plusDays(30) + " dates");
        label.setStyle("-fx-font-size: 16px");
        label2.setStyle("-fx-font-size: 16px");
        Label labelDate = new Label("Select Date : ");
        Label labelDest = new Label("Select destination : ");
        DatePicker datePicker = new DatePicker();
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(maxDate) || date.isBefore(minDate));
            }
        });
        final LocalDate[] selectDate = new LocalDate[1];
        final int[] diff = new int[1];
        EventHandler<ActionEvent> eventDate = event -> {
            selectDate[0] = datePicker.getValue();//get date from picker and send that to array
            diff[0] = Period.between(now, selectDate[0]).getDays(); //get difference between today and booking end date
        };
        label.setLayoutX(40);
        label.setLayoutY(100);
        label2.setLayoutX(75);
        label2.setLayoutY(150);

        datePicker.setLayoutX(225);
        datePicker.setLayoutY(250);
        labelDate.setLayoutX(50);
        labelDate.setLayoutY(250);

        ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(destination));
        final int[] x = new int[1];
        final String[] y = new String[1];
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                y[0] = combo_box.getValue();
                if (combo_box.getValue().equals("To Badulla")) {
                    x[0] = 1;//colmbo to badulla
                } else {
                    x[0] = 2;//baduulla to colombo
                }
            }
        };
        combo_box.setLayoutX(225);
        combo_box.setLayoutY(330);
        labelDest.setLayoutX(50);
        labelDest.setLayoutY(330);
        Button bookButton = new Button("BOOK A SEAT");
        Button closeButton = new Button("go to menu");
        bookButton.setLayoutX(275);
        bookButton.setLayoutY(550);
        closeButton.setLayoutX(175);
        closeButton.setLayoutY(550);
        closeButton.setOnAction(e -> {
            stage.close();
        });
        combo_box.setOnAction(event);
        datePicker.setOnAction(eventDate);
        pane.getChildren().addAll(combo_box, datePicker, labelDate, labelDest, bookButton, closeButton, label, label2);
        Scene scene = new Scene(pane, 600, 600);

        bookButton.setOnAction(e -> {
            //check the user has selected date & destination
            if (diff[0] == 0 || x[0] == 0) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Please select destination and date ");
                a.show();
            } else {
                addSeat(stage, scene, diff[0], selectDate[0], x[0], booking, y[0]);
            }
        });
        stage.setScene(scene);
        stage.setTitle("Book seat");
        stage.showAndWait();

    }

    //add customer seat
    private void addSeat(Stage stage, Scene scene, int diff, LocalDate date, int x, String[][][][] book, String desta) {
        Pane pane = new Pane();
        Label label = new Label("BOOK SEAT " + desta.toUpperCase() + " ON " + date);
        Label name = new Label("first name*: ");
        Label sName = new Label("surname*: ");
        Label address = new Label("Address* :");
        Label contact = new Label("Contact(TP)* :");
        Label email = new Label("Email* :");
        Label idNum = new Label("NIC* :");
        Label slSeat = new Label("Select A seat* :");
        //add text filelds to get data from user
        TextField nameText = new TextField("");
        TextField snameText = new TextField("");
        TextField addressText = new TextField("");
        TextField contactText = new TextField("");
        TextField emailText = new TextField("");
        TextField idText = new TextField("");
        Button backB = new Button("back");
        Button closeStage = new Button("Go to menu");
        Button booking = new Button("booking");

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        //setChoiceBox(choiceBox, book, x, diff);
        for (int i = 1; i <= SEAT_CAPACITI; i++) {
            if (book[x - 1][diff - 1][i - 1][0] == null) {
                choiceBox.getItems().add(String.valueOf(i));
            }
        }
        pane.getChildren().addAll(sName, snameText, idText, idNum, slSeat, name, address, contact, nameText, addressText, contactText, email, emailText, backB, closeStage, label, booking, choiceBox);
        label.setLayoutX(60);
        label.setStyle("-fx-font-size: 20px");
        name.setLayoutX(10);
        name.setLayoutY(40);
        sName.setLayoutX(10);
        sName.setLayoutY(70);
        idNum.setLayoutX(10);
        idNum.setLayoutY(100);
        address.setLayoutX(10);
        address.setLayoutY(130);
        email.setLayoutX(10);
        email.setLayoutY(190);
        contact.setLayoutX(10);
        contact.setLayoutY(160);

        nameText.setLayoutX(130);
        nameText.setLayoutY(40);
        snameText.setLayoutX(130);
        snameText.setLayoutY(70);
        idText.setLayoutX(130);
        idText.setLayoutY(100);
        addressText.setLayoutX(130);
        addressText.setLayoutY(130);
        contactText.setLayoutX(130);
        contactText.setLayoutY(160);
        emailText.setLayoutX(130);
        emailText.setLayoutY(190);
        choiceBox.setLayoutX(170);
        choiceBox.setLayoutY(230);
        choiceBox.setStyle("-fx-pref-width: 180");
        backB.setLayoutX(40);
        backB.setLayoutY(310);
        closeStage.setLayoutX(100);
        closeStage.setLayoutY(310);
        slSeat.setLayoutX(50);
        slSeat.setLayoutY(230);
        booking.setLayoutX(350);
        booking.setLayoutY(310);
        backB.setOnAction(e -> {
            stage.setScene(scene);
        });
        closeStage.setOnAction(e -> {
            stage.close();
        });
        booking.setOnAction(e -> {
            String uName = nameText.getText();
            String surName = snameText.getText();
            String uCn = contactText.getText();
            String uAddress = addressText.getText();
            String uEmail = emailText.getText();
            String sNum = choiceBox.getValue();
            String id = idText.getText();

            //data validation part
            if (uName.equals("") || surName.equals("") || uAddress.equals("") || uCn.equals("") || uEmail.equals("") || sNum == null || idNum.equals("")) {
                Alert a = new Alert(Alert.AlertType.NONE);
                a.setAlertType(Alert.AlertType.ERROR);
                a.setContentText("fill all ");
                a.show();
            } else {
                if (!uCn.matches("[0-9]+") || uCn.length() != 10 || id.length() < 10) {
                    Alert a = new Alert(Alert.AlertType.NONE);
                    a.setAlertType(Alert.AlertType.ERROR);
                    a.setContentText("Fill contact number or id correctly ");
                    a.show();

                } else {
                    addToArray(sNum, uName, surName, id, uCn, uEmail, uAddress, book, x, diff, stage, scene);

                }
            }

        });
        Scene scene1 = new Scene(pane, 650, 400);
        stage.setScene(scene1);

    }

    //send data to array
    private void addToArray(String seatNum, String name, String surname, String id, String contact, String email, String address, String[][][][] book, int x, int diff, Stage stage, Scene scene) {
        showAlert(name, seatNum);
        // 0 = seat number , 1= name , 2 = nic , 3 = address, 4 = contact , 5 = email
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][0] = seatNum;
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][1] = name;
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][2] = surname;
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][3] = id;
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][5] = address;
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][4] = contact;
        book[x - 1][diff - 1][Integer.parseInt(seatNum) - 1][6] = email;
        stage.setScene(scene);
    }

    private void showAlert(String name, String sn) {
        Alert a = new Alert(Alert.AlertType.NONE);
        a.setAlertType(Alert.AlertType.INFORMATION);
        a.setContentText(sn + " seat booked to " + name);
        a.showAndWait();
    }

    //view all seats
    private void viewAll(LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking) {
        Stage stage = new Stage();
        Pane pane = new Pane();
        Label label2 = new Label("select date and destination");
        Label label = new Label("you can view all seats between" + now.plusDays(1) + " and " + now.plusDays(30) + " dates");
        label.setStyle("-fx-font-size: 16px");
        label2.setStyle("-fx-font-size: 16px");
        Label labelDate = new Label("Select Date : ");
        Label labelDest = new Label("Select destination : ");
        DatePicker datePicker = new DatePicker();
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(maxDate) || date.isBefore(minDate));
            }
        });
        final LocalDate[] selectDate = new LocalDate[1];
        final int[] diff = new int[1];
        EventHandler<ActionEvent> eventDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selectDate[0] = datePicker.getValue();
                diff[0] = Period.between(now, selectDate[0]).getDays();//get day difference between today and select date
            }
        };
        label.setLayoutX(40);
        label.setLayoutY(100);
        label2.setLayoutX(75);
        label2.setLayoutY(150);
        datePicker.setLayoutX(225);
        datePicker.setLayoutY(250);
        labelDate.setLayoutX(50);
        labelDate.setLayoutY(250);

        ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(destination));
        final int[] x = new int[1];//destination selector
        final String[] y = new String[1];
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                y[0] = combo_box.getValue();
                if (combo_box.getValue().equals("To Badulla")) {
                    x[0] = 1;
                } else {
                    x[0] = 2;
                }
            }
        };
        combo_box.setLayoutX(225);
        combo_box.setLayoutY(330);
        labelDest.setLayoutX(50);
        labelDest.setLayoutY(330);
        Button bookButton = new Button("VIEW SEATS");
        Button closeButton = new Button("go to menu");
        bookButton.setLayoutX(275);
        bookButton.setLayoutY(550);
        closeButton.setLayoutX(175);
        closeButton.setLayoutY(550);

        closeButton.setOnAction(e -> {
            stage.close();
        });

        combo_box.setOnAction(event);
        datePicker.setOnAction(eventDate);
        pane.getChildren().addAll(combo_box, datePicker, labelDate, labelDest, bookButton, closeButton, label, label2);
        Scene scene = new Scene(pane, 600, 600);

        bookButton.setOnAction(e -> {
            if (diff[0] == 0 || x[0] == 0) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Please select destination and date ");
                a.show();
            } else {
                viewSeat(stage, scene, diff[0], selectDate[0], x[0], booking, y[0]);
            }
        });
        stage.setScene(scene);
        stage.setTitle("View seats");
        stage.showAndWait();
    }

    private void viewSeat(Stage stage, Scene scene, int diff, LocalDate date, int x, String[][][][] book, String desta) {
        BorderPane root = new BorderPane();
        Pane pane = new Pane();
        FlowPane flowpane = new FlowPane();
        pane.setMaxHeight(400);
        flowpane.setMaxWidth(650);
        flowpane.setMaxHeight(500);
        root.setCenter(pane);
        root.setBottom(flowpane);
        flowpane.setVgap(8);
        flowpane.setHgap(8);
        setPaneView(stage, pane, scene);
        setFlowPane(flowpane, diff, x, book);
        flowpane.setPadding(new Insets(10, 0, 15, 100));
        Scene scene1 = new Scene(root, 750, 650);
        stage.setScene(scene1);
    }

    private void setFlowPane(FlowPane flowPane, int diff, int x, String[][][][] book) {
        for (int i = 0; i < SEAT_CAPACITI; i++) {
            if (book[x - 1][diff - 1][i][0] == null) {
                Button button = new Button(String.valueOf(i + 1));
                flowPane.getChildren().add(button);
                //set Style for buttons
                button.setStyle("-fx-background-color: #98caff;-fx-min-width: 80px");
            } else {
                Button button = new Button(String.valueOf(i + 1));
                flowPane.getChildren().add(button);
                //set Style for buttons
                button.setStyle("-fx-background-color: #ff563c;-fx-min-width: 80px");
            }
        }
    }

    private void setPaneView(Stage addStage, Pane pane, Scene scn) {
        Label avBtn = new Label("Available seats");
        Label bBtn = new Label("Booked seats");
        //buttons
        Button backB = new Button("back");
        Button closeStage = new Button("Go to menu");
        Button bookedS = new Button();
        Button avS = new Button();
        pane.getChildren().addAll(avBtn, bBtn, backB, closeStage, bookedS, avS);
        avBtn.setLayoutX(30);
        avBtn.setLayoutY(60);
        bookedS.setLayoutX(150);
        bookedS.setLayoutY(60);
        bookedS.setStyle("-fx-background-color: #98caff; ");
        bBtn.setLayoutX(30);
        bBtn.setLayoutY(100);
        avS.setStyle("-fx-background-color: #ff622e; ");
        avS.setLayoutX(150);
        avS.setLayoutY(100);
        backB.setLayoutX(40);
        backB.setLayoutY(300);
        closeStage.setLayoutX(100);
        closeStage.setLayoutY(300);
        backB.setOnAction(e -> addStage.setScene(scn));
        closeStage.setOnAction(e -> addStage.close());
    }

    //show empty seats
    private void viewEmpty(LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking) {
        Stage stage = new Stage();
        Pane pane = new Pane();
        Label label2 = new Label("select date and destination");
        Label label = new Label("you can view empty seats between" + now.plusDays(1) + " and " + now.plusDays(30) + " dates");
        label.setStyle("-fx-font-size: 16px");
        label2.setStyle("-fx-font-size: 16px");
        Label labelDate = new Label("Select Date : ");
        Label labelDest = new Label("Select destination : ");
        DatePicker datePicker = new DatePicker();
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(maxDate) || date.isBefore(minDate));
            }
        });
        final LocalDate[] selectDate = new LocalDate[1];
        final int[] diff = new int[1];
        EventHandler<ActionEvent> eventDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selectDate[0] = datePicker.getValue();
                diff[0] = Period.between(now, selectDate[0]).getDays();
            }
        };
        label.setLayoutX(40);
        label.setLayoutY(100);
        label2.setLayoutX(75);
        label2.setLayoutY(150);
        datePicker.setLayoutX(225);
        datePicker.setLayoutY(250);
        labelDate.setLayoutX(50);
        labelDate.setLayoutY(250);

        ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(destination));
        final int[] x = new int[1];
        final String[] y = new String[1];
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                y[0] = combo_box.getValue();
                if (combo_box.getValue().equals("To Badulla")) {
                    x[0] = 1;
                } else {
                    x[0] = 2;
                }
            }
        };
        combo_box.setLayoutX(225);
        combo_box.setLayoutY(330);
        labelDest.setLayoutX(50);
        labelDest.setLayoutY(330);
        Button bookButton = new Button("VIEW SEATS");
        Button closeButton = new Button("go to menu");
        bookButton.setLayoutX(275);
        bookButton.setLayoutY(550);
        closeButton.setLayoutX(175);
        closeButton.setLayoutY(550);

        closeButton.setOnAction(e -> {
            stage.close();
        });

        combo_box.setOnAction(event);
        datePicker.setOnAction(eventDate);
        pane.getChildren().addAll(combo_box, datePicker, labelDate, labelDest, bookButton, closeButton, label, label2);
        Scene scene = new Scene(pane, 600, 600);

        bookButton.setOnAction(e -> {
            if (diff[0] == 0 || x[0] == 0) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Please select destination and date ");
                a.show();
            } else {
                emptyView(stage, scene, diff[0], selectDate[0], x[0], booking, y[0]);
            }
        });
        stage.setScene(scene);
        stage.setTitle("View empty seats");
        stage.showAndWait();
    }

    //show empty seats
    private void emptyView(Stage stage, Scene scene, int diff, LocalDate date, int x, String[][][][] book, String desta) {
        BorderPane root = new BorderPane();
        Pane pane = new Pane();
        FlowPane flowpane = new FlowPane();
        pane.setMaxHeight(100);
        flowpane.setMaxWidth(650);
        flowpane.setMaxHeight(500);
        root.setTop(pane);
        root.setBottom(flowpane);
        Button close = new Button("TO MENU");
        Button back = new Button("BACK");
        Label label = new Label("EMPTY SEATS " + desta.toUpperCase() + " ON " + date);
        label.setStyle("-fx-font-size: 20px");
        label.setLayoutX(100);
        label.setLayoutY(100);
        close.setLayoutX(200);
        close.setLayoutY(200);
        back.setLayoutX(400);
        back.setLayoutY(200);
        close.setOnAction(e -> stage.close());
        back.setOnAction(e -> {
            stage.setScene(scene);
        });
        pane.getChildren().addAll(close, back, label);
        flowpane.setVgap(8);
        flowpane.setHgap(8);
        setEmptyFlow(flowpane, diff, x, book);
        flowpane.setPadding(new Insets(10, 0, 15, 100));
        Scene scene1 = new Scene(root, 750, 550);
        stage.setScene(scene1);
    }

    //this method use to
    private void setEmptyFlow(FlowPane flowPane, int diff, int x, String[][][][] book) {
        for (int i = 0; i < SEAT_CAPACITI; i++) {
            if (book[x - 1][diff - 1][i][0] == null) {
                Button button = new Button(String.valueOf(i + 1));
                flowPane.getChildren().add(button);
                //set Style for buttons
                button.setStyle("-fx-background-color: #98caff;-fx-min-width: 80px");
            } else {
                Button button = new Button(String.valueOf(i + 1));
                flowPane.getChildren().add(button);
                //set Style for buttons
                button.setVisible(false);
                button.setStyle("-fx-background-color: #ff563c;-fx-min-width: 80px");
            }
        }
    }

    private void guiPart(LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking, LocalDate[] selectDate, int[] diff, int[] x, String[] y, boolean[] run) {
        Stage stage = new Stage();
        Pane pane = new Pane();
        Label label2 = new Label("select date and destination");
        label2.setStyle("-fx-font-size: 16px");
        Label labelDate = new Label("Select Date : ");
        Label labelDest = new Label("Select destination : ");
        DatePicker datePicker = new DatePicker();
        datePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isAfter(maxDate) || date.isBefore(minDate));
            }
        });
        //final LocalDate[] selectDate = new LocalDate[1];
        //final int[] diff = new int[1];
        EventHandler<ActionEvent> eventDate = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                selectDate[0] = datePicker.getValue();
                diff[0] = Period.between(now, selectDate[0]).getDays();
            }
        };
        label2.setLayoutX(75);
        label2.setLayoutY(150);
        datePicker.setLayoutX(225);
        datePicker.setLayoutY(250);
        labelDate.setLayoutX(50);
        labelDate.setLayoutY(250);

        ComboBox<String> combo_box = new ComboBox<>(FXCollections.observableArrayList(destination));
        //final int[] x = new int[1];
        // final String[] y = new String[1];
        EventHandler<ActionEvent> event = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                y[0] = combo_box.getValue();
                if (combo_box.getValue().equals("To Badulla")) {
                    x[0] = 1;
                } else {
                    x[0] = 2;
                }
            }
        };
        combo_box.setLayoutX(225);
        combo_box.setLayoutY(330);
        labelDest.setLayoutX(50);
        labelDest.setLayoutY(330);
        Button bookButton = new Button("OK!!!");
        Button closeButton = new Button("go to menu");
        bookButton.setLayoutX(275);
        bookButton.setLayoutY(550);
        closeButton.setLayoutX(175);
        closeButton.setLayoutY(550);

        closeButton.setOnAction(e -> {
            run[0] = false;
            stage.close();
        });
        combo_box.setOnAction(event);
        datePicker.setOnAction(eventDate);
        pane.getChildren().addAll(combo_box, datePicker, labelDate, labelDest, bookButton, closeButton, label2);
        Scene scene = new Scene(pane, 600, 600);

        bookButton.setOnAction(e -> {
            if (diff[0] == 0 || x[0] == 0) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setContentText("Please select destination and date ");
                a.show();
            } else {
                run[0] = true;
                stage.close();
            }
        });
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void deleteCustomer(Scanner sc, LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking) {
        final boolean[] run = new boolean[1];
        final int[] choseDeasta = new int[1];
        final String[] destinationName = new String[1];
        final LocalDate[] selectDate = new LocalDate[1];
        final int[] difference = new int[1];
        guiPart(minDate, maxDate, destination, now, booking, selectDate, difference, choseDeasta, destinationName, run);
        if (run[0]) {
            System.out.println("\n===================================================================");
            System.out.println("--------------- DELETE CUSTOMER FROM SEAT --------------");
            System.out.println("===================================================================\n");
            System.out.println("delete seat " + destination[0] + " on " + selectDate[0]);
            delete:
            while (true) {
                System.out.print("enter seat number to remove customer : ");
                String seatNumber = sc.next();
                if (Integer.parseInt(seatNumber) <= 42 && Integer.parseInt(seatNumber) > 0) {
                    try {
                        if (booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][0] != null) {
                            System.out.println("\n" + booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][1] + "'s seat " + seatNumber + " delete successfully" + "\n");
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][0] = null;
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][1] = null;
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][2] = null;
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][3] = null;
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][4] = null;
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][5] = null;
                            booking[choseDeasta[0] - 1][difference[0] - 1][Integer.parseInt(seatNumber) - 1][6] = null;
                        } else {
                            System.out.println(seatNumber + " seat hasn't booked!!!");
                        }
                        System.out.println("Enter \"q\" to exit\nEnter any key to continue");
                        System.out.print("Enter your option : ");
                        String option = sc.next().toLowerCase();
                        if ("q".equals(option)) {
                            break delete;
                        }
                    } catch (Exception e) {
                        System.out.println("Please input integer\ncheck and try again");
                    }
                } else {
                    System.out.println("seat number not valid");
                }
            }
        }
    }

    private void findCustomer(Scanner sc, LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking) {
        final boolean[] run = new boolean[1];
        final int[] choseDeasta = new int[1];
        final String[] destinationName = new String[1];
        final LocalDate[] selectDate = new LocalDate[1];
        final int[] difference = new int[1];
        guiPart(minDate, maxDate, destination, now, booking, selectDate, difference, choseDeasta, destinationName, run);
        if (run[0]) {
            System.out.println("\n===================================================================");
            System.out.println("--------------- FIND CUSTOMER FROM SEAT --------------");
            System.out.println("===================================================================\n");
            System.out.println("find seat " + destination[0] + " on " + selectDate[0]);
            finding:
            while (true) {
                System.out.print("Enter customer name : ");
                sc.nextLine();
                String name = sc.nextLine();
                System.out.println(name);
                boolean find = false;
                for (int x = 0; x < SEAT_CAPACITI; x++) {
                    if (name.equalsIgnoreCase(booking[choseDeasta[0] - 1][difference[0] - 1][x][1] + " " + booking[choseDeasta[0] - 1][difference[0] - 1][x][2])) {
                        //print customer details
                        System.out.println("name : " + booking[choseDeasta[0] - 1][difference[0] - 1][x][1]+" "+booking[choseDeasta[0] - 1][difference[0] - 1][x][2] + " - seat number : " + booking[choseDeasta[0] - 1][difference[0] - 1][x][0] + " - NIC : " +
                                booking[choseDeasta[0] - 1][difference[0] - 1][x][3] + " - Address : " + booking[choseDeasta[0] - 1][difference[0] - 1][x][4] + " - Contact : " + booking[choseDeasta[0] - 1][difference[0] - 1][x][5] +
                                " - Email : " + booking[choseDeasta[0] - 1][difference[0] - 1][x][6]);
                        find = true;
                    }
                }
                if (!find) {
                    System.out.println(name + " hasn't book a seat\n");
                }
                System.out.println("Enter \"q\" to exit\nEnter any key to continue");
                System.out.print("Enter your option : ");
                String option = sc.next().toLowerCase();
                if ("q".equals(option)) {
                    break;
                }
            }
        }
    }

    private void sorting(LocalDate minDate, LocalDate maxDate, String[] destination, LocalDate now, String[][][][] booking) {
        final boolean[] run = new boolean[1];
        final int[] choseDeasta = new int[1];
        final String[] destinationName = new String[1];
        final LocalDate[] selectDate = new LocalDate[1];
        final int[] difference = new int[1];
        guiPart(minDate, maxDate, destination, now, booking, selectDate, difference, choseDeasta, destinationName, run);
        if (run[0]) {
            ArrayList<String> names = new ArrayList<>();
            ArrayList<String> seats = new ArrayList<>();
            System.out.println("\n===================================================================");
            System.out.println("--------------- SORTING CUSTOMER NAME & SEAT --------------");
            System.out.println("===================================================================\n");
            System.out.println("sort name and seats " + destination[0] + " on " + selectDate[0]);

            for (int i = 0; i < SEAT_CAPACITI; i++) {
                if (booking[choseDeasta[0] - 1][difference[0] - 1][i][1] != null) {
                    names.add(booking[choseDeasta[0] - 1][difference[0] - 1][i][1]+" "+booking[choseDeasta[0] - 1][difference[0] - 1][i][2]);
                    seats.add(booking[choseDeasta[0] - 1][difference[0] - 1][i][0]);
                }
            }
            String temp;
            String tempSnum;
            for (int j = 0; j < names.size() - 1; j++) {
                for (int i = j + 1; i < names.size(); i++) {
                    if (names.get(j).compareToIgnoreCase(names.get(i)) > 0) {
                        temp = names.get(j);
                        names.set(j, names.get(i));
                        names.set(i, temp);
                        //set seat number according to the name
                        tempSnum = seats.get(j);
                        seats.set(j, seats.get(i));
                        seats.set(i, tempSnum);
                    }
                }
            }
            displaySorting(names, seats);
        }
    }

    private void displaySorting(ArrayList<String> names, ArrayList<String> seats) {
        for (int i = 0; i < names.size(); i++) {
            System.out.println(names.get(i) + " - " + seats.get(i));
        }
    }

    private void storeData(String[][][][] booking, LocalDate now, MongoCollection<Document> toBadulla, MongoCollection<Document> toColombo, Document document) {
        try {
            System.out.println("\n--------------- sending data to database ----------------\n");
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    sendData(document, toBadulla, booking, i, now);
                } else {
                    sendData(document, toColombo, booking, i, now);
                }
            }
            System.out.println("\n--------------- sending finish ----------------\n");
        } catch (Exception e) {
            System.out.println("some thing wet wrong!!!\n ********PLEASE CALL TO THE DEVELOPER*****");
        }
    }

    private void sendData(Document document, MongoCollection<Document> collection, String[][][][] booking, int i, LocalDate now) {
        try {
            for (int x = 0; x < 30; x++) {
                loop:
                for (int y = 0; y < SEAT_CAPACITI; y++) {
                    if (booking[i][x][y][0] != null) {
                        LocalDate bDate = now.plusDays(x + 1);
                        String bookDate = bDate.toString();
                        document.append("seat", booking[i][x][y][0]);
                        document.append("name", booking[i][x][y][1]);
                        document.append("sname", booking[i][x][y][2]);
                        document.append("id", booking[i][x][y][3]);
                        document.append("contact", booking[i][x][y][4]);
                        document.append("address", booking[i][x][y][5]);
                        document.append("email", booking[i][x][y][6]);
                        document.append("date", bookDate);

                        //check data already in the collection, if true miss that
                            FindIterable<Document> data = collection.find();
                            for (Document record : data) {
                                String seat = (String) record.get("seat");
                                if(seat.equals(booking[i][x][y][0]) && LocalDate.parse((String) record.get("date")).equals(bDate)){
                                    document.clear();
                                    continue loop;
                                }
                            }
                        collection.insertOne(document);
                        document.clear();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("some thing wet wrong!!!\n ********PLEASE CALL TO THE DEVELOPER*****");
        }
    }

    private void loadData(String[][][][] booking, LocalDate now, MongoCollection<Document> toBadulla, MongoCollection<Document> toColombo, Document document) {
        try {
            System.out.println("\n--------------- loading data ----------------\n");
            getData(toBadulla, now, 0, booking);
            getData(toColombo, now, 1, booking);
            System.out.println("\n--------------- data loaded ----------------\n");
        } catch (Exception e) {
            System.out.println("some thing wet wrong!!!\n ********PLEASE CALL TO THE DEVELOPER*****");
        }
    }

    private void getData(MongoCollection<Document> collection, LocalDate now, int x, String[][][][] booking) {
        try {
            FindIterable<Document> data = collection.find();
            for (Document record : data) {
                System.out.println(record);
                String seat = (String) record.get("seat");
                String name = (String) record.get("name");
                String sname = (String) record.get("sname");
                String id = (String) record.get("id");
                String address = (String) record.get("address");
                String contact = (String) record.get("contact");
                String email = (String) record.get("email");
                int diff = Period.between(now, LocalDate.parse((String) record.get("date"))).getDays();
                if (diff > 0) {
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][0] = seat;
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][1] = name;
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][2] = sname;
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][3] = id;
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][4] = address;
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][5] = contact;
                    booking[x][diff - 1][Integer.parseInt(seat) - 1][6] = email;
                }
            }
        } catch (Exception e) {
            System.out.println("Some thing went wrong\n Call to the developer!!!");
        }
    }
}

