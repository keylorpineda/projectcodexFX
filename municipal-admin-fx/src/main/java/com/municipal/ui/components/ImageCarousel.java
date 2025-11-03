package com.municipal.ui.components;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class ImageCarousel extends StackPane {
    
    private final List<Image> images = new ArrayList<>();
    private final ImageView imageView = new ImageView();
    private final HBox dotsContainer = new HBox(8);
    private final Button btnPrev = new Button("◀");
    private final Button btnNext = new Button("▶");
    private int currentIndex = 0;
    private Timeline autoPlayTimeline;
    
    public ImageCarousel() {
        this(300, 200);
    }
    
    public ImageCarousel(double width, double height) {
        setupUI(width, height);
    }
    
    private void setupUI(double width, double height) {
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        imageView.getStyleClass().add("carousel-image");
        
        btnPrev.getStyleClass().addAll("carousel-btn", "carousel-btn-prev");
        btnNext.getStyleClass().addAll("carousel-btn", "carousel-btn-next");
        
        btnPrev.setOnAction(e -> showPrevious());
        btnNext.setOnAction(e -> showNext());
        
        dotsContainer.setAlignment(Pos.CENTER);
        dotsContainer.getStyleClass().add("carousel-dots");
        
        VBox layout = new VBox(10, imageView, dotsContainer);
        layout.setAlignment(Pos.CENTER);
        
        StackPane.setAlignment(btnPrev, Pos.CENTER_LEFT);
        StackPane.setAlignment(btnNext, Pos.CENTER_RIGHT);
        
        getChildren().addAll(layout, btnPrev, btnNext);
        getStyleClass().add("image-carousel");
        
        setPrefSize(width, height + 50);
    }
    
    public void setImages(List<Image> imageList) {
        images.clear();
        if (imageList != null) {
            images.addAll(imageList);
        }
        
        currentIndex = 0;
        updateDisplay();
        updateDots();
        
        if (images.size() > 1) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }
    }
    
    public void addImage(Image image) {
        if (image != null) {
            images.add(image);
            if (images.size() == 1) {
                updateDisplay();
            }
            updateDots();
            if (images.size() > 1 && autoPlayTimeline == null) {
                startAutoPlay();
            }
        }
    }
    
    public void clearImages() {
        images.clear();
        currentIndex = 0;
        imageView.setImage(null);
        updateDots();
        stopAutoPlay();
    }
    
    private void showPrevious() {
        if (images.isEmpty()) return;
        currentIndex = (currentIndex - 1 + images.size()) % images.size();
        updateDisplay();
        updateDots();
        resetAutoPlay();
    }
    
    private void showNext() {
        if (images.isEmpty()) return;
        currentIndex = (currentIndex + 1) % images.size();
        updateDisplay();
        updateDots();
        resetAutoPlay();
    }
    
    private void updateDisplay() {
        if (images.isEmpty()) {
            imageView.setImage(null);
            btnPrev.setVisible(false);
            btnNext.setVisible(false);
        } else {
            imageView.setImage(images.get(currentIndex));
            btnPrev.setVisible(images.size() > 1);
            btnNext.setVisible(images.size() > 1);
        }
    }
    
    private void updateDots() {
        dotsContainer.getChildren().clear();
        
        for (int i = 0; i < images.size(); i++) {
            Label dot = new Label("●");
            dot.getStyleClass().add("carousel-dot");
            if (i == currentIndex) {
                dot.getStyleClass().add("active");
            }
            final int index = i;
            dot.setOnMouseClicked(e -> {
                currentIndex = index;
                updateDisplay();
                updateDots();
                resetAutoPlay();
            });
            dotsContainer.getChildren().add(dot);
        }
        
        dotsContainer.setVisible(images.size() > 1);
    }
    
    private void startAutoPlay() {
        if (autoPlayTimeline != null) {
            autoPlayTimeline.stop();
        }
        
        autoPlayTimeline = new Timeline(new KeyFrame(Duration.seconds(5), e -> showNext()));
        autoPlayTimeline.setCycleCount(Timeline.INDEFINITE);
        autoPlayTimeline.play();
    }
    
    private void stopAutoPlay() {
        if (autoPlayTimeline != null) {
            autoPlayTimeline.stop();
            autoPlayTimeline = null;
        }
    }
    
    private void resetAutoPlay() {
        if (images.size() > 1) {
            stopAutoPlay();
            startAutoPlay();
        }
    }
    
    public List<Image> getImages() {
        return new ArrayList<>(images);
    }
    
    public int getCurrentIndex() {
        return currentIndex;
    }
    
    public void cleanup() {
        stopAutoPlay();
        images.clear();
    }
}
