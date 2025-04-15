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

import com.modelviewer.model.Model3D;

public class ModelControlPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private JSpinner modelXSpinner;
    private JSpinner modelYSpinner;
    private JSpinner modelZSpinner;
    private JSpinner modelScaleSpinner;
    private JSpinner modelRotationSpinner;
    private JCheckBox modelColorCheckbox;
    private JButton modelColorButton;

    private Model3D currentModel;

    public ModelControlPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Model Controls"));

        // Create position panel
        JPanel positionPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        positionPanel.setBorder(BorderFactory.createTitledBorder("Position"));

        modelXSpinner = createSpinner(-100.0f, 100.0f, 0.1f);
        modelYSpinner = createSpinner(-100.0f, 100.0f, 0.1f);
        modelZSpinner = createSpinner(-100.0f, 100.0f, 0.1f);

        positionPanel.add(new JLabel("X:"));
        positionPanel.add(modelXSpinner);
        positionPanel.add(new JLabel("Y:"));
        positionPanel.add(modelYSpinner);
        positionPanel.add(new JLabel("Z:"));
        positionPanel.add(modelZSpinner);

        // Create transform panel
        JPanel transformPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        transformPanel.setBorder(BorderFactory.createTitledBorder("Transform"));

        modelScaleSpinner = createSpinner(0.01f, 100.0f, 0.1f);
        modelRotationSpinner = createSpinner(0.0f, 360.0f, 1.0f);

        transformPanel.add(new JLabel("Scale:"));
        transformPanel.add(modelScaleSpinner);
        transformPanel.add(new JLabel("Rotation:"));
        transformPanel.add(modelRotationSpinner);

        // Create appearance panel
        JPanel appearancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        appearancePanel.setBorder(BorderFactory.createTitledBorder("Appearance"));

        modelColorCheckbox = new JCheckBox("Custom Color");
        modelColorButton = new JButton("Choose Color");
        modelColorButton.setEnabled(false);

        appearancePanel.add(modelColorCheckbox);
        appearancePanel.add(modelColorButton);

        // Add all panels to main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(positionPanel);
        mainPanel.add(transformPanel);
        mainPanel.add(appearancePanel);

        add(mainPanel, BorderLayout.NORTH);

        setupListeners();
    }

    private JSpinner createSpinner(float min, float max, float step) {
        SpinnerNumberModel model = new SpinnerNumberModel(0.0f, min, max, step);
        return new JSpinner(model);
    }

    private void setupListeners() {
        ChangeListener positionListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (currentModel != null) {
                    currentModel.setX(((Number) modelXSpinner.getValue()).floatValue());
                    currentModel.setY(((Number) modelYSpinner.getValue()).floatValue());
                    currentModel.setZ(((Number) modelZSpinner.getValue()).floatValue());
                }
            }
        };

        modelXSpinner.addChangeListener(positionListener);
        modelYSpinner.addChangeListener(positionListener);
        modelZSpinner.addChangeListener(positionListener);

        modelScaleSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (currentModel != null) {
                    currentModel.setScale(((Number) modelScaleSpinner.getValue()).floatValue());
                }
            }
        });

        modelRotationSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (currentModel != null) {
                    currentModel.setRotY(((Number) modelRotationSpinner.getValue()).floatValue());
                }
            }
        });

        modelColorCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean useCustomColor = modelColorCheckbox.isSelected();
                modelColorButton.setEnabled(useCustomColor);
                if (currentModel != null) {
                    currentModel.setUseCustomColor(useCustomColor);
                }
            }
        });

        modelColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentModel != null) {
                    Color newColor = JColorChooser.showDialog(
                            ModelControlPanel.this,
                            "Choose Model Color",
                            currentModel.getCustomColor());
                    if (newColor != null) {
                        currentModel.setCustomColor(newColor);
                    }
                }
            }
        });
    }

    public void setModel(Model3D model) {
        this.currentModel = model;
        if (model != null) {
            modelXSpinner.setValue(model.getX());
            modelYSpinner.setValue(model.getY());
            modelZSpinner.setValue(model.getZ());
            modelScaleSpinner.setValue(model.getScale());
            modelRotationSpinner.setValue(model.getRotY());
            modelColorCheckbox.setSelected(model.isUseCustomColor());
            modelColorButton.setEnabled(model.isUseCustomColor());
        }
        updateUI();
    }
}