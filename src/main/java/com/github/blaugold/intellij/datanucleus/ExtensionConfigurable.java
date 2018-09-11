package com.github.blaugold.intellij.datanucleus;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import static com.github.blaugold.intellij.datanucleus.ExtensionConfig.API.JDO;
import static com.github.blaugold.intellij.datanucleus.ExtensionConfig.API.JPA;

public class ExtensionConfigurable<Config extends RunConfigurationBase>
        extends SettingsEditor<Config> {
    private ExtensionConfig config;

    private JPanel panel;
    private JTextField includedPackagesTextField;
    private JLabel includedPackages;
    private JTextArea infoText;
    private JCheckBox enableRuntimeEnhancementCheckBox;
    private JRadioButton jdo;
    private JRadioButton jpa;

    public ExtensionConfigurable(Config configuration) {
        this.config = ExtensionConfig.get(configuration).copy();

        styleInfoText();
        updateUI();
        setupListeners();
    }

    private void setupListeners() {
        enableRuntimeEnhancementCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                config.setEnabled(enableRuntimeEnhancementCheckBox.isSelected());
                updateUI();
            }
        });

        jdo.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                config.setApi(jdo.isSelected() ? JDO : JPA);
                updateUI();
            }
        });

        jpa.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                config.setApi(jpa.isSelected() ? JPA : JDO);
                updateUI();
            }
        });
    }

    private void styleInfoText() {
        infoText.setOpaque(true);
        infoText.setBackground(new Color(0, 0, 0, 0));
        infoText.setFont(Font.getFont(Font.SANS_SERIF));
    }

    private void updateUI() {
        includedPackagesTextField.setText(config.getPackages());
        enableRuntimeEnhancementCheckBox.setSelected(config.isEnabled());
        jdo.setSelected(config.getApi() == JDO);
        jpa.setSelected(config.getApi() == JPA);

        setEnabled(config.isEnabled());
    }

    private void setEnabled(boolean enabled) {
        includedPackagesTextField.setEnabled(enabled);
        jdo.setEnabled(enabled);
        jpa.setEnabled(enabled);
    }

    @Override
    protected void resetEditorFrom(@NotNull RunConfigurationBase settings) {
        config = ExtensionConfig.get(settings).copy();
        updateUI();
    }

    @Override
    protected void applyEditorTo(@NotNull RunConfigurationBase settings) {
        config.setPackages(includedPackagesTextField.getText());
        config.set(settings);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

}
