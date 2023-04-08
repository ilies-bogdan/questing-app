package controller;

import domain.Badge;
import domain.Quest;
import domain.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import service.BadgeService;
import utils.observer.Observer;

import java.util.Collection;

public class BadgeController implements Observer {
    private BadgeService badgeSrv;
    private User user;
    private ObservableList<Badge> modelBadges = FXCollections.observableArrayList();
    @FXML
    TableView<Badge> tableViewBadges;
    @FXML
    TableColumn<Badge, String> tableColumnBadgesTitle;
    @FXML
    TableColumn<Badge, String> tableColumnBadgesDescription;

    public void setBadgeSrv(BadgeService badgeSrv) {
        this.badgeSrv = badgeSrv;
        badgeSrv.addObserver(this);
    }

    public void setUser(User user) {
        this.user = user;
        initModel();
    }

    @Override
    public void update() {
        initModel();
    }

    private void initModel() {
        modelBadges.setAll((Collection<Badge>) badgeSrv.getAllBadgesForUser(user));
    }

    @FXML
    public void initialize() {
        tableViewBadges.setPlaceholder(new Label("No badges earned"));
        tableColumnBadgesTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        tableColumnBadgesDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        tableViewBadges.setItems(modelBadges);
    }
}
