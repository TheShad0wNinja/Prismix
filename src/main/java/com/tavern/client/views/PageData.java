package com.tavern.client.views;

import com.tavern.client.components.Cleanable;
import javafx.scene.Parent;

public record PageData(Parent root, Cleanable controller) {
}
