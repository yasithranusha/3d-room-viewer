package com.modelviewer.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.modelviewer.model.Room;

public class RoomControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JSpinner roomWidthSpinner;
    private JSpinner roomHeightSpinner;
    private JSpinner roomLengthSpinner;
    private JButton wallColorButton;
    private JButton floorColorButton;
    private JButton ceilingColorButton;
    private JSpinner wallTransparencySpinner;
    private JSpinner floorTransparencySpinner;
    private JSpinner ceilingTransparencySpinner;
    private JCheckBox showRoomCheckbox;

    private Room currentRoom;

    public RoomControlPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Room Controls"));

        // Create dimensions panel
        JPanel dimensionsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        dimensionsPanel.setBorder(BorderFactory.createTitledBorder("Dimensions"));

        roomWidthSpinner = createSpinner(1.0f, 100.0f, 0.5f);
        roomHeightSpinner = createSpinner(1.0f, 100.0f, 0.5f);
        roomLengthSpinner = createSpinner(1.0f, 100.0f, 0.5f);

        dimensionsPanel.add(new JLabel("Width:"));
        dimensionsPanel.add(roomWidthSpinner);
        dimensionsPanel.add(new JLabel("Height:"));
        dimensionsPanel.add(roomHeightSpinner);
        dimensionsPanel.add(new JLabel("Length:"));
        dimensionsPanel.add(roomLengthSpinner);

        // Create colors panel
        JPanel colorsPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        colorsPanel.setBorder(BorderFactory.createTitledBorder("Colors"));

        wallColorButton = createColorButton("Wall Color");
        floorColorButton = createColorButton("Floor Color");
        ceilingColorButton = createColorButton("Ceiling Color");

        colorsPanel.add(new JLabel("Wall:"));
        colorsPanel.add(wallColorButton);
        colorsPanel.add(new JLabel("Floor:"));
        colorsPanel.add(floorColorButton);
        colorsPanel.add(new JLabel("Ceiling:"));
        colorsPanel.add(ceilingColorButton);

        // Create transparency panel
        JPanel transparencyPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        transparencyPanel.setBorder(BorderFactory.createTitledBorder("Transparency"));

        wallTransparencySpinner = createSpinner(0.0f, 1.0f, 0.1f);
        floorTransparencySpinner = createSpinner(0.0f, 1.0f, 0.1f);
        ceilingTransparencySpinner = createSpinner(0.0f, 1.0f, 0.1f);

        transparencyPanel.add(new JLabel("Wall:"));
        transparencyPanel.add(wallTransparencySpinner);
        transparencyPanel.add(new JLabel("Floor:"));
        transparencyPanel.add(floorTransparencySpinner);
        transparencyPanel.add(new JLabel("Ceiling:"));
        transparencyPanel.add(ceilingTransparencySpinner);

        // Create visibility panel
        JPanel visibilityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showRoomCheckbox = new JCheckBox("Show Room");
        showRoomCheckbox.setSelected(true);
        visibilityPanel.add(showRoomCheckbox);

        // Add all panels to main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(visibilityPanel);
        mainPanel.add(dimensionsPanel);
        mainPanel.add(colorsPanel);
        mainPanel.add(transparencyPanel);

        add(mainPanel, BorderLayout.NORTH);

        setupListeners();
    }

    private JSpinner createSpinner(float min, float max, float step) {
        SpinnerNumberModel model = new SpinnerNumberModel(min, min, max, step);
        return new JSpinner(model);
    }

    private JButton createColorButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        return button;
    }

    private void setupListeners() {
        ChangeListener dimensionsListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (currentRoom != null) {
                    currentRoom.setDimensions(
                            ((Number) roomWidthSpinner.getValue()).floatValue(),
                            ((Number) roomHeightSpinner.getValue()).floatValue(),
                            ((Number) roomLengthSpinner.getValue()).floatValue());
                }
            }
        };

        roomWidthSpinner.addChangeListener(dimensionsListener);
        roomHeightSpinner.addChangeListener(dimensionsListener);
        roomLengthSpinner.addChangeListener(dimensionsListener);

        wallColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoom != null) {
                    Color newColor = JColorChooser.showDialog(
                            RoomControlPanel.this,
                            "Choose Wall Color",
                            currentRoom.getWallColor());
                    if (newColor != null) {
                        currentRoom.setWallColor(newColor);
                        wallColorButton.setBackground(newColor);
                    }
                }
            }
        });

        floorColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoom != null) {
                    Color newColor = JColorChooser.showDialog(
                            RoomControlPanel.this,
                            "Choose Floor Color",
                            currentRoom.getFloorColor());
                    if (newColor != null) {
                        currentRoom.setFloorColor(newColor);
                        floorColorButton.setBackground(newColor);
                    }
                }
            }
        });

        ceilingColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentRoom != null) {
                    Color newColor = JColorChooser.showDialog(
                            RoomControlPanel.this,
                            "Choose Ceiling Color",
                            currentRoom.getCeilingColor());
                    if (newColor != null) {
                        currentRoom.setCeilingColor(newColor);
                        ceilingColorButton.setBackground(newColor);
                    }
                }
            }
        });

        ChangeListener transparencyListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (currentRoom != null) {
                    currentRoom.setWallTransparency(((Number) wallTransparencySpinner.getValue()).floatValue());
                    currentRoom.setFloorTransparency(((Number) floorTransparencySpinner.getValue()).floatValue());
                    currentRoom.setCeilingTransparency(((Number) ceilingTransparencySpinner.getValue()).floatValue());
                }
            }
        };

        wallTransparencySpinner.addChangeListener(transparencyListener);
        floorTransparencySpinner.addChangeListener(transparencyListener);
        ceilingTransparencySpinner.addChangeListener(transparencyListener);
    }

    public void setRoom(Room room) {
        this.currentRoom = room;
        if (room != null) {
            float[] dimensions = room.getDimensions();
            roomWidthSpinner.setValue(dimensions[0]);
            roomHeightSpinner.setValue(dimensions[1]);
            roomLengthSpinner.setValue(dimensions[2]);

            wallColorButton.setBackground(room.getWallColor());
            floorColorButton.setBackground(room.getFloorColor());
            ceilingColorButton.setBackground(room.getCeilingColor());

            wallTransparencySpinner.setValue(room.getWallTransparency());
            floorTransparencySpinner.setValue(room.getFloorTransparency());
            ceilingTransparencySpinner.setValue(room.getCeilingTransparency());
        }
        updateUI();
    }

    public boolean isRoomVisible() {
        return showRoomCheckbox.isSelected();
    }
}