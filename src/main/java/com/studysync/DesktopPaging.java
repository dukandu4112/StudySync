package com.studysync;

import java.util.List;
import java.util.Objects;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Keeps long histories responsive while retaining access to every saved record. */
final class DesktopPaging {
    static final int PAGE_SIZE = 50;
    private static final String QUERY = "studysync.page.query", INDEX = "studysync.page.index";
    record Slice<T>(List<T> items, int index, int total, int pages) { }
    static <T> Slice<T> slice(VBox target, List<T> records, String query) {
        if (!Objects.equals(target.getProperties().get(QUERY), query)) {
            target.getProperties().put(QUERY, query); target.getProperties().put(INDEX, 0);
        }
        int pages = Math.max(1, (records.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int index = Math.max(0, Math.min((int) target.getProperties().getOrDefault(INDEX, 0), pages - 1));
        target.getProperties().put(INDEX, index);
        int from = Math.min(index * PAGE_SIZE, records.size());
        return new Slice<>(records.subList(from, Math.min(from + PAGE_SIZE, records.size())), index, records.size(), pages);
    }
    static void controls(VBox target, Slice<?> page, Runnable refresh) {
        if (page.total() == 0) return;
        int first = page.index() * PAGE_SIZE + 1;
        Label count = new Label("Showing " + first + "–" + (first + page.items().size() - 1) + " of " + page.total());
        count.setWrapText(true);
        Button previous = new Button("Previous page"), next = new Button("Next page");
        previous.setDisable(page.index() == 0); next.setDisable(page.index() + 1 == page.pages());
        previous.setOnAction(e -> move(target, page.index() - 1, refresh, "Previous page"));
        next.setOnAction(e -> move(target, page.index() + 1, refresh, "Next page"));
        target.getChildren().add(new HBox(12, previous, count, next));
    }
    private static void move(VBox target, int index, Runnable refresh, String preferred) {
        target.getProperties().put(INDEX, index); refresh.run();
        if (target.getChildren().isEmpty()) return;
        if (target.getChildren().get(target.getChildren().size() - 1) instanceof HBox controls) {
            var enabled = controls.getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast)
                .filter(button -> !button.isDisabled()).toList();
            enabled.stream().filter(button -> button.getText().equals(preferred)).findFirst()
                .or(() -> enabled.stream().findFirst()).ifPresent(Button::requestFocus);
        }
    }
}
