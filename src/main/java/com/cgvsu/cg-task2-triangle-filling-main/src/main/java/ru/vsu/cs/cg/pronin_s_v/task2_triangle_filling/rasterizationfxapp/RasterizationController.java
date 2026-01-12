package ru.vsu.cs.cg.pronin_s_v.task2_triangle_filling.rasterizationfxapp;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class RasterizationController {

    @FXML
    private Canvas canvas;

    private static class Point {
        double x, y;
        Color color;
        boolean isSet;

        Point(Color color) {
            this.color = color;
            this.isSet = false;
        }
    }

    private Point[] points = {
        new Point(Color.RED),
        new Point(Color.GREEN),
        new Point(Color.BLUE)
    };

    private int currentPointIndex = 0;
    private Integer selectedPointIndex = null;
    private static final double POINT_RADIUS = 3.0;

    @FXML
    private void initialize() {
        canvas.setOnMouseClicked(this::handleMouseClick);
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        redraw();
    }

    private void handleMouseClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        
        Integer clickedPointIndex = findPointAt(x, y);
        
        if (clickedPointIndex != null) {
            selectedPointIndex = clickedPointIndex;
        } else {
            if (currentPointIndex < points.length) {
                points[currentPointIndex].x = x;
                points[currentPointIndex].y = y;
                points[currentPointIndex].isSet = true;
                currentPointIndex++;
            }
        }
        
        redraw();
    }

    private void handleMousePressed(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();
        selectedPointIndex = findPointAt(x, y);
    }

    private void handleMouseDragged(MouseEvent event) {
        if (selectedPointIndex != null && selectedPointIndex < points.length) {
            points[selectedPointIndex].x = event.getX();
            points[selectedPointIndex].y = event.getY();
            redraw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        selectedPointIndex = null;
    }

    private Integer findPointAt(double x, double y) {
        for (int i = 0; i < points.length; i++) {
            if (points[i].isSet) {
                double dx = x - points[i].x;
                double dy = y - points[i].y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                
                if (distance <= POINT_RADIUS) {
                    return i;
                }
            }
        }
        return null;
    }

    private void redraw() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        int setPointsCount = 0;
        for (Point p : points) {
            if (p.isSet) setPointsCount++;
        }
        
        if (setPointsCount == 3) {
            Rasterization.fillTriangle(
                gc,
                points[0].x, points[0].y, points[0].color,
                points[1].x, points[1].y, points[1].color,
                points[2].x, points[2].y, points[2].color
            );
        }
        
        for (int i = 0; i < points.length; i++) {
            if (points[i].isSet) {
                gc.setFill(points[i].color);
                gc.fillOval(
                    points[i].x - POINT_RADIUS,
                    points[i].y - POINT_RADIUS,
                    POINT_RADIUS * 2,
                    POINT_RADIUS * 2
                );
            }
        }
    }
}

