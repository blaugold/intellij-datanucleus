package com.github.blaugold.intellij.datanucleus;

import com.google.common.base.Strings;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.openapi.util.Key;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ExtensionConfig {

    public static ExtensionConfig get(@NotNull RunConfigurationBase runConfiguration) {
        ExtensionConfig config = runConfiguration.getUserData(KEY);
        return config != null ? config : new ExtensionConfig();
    }

    public static void readExternal(@NotNull RunConfigurationBase runConfiguration,
                                    @NotNull Element element) {
        ExtensionConfig config = new ExtensionConfig();
        config.setPackages(element.getChildText(PACKAGES_ELEMENT_NAME));
        config.setEnabled(Boolean.valueOf(element.getChildText(ENABLED_ELEMENT_NAME)));

        String apiStr = element.getChildText(API_ELEMENT_NAME);
        if (apiStr != null) {
            config.setApi(API.parse(apiStr));
        }

        runConfiguration.putUserData(KEY, config);
    }

    public static void writeExternal(RunConfigurationBase runConfiguration, Element element) {
        get(runConfiguration).writeExternal(element);
    }

    public static enum API {
        JDO,
        JPA;

        public static API parse(String value) {
            switch (value) {
                case "JDO":
                    return JDO;
                case "JPA":
                    return JPA;
                default:
                    throw new IllegalArgumentException(value + " is not a valid DataNucleus api.");
            }
        }

    }

    public static final Key<ExtensionConfig> KEY = Key.create(
            "com.github.blaugold.intellij.datanucleus.ExtensionConfig"
    );

    private static final String PACKAGES_ELEMENT_NAME = "packages";
    private static final String API_ELEMENT_NAME = "api";
    private static final String ENABLED_ELEMENT_NAME = "enabled";

    private String packages;
    private boolean enabled = false;
    private API api = API.JDO;

    public String getPackages() {
        return packages;
    }

    public void setPackages(String packages) {
        if (Strings.isNullOrEmpty(packages)) {
            this.packages = null;
            return;
        }

        this.packages = Arrays.stream(packages.split(","))
                .map(String::trim)
                .collect(Collectors.joining());
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public ExtensionConfig() { }

    public ExtensionConfig(String packages,
                           boolean enabled,
                           API api) {
        this.packages = packages;
        this.enabled = enabled;
        this.api = api;
    }

    public ExtensionConfig copy() {
        return new ExtensionConfig(getPackages(), isEnabled(), getApi());
    }

    public void set(@NotNull RunConfigurationBase runConfiguration) {
        runConfiguration.putUserData(KEY, this);
    }

    private void writeExternal(Element element) {
        if (getPackages() != null) {
            element.addContent(new Element(PACKAGES_ELEMENT_NAME).setText(getPackages()));
        }

        element.addContent(new Element(ENABLED_ELEMENT_NAME).setText(Boolean.toString(isEnabled())));

        element.addContent(new Element(API_ELEMENT_NAME).setText(getApi().toString()));
    }

}
