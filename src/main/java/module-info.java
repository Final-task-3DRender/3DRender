module com.cgvsu {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // Экспортируем основные пакеты для возможности расширения
    opens com.cgvsu to javafx.fxml;
    exports com.cgvsu;
exports com.cgvsu.camera;
exports com.cgvsu.math;
exports com.cgvsu.model;
exports com.cgvsu.objreader;
exports com.cgvsu.objwriter;
exports com.cgvsu.render_engine;
exports com.cgvsu.transform;
exports com.cgvsu.triangulation;
}