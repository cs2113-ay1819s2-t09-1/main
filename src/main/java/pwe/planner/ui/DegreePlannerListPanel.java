package pwe.planner.ui;

import static pwe.planner.commons.util.CollectionUtil.requireAllNonNull;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import pwe.planner.model.module.Module;
import pwe.planner.model.planner.DegreePlanner;

/**
 * Panel containing the list of degreePlanners.
 */
public class DegreePlannerListPanel extends UiPart<Region> {

    private static final String FXML = "DegreePlannerListPanel.fxml";
    private ObservableList<Module> modules;

    @FXML
    private ListView<DegreePlanner> degreePlanners;

    public DegreePlannerListPanel(ObservableList<DegreePlanner> degreePlannerList,
            ObservableList<Module> moduleList) {
        super(FXML);
        requireAllNonNull(degreePlannerList, moduleList);

        modules = moduleList;
        degreePlanners.setItems(degreePlannerList);
        degreePlanners.setCellFactory(listView -> new DegreePlannerViewCell());
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code DegreePlanner}
     * using a {@code DegreePlannerListCard}.
     */
    class DegreePlannerViewCell extends ListCell<DegreePlanner> {
        @Override
        protected void updateItem(DegreePlanner degreePlanner, boolean empty) {
            super.updateItem(degreePlanner, empty);

            if (empty || degreePlanner == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new DegreePlannerCard(degreePlanner, modules).getRoot());
            }
        }
    }
}
