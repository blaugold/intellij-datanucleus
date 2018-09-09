package com.github.blaugold.intellij.datanucleus;

import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

import static com.github.blaugold.intellij.datanucleus.RunConfigurationExtension.API.JDO;
import static com.github.blaugold.intellij.datanucleus.RunConfigurationExtension.API.JPA;

public class ExtentionConfigurable<Settings extends RunConfigurationBase> extends SettingsEditor<Settings> {
    private JPanel panel;
    private JTextField includedPackagesTextField;
    private JLabel includedPackages;
    private JTextArea infoText;
    private JCheckBox enableRuntimeEnhancementCheckBox;
    private JRadioButton jdo;
    private JRadioButton jpa;
    private RunConfigurationExtension extension;

    public ExtentionConfigurable(RunConfigurationExtension extension) {
        this.extension = extension;

        includedPackagesTextField.setText(extension.getPackages());

        enableRuntimeEnhancementCheckBox.setSelected(extension.getEnabled());

        includedPackagesTextField.setEnabled(extension.getEnabled());
        jdo.setEnabled(extension.getEnabled());
        jpa.setEnabled(extension.getEnabled());

        infoText.setOpaque(true);
        infoText.setBackground(new Color(0, 0, 0, 0));
        infoText.setFont(Font.getFont(Font.SANS_SERIF));

        updateApiRadioGroup(extension.getApi());

        enableRuntimeEnhancementCheckBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                includedPackagesTextField.setEnabled(enableRuntimeEnhancementCheckBox.isSelected());
                jdo.setEnabled(enableRuntimeEnhancementCheckBox.isSelected());
                jpa.setEnabled(enableRuntimeEnhancementCheckBox.isSelected());
            }
        });

        jdo.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateApiRadioGroup(jdo.isSelected() ? JDO : JPA);
            }
        });

        jpa.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateApiRadioGroup(jpa.isSelected() ? JPA : JDO);
            }
        });
    }

    private void updateApiRadioGroup(RunConfigurationExtension.API api) {
        jdo.setSelected(api == JDO);
        jpa.setSelected(api == JPA);
    }

    @Override
    protected void resetEditorFrom(@NotNull Settings s) {

    }

    @Override
    protected void applyEditorTo(@NotNull Settings s) throws ConfigurationException {
        extension.setPackages(includedPackagesTextField.getText());
        extension.setEnabled(enableRuntimeEnhancementCheckBox.isSelected());
        extension.setApi(jdo.isSelected() ? JDO : JPA);
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return panel;
    }

}
